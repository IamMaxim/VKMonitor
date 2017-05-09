package ru.iammaxim.vkmonitor.Activities;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Messenger;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import ru.iammaxim.vkmonitor.AccessTokenManager;
import ru.iammaxim.vkmonitor.App;
import ru.iammaxim.vkmonitor.LongPollService;
import ru.iammaxim.vkmonitor.R;
import ru.iammaxim.vkmonitor.UserDB;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static boolean started = false;
    private static final int PERMISSION_WRITE_EXTERNAL_STORAGE = 2871;

    private View start, stop;
    private TextView state;

    @Override
    protected void onResume() {
        super.onResume();
        setupState();
    }

    private void setupState() {
        AccessTokenManager.Token currentToken = AccessTokenManager.getActiveToken();
        if (currentToken != null) {
            state.setText(getString(R.string.current_token, currentToken.name));
            state.setBackgroundColor(getResources().getColor(R.color.colorStateBgOK));
        } else {
            state.setText(R.string.current_token_not_set);
            state.setBackgroundColor(getResources().getColor(R.color.colorStateBgError));
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AccessTokenManager.load();
        if (!UserDB.isLoaded())
            UserDB.load();
        setContentView(R.layout.activity_main);
        state = (TextView) findViewById(R.id.state);
        start = findViewById(R.id.start);
        stop = findViewById(R.id.stop);

        setupState();

        if (!started) {
            stop.setEnabled(false);
            stop.setAlpha(0.5f);
        } else {
            start.setEnabled(false);
            start.setAlpha(0.5f);
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_WRITE_EXTERNAL_STORAGE);
        }
    }

    private void onClickStart() {
        startService(new Intent(this, LongPollService.class).putExtra("MESSENGER", new Messenger(App.updateMessageHandler)));
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.start:
                onClickStart();
                v.setEnabled(false);
                v.setAlpha(0.5f);
                stop.setEnabled(true);
                stop.setAlpha(1);
                started = true;
                break;
            case R.id.open_log:
                startActivity(new Intent(this, LogActivity.class));
                break;
            case R.id.change_filter:
                startActivity(new Intent(this, ChangeFilterActivity.class));
                break;
            case R.id.stop:
                stopService(new Intent(this, LongPollService.class));
                stop.setEnabled(false);
                stop.setAlpha(0.5f);
                start.setEnabled(true);
                start.setAlpha(1);
                started = false;
                break;
            case R.id.manage_tokens:
                startActivity(new Intent(this, AccessTokenManagerActivity.class));
                break;
            case R.id.clear_filter:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Clear filter");
                builder.setMessage("Are you sure you want to clear filter?");
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        App.clearFilter();
                        Toast.makeText(MainActivity.this, "Filter cleared", Toast.LENGTH_SHORT).show();
                    }
                });
                builder.setNegativeButton("Cancel", null);
                builder.show();
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_WRITE_EXTERNAL_STORAGE: {
                if (!(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    App.showNotification(getApplicationContext(), "Couldn't get write permission");
                    finish();
                }
            }
        }
    }
}
