package ru.iammaxim.vkmonitor.Fragments;

import android.annotation.SuppressLint;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.NetworkOnMainThreadException;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.Html;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.text.style.StyleSpan;
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
import java.util.Date;
import java.util.Iterator;

import ru.iammaxim.vkmonitor.API.Messages.Messages;
import ru.iammaxim.vkmonitor.API.Objects.Attachments.Attachment;
import ru.iammaxim.vkmonitor.API.Objects.Attachments.AttachmentDoc;
import ru.iammaxim.vkmonitor.API.Objects.Attachments.AttachmentPhoto;
import ru.iammaxim.vkmonitor.API.Objects.Attachments.AttachmentSticker;
import ru.iammaxim.vkmonitor.API.Objects.ObjectMessage;
import ru.iammaxim.vkmonitor.API.Objects.ObjectUser;
import ru.iammaxim.vkmonitor.API.Users.Users;
import ru.iammaxim.vkmonitor.App;
import ru.iammaxim.vkmonitor.R;
import ru.iammaxim.vkmonitor.Views.Attachments.AttachmentDocumentView;
import ru.iammaxim.vkmonitor.Views.ForwardedMessagesLine;
import ru.iammaxim.vkmonitor.Views.ImprovedTextView;
import ru.iammaxim.vkmonitor.Views.RecyclerViewWrapper;
import ru.iammaxim.vkmonitor.Views.ScrollablePhotoArray;

public class DialogFragment extends mFragment {
    private int peer_id;
    private TextView count_tv;
    private RecyclerViewWrapper rv;
    private Messages.OnMessagesUpdate messagesCallback;
    private Users.OnUsersUpdate usersCallback;
    private ObjectUser user;
    private CoordinatorLayout layout;

    private ImageView photo;
    private TextView title;
    private TextView subtitle;

