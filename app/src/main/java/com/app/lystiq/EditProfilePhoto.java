package com.app.lystiq;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.app.external.Preview;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

/**
 * Created by hitasoft on 27/6/16.
 * <p>
 * This class is for Edit a Profile Photo
 */

public class EditProfilePhoto extends AppCompatActivity implements View.OnClickListener {

    /**
     * Declare Layout Elements
     **/
    SurfaceView surfaceView;
    Preview preview;
    Camera camera;
    TextView take, usephoto;
    ImageView cancelbtn, imageView, flashBtn, retake;

    /**
     * Declare Variables
     **/
    final String TAG = "EditProfilePhoto";
    public static String imgPath = "";
    public static boolean editPhoto = false, flash = false;
    boolean istaken = false;
    int currentCameraId = Camera.CameraInfo.CAMERA_FACING_BACK;

    Camera.ShutterCallback shutterCallback = new Camera.ShutterCallback() {
        public void onShutter() {
            Log.d(TAG, "onShutter'd");
        }
    };

    Camera.PictureCallback rawCallback = new Camera.PictureCallback() {
        public void onPictureTaken(byte[] data, Camera camera) {
            Log.d(TAG, "onPictureTaken - raw");
        }
    };

    Camera.PictureCallback jpegCallback = new Camera.PictureCallback() {
        public void onPictureTaken(byte[] data, Camera camera) {
            new SaveImageTask().execute(data);
            Log.d(TAG, "onPictureTaken - jpeg");
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.editprofilepic);
        surfaceView = (SurfaceView) findViewById(R.id.surfaceView);

        preview = new Preview(this, surfaceView);
        preview.setKeepScreenOn(true);
        take = (TextView) findViewById(R.id.take);
        usephoto = (TextView) findViewById(R.id.usephoto);
        cancelbtn = (ImageView) findViewById(R.id.backbtn);
        imageView = (ImageView) findViewById(R.id.imageView);
        flashBtn = (ImageView) findViewById(R.id.flashBtn);
        retake = (ImageView) findViewById(R.id.retakeBtn);

        take.setOnClickListener(this);
        usephoto.setOnClickListener(this);
        cancelbtn.setOnClickListener(this);
        retake.setOnClickListener(this);
        flashBtn.setOnClickListener(this);

        if (flash) {
            flashBtn.setSelected(true);
            flashBtn.setColorFilter(getResources().getColor(R.color.colorPrimary));
        } else {
            flashBtn.setSelected(false);
            flashBtn.setColorFilter(null);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // For Internet checking
        JoysaleApplication.registerReceiver(EditProfilePhoto.this);

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                int numCams = Camera.getNumberOfCameras();
                if (numCams > 0) {
                    try {
                        if (ContextCompat.checkSelfPermission(EditProfilePhoto.this, CAMERA) != PackageManager.PERMISSION_GRANTED
                                && ContextCompat.checkSelfPermission(EditProfilePhoto.this, WRITE_EXTERNAL_STORAGE)
                                != PackageManager.PERMISSION_GRANTED) {
                            ActivityCompat.requestPermissions(EditProfilePhoto.this, new String[]{CAMERA, WRITE_EXTERNAL_STORAGE}, 100);
                        } else if (ContextCompat.checkSelfPermission(EditProfilePhoto.this, CAMERA)
                                != PackageManager.PERMISSION_GRANTED) {
                            ActivityCompat.requestPermissions(EditProfilePhoto.this, new String[]{CAMERA}, 101);
                        } else if (ContextCompat.checkSelfPermission(EditProfilePhoto.this, WRITE_EXTERNAL_STORAGE)
                                != PackageManager.PERMISSION_GRANTED) {
                            ActivityCompat.requestPermissions(EditProfilePhoto.this, new String[]{WRITE_EXTERNAL_STORAGE}, 102);
                        } else {
                            camera = Camera.open(0);
                            preview.setCamera(camera, flash);
                            camera.startPreview();
                        }
                    } catch (RuntimeException ex) {
                        ex.printStackTrace();
                        Toast.makeText(getApplicationContext(), getString(R.string.camera_not_found), Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(getApplicationContext(), getString(R.string.camera_not_found), Toast.LENGTH_LONG).show();
                }
            }
        });

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.v(TAG, "requestCode=" + requestCode);
        if (Build.VERSION.SDK_INT >= 23) {
            switch (requestCode) {
                case 100:
                    if (grantResults.length > 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                        Toast.makeText(EditProfilePhoto.this, getString(R.string.storage_camera_permisssion), Toast.LENGTH_SHORT).show();
                        finish();
                    } else if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        Toast.makeText(EditProfilePhoto.this, getString(R.string.camera_permission_access), Toast.LENGTH_SHORT).show();
                        finish();
                    } else if (grantResults.length > 1 && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                        Toast.makeText(EditProfilePhoto.this, getString(R.string.storage_permission_access), Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(EditProfilePhoto.this, getString(R.string.need_permission_to_access), Toast.LENGTH_SHORT).show();
                        finish();
                    }
                    break;
                case 101:
                    if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        Toast.makeText(EditProfilePhoto.this, getString(R.string.camera_permission_access), Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(EditProfilePhoto.this, getString(R.string.need_permission_to_access), Toast.LENGTH_SHORT).show();
                        finish();
                    }
                    break;
                case 102:
                    if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        Toast.makeText(EditProfilePhoto.this, getString(R.string.storage_permission_access), Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(EditProfilePhoto.this, getString(R.string.need_permission_to_access), Toast.LENGTH_SHORT).show();
                        finish();
                    }
                    break;
            }
        }
    }

