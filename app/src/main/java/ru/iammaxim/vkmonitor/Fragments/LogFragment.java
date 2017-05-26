package ru.iammaxim.vkmonitor.Fragments;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Scanner;
import java.util.Stack;

import ru.iammaxim.vkmonitor.App;
import ru.iammaxim.vkmonitor.LongPollService;
import ru.iammaxim.vkmonitor.API.Objects.ObjectUser;
import ru.iammaxim.vkmonitor.R;
import ru.iammaxim.vkmonitor.UpdateMessageHandler;
import ru.iammaxim.vkmonitor.API.Users.Users;
import ru.iammaxim.vkmonitor.Views.PhotoBgView;
import ru.iammaxim.vkmonitor.Views.RecyclerViewWrapper;

/**
 * Created by maxim on 5/14/17.
 */

public class LogFragment extends mFragment {
    private RecyclerViewWrapper log;

    private UpdateMessageHandler.Callback callback;
    private TextView connectionStatus;
    private Thread logLoader;
    BroadcastReceiver receiver;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_log, container, false);
        Toolbar toolbar = (Toolbar) v.findViewById(R.id.toolbar);
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        log = (RecyclerViewWrapper) v.findViewById(R.id.rv);
        log.setAdapter(new Adapter());
        log.layoutManager.setMsPerInch(150);
        FloatingActionButton scrollDownButton = (FloatingActionButton) v.findViewById(R.id.scroll_down);
        scrollDownButton.setOnClickListener(v1 -> log.scrollToBottom());
        connectionStatus = (TextView) v.findViewById(R.id.connectionStatus);
        if (isServiceRunning(LongPollService.class)) {
            connectionStatus.setText(R.string.status_connected);
            connectionStatus.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        } else {
            connectionStatus.setText(R.string.status_not_connected);
            connectionStatus.setBackgroundColor(getResources().getColor(R.color.colorErrorBG));
        }

        logLoader = new Thread(() -> {
            try {
                Stack<String> stack = new Stack<>();
                Scanner scanner = new Scanner(new File(App.logPath));
                while (!Thread.interrupted() && scanner.hasNext()) {
                    stack.addElement(scanner.nextLine());
                }

                while (!Thread.interrupted() && stack.size() > 0) {
                    int processed = 0;
                    for (int i = 0; i < 50 && stack.size() > 0; i++) {
                        JSONObject o = new JSONObject(stack.pop());
                        filterAndAddInBeginning(new Element(o));
                        processed++;
                    }

                    final int finalProcessed = processed;
                    if (getActivity() == null)
                        return;
                    getActivity().runOnUiThread(() -> log.adapter.notifyItemRangeInserted(0, finalProcessed));
                }

                if (getActivity() == null)
                    return;
                getActivity().runOnUiThread(() -> {
                    if (getView() == null)
                        return;
                    final ProgressBar pb = (ProgressBar) getView().findViewById(R.id.progressBar);
                    pb.animate().scaleX(0).scaleY(0).setDuration(300).withEndAction(() -> pb.setVisibility(View.GONE)).start();
                    log.scrollToBottom();
                    log.stopScroll();
                });
            } catch (IndexOutOfBoundsException | FileNotFoundException | JSONException e) {
                e.printStackTrace();
            }
        });
        logLoader.start();

        return v;
    }

    @Override
    public void onStart() {
        super.onStart();
        LocalBroadcastManager.getInstance(getContext()).registerReceiver((receiver), new IntentFilter(LongPollService.STATUS_CHANGED));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        App.handler.removeCallback(callback);
        logLoader.interrupt();
    }

    @Override
    public void onStop() {
        LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(receiver);
        super.onStop();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        callback = (update_code, needToLog, user_id, date, arr) -> {
            if (needToLog)
                new AsyncTask<Void, Void, Boolean>() {
                    @Override
                    protected Boolean doInBackground(Void[] params) {
                        return filterAndAdd(new Element(update_code, user_id, date, arr));
                    }

                    @Override
                    protected void onPostExecute(Boolean added) {
                        if (added) {
                            try {
                                log.adapter.notifyItemInserted(log.adapter.getItemCount() - 1);
                                log.adapter.notifyItemChanged(log.adapter.getItemCount() - 2);
                                if (log.layoutManager.findLastVisibleItemPosition() >= log.adapter.getItemCount() - 3)
                                    log.layoutManager.smoothScrollToPosition(log, null, log.adapter.getItemCount() - 1);
                            } catch (NullPointerException e) {
                            }
                        }
                    }
                }.execute();
        };
        App.handler.addCallback(callback);

        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                int status = intent.getIntExtra(LongPollService.STATUS_VALUE, 0);
                if (status == 1) {
                    connectionStatus.setText(R.string.status_connected);
                    connectionStatus.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                } else {
                    connectionStatus.setText(R.string.status_not_connected);
                    connectionStatus.setBackgroundColor(getResources().getColor(R.color.colorErrorBG));
                }
            }
        };
    }

    private boolean isServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getActivity().getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    private boolean filterAndAddInBeginning(Element e) {
        if (App.useFilter) {
            if (App.filter.contains(e.peer_id)) {
                ((Adapter) log.adapter).elements.add(0, e);
                return true;
            }
        } else {
            ((Adapter) log.adapter).elements.add(0, e);
            return true;
        }
        return false;
    }

    private boolean filterAndAdd(Element e) {
        if (App.useFilter) {
            if (App.filter.contains(e.peer_id)) {
                ((Adapter) log.adapter).elements.add(e);
                return true;
            }
        } else {
            ((Adapter) log.adapter).elements.add(e);
            return true;
        }
        return false;
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

    private String getPlatformName(int id) {
        switch (id) {
            case 1:
                return "mobile";
            case 2:
                return "iphone";
            case 3:
                return "ipad";
            case 4:
                return "android";
            case 5:
                return "wphone";
            case 6:
                return "windows";
            case 7:
                return "web";
            default:
                return String.valueOf(id);
        }
    }

    private String getDescription(int action, JSONArray arr) {
        try {
            switch (action) {
                /**
                 * $peer_id (integer) $local_id (integer)
                 * Прочтение всех входящих сообщений с $peer_id вплоть до $local_id включительно.
                 */
                case 6:
                    return "You have read in messages up to message #" + arr.getInt(2);
                /**
                 * $peer_id (integer) $local_id (integer)
                 * Прочтение всех исходящих сообщений с $peer_id вплоть до $local_id включительно.
                 */
                case 7:
                    return "Out messages have been read up to message #" + arr.getInt(2);
                /**
                 * -$peer_id (integer) $extra (integer)
                 * Друг $peer_id стал онлайн. $extra не равен 0, если в mode был передан флаг 64. В младшем байте (остаток от деления на 256) числа extra лежит идентификатор платформы.
                 */
                case 8:
                    if (arr.length() >= 3)
                        return "Became online (" + getPlatformName(arr.getInt(2) & 0xFF) + ")";
                    else
                        return "Became online";
                    /**
                     * -$peer_id (integer) $flags (integer)
                     * Друг $peer_id стал оффлайн ($flags равен 0, если пользователь покинул сайт (например, нажал выход) и 1, если оффлайн по таймауту (например, статус away)) .
                     */
                case 9:
                    return "Became offline (" + (arr.getInt(2) == 0 ? "force quit" : "timeout") + ")";
                /**
                 * $peer_id (integer) $flags (integer)
                 * Пользователь $peer_id начал набирать текст в диалоге. Событие должно приходить раз в ~5 секунд при постоянном наборе текста. $flags = 1.
                 */
                case 61:
                    return "Started typing message";
                /**
                 * $peer_id (integer) $chat_id (integer)
                 * Пользователь $peer_id начал набирать текст в беседе $chat_id.
                 */
                case 62:
                    return "Started typing in chat #" + arr.getInt(2);
                default:
                    return "Couldn't get description\n" + arr.toString();
            }
        } catch (JSONException e) {
            e.printStackTrace();
            return "Couldn't get description";
        }
    }

    public class Adapter extends RecyclerView.Adapter<Adapter.ViewHolder> {
        public ArrayList<Element> elements = new ArrayList<>();

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ViewHolder(View.inflate(parent.getContext(), R.layout.element_log, null));
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            Element element = elements.get(position);
            holder.name.setText(element.name);
            holder.date.setText(element.date);
            holder.time.setText(element.time);
            holder.description.setText(getDescription(element.update_code, element.arr));
            int color = getColorForAction(element.update_code);
            if (position == 0) {
                holder.upperConnector.setVisibility(View.INVISIBLE);
            } else {
                holder.upperConnector.setVisibility(View.VISIBLE);
                holder.upperConnector.setBackgroundColor(color);
            }
            if (position == elements.size() - 1) {
                holder.lowerConnector.setVisibility(View.INVISIBLE);
            } else {
                holder.lowerConnector.setVisibility(View.VISIBLE);
                holder.lowerConnector.setBackgroundColor(color);
            }
            holder.photoBg.setColor(color);
            Picasso.with(holder.photo.getContext()).load(element.photo_url).transform(App.circleTransformation).into(holder.photo);
        }

        @Override
        public int getItemCount() {
            return elements.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            ImageView photo, upperConnector, lowerConnector;
            TextView name, description, date, time;
            PhotoBgView photoBg;

            public ViewHolder(View itemView) {
                super(itemView);
                photo = (ImageView) itemView.findViewById(R.id.photo);
                upperConnector = (ImageView) itemView.findViewById(R.id.upperConnector);
                lowerConnector = (ImageView) itemView.findViewById(R.id.lowerConnector);
                name = (TextView) itemView.findViewById(R.id.name);
                description = (TextView) itemView.findViewById(R.id.description);
                date = (TextView) itemView.findViewById(R.id.date);
                time = (TextView) itemView.findViewById(R.id.time);
                photoBg = (PhotoBgView) itemView.findViewById(R.id.photo_bg);
            }
        }
    }

    class Element {
        String name, photo_url, date, time;
        int update_code, peer_id;
        JSONArray arr;

        public Element(JSONObject o) {
            try {
                arr = o.getJSONArray("upd");
                update_code = arr.getInt(0);
                peer_id = o.getInt("peer_id");
                if (peer_id == -1) {
                    name = "Error";
                } else {
                    ObjectUser user = Users.get(peer_id);
                    name = user.getTitle();
                    photo_url = user.photo_200;
                }

                Date _date = new Date(o.getLong("date"));
                date = App.dateSDF.format(_date);
                time = App.timeSDF.format(_date);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        public Element(int update_code, int peer_id, long date, JSONArray arr) {
            this.update_code = update_code;
            this.peer_id = peer_id;
            Date date1 = new Date(date);
            this.date = App.dateSDF.format(date1);
            this.time = App.timeSDF.format(date1);
            this.arr = arr;
            if (peer_id != -1) {
                ObjectUser user = Users.get(peer_id);
                name = user.toString();
                photo_url = user.photo_200;
            } else {
                name = "Error";
            }
        }
    }
}
