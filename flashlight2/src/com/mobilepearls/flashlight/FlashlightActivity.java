package com.mobilepearls.flashlight;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;

public class FlashlightActivity extends Activity {

	private static final int PREF_SLOW = 0;
	private static final int PREF_FRANTIC = 1;
	private static final int PREF_NORMAL = 2;
	private static final int PREF_SOS = 3;
	private static final int PREF_OFF = 4;
	
	private static final int[] SPEED_SLOW = new int[] { 1000 };
	private static final int[] SPEED_FRANTIC = new int[] { 70 };
	private static final int[] SPEED_NORMAL = new int[] { 500 };
	// Originally Posted by Federal Code 46CFR161.013-7
	// Subpart 161.013_Electric Distress Light for Boats
	//
	// Sec. 161.013-7 Signal requirements.
	//
	// (a) An electric light must have a flash characteristic of the
	// International Morse Code for S-O-S and, under design conditions,
	// (1) Each short flash must have a duration of 1/3 second;
	// (2) Each long flash must have a duration of 1 second;
	// (3) The dark period between each short flash must have a duration of 1/3
	// second;
	// (4) The dark period between each long flash must have a duration of 1/3
	// second;
	// (5) The dark period between each letter must have a duration of 2
	// seconds;
	// (6) The dark period between each S-O-S signal must have a duration of 3
	// seconds.
	// (b) The flash characteristics described in paragraph (a) must be produced
	// automatically when the signal is activated.
	private static final int[] SPEED_SOS = new int[] { 3000, // FIRST DONE,
			333, 333, 333, 333, 333, 2000,
			// BREAK
			1000, 333, 1000, 333, 1000, 2000, // BREAK
			333, 333, 333, 333, 333 // BREAK
	};

	private static final int[][] SPEEDS = new int[][]{
		SPEED_SLOW, SPEED_FRANTIC, SPEED_NORMAL, SPEED_SOS
	};
	
	private static final String SHARED_PREFS_NAME = "flashlight_prefs";
	private static final String PREFS_COLOR = "color";
	private static final String PREFS_BLINK = "pref_blink";

	private PowerManager.WakeLock wakeLock;
	public Handler uiHandler = new Handler();
	private Thread blinkThread;
	private int currentColor = Color.WHITE;

	public void setColor(int color) {
		SharedPreferences prefs = getSharedPreferences("flashlight_prefs",
				Context.MODE_PRIVATE);
		Editor editor = prefs.edit();
		editor.putInt(PREFS_COLOR, color);
		editor.commit();
		currentColor = color;

		final View view = (View) findViewById(123);
		view.setBackgroundColor(currentColor);
	}

	static class MutableInt {
		int value = -1;
	}

	public void startBlinkThread(final int[] speed) {
		final View view = (View) findViewById(123);

		final MutableInt index = new MutableInt();

		blinkThread = new Thread() {
			boolean aboutToGoBlack = true;

			@Override
			public void run() {
				while (true) {
					try {
						uiHandler.post(new Runnable() {
							@Override
							public void run() {
								if (aboutToGoBlack) {
									view.setBackgroundColor(Color.BLACK);
								} else {
									view.setBackgroundColor(currentColor);
								}
								aboutToGoBlack = !aboutToGoBlack;
							}
						});
						index.value = (index.value + 1) % speed.length;
						Thread.sleep(speed[index.value]);
					} catch (InterruptedException e) {
						uiHandler.post(new Runnable() {
							@Override
							public void run() {
								view.setBackgroundColor(currentColor);
							}
						});
						return;
					}
				}
			}
		};
		blinkThread.start();
	}

	public void stopBlinkThread() {
		if (blinkThread != null) {
			blinkThread.interrupt();
			blinkThread = null;
		}
	}

	public void stopBlink() {
		SharedPreferences prefs = getSharedPreferences(SHARED_PREFS_NAME,
				Context.MODE_PRIVATE);
		Editor editor = prefs.edit();
		editor.putInt(PREFS_BLINK, PREF_OFF);
		editor.commit();
		stopBlinkThread();
	}

	public void startBlink(int speed) {
		if (blinkThread != null) {
			stopBlink();
		}

		SharedPreferences prefs = getSharedPreferences(SHARED_PREFS_NAME,
				Context.MODE_PRIVATE);
		Editor editor = prefs.edit();
		editor.putInt(PREFS_BLINK, speed);
		editor.commit();

		int[] speeds = SPEEDS[speed];
		
		startBlinkThread(speeds);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		wakeLock = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK,
				"FlashlightActivity");

		View view = new View(this);
		view.setId(123);
		setContentView(view);
	}

	@Override
	protected void onPause() {
		super.onPause();

		wakeLock.release();
		stopBlinkThread();
	}

	@Override
	protected void onResume() {
		super.onResume();

		wakeLock.acquire();

		WindowManager.LayoutParams lp = getWindow().getAttributes();
		lp.screenBrightness = 1;
		getWindow().setAttributes(lp);

		SharedPreferences prefs = getSharedPreferences(SHARED_PREFS_NAME,
				Context.MODE_PRIVATE);

		currentColor = prefs.getInt(PREFS_COLOR, Color.WHITE);
		final View view = (View) findViewById(123);
		view.setBackgroundColor(currentColor);

		int pref = prefs.getInt(PREFS_BLINK, PREF_OFF);
		if (pref != PREF_OFF) {
			startBlinkThread(SPEEDS[pref]);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.about_menu:
			Intent aboutIntent = new Intent(this, AboutActivity.class);
			startActivity(aboutIntent);
			return true;
		case R.id.menu_blue:
			setColor(Color.BLUE);
			return true;
		case R.id.menu_cyan:
			setColor(Color.CYAN);
			return true;
		case R.id.menu_yellow:
			setColor(Color.YELLOW);
			return true;
		case R.id.menu_magenta:
			setColor(Color.MAGENTA);
			return true;
		case R.id.menu_white:
			setColor(Color.WHITE);
			return true;
		case R.id.menu_green:
			setColor(Color.GREEN);
			return true;
		case R.id.menu_red:
			setColor(Color.RED);
			return true;
		case R.id.menu_flash_slow:
			startBlink(PREF_SLOW);
			return true;
		case R.id.menu_flash_frantic:
			startBlink(PREF_FRANTIC);
			return true;
		case R.id.menu_flash_normal:
			startBlink(PREF_NORMAL);
			return true;
		case R.id.menu_flash_sos:
			startBlink(PREF_SOS);
			return true;
		case R.id.menu_flash_none:
			stopBlink();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

}