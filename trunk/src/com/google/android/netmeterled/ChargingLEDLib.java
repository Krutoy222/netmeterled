package com.google.android.netmeterled;

import java.io.DataOutputStream;

import android.graphics.Color;
import android.util.Log;

/**
 * This library offers methods to set the charging LED color.
 *
 * @author britoso
 *
 */

// TODO:
// 1: Adjust the max_brightness of each of the four LEDS
// 2: Way to set a RGB color. setLEDcolor(r%,g%,b%), setLEDCOLOR(#RGB);
public class ChargingLEDLib
{
	private final static int LOWEST_THRESHHOLD = 2;// off
	private final static int LOW_THRESHHOLD = 15;// blue
	private final static int MEDIUM_THRESHHOLD = 40;// green
	private final static int HIGH_THRESHHOLD = 75;// amber
	// above high=RED
	private boolean blue, red, amber, green, initialized;
	private static boolean  debug=true;

	private static final String DESIRE_PATH="platform/leds-microp";
	private static final String NEXUS_PATH="i2c-0/0-0066";
	private static String pathToUse=DESIRE_PATH;//default to this


	private static String[] turnOffCommands= new String[4];

	private final static String TAG = "NetMeter+LED";
	private static String SHELL_OPEN_COMMAND = "su";

	// keep the console used to issue commands open. more efficient
	private static Process suConsole = null;
	private static DataOutputStream os = null;

	private void setTurnOffCommands()
	{
		turnOffCommands[0]="echo 0 > /sys/devices/"+pathToUse+"/leds/"+"blue"+"/brightness";
		turnOffCommands[1]="echo 0 > /sys/devices/"+pathToUse+"/leds/"+"green"+"/brightness";
		turnOffCommands[2]="echo 0 > /sys/devices/"+pathToUse+"/leds/"+"amber"+"/brightness";
		turnOffCommands[3]="echo 0 > /sys/devices/"+pathToUse+"/leds/"+"red"+"/brightness";
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
		if (debug) Log.d(TAG, "CPU=" + totalCPUInt);

		// turn off all colors once at the start.
		if (initialized == false)
		{
			setPathBasedOnModel();//first
			setTurnOffCommands();
			turnOffAllLEDs();
			resetLEDBrightness();//incase someone was playing with it.
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
				turnOnLED("blue");
			}
		}
		else if (totalCPUInt >= LOW_THRESHHOLD && totalCPUInt < MEDIUM_THRESHHOLD)
		{
			if (green == false)
			{
				turnOffAllLitLEDs();
				green = true;
				turnOnLED("green");
			}
		}
		else if (totalCPUInt >= MEDIUM_THRESHHOLD && totalCPUInt < HIGH_THRESHHOLD)
		{
			if (amber == false)
			{
				turnOffAllLitLEDs();
				amber = true;
				turnOnLED("amber");
			}
		}
		else if (totalCPUInt >= HIGH_THRESHHOLD)
		{
			if (red == false)
			{
				red = true;
				turnOffAllLitLEDs();
				if(android.os.Build.MODEL.equalsIgnoreCase("HTC Desire"))
					turnOnLED("amber");//desire has no red led
				else
					turnOnLED("red");

				//setHexColor("#00FFFF");
			}
		}

	}

	private void setPathBasedOnModel()
	{
		if(debug)Log.i(TAG,"Phone Model="+android.os.Build.MODEL);

		if(android.os.Build.MODEL.equalsIgnoreCase("Nexus One"))
		{
			pathToUse=NEXUS_PATH;
			SHELL_OPEN_COMMAND="su";
		}
		if(android.os.Build.MODEL.equalsIgnoreCase("HTC Desire"))
		{
			pathToUse=DESIRE_PATH;
			SHELL_OPEN_COMMAND="sh";//root not needed
		}

	}
	private void turnOnLED(String color)
	{
		runRootCommand("echo 1 > /sys/devices/"+pathToUse+"/leds/" + color + "/brightness");
	}

	/**
	 * turn off only if needed.
	 */
	private void turnOffAllLitLEDs()
	{
		if (blue)
		{
			runRootCommand(turnOffCommands[0]);
			blue = false;
		}

		if (green)
		{
			runRootCommand(turnOffCommands[1]);
			green = false;
		}

		if (amber)
		{
			runRootCommand(turnOffCommands[2]);
			amber = false;
		}

		if (red)
		{
			if(android.os.Build.MODEL.equalsIgnoreCase("HTC Desire"))
				runRootCommand(turnOffCommands[2]);//desire has no red led
			else
				runRootCommand(turnOffCommands[3]);
			red = false;
		}
	}

	/**
	 * turn off all known LEDs
	 */
	public void turnOffAllLEDs()
	{
		runRootCommands(turnOffCommands);
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
			if (debug) Log.d(TAG, "Running command: " + command);
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

	private void runRootCommands(String[] commands)
	{
		for (int i = 0; i < commands.length; i++)
		{
			runRootCommand(commands[i]);
		}
	}

	public void setColor(int r, int g, int b)
	{
		turnOffAllLEDs();
		if(r>0)
		{
			turnOnLED("red");
			setLEDBrightness("red",r);
		}
		if(g>0)
		{
			turnOnLED("green");
			setLEDBrightness("green",g);
		}
		if(b>0)
		{
			turnOnLED("blue");
			setLEDBrightness("blue",b);
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
			turnOnLED("red");
			Log.i(TAG,"setting red color brightness :"+Color.red(inColor));
			setLEDBrightness("red",Color.red(inColor));
		}
		if(Color.green(inColor)>0)
		{
			turnOnLED("green");
			Log.i(TAG,"setting green color brightness :"+Color.green(inColor));
			setLEDBrightness("green",Color.green(inColor));
		}
		if(Color.blue(inColor)>0)
		{
			turnOnLED("blue");
			Log.i(TAG,"setting blue color brightness :"+Color.blue(inColor));
			setLEDBrightness("blue",Color.blue(inColor));
		}
	}

	private void setLEDBrightness(String color, int value)
	{
		if(color!=null && color.length()>0 && value >=0 && value <=255)
			runRootCommand("echo "+value +" > /sys/devices/"+pathToUse+"/leds/"+color+"/max_brightness");
	}

	public void resetLEDBrightness()
	{
			runRootCommand("echo 255 > /sys/devices/"+pathToUse+"/red/max_brightness");
			runRootCommand("echo 255 > /sys/devices/"+pathToUse+"/green/max_brightness");
			runRootCommand("echo 255 > /sys/devices/"+pathToUse+"/blue/max_brightness");
			runRootCommand("echo 255 > /sys/devices/"+pathToUse+"/amber/max_brightness");
	}
}

