package ru.iammaxim.vkmonitor.Fragments;


import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import ru.iammaxim.vkmonitor.API.Messages.Messages;
import ru.iammaxim.vkmonitor.API.Objects.ObjectMessage;
import ru.iammaxim.vkmonitor.API.Objects.ObjectUser;
import ru.iammaxim.vkmonitor.App;
import ru.iammaxim.vkmonitor.Net;
import ru.iammaxim.vkmonitor.API.Objects.ObjectDialog;
import ru.iammaxim.vkmonitor.R;
import ru.iammaxim.vkmonitor.UpdateMessageHandler;
import ru.iammaxim.vkmonitor.API.Users.Users;
import ru.iammaxim.vkmonitor.Views.RecyclerViewWrapper;

public class DialogsFragment extends Fragment {
    private RecyclerViewWrapper rv;
    private TextView count_tv;
    private UpdateMessageHandler.Callback callback;
    private Messages.OnMessagesUpdate messagesCallback;

    public DialogsFragment() {
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        App.handler.removeCallback(callback);
        Messages.callbacks.remove(messagesCallback);
    }

    @SuppressLint("StaticFieldLeak")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_dialogs, container, false);
        Toolbar toolbar = (Toolbar) v.findViewById(R.id.toolbar);
        toolbar.setTitle("Dialogs");
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        count_tv = (TextView) v.findViewById(R.id.count);
        rv = (RecyclerViewWrapper) v.findViewById(R.id.rv);
        rv.setAdapter(new DialogsAdapter());
        ((DialogsAdapter) rv.adapter).elements = Messages.dialogObjects;
        rv.layoutManager.setMsPerInch(200);
        count_tv.setText(getString(R.string.message_count, Messages.dialogsCount));
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                Messages.updateDialogs();

                Messages.callbacks.add(messagesCallback = new Messages.OnMessagesUpdate() {
                    @Override
                    public void onMessageGet(int prevDialogIndex, ObjectMessage msg) {
                        if (prevDialogIndex == 0)
                            rv.adapter.notifyItemChanged(prevDialogIndex);
                        else {
                            rv.adapter.notifyItemMoved(prevDialogIndex, 0);
                            if (rv.layoutManager.findFirstVisibleItemPosition() < 2)
                                rv.smoothScrollToTop();
                        }
                    }

                    @Override
                    public void onMessageFlagsUpdated(int dialogIndex, ObjectMessage msg) {
                        rv.adapter.notifyItemChanged(dialogIndex);
                    }
                });

                App.handler.addCallback(callback = (update_code, needToLog, user_id, date, arr) -> {
                });
                /*App.handler.addCallback(callback = (update_code, needToLog, peer_id, date, arr1) -> {
                    switch (update_code) {
                        case 1:
                            try {
                                int index = ((DialogsAdapter) rv.adapter).getDialogByMessageID(arr1.getInt(1));
                                if (index != -1) {
                                    ObjectDialog dialog = ((DialogsAdapter) rv.adapter).elements.get(index);
                                    dialog.message.flags = arr1.getInt(2);
                                    dialog.message.processFlags();
                                    if (dialog.message.read_state)
                                        dialog.updateUnread();
                                    rv.adapter.notifyItemChanged(index);
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            break;
                        case 2:
                            try {
                                int index = ((DialogsAdapter) rv.adapter).getDialogByMessageID(arr1.getInt(1));
                                if (index != -1) {
                                    ObjectDialog dialog = ((DialogsAdapter) rv.adapter).elements.get(index);
                                    dialog.message.flags |= ~arr1.getInt(2);
                                    dialog.message.processFlags();
                                    if (dialog.message.read_state)
                                        dialog.updateUnread();
                                    rv.adapter.notifyItemChanged(index);
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            break;
                        case 3:
                            try {
                                int index = ((DialogsAdapter) rv.adapter).getDialogByMessageID(arr1.getInt(1));
                                if (index != -1) {
                                    ObjectDialog dialog = ((DialogsAdapter) rv.adapter).elements.get(index);
                                    dialog.message.flags &= ~arr1.getInt(2);
                                    dialog.message.processFlags();
                                    if (dialog.message.read_state)
                                        dialog.updateUnread();
                                    rv.adapter.notifyItemChanged(index);
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            break;
                        case 4:
                            int index = ((DialogsAdapter) rv.adapter).getDialogIndexByPeerID(peer_id);
                            if (index != -1) {
                                ObjectDialog dialog = ((DialogsAdapter) rv.adapter).elements.remove(index);
                                try {
                                    dialog.message.id = arr1.getInt(1);
                                    dialog.message.flags = arr1.getInt(2);
                                    dialog.message.processFlags();
                                    dialog.updateUnread();
                                    dialog.message.date = arr1.getLong(4) * 1000;
                                    dialog.message.body = arr1.getString(6);
                                    JSONObject attachments = arr1.getJSONObject(7);
                                    if (attachments.has("from"))
                                        dialog.message.from_id = attachments.getInt("from");
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                    dialog.message.body = "Error";
                                }
                                ((DialogsAdapter) rv.adapter).elements.add(0, dialog);
                                if (index == 0)
                                    rv.adapter.notifyItemChanged(0);
                                else {
                                    rv.adapter.notifyItemMoved(index, 0);
                                    rv.adapter.notifyItemChanged(0);
                                    if (rv.layoutManager.findFirstVisibleItemPosition() < 2)
                                        rv.smoothScrollToTop();
                                }
                            } else {
                                new AsyncTask<Void, Void, ObjectDialog>() {
                                    @Override
                                    protected ObjectDialog doInBackground(Void... voids) {
                                        try {
                                            return Messages.getDialogByMessageID(arr1.getInt(1));
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                        return null;
                                    }

                                    @Override
                                    protected void onPostExecute(ObjectDialog dialog) {
                                        ((DialogsAdapter) rv.adapter).elements.add(0, dialog);
                                        rv.adapter.notifyItemInserted(0);
                                        if (rv.layoutManager.findFirstVisibleItemPosition() < 2)
                                            rv.smoothScrollToTop();
                                    }
                                }.execute();
                            }
                            break;
                    }
                });*/
                return null;
            }

            @Override
            protected void onPostExecute(Void v) {
                if (getActivity() != null)
                    count_tv.setText(getString(R.string.message_count, Messages.dialogsCount));
                rv.adapter.notifyDataSetChanged();
            }
        }.execute();
        return v;
    }

    class DialogsAdapter extends RecyclerView.Adapter<ViewHolder> implements View.OnClickListener {
        public ArrayList<ObjectDialog> elements = new ArrayList<>();


/*        *//**
         * @param peer_id peer_id of needed dialog
         * @return index of dialog or
         * -1 if no such dialog found
         *//*
        public int getDialogIndexByPeerID(int peer_id) {
            for (int i = 0; i < elements.size(); i++)
                if (elements.get(i).message.peer_id == peer_id)
                    return i;
            return -1;
        }

        */

        /**
         * @param message_id ID of message
         * @return index of dialog which has last message with ID == message_id
         * or -1 if no such dialogs
         *//*
        public int getDialogByMessageID(int message_id) {
            for (int i = 0; i < elements.size(); i++) {
                if (elements.get(i).message.id == message_id)
                    return i;
            }
            return -1;
        }*/
        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.element_dialog, parent, false));
        }

        @SuppressLint("StaticFieldLeak")
        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {
            holder.parent.setId(position);
            holder.parent.setOnClickListener(this);
            final ObjectDialog dialog = elements.get(position);
            holder.title.setText(dialog.message.title);
            holder.body.setText(dialog.message.body);
            holder.date.setText(App.formatDate(dialog.message.date));
            if (dialog.unread == 0) {
                holder.unread.setVisibility(View.GONE);
            } else {
                holder.unread.setVisibility(View.VISIBLE);
                holder.unread.setText(String.valueOf(dialog.unread));
                if (dialog.message.muted)
                    holder.unread.setBackgroundResource(R.drawable.unread_indicator_bg_muted);
                else
                    holder.unread.setBackgroundResource(R.drawable.unread_indicator_bg);
            }
            if (dialog.message.read_state) {
                holder.body.setBackground(null);
            } else {
                if (dialog.message.muted)
                    holder.body.setBackgroundResource(R.drawable.unread_muted_message_body_bg);
                else
                    holder.body.setBackgroundResource(R.drawable.unread_message_body_bg);
            }

            new AsyncTask<Void, Void, ObjectUser>() {
                @Override
                protected ObjectUser doInBackground(Void[] params) {
                    if (dialog.message.from_id != dialog.message.peer_id) {
/*                        if (dialog.message.out)
                            return Users.get();
                        else*/
                        return Users.get(dialog.message.from_id);
                    } else
                        return null;
                }

                @Override
                protected void onPostExecute(ObjectUser user) {
                    if (user == null)
                        holder.from_photo.setVisibility(View.GONE);
                    else {
                        holder.from_photo.setVisibility(View.VISIBLE);
                        Picasso.with(holder.from_photo.getContext()).load(user.photo_url).transform(App.circleTransformation).into(holder.from_photo);
                    }
                }
            }.execute();
            Picasso.with(holder.photo.getContext()).load(dialog.message.photo).transform(App.circleTransformation).into(holder.photo);
        }

        @Override
        public int getItemCount() {
            return elements.size();
        }

        @Override
        public void onClick(View v) {
/*            int position = v.getId();
            elements.add(0, elements.remove(position));
            notifyItemMoved(position, 0);
            if (rv.layoutManager.findFirstVisibleItemPosition() < 2)
                rv.smoothScrollToTop();*/
        }
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        View parent;
        TextView title, body, date, unread;
        ImageView photo, from_photo;

        ViewHolder(View v) {
            super(v);
            parent = v;
            title = (TextView) v.findViewById(R.id.title);
            body = (TextView) v.findViewById(R.id.body);
            date = (TextView) v.findViewById(R.id.date);
            photo = (ImageView) v.findViewById(R.id.photo);
            from_photo = (ImageView) v.findViewById(R.id.from_photo);
            unread = (TextView) v.findViewById(R.id.unread);
        }
    }
}
