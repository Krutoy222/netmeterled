package com.britoso.cpustatusled.utilclasses;

import java.io.DataOutputStream;
import java.io.File;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.britoso.cpustatusled.CPUStatusLEDActivity;
import com.britoso.cpustatusled.utilclasses.ShellCommand.CommandResult;

/**
 * This library offers methods to set the charging LED color.
 *
 * @author britoso
 *
 *         color combinations: green+blue= sky blue blue+amber= pink
 */
public class ChargingLEDLib
{
	//data
	public final static int MAX_SUPPORTED_COLORS = 4;
	private final static String ECHO_0 = "echo 0 > ";
	private final static String ECHO_1 = "echo 1 > ";
	public final static String ROOT_SHELL = "su";
	private final static String NORMAL_SHELL = "sh";
	private static final String COLOR_BLUE = "blue", COLOR_GREEN = "green", COLOR_AMBER = "amber", COLOR_RED = "red";
	private final static String TAG = "CPUStatusLED";
	public static final String PREFS_NAME = "CPUStatusLED.prefs";


	//reused
	public final static boolean DEBUG = false;
	// keep the console used to issue commands open. more efficient
	private static Process console = null;
	private static DataOutputStream os = null;
	static CPUStatusLEDActivity gui1;
	static int lastThreshhold = 0;
	public static boolean noLEDs = false;
	public static boolean canSU=false;//used to warn the user



	//prefs
	public static int [] thresholds =
	{
			3,
			15,
			40,
			75
	};
	public static String shellOpenCommand;
	private static String colorOrder[] = new String[MAX_SUPPORTED_COLORS];
	public static String ledpaths[] = new String[MAX_SUPPORTED_COLORS];
	public static String availableLEDs[];//used to populate the dropdown/spinner

	public ChargingLEDLib()
	{
	}

	private void initialize()
	{
		Log.i(TAG, "Phone Model=" + android.os.Build.MODEL);

		//leds
		String ledpathsononeline = detectLEDpaths();
		int ledcount = 0;
		ledpaths = ledpathsononeline.trim().split("\n");

		String temp[] = new String[20];//large enough
		for (int i = 0; i < ledpaths.length; i++)
		{
			if (ledpaths[i].indexOf(COLOR_RED) > -1 || ledpaths[i].indexOf(COLOR_GREEN) > -1 || ledpaths[i].indexOf(COLOR_BLUE) > -1
					|| ledpaths[i].indexOf(COLOR_AMBER) > -1)
			{
				if (ledpaths[i].indexOf(COLOR_RED) > -1) temp[ledcount] = COLOR_RED;
				if (ledpaths[i].indexOf(COLOR_GREEN) > -1) temp[ledcount] = COLOR_GREEN;
				if (ledpaths[i].indexOf(COLOR_BLUE) > -1) temp[ledcount] = COLOR_BLUE;
				if (ledpaths[i].indexOf(COLOR_AMBER) > -1) temp[ledcount] = COLOR_AMBER;
				ledcount++;
			}
		}
		availableLEDs = new String[ledcount];
		for (int i = 0; i < ledcount; i++)
			availableLEDs[i] = temp[i];

		Log.i(TAG, "Found " + ledcount + " LEDs!");

		//set colorOrder;
		setDefaultColorOrder(ledcount, ledpathsononeline);

		if (colorOrder[0] == null ||ledpaths==null || ledpaths.length==0)
		{
			ChargingLEDLib.noLEDs=true;
		}

		//uses sets ledpaths based on the colorOrder
		if(ChargingLEDLib.noLEDs==false)
			assignPathsBasedOnColorOrder();

		if (ledpaths.length > 0)
		{
			if (isWriteable(ledpaths[0]))
			{
				shellOpenCommand = NORMAL_SHELL;
			}
			else
			{
				shellOpenCommand = ROOT_SHELL;
			}
		}
		Log.i(TAG, "Shell = " + shellOpenCommand);
		if (shellOpenCommand == ROOT_SHELL)checkIfSUWorks();//set canSU

	}

