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

import java.util.Vector;

import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.telephony.TelephonyManager;
import android.widget.TextView;

public class StatsProcessor {
	final private int NUM_COUNTERS = 4;

	private Vector<StatCounter> mCounters;

	StatsProcessor(int sampling_interval,
				TelephonyManager cellular,
				WifiManager wifi,
				ConnectivityManager cx) {
		mCounters = new Vector<StatCounter>();
		for (int i = 0; i < NUM_COUNTERS; ++i) {
			mCounters.addElement(new StatCounter("B"));
		}
	}

	public void reset() {
//		for (int i=0; i < NUM_COUNTERS; ++i ) {
//			mCounters.get(i).reset();
//			if (mCounterViews != null) {
//				mCounters.get(i).paint(mCounterViews.get(i));
//			}
//		}
	}

	public Vector<StatCounter> getCounters() {
		return mCounters;
	}

	public void linkDisplay(Vector<TextView> counter_views,
							Vector<TextView> info_views,
							GraphView graph) {

	}

	public void unlinkDisplay() {

	}

	public boolean processUpdate() {
		return true;
		//processNetStatus();
		//return processIfStats();
	}

}