package io.github.teccheck.gear360app.player

import android.util.Log
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.Format
import com.google.android.exoplayer2.extractor.*
import com.google.android.exoplayer2.extractor.Extractor
import com.google.android.exoplayer2.extractor.ExtractorsFactory
import com.google.android.exoplayer2.util.MimeTypes
import com.google.android.exoplayer2.util.ParsableByteArray

import java.io.IOException

private const val TAG = "Extractor"

// Tags are 4 byte long string. Here they are ints for better performance
private const val TAG_TTTS = 0x54545453
private const val TAG_VID0 = 0x56494430
private const val TAG_VD00 = 0x56443030
private const val TAG_AUD0 = 0x41554430
private const val TAG_AU00 = 0x41553030
private const val TAG_VR00 = 0x56523030
private const val TAG_LIST = 0x4C495354
private const val TAG_MOVI = 0x6D6F7669
private const val TAG_00VD = 0x30305644
private const val TAG_00AU = 0x30304155
private const val TAG_00VR = 0x30305652

private const val TRACK_ID_VIDEO = 0

class Extractor : Extractor {

    private lateinit var extractorOutput: ExtractorOutput
    private lateinit var videoTrack: TrackOutput

    private var currentState = ExtractorState.READ_MAIN_HEADER
    private var buffer = ParsableByteArray(12)

    private var currentChunkType = ChunkType.UNKNOWN
    private var currentChunkTag: Int = 0
    private var bytesRemainingInCurrentChunk = 0
    private var currentChunkSize = 0
    private var currentChunkTimeStamp: Long = 0
    private var currentFrameType = 2

    private val mainHeaderSize = 0
    private var videoTimeStampScale = 1
    private var audioTimeStampScale = 0

    //private var timer = Timer()

    override fun init(output: ExtractorOutput) {
        extractorOutput = output
    }

    @Throws(IOException::class)
    override fun sniff(input: ExtractorInput): Boolean {
        Log.d(TAG, "Sniff ${input.position} - ${input.length}")
        return false
    }

    @Throws(IOException::class)
    override fun read(input: ExtractorInput, seekPosition: PositionHolder): Int {
        when (currentState) {
            ExtractorState.READ_MAIN_HEADER -> {
                //timer.start()
                readMainHeader(input)

                // This is important. Don't leave this call out
                extractorOutput.seekMap(SeekMap.Unseekable(C.TIME_UNSET))

                extractorOutput.endTracks()
                currentState = ExtractorState.READ_FRAME_TAG
                //Log.d(TAG, "Reading main header took ${timer.stop()}ms")
            }

            ExtractorState.READ_FRAME_TAG -> {
                //timer.start()
                readFrameTag(input)
                currentState = ExtractorState.READ_FRAME_HEADER
            }

            ExtractorState.READ_FRAME_HEADER -> {
                readFrameHead(input)
                currentState = ExtractorState.READ_FRAME_DATA
            }
            ExtractorState.READ_FRAME_DATA -> {
                if (readFrameData(input)) {
                    currentState = ExtractorState.READ_FRAME_TAG
                    //Log.d(TAG, "Chunk took ${timer.stop()}ms; Average: ${timer.getAverage()}ms")
                }
            }
        }
        return Extractor.RESULT_CONTINUE
    }

    override fun seek(position: Long, timeUs: Long) {
        Log.d(TAG, "Seek $position - $timeUs")
    }

    override fun release() {
        Log.d(TAG, "Release")
    }