	private void checkIfSUWorks()
	{
		if(shellOpenCommand.equals(ROOT_SHELL) && canSU==false)
		{
			ShellCommand sc = new ShellCommand();
			canSU=sc.canSU(true);//force
		}
	}

	private void setDefaultColorOrder(int ledcount, String ledpathsononeline)
	{
		//set default color order. 4 positions.
		int colorsused = 0;
		boolean blue = false, green = false, amber = false, red = false;
		while (colorsused < ledcount && colorsused < MAX_SUPPORTED_COLORS)
		{
			if (blue == false)
			{
				if (ledpathsononeline.indexOf(COLOR_BLUE) > -1)
				{
					colorOrder[colorsused++] = COLOR_BLUE;
					blue = true;//used
				}
				else
				{
					if (ledpathsononeline.indexOf(COLOR_GREEN) > -1)
					{
						colorOrder[colorsused++] = COLOR_GREEN;
						green = true;
					}
				}
				continue;
			}
			if (green == false)
			{
				if (ledpathsononeline.indexOf(COLOR_GREEN) > -1)
				{
					colorOrder[colorsused++] = COLOR_GREEN;
					green = true;//used
				}
				else
				{
					if (ledpathsononeline.indexOf(COLOR_AMBER) > -1)
					{
						colorOrder[colorsused++] = COLOR_AMBER;
						amber = true;
					}
				}
				continue;
			}
			if (amber == false)
			{
				if (ledpathsononeline.indexOf(COLOR_AMBER) > -1)
				{
					colorOrder[colorsused++] = COLOR_AMBER;
					amber = true;//used
				}
				else
				{
					if (ledpathsononeline.indexOf(COLOR_RED) > -1)
					{
						colorOrder[colorsused++] = COLOR_RED;
						red = true;
					}
				}
				continue;
			}
			if (red == false)
			{
				if (ledpathsononeline.indexOf(COLOR_RED) > -1)
				{
					colorOrder[colorsused++] = COLOR_RED;
					red = true;//used
				}
				continue;
			}
		}//while

		//use the last color for all the remaining slots, i.e for the Hero it will be: Green, Amber  +Amber+Amber
		for (int i = 1; i < colorOrder.length; i++)
		{
			if (colorOrder[i] == null)
			{
				colorOrder[i] = colorOrder[i - 1];
			}
		}
		if (DEBUG)
		{
			Log.i(TAG, "Colors= " + colorOrder[0] + " " + colorOrder[1] + " " + colorOrder[2] + " " + colorOrder[3] + " ");
		}
	}

	private void assignPathsBasedOnColorOrder()
	{
		//assign paths corresponding to ColorOrder
		String temp[] = new String[colorOrder.length];
		for (int j = 0; j < colorOrder.length; j++)
		{
			for (int i = 0; i < ledpaths.length; i++)
			{
				if (ledpaths[i].indexOf(colorOrder[j]) > -1)
				{
					temp[j] = ledpaths[i];
					break;
				}
			}
		}
		ledpaths = temp;

		if (DEBUG)
		{
			for (int i = 0; i < ledpaths.length; i++)
				Log.i(TAG, "Color[" + i + "]=" + colorOrder[i] + "\tpath[" + i + "] =" + ledpaths[i] + "\n");
		}
	}

	private boolean isWriteable(String file)
	{
		File f = new File(file);
		return f.canWrite();
	}

