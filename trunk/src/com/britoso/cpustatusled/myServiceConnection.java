package com.britoso.cpustatusled;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

public class myServiceConnection implements ServiceConnection
{
	public CPUStatusLEDService mService;
	CPUStatusLED activity;

	myServiceConnection(CPUStatusLED activity)
	{
		this.activity=activity;
	}
        public void onServiceConnected(ComponentName className, IBinder service) {
        	//Log.i("CPUStatusLED","service connected, gui attached.");
        	// Get reference to (local) service from binder
            mService = ((CPUStatusLEDService.CPUStatusLEDBinder)service).getService();
            // link up the display elements to be updated by the service
            mService.setGui(activity);
        }

        public void onServiceDisconnected(ComponentName className)
        {
        	//Log.i("CPUStatusLED","myServiceConnection.onServiceDisconnected()");
            mService = null;
            Log.i("CPUStatusLED", "service disconnected - should never happen");
        }
}