    private fun readMainHeader(input: ExtractorInput) {
        Log.d(TAG, "readMainHeader")

        var width = 0
        var height = 0
        var frameRate = 0.0f

        var videoCodecType = 0
        var audioCodecType = -1
        var channel = 0
        var sampleRate = 0

        var type = 0
        var headerSize = 0

        var doRead = true

        do {
            input.readFully(buffer.data, 0, 4)
            buffer.position = 0
            val tag = buffer.readInt()

            Log.d(TAG, "Main header part ${tag.toString(16)}")

            var size: Int
            var subTag: Int

            when (tag) {
                TAG_TTTS -> {
                    Log.d(TAG, "TTTS")

                    input.readFully(buffer.data, 0, 8)
                    buffer.position = 0
                    size = buffer.readInt()
                    type = buffer.readInt()
                    headerSize += 12
                }

                TAG_VID0 -> {
                    Log.d(TAG, "VID0")

                    input.readFully(buffer.data, 0, 4)
                    buffer.position = 0
                    size = buffer.readInt()

                    input.skip(16)

                    input.readFully(buffer.data, 0, 4)
                    buffer.position = 0
                    videoCodecType = buffer.readInt()

                    input.skip(12)

                    input.readFully(buffer.data, 0, 8)
                    buffer.position = 0
                    videoTimeStampScale = buffer.readInt()
                    subTag = buffer.readInt()

                    Log.d(TAG, "Subtag: ${subTag.toString(16)}")

                    if (subTag != TAG_VD00) {
                        headerSize = -1
                        doRead = false
                    } else {
                        input.skip(4)

                        input.readFully(buffer.data, 0, 8)
                        buffer.position = 0
                        frameRate = buffer.readInt().toFloat() /
                                buffer.readInt().toFloat()

                        input.skip(24)

                        input.readFully(buffer.data, 0, 8)
                        buffer.position = 0
                        width = buffer.readInt()
                        height = buffer.readInt()

                        input.skip(8)

                        headerSize += size + 4
                    }
                }

                TAG_AUD0 -> {
                    Log.d(TAG, "AUD0")

                    input.readFully(buffer.data, 0, 8)
                    buffer.position = 0
                    size = buffer.readInt()
                    channel = buffer.readInt()

                    input.skip(4)

                    input.readFully(buffer.data, 0, 8)
                    buffer.position = 0
                    audioTimeStampScale = buffer.readInt()
                    subTag = buffer.readInt()

                    Log.d(TAG, "Subtag: ${subTag.toString(16)}")

                    if (subTag != TAG_AU00) {
                        headerSize = -1
                        doRead = false
                    } else {
                        input.skip(4)

                        input.readFully(buffer.data, 0, 4)
                        buffer.position = 0
                        audioCodecType = buffer.readInt()

                        input.skip(32)

                        input.readFully(buffer.data, 0, 4)
                        buffer.position = 0
                        sampleRate = buffer.readInt()

                        input.skip(20)

                        headerSize += size + 4
                    }
                }

                TAG_VR00 -> {
                    Log.d(TAG, "VR00")

                    input.readFully(buffer.data, 0, 4)
                    buffer.position = 0
                    size = buffer.readInt()

                    input.skip(4)

                    input.readFully(buffer.data, 0, 4)
                    buffer.position = 0
                    subTag = buffer.readInt()

                    Log.d(TAG, "Subtag: ${subTag.toString(16)}")

                    if (subTag != TAG_VR00) {
                        headerSize = -1
                        doRead = false
                    } else {
                        input.skip(8)
                        headerSize += size + 4
                    }
                }

                TAG_LIST -> {
                    Log.d(TAG, "LIST")

                    input.skip(4)
                    headerSize += 8
                }

                TAG_MOVI -> {
                    Log.d(TAG, "MOVI")

                    headerSize += 4
                    doRead = false
                }

                else -> {
                    Log.d(TAG, "Invalid Tag: ${tag.toString(16)}")
                }
            }
        } while (doRead)

        if (type and 1 != 0) {
            val mimeType = when (videoCodecType) {
                0 -> "video/raw"
                1 -> MimeTypes.VIDEO_H265
                else -> MimeTypes.VIDEO_UNKNOWN
            }

            Log.d(TAG, "Video mime type: $mimeType")
            Log.d(TAG, "Frame rate: $frameRate")

            val format = Format.Builder()
                .setWidth(width)
                .setHeight(height)
                .setSampleMimeType(mimeType)
                .setId(TRACK_ID_VIDEO)
                .setFrameRate(frameRate)
                .setMaxInputSize(1000000)

            videoTrack = extractorOutput.track(TRACK_ID_VIDEO, C.TRACK_TYPE_VIDEO)
            videoTrack.format(format.build())
        }

        /*
        if (type and 2 != 0) {
            audioFormat = MediaFormat()

            when (audioCodecType) {
                0 -> audioFormat?.setString("mime", "audio/raw")
                1 -> audioFormat?.setString("mime", "audio/mp4a-latm")
                else -> audioFormat?.setString("mime", AUDIO_CODEC_UNKNOWN)
            }

            audioFormat?.setInteger("channel-count", channel)
            audioFormat?.setInteger("sample-rate", sampleRate)
            audioFormat?.setLong("durationUs", 0)
        }
         */
    }

