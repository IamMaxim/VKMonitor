package ru.iammaxim.vkmonitor.Fragments;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import ru.iammaxim.vkmonitor.API.Messages.Messages;
import ru.iammaxim.vkmonitor.API.Objects.Attachments.AttachmentPhoto;
import ru.iammaxim.vkmonitor.API.Objects.ObjectMessage;
import ru.iammaxim.vkmonitor.API.Objects.ObjectUser;
import ru.iammaxim.vkmonitor.API.Users.Users;
import ru.iammaxim.vkmonitor.App;
import ru.iammaxim.vkmonitor.R;
import ru.iammaxim.vkmonitor.Views.RecyclerViewWrapper;
import ru.iammaxim.vkmonitor.Views.ScrollablePhotoArray;

public class DialogFragment extends mFragment {
    private int peer_id;
    private TextView count_tv;
    private RecyclerViewWrapper rv;
    private Messages.OnMessagesUpdate messagesCallback;
    private Users.OnUsersUpdate usersCallback;
    private ObjectUser user;

    private ImageView photo;
    private TextView title;
    private TextView subtitle;

    private EditText message_et;
    private View send_button;

    private boolean isChat;
    private DialogAdapter adapter;
    private int messagesCount = 0;

    private ArrayList<ObjectMessage> currentlySending = new ArrayList<>();

    public static final class MessageType {
        public static final int
                OUT = 0,
                IN = 1,
                IN_TITLE_PHOTO = 2,
                IN_TITLE = 3,
                IN_PHOTO = 4,
                IN_MARGIN = 5;
    }

