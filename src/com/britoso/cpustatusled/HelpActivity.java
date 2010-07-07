/*
 * Copyright (C) 2009 Google Inc.
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

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

/**
 *
 * Activity which display the static helptext
 * defined in the string resources file.
 *
 */
public class HelpActivity extends Activity {

	/**
	 * Framework method called when activity is created.
	 */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.help);
        TextView tv = (TextView) findViewById(R.id.helptext);
        String text = this.getResources().getText(R.string.help_text).toString();
        text=text.replaceAll("APPNAME",this.getResources().getText(R.string.app_name).toString());
        text=text.replaceAll("APPVERSION",this.getResources().getText(R.string.app_version).toString());
        tv.setText(text);
        //Linkify.addLinks(tv, Linkify.ALL);

    }

}
