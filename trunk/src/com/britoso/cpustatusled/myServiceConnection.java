package com.britoso.cpustatusled;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

public class myServiceConnection implements ServiceConnection
{
	CPUStatusLEDService mService;
	CPUStatusLED activity;

	myServiceConnection(CPUStatusLED activity)
	{
		this.activity=activity;

	}
        public void onServiceConnected(ComponentName className, IBinder service) {

        	// Get reference to (local) service from binder
            mService = ((CPUStatusLEDService.CPUStatusLEDBinder)service).getService();
            Log.i("myServiceConnection", "service connected, gui attached.");
            // link up the display elements to be updated by the service
            mService.setGui(activity);
        }

        public void onServiceDisconnected(ComponentName className) {
            mService = null;
            Log.i("myServiceConnection", "service disconnected - should never happen");
        }
}