package com.google.android.netmeterled;

import java.io.DataOutputStream;

import android.graphics.Color;
import android.util.Log;

/**
 * This library offers methods to set the charging LED color.
 *
 * @author britoso
 *
 *         color combinations: green+blue= sky blue blue+amber= pink
 */
public class ChargingLEDLib
{
	private final static int LOWEST_THRESHHOLD = 2; // off
	private final static int LOW_THRESHHOLD = 15;// blue
	private final static int MEDIUM_THRESHHOLD = 40;// green
	private final static int HIGH_THRESHHOLD = 75;// amber
	// above high=RED
	private boolean blue, red, amber, green, initialized;
	private final static boolean DEBUG = false;

	private static final String NEXUS_PATH = "i2c-0/0-0066";//GBAR
	private static final String DESIRE_PATH = "platform/leds-microp";//GBA, no root
	private static final String G1_PATH = "i2c-0/0-0062";//RGB
	private static final String MAGIC_PATH = "platform/leds-cpld";//RGB
	private static final String DROID_PATH = "platform/notification-led";//RGB

	private static String pathToUse = DESIRE_PATH;//default to this

	private final static String ROOT_SHELL = "su";
	private final static String NORMAL_SHELL = "sh";
	private static String shellOpenCommand = ROOT_SHELL;//default

	private final static String COLORS_WITHAMBER = "RGBA";
	private final static String COLORS_RGB = "RGB";
	private final static String COLORS_NORED = "GBA";

	private static String colorsToUse = COLORS_RGB;//default to this

	//model name, led path, shell to use, colors supported
	String [][] MODEL_SETTINGS_MATRIX =
	{
			{
					"Nexus One",
					NEXUS_PATH,
					ROOT_SHELL,
					COLORS_WITHAMBER
			},
			{
					"HTC Desire",
					DESIRE_PATH,
					NORMAL_SHELL,
					COLORS_NORED
			},

			{
					"HTC Dream",
					G1_PATH,
					ROOT_SHELL,
					COLORS_RGB
			},
			{
					"T-Mobile G1",
					G1_PATH,
					ROOT_SHELL,
					COLORS_RGB
			},
			{
					"Era G1",
					G1_PATH,
					ROOT_SHELL,
					COLORS_RGB
			},

			{
					"HTC Magic",
					MAGIC_PATH,
					ROOT_SHELL,
					COLORS_RGB
			},
			{
					"HTC Sapphire",
					MAGIC_PATH,
					ROOT_SHELL,
					COLORS_RGB
			},
			{
					"T-Mobile myTouch 3G",
					MAGIC_PATH,
					ROOT_SHELL,
					COLORS_RGB
			},
			{
					"Docomo HT-03A",
					MAGIC_PATH,
					ROOT_SHELL,
					COLORS_RGB
			},
			{
				"Droid A855",
				DROID_PATH,
				ROOT_SHELL,
				COLORS_RGB
			},
			{
				"Milestone",
				DROID_PATH,
				ROOT_SHELL,
				COLORS_RGB
			}
	};

	private static String model = null;

	private final static String TAG = "NetMeter+LED";

	private static final String COLOR_BLUE = "blue", COLOR_GREEN = "green", COLOR_AMBER = "amber",//G1 has no amber LED, so using red
			COLOR_RED = "red";//Desire has no red LED, so using amber

	private final static String ECHO_0 = "echo 0 > ";
	private final static String ECHO_1 = "echo 1 > ";
	private final static String PATH_PREFIX = "/sys/devices/";
	private final static String PATH_MID = "/leds/";
	private final static String PATH_SUFFIX = "/brightness";

	// keep the console used to issue commands open. more efficient
	private static Process suConsole = null;
	private static DataOutputStream os = null;

	private void initialize()
	{
		model = android.os.Build.MODEL;
		Log.i(TAG, "Phone Model=" + model);
		setPathBasedOnModel();
		turnOffAllLEDs();
		//resetLEDBrightness();//incase someone was playing with it.
	}

	private void setPathBasedOnModel()
	{
		//get phone settings from the MODEL_SETTINGS_MATRIX matrix
		int row;
		for (row = 0; row < MODEL_SETTINGS_MATRIX.length; row++)
		{
			if (MODEL_SETTINGS_MATRIX[row][0].equalsIgnoreCase(model))
			{
				pathToUse = MODEL_SETTINGS_MATRIX[row][1];
				shellOpenCommand = MODEL_SETTINGS_MATRIX[row][2];
				colorsToUse = MODEL_SETTINGS_MATRIX[row][3];
				Log.i(TAG, "Using: path=" + pathToUse + " shell=" + shellOpenCommand + " colors=" + colorsToUse);
				break;
			}
		}
	}

