**What is this**:<br />
An android app that uses the charging LED to show the current CPU load.<br />
Market Link: https://market.android.com/details?id=com.britoso.cpustatusled
<br />
Credits:
Props to Bernhard.R.Suter creator of NetMeter, on which this was built upon.
<br />
**Screenshot**: ![http://imgur.com/MdlkSl.jpg](http://imgur.com/MdlkSl.jpg)<br />
**Video**: http://www.youtube.com/watch?v=5ZnMGEX0vOY<br />
<br />
**Forum**: http://forum.xda-developers.com/showthread.php?t=705645<br />
<br />
**Works On**:<br />
Nexus One: Needs Root<br />
HTC Desire: Root not needed<br />
HTC G1: Needs Root<br />
HTC Magic: Needs Root<br />
Motorola Droid: Needs Root<br />
<br />
**Here are the colors, and the order in which they are used: (colors may vary depending on the device)**<br />
```
CPU%  -  COLOR
<3     -  Off
3-15   -  blue
16-40  -  green
41-75  -  amber
>=76   -  red
```
<br />
**Instructions**:<br />
Download the apk and install it. <br />
`adb install -r NetMeterLED.apk`<br />
<br />
**To Add your Device**<br />
In order for me to add support your device I will need the following information. Make a post using the forum link above.<br />
1) Phone **Model**. settings->about phone -> model number<br />
2) **Path** to the LED device control files on your filesystem<br />
Ex: /sys/devices/i2c-0/0-0066/leds/red/brightness<br />
3) Specify **which LEDs** are present, and **work**.<br />
Ex: blue,green,amber,red<br />
To test if a LED works:<br />
`echo 1 > /sys/devices/i2c-0/0-0066/leds/red/brightness`<br />
note: read step 3 and if root is needed run 'su' first.<br />
4) Are the device files **world writeable**?<br />
Find out by running: `ls -l /sys/devices/i2c-0/0-0066/leds/red/brightness` (just an example)<br />
-rwxr--r-- <- if the last 3 letters are r-- then you need root to write to it.<br />
-rwxr--rw- <- if the last 3 letters start with rw then any user can write to it. (root not needed)<br />
<br />
**Updates upto 0.94**<br />
```
07/16/2010 
-v0.94
- added hard coded htc derire LED paths
07/16/2010
-v0.93
- fixed start-at-boot
07/16/2010
-v0.92
- misc bugfixes
- prompts you if it needs 'su' and cant find it
07/11/2010
-v0.90
- Added the ability to change thresholds and colors.
- prefs are saved and reloaded on startup
- Added LED autodetection code. If root is needed you will need to have the 'su' executable present.
07/08/2010
-v0.81
-Fixed a bug causing the signal graph to stop drawing after switching to another app and back
-Added Hero/Eris/G2 and Droid Incredible support. Both have only Green and Amber LEDs and need root.
07/07/2010
-v0.80b
- Redid the UI. Changed app name to CPUStatusLED, changed icon
- Top 3 process are displayed if the CPU usage is above 75%
- added GSM signal graph.
07/01/2010
-v0.76
- removed Cell and Wifi graphs, CPU only now.
- option to toggle enable/disable LED setting.
v0.74
- added g1, magic support
- optimized a few areas, easy addition of additional phone models.
v0.71
- Sneaky red LED bug, wasn't turning off correctly sometimes
- HTC desire uses sh and not su
6/22/10
-v0.6
- HTC desire support added
- commented out service.setForeground() as it was deprecated 
- minor code fixes
6/20/10 
-v0.5 
-Turn off LEDs on Exit, 
-Start app on boot(background).
-Changed Application package name to distinuish it from the original Netmeter. Please uninstall the old version before upgrading.
6/19/10 - bugfixes - Fixed a bug where a LED would stay on
Code change to keep the SU console open and keep reusing it.
Changed the project name from NetMeterPlus to NetMeterLED
```