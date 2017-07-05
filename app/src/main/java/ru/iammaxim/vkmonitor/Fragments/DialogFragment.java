package ru.iammaxim.vkmonitor.Fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import ru.iammaxim.vkmonitor.API.Messages.Messages;
import ru.iammaxim.vkmonitor.API.Objects.ObjectMessage;
import ru.iammaxim.vkmonitor.API.Objects.ObjectUser;
import ru.iammaxim.vkmonitor.API.Users.Users;
import ru.iammaxim.vkmonitor.App;
import ru.iammaxim.vkmonitor.R;
import ru.iammaxim.vkmonitor.Views.RecyclerViewWrapper;

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

    public DialogFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.peer_id = getArguments().getInt("peer_id");
        user = Users.get(peer_id);
        View v = inflater.inflate(R.layout.fragment_dialog, container, false);

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
        rv = (RecyclerViewWrapper) v.findViewById(R.id.rv);
        rv.setAdapter(new DialogAdapter());
        rv.layoutManager.setMsPerInch(200);
        count_tv.setText(getString(R.string.message_count, Messages.dialogsCount));

        Messages.callbacks.add(messagesCallback = new Messages.OnMessagesUpdate() {
            @Override
            public void onMessageGet(int prevDialogIndex, ObjectMessage msg) {
                // TODO: check for peer_id and add message if needed
            }

            @Override
            public void onMessageFlagsUpdated(int dialogIndex, ObjectMessage msg) {
                // TODO: check for peer_id and change message if needed
            }
        });

        Users.callbacks.add(usersCallback = (user_id, online) -> {
            // TODO: check for user_id and if it's equals to peer_id, update online status
        });

        return v;
    }

    class DialogAdapter extends RecyclerView.Adapter<ViewHolder> {

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return null;
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {

        }

        @Override
        public int getItemCount() {
            return 0;
        }
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        public ViewHolder(View itemView) {
            super(itemView);
        }
    }

    private int dpToPx(int dp) {
        DisplayMetrics displayMetrics = getContext().getResources().getDisplayMetrics();
        return Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
    }
}
