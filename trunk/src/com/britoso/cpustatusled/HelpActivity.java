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

import java.util.regex.Pattern;

import com.britoso.cpustatusled.R;

import android.app.Activity;
import android.os.Bundle;
import android.text.util.Linkify;
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

        // Set the layout for this activity.  You can find it
        // in res/layout/help.xml
        setContentView(R.layout.help);
        TextView tv = (TextView) findViewById(R.id.helptext);
        String text = this.getResources().getText(R.string.help_text).toString();
        tv.setText(text);
        Linkify.addLinks(tv, Linkify.ALL);
//        //pattern we want to match and turn into a clickable link
//        Pattern pattern1 = Pattern.compile("code.google.com");
//        //prefix our pattern with http://
//        Linkify.addLinks(tv, pattern1, "http://");
////        Pattern pattern2 = Pattern.compile("code.google.com/p/android-labs");
////        //prefix our pattern with http://
////        Linkify.addLinks(tv, pattern2, "http://");

    }

}