	/**
	 * turn off all known LEDs
	 */
	public void turnOffAllLEDs()
	{
		if (colorsToUse.indexOf("B") > -1) turnOffLED(COLOR_BLUE);
		if (colorsToUse.indexOf("G") > -1) turnOffLED(COLOR_GREEN);
		if (colorsToUse.indexOf("A") > -1) turnOffLED(COLOR_AMBER);
		if (colorsToUse.indexOf("R") > -1) turnOffLED(COLOR_RED);
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

				if (colorsToUse.indexOf("A") == -1)//no amber, use red instead, TODO: what if there is no RED
				{
					red = true;
					turnOnLED(COLOR_RED);
				}
				else
				{
					amber = true;
					turnOnLED(COLOR_AMBER);
				}
			}
		}
		else if (totalCPUInt >= HIGH_THRESHHOLD)
		{
			if (red == false)
			{
				turnOffAllLitLEDs();
				if (colorsToUse.indexOf("R") == -1)//no red, use amber
				{
					turnOnLED(COLOR_AMBER);
					amber = true;
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

	private void turnOnLED(String color)
	{
		runRootCommand(ECHO_1 + PATH_PREFIX + pathToUse + PATH_MID + color + PATH_SUFFIX);
	}

	private void turnOffLED(String color)
	{
		runRootCommand(ECHO_0 + PATH_PREFIX + pathToUse + PATH_MID + color + PATH_SUFFIX);
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
			turnOffLED(COLOR_RED);
			red = false;
		}
	}

	private boolean runRootCommand(String command)
	{
		try
		{
			// check if we have the console created.
			if (suConsole == null || os == null)
			{
				suConsole = Runtime.getRuntime().exec(shellOpenCommand);
				os = new DataOutputStream(suConsole.getOutputStream());
			}
			if (DEBUG) Log.d(TAG, "Running command: " + command);
			os.writeBytes(command + "\n");
			os.flush();
			//uncomment if deemed necessary
			//			synchronized (suConsole)
			//			{
			//				suConsole.wait(100);
			//			}
		}
		catch (Exception e)
		{
			Log.d(TAG, "Unexpected error running system command: " + command + " Error:" + e.getMessage());
			if (e instanceof java.io.IOException && e.getMessage().equalsIgnoreCase("Broken pipe"))
			{ //we may have lost our shell, force a retry next time.
				//iLog.d(TAG,"Reset Outputstream.");
				os = null;
			}
			return false;
		}
		return true;
	}

	public void setColor(int r, int g, int b)
	{
		turnOffAllLEDs();
		if (r > 0)
		{
			turnOnLED(COLOR_RED);
			setLEDBrightness(COLOR_RED, r);
		}
		if (g > 0)
		{
			turnOnLED(COLOR_GREEN);
			setLEDBrightness(COLOR_GREEN, g);
		}
		if (b > 0)
		{
			turnOnLED(COLOR_BLUE);
			setLEDBrightness(COLOR_BLUE, b);
		}
	}

	/**
	 *
	 * @param rgb
	 *            Supported formats are: #RRGGBB #AARRGGBB 'red', 'blue',
	 *            'green', 'black', 'white', 'gray', 'cyan', 'magenta',
	 *            'yellow', 'lightgray', 'darkgray'
	 */
	public void setHexColor(String rgb)
	{
		int inColor;

		try
		{
			inColor = Color.parseColor(rgb);
		}
		catch (IllegalArgumentException ie)
		{
			Log.d(TAG, "Error parsing  hex color.");
			return;
		}

		if (Color.red(inColor) > 0)
		{
			turnOnLED(COLOR_RED);
			Log.d(TAG, "setting red color brightness :" + Color.red(inColor));
			setLEDBrightness(COLOR_RED, Color.red(inColor));
		}
		if (Color.green(inColor) > 0)
		{
			turnOnLED(COLOR_GREEN);
			Log.d(TAG, "setting green color brightness :" + Color.green(inColor));
			setLEDBrightness(COLOR_GREEN, Color.green(inColor));
		}
		if (Color.blue(inColor) > 0)
		{
			turnOnLED(COLOR_BLUE);
			Log.d(TAG, "setting blue color brightness :" + Color.blue(inColor));
			setLEDBrightness(COLOR_BLUE, Color.blue(inColor));
		}
	}

	private void setLEDBrightness(String color, int value)
	{
		if (color != null && color.length() > 0 && value >= 0 && value <= 255) runRootCommand("echo " + value + " > /sys/devices/" + pathToUse + "/leds/"
				+ color + "/max_brightness");
	}

	public void resetLEDBrightness()
	{
		/*
		 * commented as it does not do anything
		 * if(!model.equalsIgnoreCase(HTC_DESIRE_MODEL))
		 * runRootCommand("echo 255 > /sys/devices/"
		 * +pathToUse+"/red/max_brightness");
		 * runRootCommand("echo 255 > /sys/devices/"
		 * +pathToUse+"/green/max_brightness");
		 * runRootCommand("echo 255 > /sys/devices/"
		 * +pathToUse+"/blue/max_brightness");
		 * runRootCommand("echo 255 > /sys/devices/"
		 * +pathToUse+"/amber/max_brightness");
		 */
	}
}