    private EditText message_et;
    private View send_button;
    private View attachButton;

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
        Messages.removeMessageCallback(messagesCallback);
        Users.removeCallback(usersCallback);
    }

    @SuppressLint("StaticFieldLeak")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.peer_id = getArguments().getInt("peer_id");

        // detect if this is multiDialog (needed for proper message display)
        isChat = peer_id > 2000000000;

        View v = inflater.inflate(R.layout.fragment_dialog, container, false);


        // setup toolbar
        View mToolbarView = inflater.inflate(R.layout.toolbar_dialog, container, false);
        photo = mToolbarView.findViewById(R.id.photo);
        title = mToolbarView.findViewById(R.id.title);
        title.setMaxLines(1);
        subtitle = mToolbarView.findViewById(R.id.subtitle);
        Toolbar toolbar = v.findViewById(R.id.toolbar);
        toolbar.setTitle("");
        toolbar.addView(mToolbarView);
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                user = Users.get(peer_id);
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                title.setText(user.getTitle());
                subtitle.setText("Subtitle");
                Picasso.with(getContext()).load(user.photo).transform(App.circleTransformation).into(photo);
            }
        }.execute();

        layout = v.findViewById(R.id.layout);
        count_tv = v.findViewById(R.id.count);
        message_et = v.findViewById(R.id.message_et);
        attachButton = v.findViewById(R.id.attachPhoto);
        send_button = v.findViewById(R.id.send);
        rv = v.findViewById(R.id.rv);
        rv.setAdapter(adapter = new DialogAdapter());
        rv.layoutManager.setMsPerInch(200);
        rv.initOnScrolledToTopListener();


        layout.setBackgroundResource(R.drawable.chat_bg);


        rv.onScrolledToTop = () -> {
            new AsyncTask<Void, Void, Void>() {
                Messages.MessagesObject msgs;

                @Override
                protected Void doInBackground(Void... voids) {
                    try {
                        msgs = Messages.getHistory(peer_id, 200, adapter.elements.size());
                        for (int i = msgs.messages.size() - 1; i >= 0; i--) {
                            adapter.elements.add(0, msgs.messages.get(i));
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    return null;
                }

                @Override
                protected void onPostExecute(Void aVoid) {
                    adapter.notifyItemRangeInserted(0, msgs.messages.size());
                }
            }.execute();
        };

        Messages.addMessageCallback(messagesCallback = new Messages.OnMessagesUpdate() {
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
//                                    adapter.elements.set(i, msg); // this will update date, body, id etc.
                                    toSend.isSending = false;
                                    toSend.date = msg.date;
                                    toSend.body = msg.body;
                                    adapter.notifyItemChanged(i);
                                    return;
                                }
                            }
                        }
                    }

                    adapter.elements.add(msg);
                    adapter.notifyItemInserted(adapter.elements.size() - 1);

                    if (rv.nearTheBottom())
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

        Users.addCallback(usersCallback = (user_id, online) -> {
            // TODO: check for user_id and if it's equals to peer_id, update online status
        });

        message_et.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                ObjectMessage msg = Messages.drafts.get(peer_id);
                if (msg == null) {
                    msg = new ObjectMessage();
                    Messages.drafts.put(peer_id, msg);
                }
                msg.body = charSequence.toString();
                new Thread(() -> Messages.setActivity(peer_id)).start();
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        attachButton.setOnClickListener(b -> {
            ObjectMessage msg = Messages.drafts.get(peer_id);
            if (msg == null) {
                msg = new ObjectMessage();
                Messages.drafts.put(peer_id, msg);
            }

//            msg.photos.add(AttachmentPhoto.upload())
        });

        send_button.setOnClickListener(b -> {
            String s;
            if (!(s = message_et.getEditableText().toString()).isEmpty()) {
                message_et.getEditableText().clear();
                ObjectMessage msg = new ObjectMessage(peer_id, Users.get().id, s);
                msg.random_id = (int) (Integer.MAX_VALUE * Math.random());
                msg.isSending = true;

                adapter.elements.add(msg);
                currentlySending.add(msg);
                adapter.notifyItemInserted(adapter.elements.size() - 1);
                if (rv.nearTheBottom())
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

        private void loadUserData(ViewHolder holder, ObjectUser user) {
            if (holder.photo != null) {
                holder.photo.setImageDrawable(null);
                Picasso.with(getContext()).load(user.photo).transform(App.circleTransformation).into(holder.photo);
            }

            if (holder.title != null) {
                holder.title.setText(user.getTitle());
            }
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            holder.container.removeAllViews();
            ObjectMessage message = elements.get(position);

            ObjectUser user = Users.get(message.user_id, new AsyncTask<ObjectUser, Void, ObjectUser>() {
                @Override
                protected ObjectUser doInBackground(ObjectUser... objectUsers) {
                    return objectUsers[0];
                }

                @Override
                protected void onPostExecute(ObjectUser user) {
                    if (holder != null)
                        loadUserData(holder, user);
                }
            });
            if (user != null)
                loadUserData(holder, user);

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

            if (holder.date != null) {
                holder.date.setText(App.timeSDF.format(new Date(message.date)));
            }

            if (!message.body.isEmpty()) {
                holder.container.addView(getBodyTextView(message.body));
            }

            addAttachments(message, holder.container);

            if (message.forwards.size() > 0) { // add forwarded messages
                holder.container.addView(createForwards(message));
            }
        }

        private void addAttachments(ObjectMessage msg, LinearLayout layout) {
            DisplayMetrics dm = getResources().getDisplayMetrics();

            if (msg.photos.size() > 0) { // add photo attachments
                ScrollablePhotoArray spa = new ScrollablePhotoArray(getContext());
                for (AttachmentPhoto p : msg.photos) {
                    spa.add(p);
                }
                layout.addView(spa);
            }

            if (msg.stickers.size() > 0) {
                for (AttachmentSticker sticker : msg.stickers) {
                    ImageView iv = new ImageView(getContext());
                    iv.setMaxHeight((int) (sticker.height * dm.density));
                    iv.setMinimumHeight((int) (sticker.height * dm.density));
                    Picasso.with(getContext()).load(sticker.getBestURL()).into(iv);
                    layout.addView(iv);
                }
            }

            if (msg.docs.size() > 0) {
                for (AttachmentDoc doc : msg.docs) {
                    layout.addView(new AttachmentDocumentView(getContext(), doc));
                }
            }

            if (msg.otherAttachments.size() > 0) {
                for (Attachment a : msg.otherAttachments) {
                    TextView tv = new TextView(layout.getContext());
                    tv.setText(Html.fromHtml("<font color=\"#9fc6f2\">" + a.type + "</font>"));
                    layout.addView(tv);
                }
            }
        }

        private LinearLayout createForwards(ObjectMessage msg) {
            LinearLayout layout = new LinearLayout(getContext());
            layout.setOrientation(LinearLayout.HORIZONTAL);
            ForwardedMessagesLine line = new ForwardedMessagesLine(getContext());
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);
            lp.setMargins(0, 0, dpToPx(4), 0);
            line.setLayoutParams(lp);
            layout.addView(line);
            LinearLayout layout1 = new LinearLayout(getContext());
            layout1.setOrientation(LinearLayout.VERTICAL);
            layout.addView(layout1);

            for (int i = 0; i < msg.forwards.size(); i++) {
                boolean isUpper = false;
                ObjectMessage message = msg.forwards.get(i);
                if (i == 0 || msg.forwards.get(i - 1).user_id != message.user_id) { // upper
                    layout1.addView(getFwdMessagesHeader(message));
                    isUpper = true;
                }

                if (!message.body.isEmpty()) {
                    TextView body = getBodyTextView(message.body);
                    /*LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    params.setMargins(0, isUpper ? dpToPx(4) : 0, 0, 0);
                    body.setLayoutParams(params);*/
                    layout1.addView(body);
                }

                addAttachments(message, layout1);

                if (message.forwards.size() > 0) {
                    layout1.addView(createForwards(message));
                }
            }
            return layout;
        }

        private View getFwdMessagesHeader(ObjectMessage msg) {
            LinearLayout layout = new LinearLayout(getContext());
            layout.setOrientation(LinearLayout.HORIZONTAL);
            layout.setPadding(0, dpToPx(4), dpToPx(4), 0);

            ImageView iv = new ImageView(getContext());
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(dpToPx(36), dpToPx(36));
            iv.setLayoutParams(lp);
            new Thread(() -> {
                ObjectUser user = Users.get(msg.user_id);
                if (getActivity() == null)
                    return;
                getActivity().runOnUiThread(() ->
                        Picasso.with(getContext()).load(user.photo).transform(App.circleTransformation).into(iv));
            }).start();
            layout.addView(iv);

            LinearLayout layout1 = new LinearLayout(getContext());
            layout1.setOrientation(LinearLayout.VERTICAL);
            layout1.setPadding(dpToPx(6), dpToPx(2), dpToPx(6), 0);

            TextView title = new TextView(getContext());
            SpannableString ss = new SpannableString(msg.title);
            ss.setSpan(new StyleSpan(Typeface.BOLD), 0, ss.length(), 0);
            title.setText(ss);
            title.setTextSize(13);
            title.setTextColor(getResources().getColor(R.color.element_message_title_color));
            layout1.addView(title);

            TextView date = new TextView(getContext());
            date.setTextSize(12);
            date.setText(App.formatDateAndTime(msg.date));
            date.setTextColor(getResources().getColor(R.color.element_message_fwd_date_color));
            layout1.addView(date);
            layout.addView(layout1);
            return layout;
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
        private TextView date;

        ViewHolder(View v) {
            super(v);

            photo = v.findViewById(R.id.photo);
            title = v.findViewById(R.id.title);
            container = v.findViewById(R.id.container);
            sending = v.findViewById(R.id.sending);
            unread = v.findViewById(R.id.unread);
            date = v.findViewById(R.id.date);
        }
    }

    private int dpToPx(int dp) {
        DisplayMetrics displayMetrics = getContext().getResources().getDisplayMetrics();
        return Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
    }

    private TextView getBodyTextView(String body) {
        ImprovedTextView tv = new ImprovedTextView(getContext());
        tv.setTextColor(getResources().getColor(R.color.messageTextColor));
        tv.setTextSize(15);
        tv.setText(body);
        return tv;
    }
}
