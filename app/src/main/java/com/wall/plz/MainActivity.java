package com.wall.plz;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.WallpaperManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.PermissionChecker;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.ByteArrayOutputStream;

public class MainActivity extends AppCompatActivity {

    private final static int REQUEST_WRITE_STORAGE_PERMISSION = 1;
    private boolean wallSwitch = false;

    private ImageView defaultWallpaper;
    private RelativeLayout homeLockPopup;
    private ParcelFileDescriptor homescreen;
    private ParcelFileDescriptor lockscreen;

    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initPermission();

        defaultWallpaper = findViewById(R.id.imageView);
        homeLockPopup = findViewById(R.id.popup);

        WallpaperManager wallpaperManager = WallpaperManager.getInstance(getApplicationContext());

        homescreen = wallpaperManager.getWallpaperFile(WallpaperManager.FLAG_SYSTEM);
        lockscreen = wallpaperManager.getWallpaperFile(WallpaperManager.FLAG_LOCK);

        Drawable wallpaperDrawable = wallpaperManager.getDrawable();

        defaultWallpaper = findViewById(R.id.imageView);
        defaultWallpaper.setImageDrawable(wallpaperDrawable);

        defaultWallpaper.setOnLongClickListener(v -> {
            if (wallSwitch) {
                if (homescreen != null) {
                    Bitmap bitmap = BitmapFactory.decodeFileDescriptor(homescreen.getFileDescriptor());
                    ((ImageView) findViewById(R.id.imageView)).setImageBitmap(bitmap);
                }
                wallSwitch = false;
            } else {
                if (lockscreen != null) {
                    Bitmap bitmap = BitmapFactory.decodeFileDescriptor(lockscreen.getFileDescriptor());
                    ((ImageView) findViewById(R.id.imageView)).setImageBitmap(bitmap);
                }
                wallSwitch = true;
            }
            SharedPreferences prefs = getSharedPreferences(
                    "sharedPreferences", Context.MODE_PRIVATE);
            prefs.edit().putBoolean("FirstBoot", false).apply();
            homeLockPopup.setVisibility(View.GONE);
            return true;
        });

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(this::customSnackbar);

        SharedPreferences prefs = getSharedPreferences("sharedPreferences", Context.MODE_PRIVATE);

        homeLockPopup.setVisibility(prefs.getBoolean("FirstBoot",
                true) ? View.VISIBLE : View.INVISIBLE);
        homeLockPopup.setOnClickListener(v -> {
            prefs.edit().putBoolean("FirstBoot", false).apply();
            homeLockPopup.setVisibility(View.GONE);
        });
    }

    private void wallpaper(Context context, Bitmap image, Boolean save) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        if (save) image.compress(Bitmap.CompressFormat.JPEG, 100, bytes);

        String path = MediaStore.Images.Media.insertImage(context.getContentResolver(),
                image, "-", null);
        Uri.parse(path);

        Intent intent = new Intent(android.content.Intent.ACTION_SEND);
        intent.putExtra(Intent.EXTRA_STREAM, Uri.parse(path));
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.setType("image/png");
        if (save) startActivity(intent);
    }

    private void customSnackbar(final View view) {
        LinearLayout.LayoutParams objLayoutParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        final Snackbar snackbar = Snackbar.make(view, "", Snackbar.LENGTH_LONG);
        Snackbar.SnackbarLayout layout = (Snackbar.SnackbarLayout) snackbar.getView();

        getSystemService(LAYOUT_INFLATER_SERVICE);
        @SuppressLint("InflateParams") View snackView = getLayoutInflater().inflate(
                R.layout.custom_snackbar, null);

        TextView textViewOne = snackView.findViewById(R.id.txtOne);
        textViewOne.setOnClickListener(v -> {
            Bitmap bitmap = ((BitmapDrawable) defaultWallpaper.getDrawable()).getBitmap();
            wallpaper(getApplicationContext(), bitmap, true);
            snackbar.dismiss();
        });

        TextView textViewTwo = snackView.findViewById(R.id.txtTwo);
        textViewTwo.setOnClickListener(v -> {
            Bitmap bitmap = ((BitmapDrawable) defaultWallpaper.getDrawable()).getBitmap();
            wallpaper(getApplicationContext(), bitmap, false);
            snackbar.dismiss();
            Snackbar.make(view, wallSwitch ? R.string.snackbar_hs_saved_wallpaper
                    : R.string.snackbar_ls_saved_wallpaper, Snackbar.LENGTH_SHORT).show();
        });

        layout.addView(snackView, objLayoutParams);
        snackbar.show();
    }

    private void initPermission() {
        if (PermissionChecker
                .checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PermissionChecker.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_WRITE_STORAGE_PERMISSION);
            ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.READ_EXTERNAL_STORAGE);
        }
    }

    @Override
    public void onRestart(){
        super.onRestart();
        Intent intent=new Intent(MainActivity.this,MainActivity.class);
        startActivity(intent);
        finish();
    }
}