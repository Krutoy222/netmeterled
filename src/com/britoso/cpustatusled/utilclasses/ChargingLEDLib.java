package com.britoso.cpustatusled.utilclasses;

import java.io.DataOutputStream;
import java.io.File;

import android.util.Log;

import com.britoso.cpustatusled.CPUStatusLED;
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
	private final static String ROOT_SHELL = "su";
	private final static String NORMAL_SHELL = "sh";
	private static final String COLOR_BLUE = "blue", COLOR_GREEN = "green", COLOR_AMBER = "amber", COLOR_RED = "red";
	private final static String TAG = "CPUStatusLED";
	
	//reused
	public final static boolean DEBUG = false;
	// keep the console used to issue commands open. more efficient
	private static Process console = null;
	private static DataOutputStream os = null;
	static CPUStatusLED gui;
	static int lastThreshhold = 0;
	
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
	
	public ChargingLEDLib(CPUStatusLED gui)
	{
		ChargingLEDLib.gui = gui;
	}
	
	public ChargingLEDLib()
	{
	}
	
	public void initialize()
	{
		Log.i(TAG, "Phone Model=" + android.os.Build.MODEL);
		
		//leds
		String ledpathsononeline = detectLEDs();
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
		
		if (colorOrder[0] == null)
		{
			if (gui != null) gui.showNoLEDsAlert();//exit or continue
		}
		
		//uses sets ledpaths based on the colorOrder 
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
	
	private String detectLEDs()
	{
		ShellCommand cmd = new ShellCommand();
		CommandResult r = cmd.sh.runWaitFor("find /sys/devices/ -name \"brightness\"");
		String result = "";
		
		if (!r.success())
		{
			//try su. need root to run find on some phones!
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
		if (CPUStatusLED.noLEDs) return;
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
		if (CPUStatusLED.disabledLEDs || CPUStatusLED.noLEDs) return;//dont do anything
		
		if (DEBUG) Log.d(TAG, "CPU=" + totalCPUInt);
		
		if (ledpaths == null || ledpaths.length == 0)
		{
			initialize();
			turnOffAllLEDs();
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
}
