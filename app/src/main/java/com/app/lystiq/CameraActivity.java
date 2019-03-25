package com.app.lystiq;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.gallery.multipleimageselect.activities.AlbumSelectActivity;
import com.gallery.multipleimageselect.activities.ImageSelectActivity;
import com.app.external.Preview;
import com.app.external.RecyclerItemClickListener;
import com.app.utils.Constants;
import com.app.utils.DefensiveClass;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static com.gallery.multipleimageselect.helpers.Constants.REQUEST_CODE;
import static com.app.external.Preview.setCameraDisplayOrientation;

public class CameraActivity extends AppCompatActivity implements OnClickListener {

    // Widget Declaration
    RecyclerView recyclerView;
    ImageView cancelBtn, snapBtn, flashBtn, retake;
    TextView gallery, next;
    SurfaceView surfaceView;
    Preview preview;
    Camera camera;
    @SuppressLint("StaticFieldLeak")
    public ImagesAdapter imagesAdapter;
    public static CameraActivity activity;

    // Variable Declaration
    String from = "";
    private final String TAG = this.getClass().getSimpleName();
    public static boolean flash = false, fromedit;
    int currentCameraId = Camera.CameraInfo.CAMERA_FACING_BACK, previewWidth, previewHeight;

    public ArrayList<HashMap<String, Object>> temp = new ArrayList<HashMap<String, Object>>();
    public ArrayList<HashMap<String, Object>> selectedImages = new ArrayList<HashMap<String, Object>>();
    private HashMap<String, String> itemMap = new HashMap<>();

    // callback For Camera purpose
    ShutterCallback shutterCallback = new ShutterCallback() {
        public void onShutter() {
            Log.d("onShutter'd", "onShutter'd");
        }
    };

    PictureCallback rawCallback = new PictureCallback() {
        public void onPictureTaken(byte[] data, Camera camera) {
            Log.d("onPictureTaken", "onPictureTaken - raw");
        }
    };

    PictureCallback jpegCallback = new PictureCallback() {
        public void onPictureTaken(byte[] data, Camera camera) {
            new SaveImageTask().execute(data);
            resetCam();
            Log.d("onPictureTaken", "onPictureTaken - jpeg");
        }
    };

