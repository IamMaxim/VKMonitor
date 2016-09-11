package ru.iammaxim.vkmonitor;

import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;

public class ChangeFilterActivity extends AppCompatActivity {
    private CircleTransformation circleTransformation = new CircleTransformation();
    private RecyclerView rv;
    private FilterAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_filter);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        rv = (RecyclerView) findViewById(R.id.rv);
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new FilterAdapter();
        rv.setAdapter(adapter);
        for (int id : UserDB.getUserIDs()) {
            adapter.elements.add(new UserElement(id));
        }
        Collections.sort(adapter.elements);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        App.saveFilter();
    }

    class FilterAdapter extends RecyclerView.Adapter<FilterAdapter.ViewHolder> {
        public ArrayList<UserElement> elements = new ArrayList<>();

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ViewHolder(View.inflate(parent.getContext(), R.layout.filter_element, null));
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, final int position) {
            final UserElement element = elements.get(position);
            holder.name.setText(element.name);
            holder.cb.setChecked(element.enabled);
            holder.cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (!buttonView.isPressed()) return;
                    if (isChecked) App.filter.add(element.user_id);
                    else App.filter.remove((Integer) element.user_id);
                    element.enabled = !element.enabled;
                }
            });
            Picasso.with(holder.photo.getContext()).load(element.photo_url).transform(circleTransformation).into(holder.photo);
        }

        @Override
        public int getItemCount() {
            return elements.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            ImageView photo;
            TextView name;
            CheckBox cb;

            public ViewHolder(View itemView) {
                super(itemView);
                photo = (ImageView) itemView.findViewById(R.id.photo);
                name = (TextView) itemView.findViewById(R.id.name);
                cb = (CheckBox) itemView.findViewById(R.id.cb);
            }
        }
    }

    class UserElement implements Comparable<UserElement> {
        String name, photo_url;
        int user_id;
        boolean enabled;

        public UserElement(int user_id) {
            this.user_id = user_id;
            ObjectUser user = Users.get(user_id);
            name = user.toString();
            photo_url = user.photo_url;
            enabled = App.filter.contains(user_id);
        }

        @Override
        public int compareTo(UserElement o) {
            return name.compareTo(o.name);
        }
    }
}
