package ru.iammaxim.vkmonitor.RequestGenerator.Views;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;

import ru.iammaxim.vkmonitor.API.Objects.ObjectMessage;
import ru.iammaxim.vkmonitor.R;
import ru.iammaxim.vkmonitor.RequestGenerator.RequestGeneratorMain;
import ru.iammaxim.vkmonitor.API.Users.Users;

public class ViewObject {
    public static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private LinearLayout view;
    private String title, json;

    public ViewObject(final Context ctx, ObjectMessage msg) {
        json = msg.json.toString();
        String tmp_title = " ";
        String date = sdf.format(new Date(msg.date * 1000));
        String user_title = Users.get(msg.from_id).getTitle();
        if (!msg.title.equals(user_title) && !msg.out) tmp_title = " (" + msg.title + ") ";
        title = user_title + tmp_title + date;

        view = new LinearLayout(ctx);
        view.setBackground(ctx.getResources().getDrawable(R.drawable.rg_output_window_bg));
        view.setOrientation(LinearLayout.VERTICAL);
        TextView title_tv = new TextView(ctx);
        TextView body_tv = new TextView(ctx);
        title_tv.setText(title);
        title_tv.setTextColor(Color.WHITE);
        body_tv.setText(msg.body);
        body_tv.setTextColor(Color.WHITE);
        view.addView(title_tv);
        view.addView(body_tv);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDetails();
            }
        });

        if (msg.json.has("attachments")) {
            try {
                JSONArray attachments = msg.json.getJSONArray("attachments");
                for (int i = 0; i < attachments.length(); i++) {
                    JSONObject attachment = attachments.getJSONObject(i);
                    String type = attachment.getString("type");
                    switch (type) {
                        case "photo":
                            attachment = attachment.getJSONObject("photo");
                            final ImageView image = new ImageView(ctx);
                            view.addView(image);
                            int width = -1;
                            Iterator<String> it = attachment.keys();
                            while (it.hasNext()) {
                                String s = it.next();
                                if (!s.startsWith("photo_"))
                                    continue;
                                int w = Integer.parseInt(s.replace("photo_", ""));
                                if (w > width)
                                    width = w;
                            }
                            if (width != -1) {
                                final JSONObject finalAttachment = attachment;
                                final int finalWidth = width;
                                ((Activity) ctx).runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        try {
                                            Picasso.with(ctx).load(finalAttachment.getString("photo_" + finalWidth)).into(image);
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                });
                            }
                            break;
                        default:
                            break;
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
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
        AlertDialog dialog = new AlertDialog.Builder(RequestGeneratorMain.context).create();
        dialog.setTitle(this.title);
        dialog.setMessage(json);
        dialog.show();
    }
}