	private String detectLEDpaths()
	{
		String result = "";

		if(android.os.Build.MODEL.equalsIgnoreCase("HTC Desire"))
		{
			//su not needed, but cant run find!
			return "/sys/devices/platform/leds-microp/leds/green/brightness\n/sys/devices/platform/leds-microp/leds/blue/brightness\n/sys/devices/platform/leds-microp/leds/amber/brightness";			
		}
		ShellCommand cmd = new ShellCommand();
		CommandResult r = cmd.sh.runWaitFor("find /sys/devices/ -name \"brightness\"");

		if (!r.success())
		{
			//try su. need root to run find on some phones!
			ShellCommand sc= new ShellCommand();
			if(sc.canSU())
			{
				CommandResult r1 = cmd.su.runWaitFor("find /sys/devices/ -name \"brightness\"");
				if (!r1.success())
				{
					Log.d(TAG, "Error in detectLEDs: " + r.stderr);
				}
				else
				{
					result = r1.stdout;
				}
			}
		}
		else
		{
			//Log.d(TAG, "Successfully executed. Result: " + r.stdout);
			result = r.stdout;
		}
		return result;
	}

	/**
	 * turn off all known LEDs
	 */
	public void turnOffAllLEDs()
	{
		if (ChargingLEDLib.noLEDs) return;
		for (int i = 0; i < ledpaths.length; i++)
			runShellCommand(ECHO_0 + ledpaths[i]);
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
		if (CPUStatusLEDActivity.disabledLEDs || ChargingLEDLib.noLEDs) return;//dont do anything

		if (DEBUG) Log.d(TAG, "CPU=" + totalCPUInt);

		//workaround for bug causing the ledpaths to be null.
		if (ledpaths == null || ledpaths.length == 0 ||ledpaths[0].length()==0)
		{
			readPrefs();
		}

		int threshhold = getThreshHold(totalCPUInt);

		if (threshhold != lastThreshhold)
		{
			if (lastThreshhold >= 0)
			{
				runShellCommand(ECHO_0 + ledpaths[lastThreshhold]);
			}
			if (threshhold >= 0)
			{
				runShellCommand(ECHO_1 + ledpaths[threshhold]);
			}
			lastThreshhold = threshhold;
		}

	}

	private int getThreshHold(int totalCPUInt)
	{
		if (totalCPUInt <= thresholds[0])
		{
			return -1;//off
		}
		else if (totalCPUInt > thresholds[0] && totalCPUInt < thresholds[1])
		{
			return 0;
		}
		else if (totalCPUInt >= thresholds[1] && totalCPUInt < thresholds[2])
		{
			return 1;
		}
		else if (totalCPUInt >= thresholds[2] && totalCPUInt < thresholds[3])
		{
			return 2;
		}
		else if (totalCPUInt >= thresholds[3])
		{
			return 3;
		}
		return -1;
	}

	private boolean runShellCommand(String command)
	{
		try
		{
			// check if we have the console created.
			if (console == null || os == null)
			{
				console = Runtime.getRuntime().exec(shellOpenCommand);
				os = new DataOutputStream(console.getOutputStream());
			}
			if (DEBUG) Log.d(TAG, "Running command: " + command);
			os.writeBytes(command + "\n");
			os.flush();
		}
		catch (Exception e)
		{
			Log.d(TAG, "Unexpected error running system command: " + command + " Error:" + e.getMessage());
			if (e instanceof java.io.IOException && e.getMessage().equalsIgnoreCase("Broken pipe"))
			{
				//we may have lost our shell, force a retry next time.
				os = null;
			}
			return false;
		}
		return true;
	}

	public String [] getColorOrder()
	{
		return colorOrder;
	}

	public void setColorOrder(String [] colorOrder)
	{
		ChargingLEDLib.colorOrder = colorOrder;
		assignPathsBasedOnColorOrder();
	}

	public static Context context;
	//private static boolean prefsRead=false;

