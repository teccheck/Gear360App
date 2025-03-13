# Gear360App
An open source Gear 360 app that should work an any device with Android 5+

**IMPORTANT NOTE**: Parts of this app are just a prototype/poc but feel free to try it out.

## Road map
* [x] Connect to the camera from the app
* [x] Basic communication
* [x] Remote control
* [x] Hardware information
* [x] Camera status
* [ ] Live preview
* [ ] Gallery
* [ ] Developer menu
* [ ] Firmware update (maybe)

## Remote control
The most useful feature of the app is a remote control for your Gear 360. You can start and stop recording, take photos and change some settings.
Beware if you change the mode, it does not reflect on the camera's screen until it redraws, but it works regardless.

## How to get a live preview
Currently live preview is less than ideal, but it works more or less. These are the steps to make it work:

1. Connect your Gear 360
2. Click on the `Camera` button
3. Connect with the Wi-Fi network of your camera like you normally connect to a Wi-Fi network
   * The name should be something like `AP_Gear 360(XX:XX:XX)`. If you need the password you can find in the section `Hardware info`
   * If the AP won't show up you might need to manually add it. You can find SSID (The Wi-Fi name) and password in the section `Hardware info`
4. Go into the test menu
5. Click on `EXOPLAYER`

## Contributing
Contributions are always welcome on any part of the app.

Currently, I'm rewriting/removing all the old Java code as I intend to replace it with cleaner Kotlin code. Some things are still written in Java though. For example the [`BTMProviderService`](app/src/main/java/io/github/teccheck/gear360app/bluetooth/BTMProviderService.java) is written in Java because Samsung Accessory Service can't handle this class being a Kotlin class.

## Requirements
The app needs the [Samsung Accessory SDK](https://developer.samsung.com/galaxy-watch/develop/sdk) to work.

## Important Note
Be sure to always have `[MAJOR].[MINOR]` version numbers in the [`accessoryservices.xml`](app/src/main/res/xml/accessoryservices.xml) file. Otherwise, Samsung Accessory Service **will** crash. It took me way too long to figure this out xD

## About the tech
I'd like to describe the tech that makes it all work. To me, it seems like the Gear 360 (and by that I mean both models) is a product that was rushed to market. As you can find on the internet, it shares the basic firmware with some NX series cameras. As well as that the app seems way too complicated and complex for what it does. There are classes with more than 10000 lines of code (at least in the decompilation), the packages are all over the place and don't get me started on the names they used.

If you open the app, it tries to connect to the camera via Bluetooth and once connected, sends json encoded messages back and forth. These are used to get information, status and configuration from the camera as well as time and location from the phone. It can also be used to configure for example camera mode and command the camera to take a photo or record a video. These messages have a wierd format. It seems like they are adapted from another protocoll, but I'm not sure.

```json
{
   "title": "Date-Time request Message",
   "description": "Message structure in JSON for Date-Time request",
   "type": "object",
   "properties": {
      "msgId": "date-time-req"
   }
},
```

This is one of the simplest messages. As you can see there are two human-readable strings, which is weird for a machine to machine interface. In general the format is much more complicated and inconsistent as it should be.

More might follow here once I find the motivation to write...