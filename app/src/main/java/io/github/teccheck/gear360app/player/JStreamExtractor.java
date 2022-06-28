package io.github.teccheck.gear360app.player;

import android.util.Log;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.Format;
import com.google.android.exoplayer2.extractor.Extractor;
import com.google.android.exoplayer2.extractor.ExtractorInput;
import com.google.android.exoplayer2.extractor.ExtractorOutput;
import com.google.android.exoplayer2.extractor.ExtractorsFactory;
import com.google.android.exoplayer2.extractor.PositionHolder;
import com.google.android.exoplayer2.extractor.SeekMap;
import com.google.android.exoplayer2.extractor.TrackOutput;
import com.google.android.exoplayer2.util.MimeTypes;
import com.google.android.exoplayer2.util.ParsableByteArray;

import java.io.IOException;

import io.github.teccheck.gear360app.debug.Timer;

public class JStreamExtractor implements Extractor {

    private static final String TAG = "StreamExtractor";

    private static final int TAG_TTTS = 0x54545453;
    private static final int TAG_VID0 = 0x56494430;
    private static final int TAG_VD00 = 0x56443030;
    private static final int TAG_AUD0 = 0x41554430;
    private static final int TAG_AU00 = 0x41553030;
    private static final int TAG_VR00 = 0x56523030;
    private static final int TAG_LIST = 0x4C495354;
    private static final int TAG_MOVI = 0x6D6F7669;
    private static final int TAG_00VD = 0x30305644;
    private static final int TAG_00AU = 0x30304155;
    private static final int TAG_00VR = 0x30305652;

    private static final int TRACK_ID_VIDEO = 0;

    private ExtractorOutput output;
    private TrackOutput videoTrack;

    private ExtractorState currentState = ExtractorState.READ_MAIN_HEADER;
    private final ParsableByteArray buffer = new ParsableByteArray(12);

    private ChunkType currentChunkType = ChunkType.UNKNOWN;
    private int bytesRemainingInCurrentChunk = 0;
    private int currentChunkSize = 0;
    private long currentChunkTimeStamp = 0;
    private int currentFrameType = 2;

    private int mainHeaderSize = 0;
    private int videoTimeStampScale = 1;
    private int audioTimeStampScale = 0;

    @Override
    public void init(ExtractorOutput output) {
        this.output = output;
    }

    @Override
    public boolean sniff(ExtractorInput input) {
        Log.d(TAG, "sniff");
        return false;
    }

    @Override
    public int read(ExtractorInput input, PositionHolder seekPosition) throws IOException {
        switch (currentState) {
            case READ_MAIN_HEADER:
                readMainHeader(input);

                // This is important. Don't leave this call out
                output.seekMap(new SeekMap.Unseekable(C.TIME_UNSET));

                output.endTracks();
                currentState = ExtractorState.READ_FRAME_TAG;

                break;

            case READ_FRAME_TAG:
                readFrameTag(input);
                currentState = ExtractorState.READ_FRAME_HEADER;
                break;

            case READ_FRAME_HEADER:
                readFrameHead(input);
                currentState = ExtractorState.READ_FRAME_DATA;
                break;

            case READ_FRAME_DATA:
                if (readFrameData(input)) {
                    currentState = ExtractorState.READ_FRAME_TAG;
                }
                break;
        }

        //timer2.stop(true);
        return Extractor.RESULT_CONTINUE;
    }

    @Override
    public void seek(long position, long timeUs) {
        Log.d(TAG, "seek");
    }

    @Override
    public void release() {
        Log.d(TAG, "release");
    }

