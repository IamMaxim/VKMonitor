package ru.iammaxim.vkmonitor.Activities;

import android.Manifest;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import ru.iammaxim.vkmonitor.AccessTokenManager;
import ru.iammaxim.vkmonitor.App;
import ru.iammaxim.vkmonitor.Fragments.MainFragment;
import ru.iammaxim.vkmonitor.R;
import ru.iammaxim.vkmonitor.API.Users.UserDB;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static final int PERMISSION_WRITE_EXTERNAL_STORAGE = 2871;

    private void loadFiles() {
        AccessTokenManager.load();
        if (!UserDB.isLoaded())
            UserDB.load();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        App.init();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_WRITE_EXTERNAL_STORAGE);
        } else loadFiles();
        App.updateShortcuts(this);
        setContentView(R.layout.activity_main);
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();
        transaction.add(R.id.container, new MainFragment());
        transaction.commit();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_WRITE_EXTERNAL_STORAGE: {
                if (!(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    App.showNotification(getApplicationContext(), "Couldn't get write permission");
                    finish();
                } else loadFiles();
            }
        }
    }

    @Override
    public void onClick(View v) {
        Fragment f = getSupportFragmentManager().findFragmentById(R.id.container);
        if (f instanceof View.OnClickListener)
            ((View.OnClickListener) f).onClick(v);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        Fragment f = getSupportFragmentManager().findFragmentById(R.id.container);
        return f.onOptionsItemSelected(item);
    }
}