    private fun readFrameTag(input: ExtractorInput) {
        while (true) {
            input.readFully(buffer.data, 0, 4)
            buffer.position = 0
            currentChunkTag = buffer.readInt()

            if (currentChunkTag == TAG_TTTS) {
                input.skip(mainHeaderSize - 4)
            } else {
                currentChunkType = when (currentChunkTag) {
                    TAG_00VD -> ChunkType.VIDEO
                    TAG_00AU -> ChunkType.AUDIO
                    TAG_00VR -> ChunkType.VROT
                    else -> ChunkType.UNKNOWN
                }
                return
            }
        }
    }

    private fun readFrameHead(input: ExtractorInput) {
        input.readFully(buffer.data, 0, 12)
        buffer.position = 0
        currentChunkSize = buffer.readInt()
        currentChunkTimeStamp = buffer.readLong()
        bytesRemainingInCurrentChunk = currentChunkSize

        if (currentChunkType == ChunkType.VROT) {
            input.skip(24)
        } else {
            input.peekFully(buffer.data, 0, 5)
            input.resetPeekPosition()
            buffer.position = 4
            currentFrameType = buffer.readUnsignedByte()
        }
    }

    private fun readFrameData(input: ExtractorInput): Boolean {
        when (currentChunkType) {
            ChunkType.VIDEO -> return readFrameSampleData(input)
            ChunkType.AUDIO -> Log.w(TAG, "ChunkType is AUDIO")
            ChunkType.VROT -> Log.w(TAG, "ChunkType is VROT")
            ChunkType.UNKNOWN -> Log.w(TAG, "Invalid ChunkType")
        }

        return true
    }

    private fun readFrameSampleData(input: ExtractorInput): Boolean {
        bytesRemainingInCurrentChunk -= videoTrack.sampleData(
            input,
            bytesRemainingInCurrentChunk,
            false
        )

        val done = bytesRemainingInCurrentChunk == 0
        if (done) {
            val flags = if (isKeyFrame()) C.BUFFER_FLAG_KEY_FRAME else 0
            videoTrack.sampleMetadata(getSampleTime(), flags, currentChunkSize - 5, 0, null)
        }

        return done
    }

    private fun getSampleTime(): Long {
        return if (currentChunkType == ChunkType.VIDEO) {
            currentChunkTimeStamp * 1000000 / videoTimeStampScale.toLong()
        } else currentChunkTimeStamp * 1000000 / audioTimeStampScale.toLong()
    }

    private fun isKeyFrame(): Boolean {
        return currentFrameType == 64 || currentFrameType == 38
    }

    private enum class ChunkType {
        VIDEO,
        AUDIO,
        VROT,
        UNKNOWN
    }

    private enum class ExtractorState {
        READ_MAIN_HEADER,
        READ_FRAME_TAG,
        READ_FRAME_HEADER,
        READ_FRAME_DATA
    }

    class Factory : ExtractorsFactory {
        override fun createExtractors(): Array<Extractor> {
            Log.d(TAG, "createExtractors")
            return arrayOf(Extractor())
        }
    }
}