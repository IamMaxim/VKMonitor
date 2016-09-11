package ru.iammaxim.vkmonitor;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

public class LogActivity extends AppCompatActivity {
    private RecyclerView log;
    private LogAdapter adapter;
    private RecyclerView.LayoutManager layoutManager;
    private CircleTransformation circleTransformation = new CircleTransformation();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        log = (RecyclerView) findViewById(R.id.rv);
        layoutManager = new LinearLayoutManager(this);
        log.setLayoutManager(layoutManager);
        adapter = new LogAdapter();
        //log.setAdapter(adapter);

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Scanner scanner = new Scanner(new File(App.logPath));
                    while (scanner.hasNext()) {
                        String s = scanner.next();
                        JSONObject o = new JSONObject(s);
                        if (App.useFilter) {
                            if (App.filter.contains(o.getInt("user_id")))
                                adapter.elements.add(new LogElement(o));
                        } else {
                            adapter.elements.add(new LogElement(o));
                        }
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            log.setAdapter(adapter);
                            findViewById(R.id.progressBar).setVisibility(View.GONE);
                        }
                    });
                } catch (FileNotFoundException | JSONException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private int getColorForAction(int action) {
        switch (action) {
            case 6:
            case 7:
                return 0xff5c5cff;
            case 8:
                return 0xff00bb00;
            case 9:
                return 0xffff0000;
            case 61:
            case 62:
                return 0xffffd35c;
            default:
                return 0xff808080;
        }
    }

    private String getDescription(int action, int user_id, int[] args) {
        switch (action) {
            /**
             * $peer_id (integer) $local_id (integer)
             * Прочтение всех входящих сообщений с $peer_id вплоть до $local_id включительно.
             */
            case 6:
                return "You have read in messages upto message #" + args[0];
            /**
             * $peer_id (integer) $local_id (integer)
             * Прочтение всех исходящих сообщений с $peer_id вплоть до $local_id включительно.
             */
            case 7:
                return "Out messages have been read upto message #" + args[0];
            /**
             * -$user_id (integer) $extra (integer)
             * Друг $user_id стал онлайн. $extra не равен 0, если в mode был передан флаг 64. В младшем байте (остаток от деления на 256) числа extra лежит идентификатор платформы.
             */
            case 8:
                return "Became online";
            /**
             * -$user_id (integer) $flags (integer)
             * Друг $user_id стал оффлайн ($flags равен 0, если пользователь покинул сайт (например, нажал выход) и 1, если оффлайн по таймауту (например, статус away)) .
             */
            case 9:
                return "Became offine (" + (args[0] == 0 ? "force quit" : "timeout") + ")";
            /**
             * $user_id (integer) $flags (integer)
             * Пользователь $user_id начал набирать текст в диалоге. Событие должно приходить раз в ~5 секунд при постоянном наборе текста. $flags = 1.
             */
            case 61:
                return "Started typing message";
            /**
             * $user_id (integer) $chat_id (integer)
             * Пользователь $user_id начал набирать текст в беседе $chat_id.
             */
            case 62:
                return  "Started typing in chat #" + args[0];
            default:
                return "cannot get description";
        }
    }

    class LogAdapter extends RecyclerView.Adapter<LogAdapter.ViewHolder> {
        public ArrayList<LogElement> elements = new ArrayList<>();

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ViewHolder(View.inflate(parent.getContext(), R.layout.log_element, null));
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            LogElement element = elements.get(position);
            holder.name.setText(element.name);
            holder.time.setText(element.time);
            holder.description.setText(getDescription(element.update_code, element.user_id, element.args));
            int color = getColorForAction(element.update_code);
            if (position == 0) {
                holder.upperConnector.setVisibility(View.INVISIBLE);
            } else {
                holder.upperConnector.setVisibility(View.VISIBLE);
                holder.upperConnector.setBackgroundColor(color);
            }
            if (position == elements.size()-1) {
                holder.lowerConnector.setVisibility(View.INVISIBLE);
            } else {
                holder.lowerConnector.setVisibility(View.VISIBLE);
                holder.lowerConnector.setBackgroundColor(color);
            }
            holder.photoBg.setColor(color);
            Picasso.with(holder.photo.getContext()).load(element.photo_url).transform(circleTransformation).into(holder.photo);
        }

        @Override
        public int getItemCount() {
            return elements.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            ImageView photo, upperConnector, lowerConnector;
            TextView name, description, time;
            PhotoBgView photoBg;

            public ViewHolder(View itemView) {
                super(itemView);
                photo = (ImageView) itemView.findViewById(R.id.photo);
                upperConnector = (ImageView) itemView.findViewById(R.id.upperConnector);
                lowerConnector = (ImageView) itemView.findViewById(R.id.lowerConnector);
                name = (TextView) itemView.findViewById(R.id.name);
                description = (TextView) itemView.findViewById(R.id.description);
                time = (TextView) itemView.findViewById(R.id.time);
                photoBg = (PhotoBgView) itemView.findViewById(R.id.photo_bg);
            }
        }
    }

    class LogElement {
        String name, photo_url, time;
        int update_code, user_id;
        int[] args;

        public LogElement(JSONObject o) {
            try {
                time = o.getString("time");
                user_id = o.getInt("user_id");
                update_code = o.getInt("action");
                ObjectUser user = Users.get(user_id);
                name = user.toString();
                photo_url = user.photo_url;
                JSONArray arr = o.getJSONArray("args");
                args = new int[arr.length()];
                for (int i = 0; i < arr.length(); i++)
                    args[i] = arr.getInt(i);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