    @Override
    protected void onPause() {
        // For Internet checking disconnect
        JoysaleApplication.unregisterReceiver(EditProfilePhoto.this);
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (camera != null) {
                    camera.stopPreview();
                    preview.setCamera(null, flash);
                    camera.release();
                    camera = null;
                }
            }
        });
    }

    // Function for rotate the bitmap
    public Bitmap rotate(Bitmap src, float degree) {
        // create new matrix object
        Matrix matrix = new Matrix();
        // setup rotation degree
        matrix.postRotate(degree);

        // return new bitmap rotated using matrix
        return Bitmap.createBitmap(src, 0, 0, src.getWidth(), src.getHeight(), matrix, true);
    }

    public static int getRoatationAngle(Activity mContext, int cameraId) {
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(cameraId, info);
        int rotation = mContext.getWindowManager().getDefaultDisplay().getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }
        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360; // compensate the mirror
        } else { // back-facing
            result = (info.orientation - degrees + 360) % 360;
        }
        return result;
    }

    private Bitmap decodeFile(String fPath) {
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inJustDecodeBounds = true;
        opts.inDither = false;
        opts.inPurgeable = true;
        opts.inInputShareable = true;
        BitmapFactory.decodeFile(fPath, opts);
        final int REQUIRED_SIZE = 1024;
        int scale = 1;

        if (opts.outHeight > REQUIRED_SIZE || opts.outWidth > REQUIRED_SIZE) {
            final int heightRatio = Math.round((float) opts.outHeight
                    / (float) REQUIRED_SIZE);
            final int widthRatio = Math.round((float) opts.outWidth
                    / (float) REQUIRED_SIZE);
            scale = heightRatio < widthRatio ? heightRatio : widthRatio;//
        }
        opts.inJustDecodeBounds = false;
        opts.inSampleSize = scale;
        Bitmap bm = BitmapFactory.decodeFile(fPath, opts).copy(
                Bitmap.Config.RGB_565, false);
        return bm;
    }

    private void refreshGallery(File file) {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        mediaScanIntent.setData(Uri.fromFile(file));
        sendBroadcast(mediaScanIntent);
    }

    // to Reset camera preview
    private void resetCam() {
        preview.setCamera(camera, flash);
        camera.startPreview();
    }

    /**
     * Class to Store a image in device
     **/

    class SaveImageTask extends AsyncTask<byte[], Void, Void> {

        Bitmap bitmapImage;

        @Override
        protected Void doInBackground(byte[]... data) {
            FileOutputStream outStream = null;

            // Write to SD Card
            try {

                File sdCard = Environment.getExternalStorageDirectory();
                File dir = new File(sdCard.getAbsolutePath() + "/" + getString(R.string.app_name));
                dir.mkdirs();

                String fileName = String.format("%d.jpg", System.currentTimeMillis());
                File outFile = new File(dir, fileName);

                outStream = new FileOutputStream(outFile);

                outStream.write(data[0]);
                outStream.flush();
                outStream.close();

                imgPath = outFile.getAbsolutePath();

                Bitmap realImage = decodeFile(outFile.getAbsolutePath());

                int angleToRotate = getRoatationAngle(EditProfilePhoto.this, currentCameraId);

                if (currentCameraId != 0) {
                    Matrix matrix = new Matrix();
                    float[] mirrorY = {-1, 0, 0, 0, 1, 0, 0, 0, 1};
                    Matrix matrixMirrorY = new Matrix();
                    matrixMirrorY.setValues(mirrorY);

                    matrix.postConcat(matrixMirrorY);
                    matrix.postRotate(90);
                    bitmapImage = Bitmap.createBitmap(realImage, 0, 0, realImage.getWidth(), realImage.getHeight(), matrix, true);
                } else {
                    bitmapImage = rotate(realImage, angleToRotate);
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        imageView.setImageBitmap(bitmapImage);
                        imageView.setVisibility(View.VISIBLE);
                    }
                });

                refreshGallery(outFile);

                File file = new File(dir, fileName);
                if (file.exists()) file.delete();
                try {
                    FileOutputStream out = new FileOutputStream(file);
                    bitmapImage.compress(Bitmap.CompressFormat.JPEG, 90, out);
                    out.flush();
                    out.close();

                } catch (Exception e) {
                    e.printStackTrace();
                }

                Log.d("onPictureTaken", "onPictureTaken" + outFile.getAbsolutePath());

                refreshGallery(file);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (NullPointerException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
            }
            return null;
        }
    }

    /**
     * On Click Event
     **/

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.backbtn:
                finish();
                break;
            case R.id.take:
                if (!istaken) {
                    camera.takePicture(shutterCallback, rawCallback, jpegCallback);
                    imageView.setImageBitmap(null);
                    take.setText(getString(R.string.retake));
                    imageView.setVisibility(View.VISIBLE);
                    retake.setVisibility(View.GONE);
                    flashBtn.setVisibility(View.GONE);
                    istaken = true;
                } else {
                    resetCam();
                    imageView.setVisibility(View.GONE);
                    retake.setVisibility(View.VISIBLE);
                    flashBtn.setVisibility(View.VISIBLE);
                    take.setText(getString(R.string.take));
                    istaken = false;
                }
                break;
            case R.id.usephoto:
                try {
                    if (istaken) {
                        editPhoto = true;
                        finish();
                    } else {
                        Toast.makeText(getApplicationContext(), getString(R.string.please_take_snap), Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case R.id.retakeBtn:
                //NB: if you don't release the current camera before switching, you app will crash
                if (camera != null) {
                    camera.stopPreview();
                    preview.setCamera(null, flash);
                    camera.release();
                    camera = null;
                }
                //swap the id of the camera to be used
                if (currentCameraId == Camera.CameraInfo.CAMERA_FACING_BACK) {
                    currentCameraId = Camera.CameraInfo.CAMERA_FACING_FRONT;
                } else {
                    currentCameraId = Camera.CameraInfo.CAMERA_FACING_BACK;
                }
                camera = Camera.open(currentCameraId);
                Preview.setCameraDisplayOrientation(EditProfilePhoto.this, currentCameraId, camera);
                try {
                    camera.setPreviewDisplay(preview.getSurfaceHolder());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                camera.startPreview();
                break;
            case R.id.flashBtn:
                if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)) {
                    if (flashBtn.isSelected()) {
                        flashBtn.setSelected(false);
                        flashBtn.setColorFilter(null);
                        flash = false;
                    } else {
                        flashBtn.setSelected(true);
                        flashBtn.setColorFilter(getResources().getColor(R.color.colorPrimary));
                        flash = true;
                    }
                    resetCam();
                } else {
                    Toast.makeText(getApplicationContext(), getString(R.string.your_device_doesnt_flash), Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }
}
