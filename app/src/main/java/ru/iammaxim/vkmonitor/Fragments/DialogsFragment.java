package ru.iammaxim.vkmonitor.Fragments;


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

import ru.iammaxim.vkmonitor.App;
import ru.iammaxim.vkmonitor.Net;
import ru.iammaxim.vkmonitor.Objects.ObjectDialog;
import ru.iammaxim.vkmonitor.R;
import ru.iammaxim.vkmonitor.Users;
import ru.iammaxim.vkmonitor.Views.RecyclerViewWrapper;

public class DialogsFragment extends Fragment {
    private RecyclerViewWrapper rv;
    private TextView count_tv;

    public DialogsFragment() {
    }

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
        rv.layoutManager.setMsPerInch(200);
        new AsyncTask<Void, Void, Integer>() {
            @Override
            protected Integer doInBackground(Void... params) {
                int count = 0;
                try {
                    final JSONObject o = new JSONObject(Net.processRequest("messages.getDialogs", true, "count=20")).getJSONObject("response");
                    count = o.getInt("count");
                    JSONArray arr = o.getJSONArray("items");
                    for (int i = 0; i < arr.length(); i++) {
                        ObjectDialog dialog = new ObjectDialog(arr.getJSONObject(i));
                        ((DialogsAdapter) rv.adapter).elements.add(dialog);
                    }
                } catch (JSONException | IOException e) {
                    e.printStackTrace();
                }
                return count;
            }

            @Override
            protected void onPostExecute(Integer count) {
                count_tv.setText("Count: " + count);
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

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            holder.parent.setId(position);
            holder.parent.setOnClickListener(this);
            ObjectDialog dialog = elements.get(position);
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
            if (dialog.message.out) {
                Picasso.with(holder.from_photo.getContext()).load(Users.get().photo_url).transform(App.circleTransformation).into(holder.from_photo);
            } else if (dialog.message.from_id != dialog.message.user_id)
                Picasso.with(holder.from_photo.getContext()).load(Users.get(dialog.message.user_id).photo_url).transform(App.circleTransformation).into(holder.from_photo);
            else
                holder.from_photo.setVisibility(View.GONE);

            Picasso.with(holder.photo.getContext()).load(dialog.message.photo).transform(App.circleTransformation).into(holder.photo);
        }

        @Override
        public int getItemCount() {
            return elements.size();
        }

        @Override
        public void onClick(View v) {
            int position = v.getId();
            elements.add(0, elements.remove(position));
            notifyItemMoved(position, 0);
            if (rv.layoutManager.findFirstVisibleItemPosition() < 2)
                rv.smoothScrollToTop();
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
