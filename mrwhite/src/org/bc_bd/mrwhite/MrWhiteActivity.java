/*
 * Copyright (c) 2012 Stefan Völkel <bd@bc-bd.org>
 *
 * Released under the GPL v2. See file License for details.
 *
 * */

package org.bc_bd.mrwhite;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;

public class MrWhiteActivity extends Activity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        
        View mrwhite = new View(this);
        mrwhite.setBackgroundColor(Color.WHITE);
        mrwhite.setKeepScreenOn(true);
        setContentView(mrwhite);
    }
    
    @Override
    public void onStart() {
    	super.onStart();
    	
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.screenBrightness = 1.0f;
        getWindow().setAttributes(lp);

        // for full screen mode
        getWindow().addFlags(LayoutParams.FLAG_FULLSCREEN);
    }
}
