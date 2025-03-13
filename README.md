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

### Requirements
The app needs the [Samsung Accessory SDK](https://developer.samsung.com/galaxy-watch/develop/sdk) to work.

### Important Note
Be sure to always have `[MAJOR].[MINOR]` version numbers in the [`accessoryservices.xml`](app/src/main/res/xml/accessoryservices.xml) file. Otherwise, Samsung Accessory Service **will** crash. It took me way too long to figure this out xD
