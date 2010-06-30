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

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Vector;

import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.TextView;

public class StatsProcessor {
	final private int NUM_COUNTERS = 4;
	final private String DEV_FILE = "/proc/self/net/dev";
	final private String WIFI_DEV = "tiwlan0";
	final private String CELL_DEV = "rmnet0";

	private int mSamplingInterval;

	private Vector<StatCounter> mCounters;
	private Vector<TextView> mCounterViews;

	StatsProcessor(int sampling_interval,
				TelephonyManager cellular,
				WifiManager wifi,
				ConnectivityManager cx) {
		mSamplingInterval = sampling_interval;
		mCounters = new Vector<StatCounter>();
		for (int i = 0; i < NUM_COUNTERS; ++i) {
			mCounters.addElement(new StatCounter("B"));
		}
	}

	public void reset() {
		for (int i=0; i < NUM_COUNTERS; ++i ) {
			mCounters.get(i).reset();
			if (mCounterViews != null) {
				mCounters.get(i).paint(mCounterViews.get(i));
			}
		}
	}

	public Vector<StatCounter> getCounters() {
		return mCounters;
	}

	public void linkDisplay(Vector<TextView> counter_views,
							Vector<TextView> info_views,
							GraphView graph) {
		mCounterViews = counter_views;
	}

	public void unlinkDisplay() {
		mCounterViews = null;
	}

	public boolean processUpdate() {
		return true;
		//processNetStatus();
		//return processIfStats();
	}

	public boolean processIfStats1() {
		FileReader fstream;
		try {
			fstream = new FileReader(DEV_FILE);
		} catch (FileNotFoundException e) {
			Log.e("MonNet", "Could not read " + DEV_FILE);
			return false;
		}
		BufferedReader in = new BufferedReader(fstream, 500);
		String line;
		String[] segs;
		try {
			while ((line = in.readLine()) != null) {

				if (line.startsWith(CELL_DEV)) {
					segs = line.trim().split("[: ]+");
					updateStatCounter(segs[1], 0);
					updateStatCounter(segs[9], 1);
				} else if (line.startsWith(WIFI_DEV)) {
					segs = line.trim().split("[: ]+");
					updateStatCounter(segs[1], 2);
					updateStatCounter(segs[9], 3);
				}
			}
			return true;
		} catch (IOException e) {
			Log.e("MonNet", e.toString());
			return false;
		}
	}

	private void updateStatCounter(String text, int index) {
		if (mCounters.get(index).update(text, mSamplingInterval)) {
			if (mCounterViews != null) {
				mCounters.get(index).paint(mCounterViews.get(index));
			}
		}
	}

}