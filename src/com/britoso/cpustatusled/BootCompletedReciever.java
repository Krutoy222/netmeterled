package com.britoso.cpustatusled;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.britoso.cpustatusled.utilclasses.ChargingLEDLib;

/**
 *
 * @author britoso
 *
 */
public class BootCompletedReciever extends BroadcastReceiver
{
	@Override
	public void onReceive(Context context, Intent intentin)
	{
		ChargingLEDLib lib = new ChargingLEDLib();
		ChargingLEDLib.context=context;//set the context
		lib.readPrefs();
		//start the service.
		Intent serviceIntent = new Intent();
		serviceIntent.setAction("com.britoso.cpustatusled.CPUStatusLEDService");
		context.startService(serviceIntent);
		Toast.makeText(context, "CPUStatusLED service started.", Toast.LENGTH_SHORT).show();
	}

}
