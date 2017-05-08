package ru.iammaxim.vkmonitor.Activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Messenger;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import ru.iammaxim.vkmonitor.AccessTokenManager;
import ru.iammaxim.vkmonitor.App;
import ru.iammaxim.vkmonitor.LongPollService;
import ru.iammaxim.vkmonitor.R;
import ru.iammaxim.vkmonitor.UserDB;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static boolean started = false;
    private static final int PERMISSION_WRITE_EXTERNAL_STORAGE = 2871;

    private Button start, save_user_db, open_log, change_filter, stop, manage_tokens;
    private TextView state;

    @Override
    protected void onResume() {
        super.onResume();
        System.out.println("MainActivity onResume()");
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
        setContentView(R.layout.activity_main);
        new Thread(new Runnable() {
            @Override
            public void run() {
                AccessTokenManager.load();
            }
        }).start();
        state = (TextView) findViewById(R.id.state);
        start = (Button) findViewById(R.id.start);
        start.setOnClickListener(this);
        save_user_db = (Button) findViewById(R.id.save_user_db);
        save_user_db.setOnClickListener(this);
        open_log = (Button) findViewById(R.id.open_log);
        open_log.setOnClickListener(this);
        change_filter = (Button) findViewById(R.id.change_filter);
        change_filter.setOnClickListener(this);
        stop = (Button) findViewById(R.id.stop);
        stop.setOnClickListener(this);
        manage_tokens = (Button) findViewById(R.id.manage_tokens);
        manage_tokens.setOnClickListener(this);

        setupState();

        if (!started) {
            save_user_db.setEnabled(false);
//            open_log.setEnabled(false);
//            change_filter.setEnabled(false);
            stop.setEnabled(false);
        } else start.setEnabled(false);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_WRITE_EXTERNAL_STORAGE);
        }
    }

    private void onClickStart() {
        //App.setAccessToken(((EditText) findViewById(R.id.at)).getEditableText().toString());
        startService(new Intent(this, LongPollService.class).putExtra("MESSENGER", new Messenger(App.updateMessageHandler)));
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.start:
                onClickStart();
                v.setEnabled(false);
                save_user_db.setEnabled(true);
                open_log.setEnabled(true);
                change_filter.setEnabled(true);
                stop.setEnabled(true);
                started = true;
                break;
            case R.id.save_user_db:
                UserDB.save();
                break;
            case R.id.open_log:
                startActivity(new Intent(this, LogActivity.class));
                break;
            case R.id.change_filter:
                startActivity(new Intent(this, ChangeFilterActivity.class));
                break;
            case R.id.stop:
                stopService(new Intent(this, LongPollService.class));
                save_user_db.setEnabled(false);
                open_log.setEnabled(false);
                change_filter.setEnabled(false);
                stop.setEnabled(false);
                start.setEnabled(true);
                started = false;
                break;
            case R.id.manage_tokens:
                startActivity(new Intent(this, AccessTokenManagerActivity.class));
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
