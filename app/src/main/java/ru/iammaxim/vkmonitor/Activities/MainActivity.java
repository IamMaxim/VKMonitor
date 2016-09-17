package ru.iammaxim.vkmonitor.Activities;

import android.content.Intent;
import android.os.Messenger;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import ru.iammaxim.vkmonitor.App;
import ru.iammaxim.vkmonitor.LongPollService;
import ru.iammaxim.vkmonitor.R;
import ru.iammaxim.vkmonitor.UserDB;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static boolean started = false;

    private Button start, save_user_db, open_log, change_filter, stop;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
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

        if (!started) {
            save_user_db.setEnabled(false);
            open_log.setEnabled(false);
            change_filter.setEnabled(false);
            stop.setEnabled(false);
        }
        else start.setEnabled(false);
    }

    private void onClickStart() {
        App.setAccessToken(((EditText)findViewById(R.id.at)).getEditableText().toString());
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
        }
    }
}
