package com.google.android.netmeterled;

import java.io.DataOutputStream;

import android.graphics.Color;
import android.util.Log;

/**
 * This library offers methods to set the charging LED color.
 *
 * @author britoso
 * green+blue= sky blue
 * blue+amber= pink
 */
public class ChargingLEDLib
{
	private final static int LOWEST_THRESHHOLD = 2; // off
	private final static int LOW_THRESHHOLD = 15;   // blue
	private final static int MEDIUM_THRESHHOLD = 40;// green
	private final static int HIGH_THRESHHOLD = 75;  // amber
												    // above high=RED
	private boolean blue, red, amber, green, initialized;
	private final static boolean DEBUG=true;

	private static final String DESIRE_PATH="platform/leds-microp";
	private static final String NEXUS_PATH="i2c-0/0-0066";
	//private static final String G1_PATH="i2c-0/0-0062";
	private static String pathToUse=DESIRE_PATH;//default to this

	private static final String NEXUS_ONE_MODEL = "Nexus One";
	private static final String HTC_DESIRE_MODEL = "HTC Desire";

	private static String model=null;

	private final static String TAG = "NetMeter+LED";
	private static String SHELL_OPEN_COMMAND = "su";

	private static final String COLOR_BLUE="blue",
							    COLOR_GREEN="green",
							    COLOR_AMBER="amber",
							    COLOR_RED="red";

	// keep the console used to issue commands open. more efficient
	private static Process suConsole = null;
	private static DataOutputStream os = null;

	private void initialize()
	{
		model=android.os.Build.MODEL;
		Log.i(TAG,"Phone Model="+model);
		setPathBasedOnModel();
		turnOffAllLEDs();
		resetLEDBrightness();//incase someone was playing with it.
	}

	/**
	 * set the charging LED color appropriately
	 *
	 * @author britoso
	 * @param totalCPUInt
	 *            the cpu usage percent
	 */
	public void setLEDColor(int totalCPUInt)
	{
		if (DEBUG) Log.d(TAG, "CPU=" + totalCPUInt);

		// turn off all colors once at the start.
		if (initialized == false)
		{
			initialize();
			initialized = true;
		}

		if (totalCPUInt <= LOWEST_THRESHHOLD)
		{
			turnOffAllLitLEDs();
		}
		else if (totalCPUInt > LOWEST_THRESHHOLD && totalCPUInt < LOW_THRESHHOLD)
		{
			if (blue == false)
			{
				turnOffAllLitLEDs();
				blue = true;
				turnOnLED(COLOR_BLUE);
			}
		}
		else if (totalCPUInt >= LOW_THRESHHOLD && totalCPUInt < MEDIUM_THRESHHOLD)
		{
			if (green == false)
			{
				turnOffAllLitLEDs();
				green = true;
				turnOnLED(COLOR_GREEN);
			}
		}
		else if (totalCPUInt >= MEDIUM_THRESHHOLD && totalCPUInt < HIGH_THRESHHOLD)
		{
			if (amber == false)
			{
				turnOffAllLitLEDs();
				amber = true;
				turnOnLED(COLOR_AMBER);
			}
		}
		else if (totalCPUInt >= HIGH_THRESHHOLD)
		{
			if (red == false)
			{
				turnOffAllLitLEDs();
				if(model.equalsIgnoreCase(HTC_DESIRE_MODEL))
				{
					turnOnLED(COLOR_AMBER);//desire has no red led
					amber=true;
				}
				else
				{
					turnOnLED(COLOR_RED);
					red = true;
				}

				//setHexColor("#00FFFF");
			}
		}

	}

	private void setPathBasedOnModel()
	{
		if(model.equalsIgnoreCase(NEXUS_ONE_MODEL))
		{
			pathToUse=NEXUS_PATH;
			SHELL_OPEN_COMMAND="su";
		}
		if(model.equalsIgnoreCase(HTC_DESIRE_MODEL))
		{
			pathToUse=DESIRE_PATH;
			SHELL_OPEN_COMMAND="sh";//root not needed
		}

	}

	private void turnOnLED(String color)
	{
		runRootCommand("echo 1 > /sys/devices/"+pathToUse+"/leds/" + color + "/brightness");
	}

	private void turnOffLED(String color)
	{
		runRootCommand("echo 0 > /sys/devices/"+pathToUse+"/leds/" + color + "/brightness");
	}

