package ru.iammaxim.vkmonitor.RequestGenerator.Views;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;

import ru.iammaxim.vkmonitor.Objects.ObjectMessage;
import ru.iammaxim.vkmonitor.R;
import ru.iammaxim.vkmonitor.RequestGenerator.RequestGeneratorMain;
import ru.iammaxim.vkmonitor.Users;

public class ViewObject {
    public static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    LinearLayout view;
    public String title, json;
    AlertDialog dialog;

    public ViewObject(Context ctx, ObjectMessage msg) {
        title = msg.title;
        json = msg.json.toString();
        String tmp_title = " ";
        String user_title = Users.get(msg.from_id).getTitle();
        System.out.println(title + " " + user_title);
        if (!title.equals(user_title) && !msg.out) tmp_title = " (" + title + ") ";
        String date = null;
        try {
            date = sdf.format(new Date(msg.date * 1000));
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        try {
            this.title = user_title + tmp_title + date;
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        view = new LinearLayout(ctx);
        view.setBackground(ctx.getResources().getDrawable(R.drawable.rg_output_window_bg));
        view.setOrientation(LinearLayout.VERTICAL);
        TextView user_id_tv = new TextView(ctx);
        TextView body = new TextView(ctx);
        user_id_tv.setText(this.title);
        body.setText(msg.body);
        user_id_tv.setTextColor(Color.WHITE);
        body.setTextColor(Color.WHITE);
        view.addView(user_id_tv);
        view.addView(body);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDetails();
            }
        });

        json = msg.json.toString();
        constructForwards(view, msg.json);
    }

    private void constructForwards(LinearLayout v, JSONObject msg) {
        if (msg.has("fwd_messages"))
            try {
                JSONArray msgs = msg.getJSONArray("fwd_messages");
                for (int i = 0; i < msgs.length(); i++) {
                    JSONObject msg1 = msgs.getJSONObject(i);
                    view.addView(new ViewObject(v.getContext(), new ObjectMessage(msg1)).view);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
    }

    public View getView() {
        return view;
    }

    public void showDetails() {
        dialog = new AlertDialog.Builder(RequestGeneratorMain.context).create();
        dialog.setTitle(this.title);
        dialog.setMessage(json);
        dialog.show();
    }
}
