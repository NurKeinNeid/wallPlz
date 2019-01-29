package com.wall.plz;

import android.annotation.SuppressLint;
import android.app.WallpaperManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private boolean wallSwitch = false;

    private ImageView defaultWallpaper;
    private ParcelFileDescriptor homescreen;
    private ParcelFileDescriptor lockscreen;
    private RelativeLayout homeLockPopup;
    private SharedPreferences prefs;
    private Snackbar snackbar;

    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Utils.hideStatusbar(MainActivity.this);
        Utils.initPermission(MainActivity.this);

        defaultWallpaper = findViewById(R.id.imageView);
        homeLockPopup = findViewById(R.id.popup);

        WallpaperManager wallpaperManager = WallpaperManager.getInstance(getApplicationContext());
        Drawable wallpaperDrawable = wallpaperManager.getDrawable();
        prefs = getSharedPreferences("sharedPreferences", Context.MODE_PRIVATE);

        homescreen = wallpaperManager.getWallpaperFile(WallpaperManager.FLAG_SYSTEM);
        lockscreen = wallpaperManager.getWallpaperFile(WallpaperManager.FLAG_LOCK);

        defaultWallpaper.setImageDrawable(wallpaperDrawable);

        defaultWallpaper.setOnClickListener(v -> {
            if (snackbar != null) {
                snackbar.dismiss();
            }
        });

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
            prefs.edit().putBoolean("FirstBoot", false).apply();
            homeLockPopup.setVisibility(View.GONE);
            return true;
        });

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(this::customSnackbar);

        homeLockPopup.setVisibility(prefs.getBoolean("FirstBoot",
                true) ? View.VISIBLE : View.INVISIBLE);
        homeLockPopup.setOnClickListener(v -> {
            prefs.edit().putBoolean("FirstBoot", false).apply();
            homeLockPopup.setVisibility(View.GONE);
        });
    }

    private void customSnackbar(View view) {
        LinearLayout.LayoutParams objLayoutParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        snackbar = Snackbar.make(view, "", Snackbar.LENGTH_LONG);
        Snackbar.SnackbarLayout layout = (Snackbar.SnackbarLayout) snackbar.getView();

        getSystemService(LAYOUT_INFLATER_SERVICE);
        @SuppressLint("InflateParams") View snackView = getLayoutInflater().inflate(
                R.layout.custom_snackbar, null);

        TextView textViewOne = snackView.findViewById(R.id.txtOne);
        textViewOne.setOnClickListener(v -> {
            Bitmap bitmap = ((BitmapDrawable) defaultWallpaper.getDrawable()).getBitmap();
            Utils.wallpaper(getApplicationContext(), bitmap, true, MainActivity.this);
            snackbar.dismiss();
        });

        TextView textViewTwo = snackView.findViewById(R.id.txtTwo);
        textViewTwo.setOnClickListener(v -> {
            Bitmap bitmap = ((BitmapDrawable) defaultWallpaper.getDrawable()).getBitmap();
            Utils.wallpaper(getApplicationContext(), bitmap, false, MainActivity.this);
            snackbar.dismiss();
            Snackbar.make(view, wallSwitch ? R.string.snackbar_hs_saved_wallpaper
                    : R.string.snackbar_ls_saved_wallpaper, Snackbar.LENGTH_SHORT).show();
        });

        layout.addView(snackView, objLayoutParams);
        snackbar.show();
    }

    @Override
    public void onRestart(){
        super.onRestart();
        Intent intent=new Intent(MainActivity.this,MainActivity.class);
        startActivity(intent);
        finish();
    }
}