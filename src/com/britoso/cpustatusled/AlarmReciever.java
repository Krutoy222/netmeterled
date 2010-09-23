package com.britoso.cpustatusled;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.britoso.cpustatusled.utilclasses.ChargingLEDLib;
import com.britoso.cpustatusled.utilclasses.CpuMon;

public class AlarmReciever extends BroadcastReceiver
{
	public static CpuMon mCpuMon;
	public static ChargingLEDLib lib;
	private static final String TAG = "CPUStatusLED.AlarmReciever";

	@Override
	public void onReceive(Context context, Intent intent)
	{
		try
		{
			Bundle bundle = intent.getExtras();
			String message = bundle.getString("alarm_message");
			if (message.equalsIgnoreCase("fire"))
			{
				// hooray we don't have to do anything. the alarm simply wakes
				// up our process.
				// Log.i(TAG, "Alarm");
				// bind to the service
				// call mCpuMon.readStats

			}
			// Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
		}
		catch (Exception e)
		{
			// Toast.makeText(context,
			// "There was an error somewhere, but we still received an alarm",
			// Toast.LENGTH_SHORT).show();
			e.printStackTrace();
			Log.e(TAG, "Error receiving alarm. "+e.getMessage());

		}
	}

}