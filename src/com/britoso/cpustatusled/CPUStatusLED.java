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


import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.britoso.cpustatusled.utilclasses.ChargingLEDLib;

/**
 *
 * Main controller activity for CPUStatusLED application.
 *
 * Creates the display (table plus graph view) and connects to
 * the CPUStatusLEDService, starting it if necessary. Since the service
 * will directly update the display when it generates new data, references
 * of the display elements are passed to the service after binding.
 */
public class CPUStatusLED extends Activity {
	 private String TAG;
	 View chart=null;
	 public static boolean disabledLEDs=false;
	 public static boolean noLEDs=false;
	 CPUStatusChart cpuStatusChart;
	 ChargingLEDLib lib;

	/**
	 * Service connection callback object used to establish communication with
	 * the service after binding to it.
	 */
	private myServiceConnection mConnection;

    /**
     * Framework method called when the activity is first created.
     * */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TAG=this.getResources().getText(R.string.app_name).toString();
        lib=new ChargingLEDLib(this);
        //Log.i(TAG, "onCreate");
        startService(new Intent(this, CPUStatusLEDService.class));
        //CPUStatusLEDService.setTelephonyManager((TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE));

        //Log.i(TAG, "checking intent for parameters");
        if(this.getIntent()!=null && this.getIntent().getExtras()!=null && this.getIntent().getExtras().getBoolean("start_minimized"))
        {
        	this.getIntent().getExtras().remove("start_minimized");
        	//TODO: try commenting out the next line.
            this.moveTaskToBack(true);
        	Toast.makeText(this,TAG +" sent to the background.", Toast.LENGTH_SHORT).show();
        	return;
        }

        setContentView(R.layout.layout);
        if(cpuStatusChart==null)cpuStatusChart=new CPUStatusChart();
        //chart= cpuStatusChart.createView(this);
        //chart.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
        //ScrollView sv= (ScrollView)this.findViewById(R.id.scrollview);
        //sv.addView(chart);
        mConnection = new myServiceConnection(this);

        }

    /**
     * Framework method to create menu structure.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	MenuInflater inflater = getMenuInflater();
    	inflater.inflate(R.menu.main_menu, menu);
    	return super.onCreateOptionsMenu(menu);
    }

    /**
     * Framework method called when activity menu option is selected.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	switch(item.getItemId()) {
    	case R.id.disableleds:
    		CPUStatusLED.disabledLEDs=!CPUStatusLED.disabledLEDs;
    		break;
    	case R.id.help:
    		Intent myIntent = new Intent();
    		myIntent.setClass(this, HelpActivity.class);
    		startActivity(myIntent);
    		break;
    	case R.id.stop:
    		lib.turnOffAllLEDs();
    		stopService(new Intent(this, CPUStatusLEDService.class));
    		mConnection.mService.stopSelf();
    		mConnection.mService.stopService(getIntent());
    		finish();
    		if(mConnection.mService!=null)
    			System.exit(RESULT_OK);//service just wont die otherwise!!!
    		break;
    	}
    	return true;
    }

    /**
     * Framework method called when activity becomes the foreground activity.
     *
     * onResume/onPause implement the most narrow window of activity life-cycle
     * during which the activity is in focus and foreground.
     */
    @Override
    public void onResume() {
    	super.onResume();
    	bindService(new Intent(this,CPUStatusLEDService.class), mConnection, Context.BIND_AUTO_CREATE);

    }

    /**
     * Framework method called when activity looses foreground position
     */
    @Override
    public void onPause() {
    	super.onPause();
    	unbindService(mConnection);
    }

    public void setCPUValues(String value)
    {
    	TextView tv= (TextView)this.findViewById(R.id.widget31);
    	tv.setText(value);
    }

	public void updateGraph(ArrayList<Integer> userHistory, ArrayList<Integer> systemHistory,ArrayList<Integer> signalHistory, ArrayList<String> topProcesses)
	{
		if(topProcesses !=null && topProcesses.size()>=3)
		{
			((TextView)this.findViewById(R.id.top_process)).setText(topProcesses.get(0)+"  "+topProcesses.get(1)+"  "+topProcesses.get(2));
		}
		else
		{
			((TextView)this.findViewById(R.id.top_process)).setText("");
		}

		chart=cpuStatusChart.createView(this, userHistory, systemHistory, signalHistory);
		chart.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
        ScrollView sv= (ScrollView)this.findViewById(R.id.scrollview);
        sv.removeAllViews();
        sv.addView(chart);
	}

	public void showNoLEDsAlert()
	{
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage("No LEDs were detected. Do you want to exit? The app will run without LEDs too.")
		       .setCancelable(false)
		       .setPositiveButton("Exit", new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) {
		        	    //mConnection.mService.stopSelf();
		        	    mConnection.mService.stopService(getIntent());
		                CPUStatusLED.this.finish();
		                //System.exit(0);
		           }
		       })
		       .setNegativeButton("Continue", new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) {
		                dialog.cancel();
		                CPUStatusLED.noLEDs=true;
		           }
		       });
		builder.create().show();
	}
}