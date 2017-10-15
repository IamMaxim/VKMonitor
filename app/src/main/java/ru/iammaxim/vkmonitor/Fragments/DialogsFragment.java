package ru.iammaxim.vkmonitor.Fragments;


import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import ru.iammaxim.vkmonitor.API.Messages.Messages;
import ru.iammaxim.vkmonitor.API.Objects.ObjectMessage;
import ru.iammaxim.vkmonitor.API.Objects.ObjectUser;
import ru.iammaxim.vkmonitor.App;
import ru.iammaxim.vkmonitor.API.Objects.ObjectDialog;
import ru.iammaxim.vkmonitor.R;
import ru.iammaxim.vkmonitor.UpdateMessageHandler;
import ru.iammaxim.vkmonitor.API.Users.Users;
import ru.iammaxim.vkmonitor.Views.PhotoBgView;
import ru.iammaxim.vkmonitor.Views.RecyclerViewWrapper;

public class DialogsFragment extends mFragment {
    private RecyclerViewWrapper rv;
    private TextView count_tv;
    private UpdateMessageHandler.Callback callback;
    private Messages.OnMessagesUpdate messagesCallback;
    private Users.OnUsersUpdate usersCallback;
    private Messages.OnDialogsUpdate dialogsCallback;

    public DialogsFragment() {
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        App.handler.removeCallback(callback);
        Messages.removeMessageCallback(messagesCallback);
        Messages.removeDialogsCallback(dialogsCallback);
        Users.removeCallback(usersCallback);
    }

    @SuppressLint("StaticFieldLeak")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_dialogs, container, false);
        Toolbar toolbar = v.findViewById(R.id.toolbar);
        toolbar.setTitle("Dialogs");
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        count_tv = v.findViewById(R.id.count);
        rv = v.findViewById(R.id.rv);
        rv.setAdapter(new DialogsAdapter());
        rv.layoutManager.setMsPerInch(200);
        count_tv.setText(getString(R.string.message_count, Messages.dialogsCount));


        rv.initOnScrolledToBottomListener();
        rv.onScrolledToBottom = () ->
                new AsyncTask<Void, Void, Void>() {
                    int oldCount, loaded;

                    @Override
                    protected Void doInBackground(Void... voids) {
                        oldCount = rv.adapter.getItemCount();
                        loaded = Messages.loadAdditionalDialogs();
                        return null;
                    }

                    @Override
                    protected void onPostExecute(Void aVoid) {
                        if (loaded == 0)
                            return;

                        ((DialogsAdapter) rv.adapter).elements = Messages.dialogObjects;
                        rv.adapter.notifyItemRangeInserted(oldCount, loaded);
                    }
                }.execute();


        Messages.addMessageCallback(messagesCallback = new Messages.OnMessagesUpdate() {
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
            public void onMessageFlagsUpdated(int message_id, int update_code, int flags) {

            }
        });

        Messages.addDialogsCallback(dialogsCallback = dialogIndex -> rv.adapter.notifyItemChanged(dialogIndex));

        Users.addCallback(usersCallback = (user_id, online) -> {
            for (int i = 0; i < Messages.dialogObjects.size(); i++) {
                ObjectDialog dialog = Messages.dialogObjects.get(i);
                if (dialog.message.peer_id == user_id) {
                    rv.adapter.notifyItemChanged(i);
                    break;
                }
            }
        });

        toolbar.setTitle("Updating...");
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                Messages.updateDialogs();
                return null;
            }

            @Override
            protected void onPostExecute(Void v) {
                ((DialogsAdapter) rv.adapter).elements = Messages.dialogObjects;
                toolbar.setTitle("Dialogs");
                if (getActivity() != null)
                    count_tv.setText(getString(R.string.message_count, Messages.dialogsCount));
                rv.adapter.notifyDataSetChanged();
            }
        }.execute();
        return v;
    }

    class DialogsAdapter extends RecyclerView.Adapter<ViewHolder> implements View.OnClickListener {
        public ArrayList<ObjectDialog> elements = new ArrayList<>();

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
            holder.body.setText(dialog.message.getFullBody());
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

            new AsyncTask<Void, Void, Boolean>() {
                @Override
                protected Boolean doInBackground(Void[] objects) {
                    return Users.get(dialog.message.peer_id).online;
                }

                @Override
                protected void onPostExecute(Boolean isOnline) {

                }
            }.execute();

            new AsyncTask<Void, Void, ObjectUser>() {
                @Override
                protected ObjectUser doInBackground(Void[] params) {
                    if (dialog.message.user_id != dialog.message.peer_id)
                        return Users.get(dialog.message.user_id);
                    else
                        return null;
                }

                @Override
                protected void onPostExecute(ObjectUser user) {
                    if (user == null)
                        holder.from_photo.setVisibility(View.GONE);
                    else {
                        holder.from_photo.setVisibility(View.VISIBLE);
                        Picasso.with(holder.from_photo.getContext()).load(user.photo).transform(App.circleTransformation).into(holder.from_photo);
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
            // open dialog with clicked peer_id
            DialogFragment fragment = new DialogFragment();
            Bundle args = new Bundle();
            args.putInt("peer_id", elements.get(v.getId()).message.peer_id);
            fragment.setArguments(args);
            addFragment(fragment);
        }
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        View parent;
        TextView title, body, date, unread;
        ImageView photo, from_photo;
        PhotoBgView photoBg;

        ViewHolder(View v) {
            super(v);
            parent = v;
            title = v.findViewById(R.id.title);
            body = v.findViewById(R.id.body);
            date = v.findViewById(R.id.date);
            photo = v.findViewById(R.id.photo);
            photoBg = v.findViewById(R.id.photo_bg);
            from_photo = v.findViewById(R.id.from_photo);
            unread = v.findViewById(R.id.unread);
        }
    }

    private void addFragment(Fragment fragment) {
        FragmentManager fm = getFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();
        transaction.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left, R.anim.slide_in_left, R.anim.slide_out_right);
        transaction.replace(R.id.container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }
}