	/*read shared preferences, fall back to initialize() which auto-detects*/
	public void readPrefs()
	{
		//if (prefsRead) return;
		SharedPreferences settings=null;
		try
		{
			settings = PreferenceManager.getDefaultSharedPreferences(context);
		}
		catch(Exception e)
		{
			Log.i(TAG,"No saved preferences found");
		}
		if(settings==null)
		{
			initialize();
			return;//prefs dont exist, stop reading.
		}
		//read shellOpenCommand, thresholds, colorOrder, ledpaths, availableLEDs
		String shell = settings.getString("shell", null);
		if (shell != null)
		{
			ChargingLEDLib.shellOpenCommand = shell;
			if (ChargingLEDLib.DEBUG) Log.i(TAG, "Read: shell=" + shell);
			if (shellOpenCommand == ROOT_SHELL)checkIfSUWorks();//set canSU
		}
		else
		{
			initialize();
			return;//prefs dont exist, stop reading.
		}

		int i = 0;
		int availLEDCount = 0;
		String colorOrder[] = new String[ChargingLEDLib.MAX_SUPPORTED_COLORS];
		String [] tempLEDColor = new String[10];
		//try to read 4 values
		while (i < ChargingLEDLib.MAX_SUPPORTED_COLORS)
		{
			String color, ledpath;
			int threshold;
			color = settings.getString("color" + i, null);
			if (color != null) colorOrder[i] = color;

			threshold = settings.getInt("threshold" + i, -1);
			if (threshold >= 0) ChargingLEDLib.thresholds[i] = threshold;

			ledpath = settings.getString("ledpath" + i, null);
			if (ledpath != null) ChargingLEDLib.ledpaths[i] = ledpath;

			String availLED = settings.getString("availableLED" + i, null);
			if (availLED != null)
			{
				tempLEDColor[availLEDCount++] = availLED;
			}

			i++;
		}
		setColorOrder(colorOrder);
		if (ChargingLEDLib.DEBUG) Log.i(TAG, "Read: colorOrder=" + colorOrder[0] + "  " + colorOrder[1] + "  " + colorOrder[2] + "  " + colorOrder[3]);
		if (ChargingLEDLib.DEBUG) Log.i(TAG, "Read: ledpaths=" + ChargingLEDLib.ledpaths[0] + "  " + ChargingLEDLib.ledpaths[1] + "  "
				+ ChargingLEDLib.ledpaths[2] + "  " + ChargingLEDLib.ledpaths[3]);
		if (ChargingLEDLib.DEBUG) Log.i(TAG, "Read: thresholds=" + ChargingLEDLib.thresholds[0] + "  " + ChargingLEDLib.thresholds[1] + "  "
				+ ChargingLEDLib.thresholds[2] + "  " + ChargingLEDLib.thresholds[3]);
		ChargingLEDLib.availableLEDs = new String[availLEDCount];
		for (int j = 0; j < availLEDCount; j++)
		{
			ChargingLEDLib.availableLEDs[j] = tempLEDColor[j];
			if (ChargingLEDLib.DEBUG) Log.i(TAG, "Read: availableLED=" + ChargingLEDLib.availableLEDs[j]);
		}
		turnOffAllLEDs();
		//prefsRead=true;
	}

	/**
	 * Save to sharedPrerefences: shellOpenCommand, thresholds, colorOrder,
	 * ledpaths, availableLEDs
	 */
	public void writePrefs()
	{
		//SharedPreferences settings = getPreferences(MODE_PRIVATE );
		SharedPreferences settings =PreferenceManager.getDefaultSharedPreferences(context);
		SharedPreferences.Editor e = settings.edit();

		e.putString("shell", ChargingLEDLib.shellOpenCommand);
		for (int i = 0; i < colorOrder.length; i++)
		{
			e.putString("color" + i, colorOrder[i]);
			e.putInt("threshold" + i, ChargingLEDLib.thresholds[i]);
			e.putString("ledpath" + i, ChargingLEDLib.ledpaths[i]);
		}
		for (int i = 0; i < ChargingLEDLib.availableLEDs.length; i++)
		{
			e.putString("availableLED" + i, ChargingLEDLib.availableLEDs[i]);
		}
		e.commit();
	}

}
