package ru.iammaxim.vkmonitor;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.start).setOnClickListener(this);
        findViewById(R.id.save_user_db).setOnClickListener(this);
        findViewById(R.id.open_log).setOnClickListener(this);
        findViewById(R.id.change_filter).setOnClickListener(this);
    }

    private void onClickStart() {
        App.setAccessToken(((EditText)findViewById(R.id.at)).getEditableText().toString());
        startService(new Intent(this, LongPollService.class));
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.start:
                onClickStart();
                break;
            case R.id.save_user_db:
                UserDB.save();
                break;
            case R.id.open_log:
                startActivity(new Intent(this, LogActivity.class));
                break;
            case R.id.change_filter:
                startActivity(new Intent(this, ChangeFilterActivity.class));
        }
    }
}