	/**
	 * turn off only if needed.
	 */
	private void turnOffAllLitLEDs()
	{
		if (blue)
		{
			turnOffLED(COLOR_BLUE);
			blue = false;
		}

		if (green)
		{
			turnOffLED(COLOR_GREEN);
			green = false;
		}

		if (amber)
		{
			turnOffLED(COLOR_AMBER);
			amber = false;
		}

		if (red)
		{
			if(model.equalsIgnoreCase(HTC_DESIRE_MODEL))
				turnOffLED(COLOR_AMBER);//desire has no red led
			else
				turnOffLED(COLOR_RED);
			red = false;
		}
	}

	/**
	 * turn off all known LEDs
	 */
	public void turnOffAllLEDs()
	{
		turnOffLED(COLOR_BLUE);
		turnOffLED(COLOR_GREEN);
		turnOffLED(COLOR_AMBER);
		if(!model.equalsIgnoreCase(HTC_DESIRE_MODEL))
			turnOffLED(COLOR_RED);
	}

	private boolean runRootCommand(String command)
	{
		try
		{
			// check if we have the console created.
			if (suConsole == null || os == null)
			{
				suConsole = Runtime.getRuntime().exec(SHELL_OPEN_COMMAND);
				os = new DataOutputStream(suConsole.getOutputStream());
			}
			if (DEBUG) Log.d(TAG, "Running command: " + command);
			os.writeBytes(command + "\n");
			os.flush();
			synchronized (suConsole)
			{
				suConsole.wait(100);
			}
		}
		catch (Exception e)
		{
			Log.d(TAG, "Unexpected error running system command: "+command+" Error:"+e.getMessage());
			if(e instanceof java.io.IOException && e.getMessage().equalsIgnoreCase("Broken pipe"))
			{//we may have lost our shell, force a retry next time.
				//Log.i(TAG,"Reset Outputstream.");
				os=null;
			}
			return false;
		}
		return true;
	}

	public void setColor(int r, int g, int b)
	{
		turnOffAllLEDs();
		if(r>0)
		{
			turnOnLED(COLOR_RED);
			setLEDBrightness(COLOR_RED,r);
		}
		if(g>0)
		{
			turnOnLED(COLOR_GREEN);
			setLEDBrightness(COLOR_GREEN,g);
		}
		if(b>0)
		{
			turnOnLED(COLOR_BLUE);
			setLEDBrightness(COLOR_BLUE,b);
		}
	}

	/**
	 *
	 * @param rgb Supported formats are:
	 * #RRGGBB #AARRGGBB
	 * 'red', 'blue', 'green',
	 * 'black', 'white', 'gray', 'cyan', 'magenta', 'yellow', 'lightgray', 'darkgray'
	 */
	public void setHexColor(String rgb)
	{
		int inColor;

		try
		{
			inColor=Color.parseColor (rgb);
		}
		catch(IllegalArgumentException ie)
		{
			Log.i(TAG,"Error parsing  hex color.");
			return;
		}

		if(Color.red(inColor)>0)
		{
			turnOnLED(COLOR_RED);
			Log.i(TAG,"setting red color brightness :"+Color.red(inColor));
			setLEDBrightness(COLOR_RED,Color.red(inColor));
		}
		if(Color.green(inColor)>0)
		{
			turnOnLED(COLOR_GREEN);
			Log.i(TAG,"setting green color brightness :"+Color.green(inColor));
			setLEDBrightness(COLOR_GREEN,Color.green(inColor));
		}
		if(Color.blue(inColor)>0)
		{
			turnOnLED(COLOR_BLUE);
			Log.i(TAG,"setting blue color brightness :"+Color.blue(inColor));
			setLEDBrightness(COLOR_BLUE,Color.blue(inColor));
		}
	}

	private void setLEDBrightness(String color, int value)
	{
		if(color!=null && color.length()>0 && value >=0 && value <=255)
			runRootCommand("echo "+value +" > /sys/devices/"+pathToUse+"/leds/"+color+"/max_brightness");
	}

	public void resetLEDBrightness()
	{
		/*commented as it does not do anything
			if(!model.equalsIgnoreCase(HTC_DESIRE_MODEL))
				runRootCommand("echo 255 > /sys/devices/"+pathToUse+"/red/max_brightness");
			runRootCommand("echo 255 > /sys/devices/"+pathToUse+"/green/max_brightness");
			runRootCommand("echo 255 > /sys/devices/"+pathToUse+"/blue/max_brightness");
			runRootCommand("echo 255 > /sys/devices/"+pathToUse+"/amber/max_brightness");
		*/
	}
}

