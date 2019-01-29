package com.wall.plz;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.PermissionChecker;
import android.view.View;
import android.view.WindowManager;

import java.io.ByteArrayOutputStream;

class Utils {

    static void hideStatusbar(Activity callingActivity) {
        callingActivity.getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            WindowManager.LayoutParams lp = callingActivity.getWindow().getAttributes();
            lp.layoutInDisplayCutoutMode =
                    WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
            callingActivity.getWindow().setAttributes(lp);
        }
        callingActivity.getWindow().addFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    static void initPermission(Activity callingActivity) {
        if (PermissionChecker
                .checkSelfPermission(callingActivity, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PermissionChecker.PERMISSION_GRANTED) {
            int REQUEST_WRITE_STORAGE_PERMISSION = 1;
            callingActivity.requestPermissions(new String[]{
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_WRITE_STORAGE_PERMISSION);
            ActivityCompat.checkSelfPermission(callingActivity,
                    Manifest.permission.READ_EXTERNAL_STORAGE);
        }
    }

    static void wallpaper(Context context, Bitmap image, Boolean save, Activity callingActivity) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        if (save) image.compress(Bitmap.CompressFormat.JPEG, 100, bytes);

        String path = MediaStore.Images.Media.insertImage(context.getContentResolver(),
                image, "-", null);
        Uri.parse(path);

        Intent intent = new Intent(android.content.Intent.ACTION_SEND);
        intent.putExtra(Intent.EXTRA_STREAM, Uri.parse(path));
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.setType("image/png");
        if (save) callingActivity.startActivity(intent);
    }
}
