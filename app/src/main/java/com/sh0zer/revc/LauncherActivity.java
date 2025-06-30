package com.sh0zer.revc;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.os.Build;
import android.view.MenuItem;
import android.view.Window;
import android.view.View;
import android.Manifest;
import android.content.pm.PackageManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Toast;
import android.provider.Settings;
import android.net.Uri;

import org.libsdl.app.SDLActivity;

import java.io.File;

public class LauncherActivity extends Activity {
    public static native void setenv(String value);

    static public EditText editText;
    private static final int REQUEST_PERMISSION = 1001;
    private static final int REQUEST_MANAGE_STORAGE = 1002;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);

        editText = findViewById(R.id.editText);
        String savedPath = prefs.getString("game_path", "");
        if(savedPath == "")
            savedPath = "/storage/emulated/0/revc";
        editText.setText(savedPath);

        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                String newText = s.toString();
                getSharedPreferences("app_prefs", MODE_PRIVATE)
                        .edit()
                        .putString("game_path", newText)
                        .apply();
            }
        });

        ImageView menuButton = findViewById(R.id.menuIcon);
        menuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMenu();
            }
        });

        Button browseButton = findViewById(R.id.browseButton);
        browseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LauncherActivity.this, FilepickerActivity.class);
                startActivityForResult(intent, 123);
            }
        });

        Button launchButton = findViewById(R.id.launchButton);
        launchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startGta();
            }
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                requestAllFilesAccess();
            }
        } else {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_PERMISSION);
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            Window window = getWindow();
            window.getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            );
        }
    }

    private void requestAllFilesAccess() {
        Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
        intent.setData(Uri.parse("package:" + getPackageName()));
        startActivityForResult(intent, REQUEST_MANAGE_STORAGE);
        Toast.makeText(this, "Please grant 'All Files Access'", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "No permissions", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode)
        {
            case REQUEST_MANAGE_STORAGE: {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && Environment.isExternalStorageManager()) {
                    Toast.makeText(this, "All Files Access granted", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "All Files Access denied", Toast.LENGTH_SHORT).show();
                }
            }
            case 123:
                if (resultCode == RESULT_OK) {

                    editText.setText(data.getStringExtra("path"));
                }

        }
    }

    public void startGta() {
        String gamepath = editText.getText().toString();
        File file = new File(gamepath + "/models/gta3.img");
        if(!file.exists())
        {
            AlertDialog.Builder dlgAlert  = new AlertDialog.Builder(this);
            dlgAlert.setMessage("An error occurred while trying to start the application."
                + System.getProperty("line.separator")
                + System.getProperty("line.separator")
                + "Error: " + "gta3.img not found. Check your file path");
                dlgAlert.setTitle("Game files not found");
                dlgAlert.setPositiveButton("Exit",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog,int id) {
                    }
                });
        dlgAlert.setCancelable(false);
        dlgAlert.create().show();
        return;
        }
        Intent intent = new Intent(LauncherActivity.this, SDLActivity.class);
        startActivity(intent);
    }

    static public void initEnv() {
        String gamepath = editText.getText().toString();
        setenv(gamepath);
        File file = new File(gamepath);
        Log.d("REVC", "Game directory: " + file.exists());
    }

    void showMenu() {
        PopupMenu popupMenu = new PopupMenu(LauncherActivity.this, findViewById(R.id.menuIcon));
        popupMenu.getMenuInflater().inflate(R.menu.menu_main, popupMenu.getMenu());
        popupMenu.show();


        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (item.getItemId() == R.id.action_menu) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(LauncherActivity.this);
                    builder.setMessage(R.string.about_text)
                            .setTitle(R.string.action_about);
                    AlertDialog dialog = builder.create();
                    dialog.show();
                    return true;
                } else
                    return false;
            }
        });
    }
}

