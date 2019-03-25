package com.app.helper;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import com.app.lystiq.R;
import com.app.utils.AppUtils;

import java.io.File;
import java.io.FileOutputStream;

import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

/**
 * Created by hitasoft on 7/3/17.
 */

public class ImageStorage {

    private static Context context;
    private static Activity activity;
    public ImageStorage(Activity activity,Context context) {
        this.context = context;
        this.activity=activity;
    }

    public String saveToSdCard(Bitmap bitmap, String from, String filename, String timeStamp) {

        String stored = "";

        File sdcard = Environment.getExternalStorageDirectory();

        String path = "";

        if (from.equals("sent")) {
            path = "/" + context.getString(R.string.app_name) + AppUtils.IMG_SENT_PATH;
        } else if (from.equals("profile")) {
            path = "/" + context.getString(R.string.app_name) + AppUtils.IMG_PROFILE_PATH;
        } else {
            path = "/" + context.getString(R.string.app_name) + AppUtils.IMG_HOME_PATH;
        }

        File folder = new File(sdcard.getAbsoluteFile(), path);
        if (from.equals("sent")) {
            folder.mkdirs();
        } else if (from.equals("profile")) {
            folder.mkdirs();
        } else {
            folder.mkdir();
        }

        File file = new File(folder.getAbsoluteFile(), filename);
        if (file.exists())
            return "success";

        try {
            FileOutputStream out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
            out.flush();
            out.close();
            stored = "success";
        } catch (Exception e) {
            e.printStackTrace();
        }

        /*Show Images in Gallery*/
        if (ContextCompat.checkSelfPermission(context, WRITE_EXTERNAL_STORAGE)== PackageManager.PERMISSION_GRANTED) {
            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.Media.TITLE, filename);
            values.put(MediaStore.Images.Media.DESCRIPTION, "joysaleimage-" + filename);
            values.put(MediaStore.Images.Media.DATE_TAKEN, timeStamp);
            values.put(MediaStore.Images.ImageColumns.BUCKET_ID, file.toString().toLowerCase(
                    context.getResources().getConfiguration().locale).hashCode());
            values.put(MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME, file.getName().toLowerCase(
                    context.getResources().getConfiguration().locale));
            values.put("_data", file.getAbsolutePath());
            ContentResolver cr = context.getContentResolver();
            cr.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        }
        else{
            ActivityCompat.requestPermissions(activity, new String[]{WRITE_EXTERNAL_STORAGE}, 102);
        }
        return stored;
    }

    public File getImage(String from, String imagename) {

        File mediaImage = null;
        try {
            String root = Environment.getExternalStorageDirectory().toString();
            File myDir = new File(root);
            if (!myDir.exists())
                return null;

            String path = "";
            if (from.equals("sent")) {
                path = "/" + context.getString(R.string.app_name) + AppUtils.IMG_SENT_PATH;
            } else if (from.equals("profile")) {
                path = "/" + context.getString(R.string.app_name) + AppUtils.IMG_PROFILE_PATH;
            } else if (from.equals("thumb")) {
                path = "/" + context.getString(R.string.app_name) + AppUtils.IMG_THUMBNAIL_PATH;
            } else {
                path = "/" + context.getString(R.string.app_name) + AppUtils.IMG_HOME_PATH;
            }
            mediaImage = new File(myDir.getPath() + path + imagename);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return mediaImage;
    }

    public boolean checkifImageExists(String from, String imagename) {
        Bitmap b = null;
        File file = getImage(from, imagename);
        String path = file.getAbsolutePath();

        if (path != null)
            b = BitmapFactory.decodeFile(path);

        if (b == null || b.equals("")) {
            return false;
        }
        return true;
    }
}