    /**
     * for rotating the captured image to correct angle
     **/
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.camera_layout);

        activity = this;
        cancelBtn = (ImageView) findViewById(R.id.backbtn);
        gallery = (TextView) findViewById(R.id.galery);
        next = (TextView) findViewById(R.id.next);
        surfaceView = (SurfaceView) findViewById(R.id.surfaceView);
        snapBtn = (ImageView) findViewById(R.id.snap);
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        flashBtn = (ImageView) findViewById(R.id.flashBtn);
        retake = (ImageView) findViewById(R.id.retakeBtn);

        cancelBtn.setVisibility(View.VISIBLE);

        snapBtn.setOnClickListener(this);
        retake.setOnClickListener(this);
        cancelBtn.setOnClickListener(this);
        flashBtn.setOnClickListener(this);
        gallery.setOnClickListener(this);
        next.setOnClickListener(this);

        //title.setText(getString(R.string.snaptheproduct));
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                preview = new Preview(CameraActivity.this, surfaceView);
                preview.setKeepScreenOn(true);

                imagesAdapter = new ImagesAdapter(CameraActivity.this, temp);
                LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
                layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
                recyclerView.setLayoutManager(layoutManager);
                recyclerView.setAdapter(imagesAdapter);
                imagesAdapter.notifyDataSetChanged();

                if (flash) {
                    flashBtn.setSelected(true);
                    flashBtn.setColorFilter(getResources().getColor(R.color.colorPrimary));
                } else {
                    flashBtn.setSelected(false);
                    flashBtn.setColorFilter(null);
                }
            }
        });

        try {
            from = getIntent().getExtras().getString("from");
            if (getIntent().getExtras().get("data") != null) {
                itemMap = (HashMap<String, String>) getIntent().getExtras().get("data");
                JSONArray photos = null;
                try {
                    photos = new JSONArray(itemMap.get(Constants.TAG_PHOTOS));
                    for (int i = 0; i < photos.length(); i++) {
                        JSONObject jph = photos.getJSONObject(i);
                        HashMap<String, Object> map = new HashMap<String, Object>();
                        Log.e(TAG, "onCreate: " + jph);
                        String imageurl = "";
                        String fileName = imageurl.substring(imageurl.lastIndexOf('/') + 1, imageurl.length());
                        String type = "url";

                        if (jph.has(Constants.TAG_ITEM_URL_350)) {
                            imageurl = DefensiveClass.optString(jph, Constants.TAG_ITEM_URL_350);
                        } else if (jph.has(Constants.TAG_TYPE)) {
                            type = DefensiveClass.optString(jph, Constants.TAG_TYPE);
                            if (type.equals(Constants.KEY_URL)) {
                                imageurl = DefensiveClass.optString(jph, Constants.KEY_IMAGE);
                            } else if (type.equals(Constants.TAG_PATH)) {
                                imageurl = DefensiveClass.optString(jph, Constants.KEY_IMAGE);
                            }
                        }
                        map.put("type", type);
                        map.put("image", imageurl);
                        map.put(Constants.TAG_PATH, jph.optString(Constants.TAG_PATH, "" + null));
                        if (!temp.contains(map)) {
                            temp.add(map);
                            selectedImages.add(map);
                        }
                    }

                    if (imagesAdapter != null) {
                        imagesAdapter.notifyDataSetChanged();
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        }

    }


    @Override
    protected void onResume() {
        super.onResume();
        // For Internet checking
        JoysaleApplication.registerReceiver(CameraActivity.this);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                int numCams = Camera.getNumberOfCameras();
                if (numCams > 0) {
                    try {
                        if (ContextCompat.checkSelfPermission(CameraActivity.this, CAMERA) != PackageManager.PERMISSION_GRANTED
                                && ContextCompat.checkSelfPermission(CameraActivity.this, WRITE_EXTERNAL_STORAGE)
                                != PackageManager.PERMISSION_GRANTED) {
                            ActivityCompat.requestPermissions(CameraActivity.this, new String[]{CAMERA, WRITE_EXTERNAL_STORAGE}, 100);
                        } else if (ContextCompat.checkSelfPermission(CameraActivity.this, CAMERA)
                                != PackageManager.PERMISSION_GRANTED) {
                            ActivityCompat.requestPermissions(CameraActivity.this, new String[]{CAMERA}, 101);
                        } else if (ContextCompat.checkSelfPermission(CameraActivity.this, WRITE_EXTERNAL_STORAGE)
                                != PackageManager.PERMISSION_GRANTED) {
                            ActivityCompat.requestPermissions(CameraActivity.this, new String[]{WRITE_EXTERNAL_STORAGE}, 102);
                        } else {
                            if (currentCameraId != 0) {
                                camera = Camera.open(1);
                            } else {
                                camera = Camera.open(0);
                            }
                            camera.setDisplayOrientation(90);
                            try {
                                camera.setPreviewDisplay(preview.getSurfaceHolder());
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            preview.setCamera(camera, flash);
                            camera.startPreview();

                        }
                    } catch (RuntimeException e) {
                        e.printStackTrace();
                    }
                } else {
                    Toast.makeText(getApplicationContext(), getString(R.string.camera_not_found), Toast.LENGTH_LONG).show();
                }
            }
        });

    }

    private void releaseCameraAndPreview() {
        preview.setCamera(null, false);
        if (camera != null) {
            camera.release();
            camera = null;
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.v("requestCode", "requestCode=" + requestCode);
        switch (requestCode) {
            case 100:
                if (grantResults.length > 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    finish();
                } else if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    finish();
                } else if (grantResults.length > 1 && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    finish();
                } else {
                    Toast.makeText(CameraActivity.this, getString(R.string.need_permission_to_access), Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
            case 101:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    finish();
                } else {
                    Toast.makeText(CameraActivity.this, getString(R.string.need_permission_to_access), Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
            case 102:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    finish();
                } else {
                    Toast.makeText(CameraActivity.this, getString(R.string.need_permission_to_access), Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
        }

    }

    @Override
    protected void onStop() {
        super.onStop();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (camera != null) {
                    camera.stopPreview();
                    preview.setCamera(camera, flash);
                    camera.release();
                    camera = null;
                }
            }
        });
    }

    private void resetCam() {
        preview.setCamera(camera, flash);
        camera.startPreview();
    }

    private void refreshGallery(File file) {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        mediaScanIntent.setData(Uri.fromFile(file));
        sendBroadcast(mediaScanIntent);
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

        Log.v("opts.outHeight", "=" + opts.outHeight + "&" + opts.outWidth);
        if (opts.outHeight > REQUIRED_SIZE || opts.outWidth > REQUIRED_SIZE) {
            final int heightRatio = Math.round((float) opts.outHeight
                    / (float) REQUIRED_SIZE);
            final int widthRatio = Math.round((float) opts.outWidth
                    / (float) REQUIRED_SIZE);
            scale = heightRatio < widthRatio ? heightRatio : widthRatio;//
            Log.v("In", "In=" + scale);
        }
        opts.inJustDecodeBounds = false;
        opts.inSampleSize = scale;
        Bitmap bm = BitmapFactory.decodeFile(fPath, opts).copy(
                Bitmap.Config.RGB_565, false);
        return bm;
    }

    public Bitmap rotate(Bitmap src, float degree) {
        // create new matrix object
        Matrix matrix = new Matrix();
        // setup rotation degree
        matrix.postRotate(degree);

        // return new bitmap rotated using matrix
        return Bitmap.createBitmap(src, 0, 0, src.getWidth(), src.getHeight(), matrix, true);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode == RESULT_OK) {
            Log.v("RESULT_OK", "");
            if (requestCode == REQUEST_CODE) {
                /*if (requestCode == 2) {*/
                try {
                    Log.v("gallery code opened", "");

                    if (data.getStringExtra("fromGallery") != null) {
                        temp.addAll(ImageSelectActivity.selectedImgLists);
                        selectedImages.addAll(ImageSelectActivity.selectedImgLists);
                    }
                    //temp.addAll(GalleryActivity.images);
                    else {
                        temp.addAll(null);
                        selectedImages.addAll(temp);
                    }
                    imagesAdapter.notifyDataSetChanged();

                    //	new ImageUploadTask().execute(picturePath);
                } catch (NullPointerException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                } catch (OutOfMemoryError ome) {
                    ome.printStackTrace();
                }

            }
        }
    }

    private int totalImageCount() {
        return selectedImages.size();
    }

    @Override
    protected void onPause() {
        // For Internet checking disconnect
        JoysaleApplication.unregisterReceiver(CameraActivity.this);
        super.onPause();
        releaseCameraAndPreview();
    }

    @Override
    public void onBackPressed() {
        if (fromedit) {
            fromedit = false;
        }

        if (from.equals("add") || from.equals("edit")) {
            setResult(Activity.RESULT_CANCELED);
            finish();
        } else {
            finish();
        }
        super.onBackPressed();
    }


    private void releaseCamera() {
        if (camera != null) {
            camera.stopPreview();
            preview.setCamera(null, flash);
            camera.release();
            camera = null;
        }
    }

    /**
     * Save the captured image to gallery
     **/
    private class SaveImageTask extends AsyncTask<byte[], Void, Void> {

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

                //     boolean bo = realImage.compress(Bitmap.CompressFormat.JPEG, 100, outStream);
                outStream.write(data[0]);
                outStream.flush();
                outStream.close();

                Bitmap realImage = decodeFile(outFile.getAbsolutePath());

                int angleToRotate = getRoatationAngle(CameraActivity.this, 0);

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

                previewWidth = bitmapImage.getWidth();
                previewHeight = bitmapImage.getHeight() * 75 / 100;
                Log.v("previewWidth&Height", "=" + previewWidth + "&" + previewHeight);
                bitmapImage = Bitmap.createBitmap(bitmapImage, 0, 0, previewWidth, previewHeight);

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

                HashMap<String, Object> map = new HashMap<String, Object>();
                map.put("type", "path");
                map.put("image", file.getAbsolutePath());
                //    map.put("path", bitmapImage);
                temp.add(map);
                selectedImages.add(map);
                //		Log.v("thumbnail",""+thumbnail);

                Log.v("imagesAry", "" + temp);
                Log.d("onPictureTaken", "onPictureTaken - wrote bytes: " + data.length + " to " + outFile.getAbsolutePath());
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

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            snapBtn.setOnClickListener(CameraActivity.this);
            imagesAdapter.notifyDataSetChanged();
        }

    }

    // for adding muliple images //
    public class ImagesAdapter extends RecyclerView.Adapter<ImagesAdapter.ViewHolder> {
        ArrayList<HashMap<String, Object>> imgAry;
        private Context mContext;

        public ImagesAdapter(Context ctx, ArrayList<HashMap<String, Object>> data) {
            mContext = ctx;
            imgAry = data;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.singleimage, parent, false);
            return new ViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            holder.singleImage.setVisibility(View.VISIBLE);

            final HashMap<String, Object> tempMap = imgAry.get(position);
//            Log.e(TAG, "onBindViewHolder: " + tempMap);
//                Log.e(TAG, "getView: " + tempMap);
            if (selectedImages.contains(tempMap)) {
                holder.gradient.setVisibility(View.VISIBLE);
                holder.tick.setVisibility(View.VISIBLE);
            } else {
                holder.gradient.setVisibility(View.GONE);
                holder.tick.setVisibility(View.GONE);
            }

            holder.singleImage.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    final HashMap<String, Object> tempMap = imgAry.get(position);
                    if (!selectedImages.contains(tempMap))
                        selectedImages.add(tempMap);
                    else
                        selectedImages.remove(tempMap);
                    notifyItemChanged(position);
                }
            });
            if (tempMap.containsKey(Constants.TAG_TYPE) && tempMap.get(Constants.TAG_TYPE).equals(Constants.TAG_PATH)) {
                Picasso.with(mContext).load("file://" + tempMap.get(Constants.KEY_IMAGE).toString()).into(holder.singleImage);
            } else if (tempMap.containsKey(Constants.TAG_ITEM_URL_350)) {
                Picasso.with(mContext).load(tempMap.get(Constants.TAG_ITEM_URL_350).toString()).into(holder.singleImage);
            } else if (tempMap.containsKey(Constants.KEY_IMAGE)) {
                Picasso.with(mContext).load(tempMap.get(Constants.KEY_IMAGE).toString()).into(holder.singleImage);
            }
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public int getItemCount() {
            return imgAry.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            ImageView singleImage, gradient, tick;

            public ViewHolder(View itemView) {
                super(itemView);
                singleImage = (ImageView) itemView.findViewById(R.id.imageView);
                gradient = (ImageView) itemView.findViewById(R.id.imageView2);
                tick = (ImageView) itemView.findViewById(R.id.tick);

            }
        }

    }


    @SuppressLint("StringFormatInvalid")
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.snap:
                snapBtn.setOnClickListener(null);
                try {
                    camera.takePicture(shutterCallback, rawCallback, jpegCallback);
                } catch (NullPointerException e) {
                    e.printStackTrace();
                } catch (RuntimeException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case R.id.next:
                int totalCount = totalImageCount();
                Log.v("CameraActivity", "addproductvalue=" + from + " ");
                if (temp.size() == 0 || totalCount < 0) {
                    Toast.makeText(CameraActivity.this, getString(R.string.please_add_image), Toast.LENGTH_SHORT).show();
                } else if (totalCount > Constants.IMAGE_COUNT) {
                    Toast.makeText(CameraActivity.this, getString(R.string.error_msg_to_imgcount, Constants.IMAGE_COUNT), Toast.LENGTH_SHORT).show();
                } else if (selectedImages.size() == 0 && totalCount == 0) {
                    Toast.makeText(CameraActivity.this, getString(R.string.please_select_images), Toast.LENGTH_SHORT).show();
                } else {
                    fromedit = false;
                    Intent i = new Intent(CameraActivity.this, AddProductDetail.class);
                    i.putExtra("from", from);
                    switch (from) {
                        case "edit":
//                            Log.e("onClick: ", "" + selectedImages.size());
                            i.putExtra("data", "" + new JSONArray(selectedImages));
                            i.putExtra("from", from);
                            setResult(Activity.RESULT_OK, i);
                            finish();
                            break;
                        case "home":
//                            Log.e(TAG, "onClick: " + selectedImages.size());
                            i.putExtra("data", "" + new JSONArray(selectedImages));
                            i.putExtra("from", from);
                            startActivity(i);
                            finish();
                            break;
                        case "add":
//                            Log.e(TAG, "onClick: " + selectedImages.size());
                            i.putExtra("data", "" + new JSONArray(selectedImages));
                            i.putExtra("from", from);
//                            startActivity(i);
                            setResult(Activity.RESULT_OK, i);
                            finish();
                            break;
                        default:
                            Log.v("CameraActivity", "called2");
                            startActivity(i);
                            break;
                    }

                }
                break;

            case R.id.backbtn:
                onBackPressed();
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
                        if (currentCameraId == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                            flash = false;
                        } else {
                            flash = true;
                        }
                    }

                    resetCam();

                } else {
                    Toast.makeText(getApplicationContext(), getString(R.string.your_device_doesnt_flash), Toast.LENGTH_SHORT).show();
                }
                break;

            case R.id.galery:
/*                Intent in = new Intent(
                        Intent.ACTION_PICK,
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(in, 2);*/
                Intent in = new Intent(this, AlbumSelectActivity.class);
                startActivityForResult(in, REQUEST_CODE);
                break;
            case R.id.retakeBtn:
                //NB: if you don't release the current camera before switching, your app will crash
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
                setCameraDisplayOrientation(CameraActivity.this, currentCameraId, camera);
                try {
                    camera.setPreviewDisplay(preview.getSurfaceHolder());
                } catch (IOException e) {
                    e.printStackTrace();
                }

                if (currentCameraId == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                    flash = false;
                } else if (currentCameraId == Camera.CameraInfo.CAMERA_FACING_BACK && flashBtn.isSelected()) {
                    flash = true;
                }
                preview.setCamera(camera, flash);
                camera.startPreview();

                break;
        }

    }

}
