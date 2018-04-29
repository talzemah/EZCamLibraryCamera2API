package talzemah.ezcamlibrarycamera2api;

import android.content.Intent;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.TextureView;
import android.view.View;

import java.io.File;
import java.io.IOException;

import me.aflak.ezcam.EZCam;
import me.aflak.ezcam.EZCamCallback;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private EZCam camera;
    private TextureView textureView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textureView = findViewById(R.id.textureView);
        textureView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (camera != null)
                    camera.takePicture();
            }
        });

        startEZCam();
    }

    @Override
    protected void onDestroy() {
        camera.close();

        super.onDestroy();
    }

    private void startEZCam() {

        camera = new EZCam(this);
        // should check if LENS_FACING_BACK exist before calling get()
        String id = camera.getCamerasList().get(CameraCharacteristics.LENS_FACING_BACK);
        camera.selectCamera(id);

        camera.setCameraCallback(new EZCamCallback() {
            @Override
            public void onCameraReady() {
                // triggered after camera.open(...)

                // Set capture settings.
                //camera.setCaptureSetting(CaptureRequest.FLASH_MODE, CameraMetadata.FLASH_STATE_FIRED);
                //camera.setCaptureSetting(CaptureRequest.CONTROL_AF_MODE, CameraMetadata.CONTROL_AF_MODE_AUTO);
                /// camera.setCaptureSetting(CaptureRequest.COLOR_CORRECTION_ABERRATION_MODE, CameraMetadata.COLOR_CORRECTION_ABERRATION_MODE_HIGH_QUALITY);
                /// camera.setCaptureSetting(CaptureRequest.CONTROL_EFFECT_MODE, CameraMetadata.CONTROL_EFFECT_MODE_NEGATIVE);

                camera.startPreview();
            }

            @Override
            public void onPicture(Image image) {
                // internal storage
                //File file = new File(getFilesDir(), System.currentTimeMillis() + ".jpg");
                // external storage, need permissions
                // File file = new File(getExternalFilesDir(null) ,  System.currentTimeMillis() + ".jpg");

                File externalStorageDirectory = Environment.getExternalStorageDirectory();
                File appDirectory = new File(externalStorageDirectory.getAbsolutePath() + "/EZCamLibrary");
                appDirectory.mkdirs();

                String fileName = String.format("%d.jpg", System.currentTimeMillis());
                File imageFile = new File(appDirectory, fileName);

                try {
                    EZCam.saveImage(image, imageFile);
                    Log.d(TAG, "Image Saved to: " + imageFile.getAbsolutePath());

                    refreshGallery(imageFile);

                } catch (IOException e) {
                    Log.e(TAG, e.getStackTrace().toString());
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(String message) {
                // all errors will be passed through this methods
                Log.e(TAG, message);
            }

            @Override
            public void onCameraDisconnected() {
                // camera disconnected
                // Log.e(TAG, "onCameraDisconnected");
            }
        });

        //camera.open(CameraDevice.TEMPLATE_STILL_CAPTURE, textureView);
        camera.open(CameraDevice.TEMPLATE_ZERO_SHUTTER_LAG, textureView);
    }

    // Show the capture image in gallery.
    private void refreshGallery(File file) {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        mediaScanIntent.setData(Uri.fromFile(file));
        sendBroadcast(mediaScanIntent);

        Log.d(TAG, "Image added to gallery");
    }

}
