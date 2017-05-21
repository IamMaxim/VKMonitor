package ru.iammaxim.vkmonitor.RequestGenerator;

import android.app.AlertDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import ru.iammaxim.vkmonitor.Net;
import ru.iammaxim.vkmonitor.API.Objects.ObjectMessage;
import ru.iammaxim.vkmonitor.R;
import ru.iammaxim.vkmonitor.RequestGenerator.Views.ViewObject;


public class RequestGeneratorMain extends AppCompatActivity {
    private EditText et1, et2, et4, et5, et6, et7, et8, et9;
    private Button b1, b2;
    private TextView tv1;
    private LinearLayout objs_container;
    private boolean arg1 = false, arg2 = false, arg3 = false;
    public static Context context;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request_generator);
        context = this;

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        et1 = (EditText) findViewById(R.id.et1);
        et2 = (EditText) findViewById(R.id.et2);
        et4 = (EditText) findViewById(R.id.et4);
        et5 = (EditText) findViewById(R.id.et5);
        et6 = (EditText) findViewById(R.id.et6);
        et7 = (EditText) findViewById(R.id.et7);
        et8 = (EditText) findViewById(R.id.et8);
        et9 = (EditText) findViewById(R.id.et9);
        b1 = (Button) findViewById(R.id.b1);
        tv1 = (TextView) findViewById(R.id.tv1);
        b2 = (Button) findViewById(R.id.b2);
        tv1.setOnClickListener(v -> {
            AlertDialog dialog = new AlertDialog.Builder(RequestGeneratorMain.context).create();
            dialog.setMessage(tv1.getText());
            dialog.show();
        });
        objs_container = (LinearLayout) findViewById(R.id.objs_container);
        b1.setOnClickListener(v -> new RunMethod().execute());
        b2.setOnClickListener(v -> objs_container.removeAllViews());
    }

    String getString(final EditText et) {
        String returnText = et.getText().toString();
        if (returnText.equals("")) {
            runOnUiThread(() -> tv1.setText("One of specified parameters are invalid! Request failed! " + et.toString()));
            return null;
        }
        return returnText;
    }

    void sendLogMessage(final String str) {
        if (str != null)
            if (!str.equals(""))
                runOnUiThread(() -> tv1.setText(str));
    }

    private class RunMethod extends AsyncTask <Void, Void, Void> {
        private String response;

        //noinspection ResourceType
        @Override
        protected Void doInBackground(Void[] params) {
            arg1 = false;
            arg2 = false;
            arg3 = false;

            //noinspection ResourceType
            if (!et6.getEditableText().toString().equals("")) arg1 = true;
            //noinspection ResourceType
            if (!et8.getEditableText().toString().equals("")) arg2 = true;
            //noinspection ResourceType
            if (!et4.getEditableText().toString().equals("")) arg3 = true;

            ArrayList<String> args = new ArrayList<>();

            if (arg1)
                args.add(getString(et6) + "=" + getString(et7));
            if (arg2)
                args.add(getString(et8) + "=" + getString(et9));
            if (arg3)
                args.add(getString(et4) + "=" + getString(et5));

            try {
                //noinspection ResourceType
                JSONObject json_obj = new JSONObject(response = Net.processRequest(et1.getText().toString() + "." + et2.getText().toString(), true, args.toArray(new String[args.size()])));

                //noinspection ResourceType
                switch (et1.getText().toString() + "." + et2.getText().toString()) {
                    case "messages.getHistory":
                    case "messages.get":
                    case "messages.getDialogs":
                        addMessages(json_obj);
                        break;
                    default:
                        break;
                }
            } catch (IOException | JSONException e) {
                e.printStackTrace();
                sendLogMessage("Exception thrown:\n" + e.getMessage());
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void o) {
            super.onPostExecute(o);
            if (response != null)
                tv1.setText(response);
        }
    }

    public void addMessages(final JSONObject msgs) {
        new Thread(() -> {
            try {
                JSONObject json_obj2 = msgs.getJSONObject("response");
                final JSONArray json_ar2 = json_obj2.getJSONArray("items");
                final int count = json_ar2.length();
                final List<View> messages = new ArrayList<>(count);
                for (int i = 0; i < count; i++) {
                    JSONObject tmp_obj = (JSONObject) json_ar2.get(i);
                    if (tmp_obj.has("message"))
                        tmp_obj = tmp_obj.getJSONObject("message");
                    messages.add(new ViewObject(context, new ObjectMessage(tmp_obj)).getView());
                }

                runOnUiThread(() -> {
                    for (View msg : messages) {
                        objs_container.addView(msg, 0);
                    }
                });
            } catch (JSONException e) {
                e.printStackTrace();
                sendLogMessage(e.getMessage() + "\n" + msgs.toString());
            }
        }
        ).start();
    }
}
