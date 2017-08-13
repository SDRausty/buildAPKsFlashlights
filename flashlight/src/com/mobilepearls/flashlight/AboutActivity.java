package com.mobilepearls.flashlight;

import android.app.Activity;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

public class AboutActivity extends Activity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.about);

		try {
			PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
			TextView aboutText = (TextView) findViewById(R.id.about_text);
			aboutText.setText(aboutText.getText().toString().replace("$VERSION$", packageInfo.versionName));
		} catch (NameNotFoundException e) {
			Log.e(getClass().getSimpleName(), e.getMessage(), e);
		}
	}

}
