package com.example.flashlight;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.hardware.camera2.CameraManager;
import android.os.Build;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

public class MainActivity extends Activity implements View.OnClickListener {

    private ImageView powerImageView;

    private CameraManager camManager;
    private Camera cam;

    private boolean lightIsOn = false;
    
    // Record the touch down position & base brightness
    private float startX = -1;
    private float startY = -1;
    private int startBrightness = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_main);

        powerImageView = (ImageView) findViewById(R.id.iv_poweronoff);
        powerImageView.setOnClickListener(this);
    }

    private void flashLightOn() {
        // >= android 6.0
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (camManager == null) {
                camManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
            }
            try {
                // Usually back camera is at 0 position.
                String cameraId = camManager.getCameraIdList()[0];
                camManager.setTorchMode(cameraId, true);
            } catch (Exception e) {
                showError(e);
            }
            return;
        }

        try {
            if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)) {
                cam = Camera.open();
                Camera.Parameters p = cam.getParameters();
                p.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                cam.setParameters(p);
                cam.startPreview();
            }
        } catch (Exception e) {
            showError(e);
        }
    }

    private void flashLightOff() {
        // >= android 6.0
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                String cameraId = camManager.getCameraIdList()[0];
                camManager.setTorchMode(cameraId, false);
            } catch (Exception e) {
                showError(e);
            }
            return;
        }

        try {
            if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)) {
                cam.stopPreview();
                cam.release();
                cam = null;
            }
        } catch (Exception e) {
            showError(e);
        }
    }

    private void showError(Exception e) {
        String message = "Error: " + e.getMessage();
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();

        if (id == R.id.iv_poweronoff) {
            if (lightIsOn) {
                flashLightOff();
                powerImageView.setImageResource(R.drawable.poweroff);
            } else {
                flashLightOn();
                powerImageView.setImageResource(R.drawable.poweron);
            }
            lightIsOn = !lightIsOn;
        }
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent motionEvent) {
        float x = motionEvent.getX();
        float y = motionEvent.getY();

        int action = motionEvent.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                startX = x;
                startY = y;
                startBrightness = getBrightness();
                break;
            case MotionEvent.ACTION_UP:
                startX = -1;
                startY = -1;
                break;
            case MotionEvent.ACTION_MOVE:
                // Try to adjust screen brightness only when start position is valid
                if (startX < 0 || startY < 0) {
                    return false;
                }
                // Stop brightness adjusting when detect the scroll path is not vertical
                if (Math.abs(y - startY) > 50 && Math.abs(y - startY) < 2 * Math.abs(x - startX)) {
                    startX = -1;
                    startY = -1;
                    return false;
                }
                // Scroll up (smaller y) means increase the brightness
                float delta = startY - y;
                delta /= 2;
                adjustBrightness((int) delta);
                break;
        }

        return false;
    }

    // Get current screen brightness
    private int getBrightness() {
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        return (int) (lp.screenBrightness * 255);
    }

    private void adjustBrightness(int delta) {
        int brightness = startBrightness + delta;
        if (brightness < 0) brightness = 0;
        if (brightness > 255) brightness = 255;
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.screenBrightness = brightness * (1f / 255f);
        getWindow().setAttributes(lp);
    }
}