    private void readMainHeader(ExtractorInput input) throws IOException {
        Log.d(TAG, "readMainHeader");

        int type = 0;

        int width = 0;
        int height = 0;
        float frameRate = 0.0f;

        int videoCodecType = 0;
        int audioCodecType = -1;
        int channel = 0;
        int sampleRate = 0;

        boolean doRead = true;

        do {
            input.readFully(buffer.getData(), 0, 4);
            buffer.setPosition(0);
            int tag = buffer.readInt();

            Log.d(TAG, "Header tag: " + Integer.toHexString(tag));

            int size;
            int subTag;

            switch (tag) {
                case TAG_TTTS:
                    Log.d(TAG, "TTTS");

                    input.readFully(buffer.getData(), 0, 8);
                    buffer.setPosition(0);
                    size = buffer.readInt();
                    type = buffer.readInt();
                    mainHeaderSize += 12;

                    break;

                case TAG_VID0:
                    Log.d(TAG, "VID0");

                    input.readFully(buffer.getData(), 0, 4);
                    buffer.setPosition(0);
                    size = buffer.readInt();

                    input.skip(16);

                    input.readFully(buffer.getData(), 0, 4);
                    buffer.setPosition(0);
                    videoCodecType = buffer.readInt();

                    input.skip(12);

                    input.readFully(buffer.getData(), 0, 8);
                    buffer.setPosition(0);
                    videoTimeStampScale = buffer.readInt();
                    subTag = buffer.readInt();

                    Log.d(TAG, "Subtag: " + Integer.toHexString(subTag));

                    if (subTag != TAG_VD00) {
                        mainHeaderSize = -1;
                        doRead = false;
                    } else {
                        input.skip(4);

                        input.readFully(buffer.getData(), 0, 8);
                        buffer.setPosition(0);
                        frameRate = (float) buffer.readInt() / (float) buffer.readInt();

                        input.skip(24);

                        input.readFully(buffer.getData(), 0, 8);
                        buffer.setPosition(0);
                        width = buffer.readInt();
                        height = buffer.readInt();

                        input.skip(8);

                        mainHeaderSize += size + 4;
                    }

                    break;

                case TAG_AUD0:
                    Log.d(TAG, "AUD0");

                    input.readFully(buffer.getData(), 0, 8);
                    buffer.setPosition(0);
                    size = buffer.readInt();
                    channel = buffer.readInt();

                    input.skip(4);

                    input.readFully(buffer.getData(), 0, 8);
                    buffer.setPosition(0);
                    audioTimeStampScale = buffer.readInt();
                    subTag = buffer.readInt();

                    Log.d(TAG, "Subtag: " + Integer.toHexString(subTag));

                    if (subTag != TAG_AU00) {
                        mainHeaderSize = -1;
                        doRead = false;
                    } else {
                        input.skip(4);

                        input.readFully(buffer.getData(), 0, 4);
                        buffer.setPosition(0);
                        audioCodecType = buffer.readInt();

                        input.skip(32);

                        input.readFully(buffer.getData(), 0, 4);
                        buffer.setPosition(0);
                        sampleRate = buffer.readInt();

                        input.skip(20);

                        mainHeaderSize += size + 4;
                    }

                    break;

                case TAG_VR00:
                    Log.d(TAG, "VR00");

                    input.readFully(buffer.getData(), 0, 4);
                    buffer.setPosition(0);
                    size = buffer.readInt();

                    input.skip(4);

                    input.readFully(buffer.getData(), 0, 4);
                    buffer.setPosition(0);
                    subTag = buffer.readInt();

                    Log.d(TAG, "Subtag: " + Integer.toHexString(subTag));

                    if (subTag != TAG_VR00) {
                        mainHeaderSize = -1;
                        doRead = false;
                    } else {
                        input.skip(8);
                        mainHeaderSize += size + 4;
                    }

                    break;

                case TAG_LIST:
                    Log.d(TAG, "LIST");

                    input.skip(4);

                    mainHeaderSize += 8;

                case TAG_MOVI:
                    Log.d(TAG, "MOVI");

                    mainHeaderSize += 4;
                    doRead = false;
                    break;

                default:
                    Log.d(TAG, "Invalid Tag: " + Integer.toHexString(tag));
                    break;
            }
        } while (doRead);

        if ((type & 1) != 0) {
            String mimeType;
            switch (videoCodecType) {
                case 0:
                    mimeType = "video/raw";
                    break;
                case 1:
                    mimeType = MimeTypes.VIDEO_H265;
                    break;
                default:
                    mimeType = MimeTypes.VIDEO_UNKNOWN;
            }

            Log.d(TAG, "Video mime type: " + mimeType);
            Log.d(TAG, "Frame rate: " + frameRate);

            Format format = new Format.Builder()
                    .setWidth(width)
                    .setHeight(height)
                    .setSampleMimeType(mimeType)
                    .setId(TRACK_ID_VIDEO)
                    .setFrameRate(frameRate)
                    .setMaxInputSize(1000000)
                    .build();

            videoTrack = output.track(TRACK_ID_VIDEO, C.TRACK_TYPE_VIDEO);
            videoTrack.format(format);
        }
    }

