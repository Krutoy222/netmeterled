package com.google.android.netmeterled;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

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
		Intent intent = new Intent(context, NetMeter.class );
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		Bundle bundle = new Bundle();
		bundle.putBoolean("start_minimized", true);
		intent.putExtras(bundle);
		context.startActivity(intent);
	}
	
}
