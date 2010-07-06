/*
 * Copyright (C) 2008 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.britoso.cpustatusled;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

/**
 * Local service which operates in close cooperation with CPUStatusLED activity.
 *
 * Execute monitoring through periodic polling, update in-memory history
 * buffers and update display if linkage has been established by the
 * activity after binding to the service.
 *
 * Whenever running, maintain a persistent notification in the status bar, which
 * sends an intent to (re)start CPUStatusLED activity.
 */
public class CPUStatusLEDService extends Service
{

	final private String TAG="CPUStatusLEDService";
	final private int SAMPLING_INTERVAL = 3;

	private NotificationManager mNM;

	/**
	 *
	 * Binder implementation which passes through a reference to
	 * this service. Since this is a local service, the activity
	 * can then call directly methods on this service instance.
	 */
	public class CPUStatusLEDBinder extends Binder {
        CPUStatusLEDService getService() {
            return CPUStatusLEDService.this;
        }
    }
	private final IBinder mBinder = new CPUStatusLEDBinder();

	private CpuMon mCpuMon;
	//private GraphView mGraph = null;
	private CPUStatusLED gui;

	public CPUStatusLED getGui()
	{
		return gui;
	}

	public void setGui(CPUStatusLED gui)
	{
		this.gui = gui;
		mCpuMon.linkDisplay(gui);
	}

	// All the polling and display updating is driven from this
	// hander which is periodically executed every SAMPLING_INTERVAL seconds.
	private Handler mHandler = new Handler();
	private Runnable mRefresh = new Runnable() {
		public void run() {
			//this reads the /proc/stats file
			mCpuMon.readStats();
			mHandler.postDelayed(mRefresh, SAMPLING_INTERVAL * 1000);
		}
	};

	/**
	 * Framework method called when the service is first created.
	 */
	@Override
    public void onCreate() {
		Log.i(TAG, "onCreate");

		mCpuMon = new CpuMon();

		mNM = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);

		mHandler.postDelayed(mRefresh, SAMPLING_INTERVAL * 1000);

	}

	/**
	 * Framework method called when the service is stopped/destroyed
	 */
	@Override
    public void onDestroy() {
		Log.i(TAG, "onDestroy");
		mNM.cancel(R.string.iconized);
		mHandler.removeCallbacks(mRefresh);
	}

	/**
	 * Framework method called whenever an activity binds to this service.
	 */
	@Override
	public IBinder onBind(Intent arg0) {
		Log.i(TAG, "onBind");
		return mBinder;
	}
	/**
	 * Framework method called when an activity binding to the service
	 * is broken.
	 */
	@Override
	public boolean onUnbind(Intent arg) {
		Log.i(TAG, "onUnbind");
		mCpuMon.unlinkDisplay();
		//mGraph = null;
		return true;
	}

}