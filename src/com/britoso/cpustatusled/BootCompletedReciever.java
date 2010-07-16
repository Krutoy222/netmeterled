package com.britoso.cpustatusled;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

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
		//start the service.
		Intent serviceIntent = new Intent(context,CPUStatusLEDService.class);
		context.startService(serviceIntent);
		Toast.makeText(context, "CPUStatusLED service started.", Toast.LENGTH_SHORT).show();
	}

}