    public DialogFragment() {
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Messages.messageCallbacks.remove(messagesCallback);
        Users.callbacks.remove(usersCallback);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.peer_id = getArguments().getInt("peer_id");
        user = Users.get(peer_id);

        // detect if this is multiDialog (needed for proper message display)
        if (peer_id > 2000000000)
            isChat = true;
        else isChat = false;

        View v = inflater.inflate(R.layout.fragment_dialog, container, false);


        // setup toolbar
        View mToolbarView = inflater.inflate(R.layout.toolbar_dialog, container, false);
        photo = (ImageView) mToolbarView.findViewById(R.id.photo);
        title = (TextView) mToolbarView.findViewById(R.id.title);
        subtitle = (TextView) mToolbarView.findViewById(R.id.subtitle);
        title.setText(user.getTitle());
        subtitle.setText("Subtitle");
        Picasso.with(getContext()).load(user.photo_200).transform(App.circleTransformation).into(photo);
        Toolbar toolbar = (Toolbar) v.findViewById(R.id.toolbar);
        toolbar.setTitle("");
        toolbar.addView(mToolbarView);
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        count_tv = (TextView) v.findViewById(R.id.count);
        message_et = (EditText) v.findViewById(R.id.message_et);
        send_button = v.findViewById(R.id.send);
        rv = (RecyclerViewWrapper) v.findViewById(R.id.rv);
        rv.setAdapter(adapter = new DialogAdapter());
        rv.layoutManager.setMsPerInch(200);

        Messages.messageCallbacks.add(messagesCallback = new Messages.OnMessagesUpdate() {
            @Override
            public void onMessageGet(int prevDialogIndex, ObjectMessage msg) {
                // TODO: process action messages
                if (msg.peer_id == peer_id) {
                    Iterator<ObjectMessage> it = currentlySending.iterator();
                    while (it.hasNext()) {
                        ObjectMessage toSend = it.next();
                        if (toSend.random_id == msg.random_id) {
                            it.remove();
                            for (int i = adapter.elements.size() - 1; i >= 0; i--) {
                                if (adapter.elements.get(i) == toSend) {
                                    adapter.elements.set(i, msg); // this will update date, body, id etc.
                                    adapter.notifyItemChanged(i);
                                    return;
                                }
                            }
                        }
                    }

                    adapter.elements.add(msg);
                    adapter.notifyItemInserted(adapter.elements.size() - 1);

                    if (rv.onTheBottom())
                        rv.smoothScrollToBottom();
                }
            }

            @Override
            public void onMessageFlagsUpdated(int message_id, int update_code, int flags) {
                for (int i = adapter.elements.size() - 1; i >= 0; i--) {
                    ObjectMessage message = adapter.elements.get(i);
                    if (message_id == message.id) {
                        message.applyFlags(update_code, flags);
                        message.processFlags();
                        adapter.notifyItemChanged(i);
                        return;
                    }
                }
            }
        });

        Users.callbacks.add(usersCallback = (user_id, online) -> {
            // TODO: check for user_id and if it's equals to peer_id, update online status
        });

        send_button.setOnClickListener(b -> {
            String s;
            if (!(s = message_et.getEditableText().toString()).isEmpty()) {
                message_et.getEditableText().clear();
                ObjectMessage msg = new ObjectMessage(peer_id, Users.get().id, s);
                msg.isSending = true;

                adapter.elements.add(msg);
                currentlySending.add(msg);
                adapter.notifyItemInserted(adapter.elements.size() - 1);
                if (rv.onTheBottom())
                    rv.smoothScrollToBottom();

                new Thread(() -> {
                    try {
                        Messages.send(msg);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }).start();
            }
        });

        // load latest messages
        new Thread(() -> {
            try {
                Messages.MessagesObject obj = Messages.getHistory(peer_id, 200, 0);
                if (getActivity() == null)
                    return;
                getActivity().runOnUiThread(() -> {
                    setMessagesCount(obj.count);
                    for (ObjectMessage message : obj.messages) {
                        adapter.elements.add(0, message);
                    }
                    adapter.notifyItemRangeInserted(0, obj.messages.size());
                    rv.scrollToBottom();
                });
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }).start();
        return v;
    }

    private void setMessagesCount(int count) {
        messagesCount = count;
        count_tv.setText(getString(R.string.message_count, count));
    }

    class DialogAdapter extends RecyclerView.Adapter<ViewHolder> {
        public ArrayList<ObjectMessage> elements = new ArrayList<>();

        @Override
        public int getItemViewType(int position) {
            ObjectMessage msg = elements.get(position);

            if (msg.out) {
                return MessageType.OUT;
            } else {
                if (isChat) {
                    boolean isUpper = false;

                    if (position == 0 || elements.get(position - 1).user_id != msg.user_id)
                        isUpper = true;

                    if (isUpper)
                        return MessageType.IN_TITLE_PHOTO;
                    else
                        return MessageType.IN_MARGIN;
                } else
                    return MessageType.IN;
            }
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            switch (viewType) {
                case MessageType.OUT:
                    return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.element_message_out, parent, false));
                case MessageType.IN:
                    return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.element_message_in, parent, false));
                case MessageType.IN_MARGIN:
                    return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.element_message_in_margin, parent, false));
                case MessageType.IN_TITLE_PHOTO:
                    return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.element_message_in_title_photo, parent, false));
            }
            return null;
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            holder.container.removeAllViews();
            ObjectMessage message = elements.get(position);
            ObjectUser user = Users.get(message.user_id);

            if (holder.photo != null) {
                holder.photo.setImageDrawable(null);
                Picasso.with(getContext()).load(user.photo_200).transform(App.circleTransformation).into(holder.photo);
            }

            if (holder.title != null) {
                holder.title.setText(user.getTitle());
            }

            if (holder.unread != null) {
                if (message.read_state)
                    holder.unread.setVisibility(View.GONE);
                else
                    holder.unread.setVisibility(View.VISIBLE);
            }

            if (holder.sending != null) {
                if (message.isSending)
                    holder.sending.setVisibility(View.VISIBLE);
                else
                    holder.sending.setVisibility(View.GONE);
            }

            if (!message.body.isEmpty()) {
                TextView msg = new TextView(getContext());
                msg.setTextColor(0xff000000);
                msg.setTextSize(15);
                msg.setText(message.body);
                holder.container.addView(msg);
            }

            if (message.photos.size() > 0) { // add photo attachments
                ScrollablePhotoArray spa = new ScrollablePhotoArray(getContext());
                for (AttachmentPhoto p : message.photos) {
/*                ImageView photo = new ImageView(getContext());
                Picasso.with(getContext()).load(p.getBestURL()).into(photo);
                holder.container.addView(photo);*/
                    spa.add(p);
                }
                holder.container.addView(spa);
            }
        }

        @Override
        public int getItemCount() {
            return elements.size();
        }
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView photo;
        private TextView title;
        private LinearLayout container;
        private View sending;
        private View unread;

        public ViewHolder(View v) {
            super(v);

            photo = (ImageView) v.findViewById(R.id.photo);
            title = (TextView) v.findViewById(R.id.title);
            container = (LinearLayout) v.findViewById(R.id.container);
            sending = v.findViewById(R.id.sending);
            unread = v.findViewById(R.id.unread);
        }
    }

    private int dpToPx(int dp) {
        DisplayMetrics displayMetrics = getContext().getResources().getDisplayMetrics();
        return Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
    }
}
