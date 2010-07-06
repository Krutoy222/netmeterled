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
package com.google.android.netmeterled;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

/**
 * Local service which operates in close cooperation with NetMeter activity.
 *
 * Execute monitoring through periodic polling, update in-memory history
 * buffers and update display if linkage has been established by the
 * activity after binding to the service.
 *
 * Whenever running, maintain a persistent notification in the status bar, which
 * sends an intent to (re)start NetMeter activity.
 */
public class NetMeterLEDService extends Service {
	final private String TAG="NetMeterLEDService";
	final private int SAMPLING_INTERVAL = 3;

	private NotificationManager mNM;

	/**
	 *
	 * Binder implementation which passes through a reference to
	 * this service. Since this is a local service, the activity
	 * can then call directly methods on this service instance.
	 */
	public class NetMeterBinder extends Binder {
        NetMeterLEDService getService() {
            return NetMeterLEDService.this;
        }
    }
	private final IBinder mBinder = new NetMeterBinder();

	private CpuMon mCpuMon;
	//private GraphView mGraph = null;
	private NetMeter gui;

	public NetMeter getGui()
	{
		return gui;
	}

	public void setGui(NetMeter gui)
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

	/**
	 * Set up the notification in the status bar, which can be used to restart the
	 * NetMeter main display activity.
	 */
	@SuppressWarnings("unused")
    private void postNotification() {
    	// In this sample, we'll use the same text for the ticker and the expanded notification
    	CharSequence text = getText(R.string.app_name);

    	// Set the icon, scrolling text and timestamp
    	Notification notification = new Notification(R.drawable.status_icon, text,
    			System.currentTimeMillis());
    	notification.flags |= Notification.FLAG_NO_CLEAR|Notification.FLAG_ONGOING_EVENT;

    	// The PendingIntent to launch our activity if the user selects this notification
    	PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
    			new Intent(this, NetMeter.class), 0);

    	// Set the info for the views that show in the notification panel.
    	notification.setLatestEventInfo(this, getText(R.string.iconized),text, contentIntent);

    	// Send the notification.
    	// We use a string id because it is a unique number.  We use it later to cancel.
    	mNM.notify(R.string.iconized, notification);

    }

}