    private void readFrameTag(ExtractorInput input) throws IOException {
        while (true) {
            input.readFully(buffer.getData(), 0, 4);
            buffer.setPosition(0);
            int currentChunkTag = buffer.readInt();

            switch (currentChunkTag) {
                case TAG_00VD:
                    currentChunkType = ChunkType.VIDEO;
                    return;
                case TAG_00AU:
                    currentChunkType = ChunkType.AUDIO;
                    return;
                case TAG_00VR:
                    currentChunkType = ChunkType.VROT;
                    return;
                case TAG_TTTS:
                    input.skip(mainHeaderSize - 4);
                    break;
                default:
                    currentChunkType = ChunkType.UNKNOWN;
                    return;
            }
        }
    }

    private void readFrameHead(ExtractorInput input) throws IOException {
        input.readFully(buffer.getData(), 0, 12);
        buffer.setPosition(0);
        currentChunkSize = buffer.readInt();
        currentChunkTimeStamp = buffer.readLong();
        bytesRemainingInCurrentChunk = currentChunkSize;

        if (currentChunkType == ChunkType.VROT) {
            input.skip(24);
        } else {
            input.peekFully(buffer.getData(), 0, 5);
            input.resetPeekPosition();
            buffer.setPosition(4);
            currentFrameType = buffer.readUnsignedByte();
        }
    }

    private boolean readFrameData(ExtractorInput input) throws IOException {
        switch (currentChunkType) {
            case VIDEO:
                return readFrameSampleData(input);
            case AUDIO:
                Log.w(TAG, "ChunkType is AUDIO");
                break;
            case VROT:
                Log.w(TAG, "ChunkType is VROT");
                break;
            case UNKNOWN:
                throw new RuntimeException("Invalid ChunkType");
                //Log.w(TAG, "Invalid ChunkType");
                //break;
        }

        return true;
    }

    private boolean readFrameSampleData(ExtractorInput input) throws IOException {
        bytesRemainingInCurrentChunk -= videoTrack.sampleData(
                input,
                bytesRemainingInCurrentChunk,
                false
        );

        boolean done = bytesRemainingInCurrentChunk == 0;
        if (done) {
            int flags = isKeyFrame() ? C.BUFFER_FLAG_KEY_FRAME : 0;
            videoTrack.sampleMetadata(getSampleTime(), flags, currentChunkSize - 5, 0, null);
        }

        return done;
    }

    private long getSampleTime() {
        if (currentChunkType == ChunkType.VIDEO)
            return currentChunkTimeStamp * 1000000L / videoTimeStampScale;
        else
            return currentChunkTimeStamp * 1000000L / audioTimeStampScale;
    }

    private boolean isKeyFrame() {
        return currentFrameType == 64 || currentFrameType == 38;
    }

    private enum ExtractorState {
        READ_MAIN_HEADER,
        READ_FRAME_TAG,
        READ_FRAME_HEADER,
        READ_FRAME_DATA
    }

    private enum ChunkType {
        VIDEO,
        AUDIO,
        VROT,
        UNKNOWN
    }

    public static class Factory implements ExtractorsFactory {
        @Override
        public Extractor[] createExtractors() {
            return new Extractor[]{new JStreamExtractor()};
        }
    }
}
