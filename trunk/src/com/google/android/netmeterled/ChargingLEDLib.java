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
	
	private final static String[] turnOffCommands =
	{
			"echo 0 > /sys/devices/i2c-0/0-0066/leds/blue/brightness", 
			"echo 0 > /sys/devices/i2c-0/0-0066/leds/green/brightness",
			"echo 0 > /sys/devices/i2c-0/0-0066/leds/amber/brightness", 
			"echo 0 > /sys/devices/i2c-0/0-0066/leds/red/brightness"
	};
	
	private final static String TAG = "NetMeter+LED";
	
	// keep the console used to issue commands open. more efficient
	private static Process suConsole = null;
	private static DataOutputStream os = null;
	
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
			turnOffAllLEDs();
			resetLEDBrightness();
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
				turnOnLED("red");
				//setHexColor("#00FFFF");
			}
		}
		
	}
	
	private void turnOnLED(String color)
	{
		runRootCommand("echo 1 > /sys/devices/i2c-0/0-0066/leds/" + color + "/brightness");
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
				suConsole = Runtime.getRuntime().exec("su");
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
			Log.d(TAG, "Unexpected error running system command: " + e.getMessage());
			return false;
		}
		return true;
	}
	
	private boolean runRootCommands(String[] commands)
	{
		try
		{
			// check if we have the console created.
			if (suConsole == null || os == null)
			{
				suConsole = Runtime.getRuntime().exec("su");
				os = new DataOutputStream(suConsole.getOutputStream());
			}
			for (int i = 0; i < commands.length; i++)
				os.writeBytes(commands[i] + "\n");
			os.flush();
			synchronized (suConsole)
			{
				suConsole.wait(100);
			}
		}
		catch (Exception e)
		{
			Log.d(TAG, "Unexpected error running system commands: " + e.getMessage());
			return false;
		}
		return true;
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
			runRootCommand("echo "+value +" > /sys/devices/i2c-0/0-0066/leds/"+color+"/max_brightness");
	}

	public void resetLEDBrightness()
	{
			runRootCommand("echo 255 > /sys/devices/i2c-0/0-0066/leds/red/max_brightness");
			runRootCommand("echo 255 > /sys/devices/i2c-0/0-0066/leds/green/max_brightness");
			runRootCommand("echo 255 > /sys/devices/i2c-0/0-0066/leds/blue/max_brightness");
	}
}

