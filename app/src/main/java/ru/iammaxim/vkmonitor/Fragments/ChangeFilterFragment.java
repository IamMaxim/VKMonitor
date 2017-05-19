package ru.iammaxim.vkmonitor.Fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;

import ru.iammaxim.vkmonitor.App;
import ru.iammaxim.vkmonitor.Objects.ObjectUser;
import ru.iammaxim.vkmonitor.R;
import ru.iammaxim.vkmonitor.UserDB;
import ru.iammaxim.vkmonitor.Users;
import ru.iammaxim.vkmonitor.Views.CircleTransformation;

/**
 * Created by maxim on 5/19/17.
 */

public class ChangeFilterFragment extends Fragment {
    private CircleTransformation circleTransformation = new CircleTransformation();
    private RecyclerView rv;
    private FilterAdapter adapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        adapter = new FilterAdapter();
        for (int id : UserDB.getUserIDs()) {
            adapter.elements.add(new UserElement(id));
        }
        Collections.sort(adapter.elements);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_change_filter, container, false);
        Toolbar toolbar = (Toolbar) v.findViewById(R.id.toolbar);
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        rv = (RecyclerView) v.findViewById(R.id.rv);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        rv.setAdapter(adapter);
        return v;
    }

    class FilterAdapter extends RecyclerView.Adapter<FilterAdapter.ViewHolder> {
        ArrayList<UserElement> elements = new ArrayList<>();
        private CompoundButton.OnCheckedChangeListener checkboxListener = new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                UserElement element = elements.get(buttonView.getId());
                if (!buttonView.isPressed()) return;
                if (isChecked) App.filter.add(element.user_id);
                else App.filter.remove((Integer) element.user_id);
                element.enabled = !element.enabled;
            }
        };

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ViewHolder(View.inflate(parent.getContext(), R.layout.filter_element, null));
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, final int position) {
            UserElement element = elements.get(position);
            holder.name.setText(element.name);
            holder.cb.setId(position);
            holder.cb.setChecked(element.enabled);
            holder.cb.setOnCheckedChangeListener(checkboxListener);
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

            ViewHolder(View itemView) {
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

        UserElement(int user_id) {
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
