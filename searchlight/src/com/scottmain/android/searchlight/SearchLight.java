/*
Copyright 2010 Google Inc.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

package com.scottmain.android.searchlight;

import android.app.Activity;
import android.graphics.drawable.TransitionDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

public class SearchLight extends Activity {
	ImageButton bulb;
	TransitionDrawable mDrawable;
	PreviewSurface mSurface;
	boolean on = false;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        bulb = (ImageButton) findViewById(R.id.button);
        mDrawable = (TransitionDrawable) bulb.getDrawable();
        mDrawable.setCrossFadeEnabled(true);
        mSurface = (PreviewSurface) findViewById(R.id.surface);
    }
    
    public void toggleLight(View v) {
    	if (on) {
    		turnOff();
    	} else {
    		turnOn();
    	}
    }
    
    private void turnOn() {
    	if (!on) {
    	    on = true;
    	    mDrawable.startTransition(200);
    	    mSurface.lightOn();
    	}
    }
    
    private void turnOff() {
    	if (on) {
	        on = false;
	        mDrawable.reverseTransition(300);
    	    mSurface.lightOff();
    	}
    }
    
	@Override
	protected void onResume() {
		turnOn();
		super.onResume();
	}

	@Override
	protected void onPause() {
		turnOff();
		super.onPause();
	}
}