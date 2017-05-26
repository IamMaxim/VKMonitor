package ru.iammaxim.vkmonitor.Fragments;

import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import java.util.ArrayList;

import ru.iammaxim.vkmonitor.AccessTokenManager;
import ru.iammaxim.vkmonitor.App;
import ru.iammaxim.vkmonitor.R;

public class AccessTokenManagerFragment extends mFragment {
    private RecyclerView rv;
    private LinearLayoutManager layoutManager;
    private Adapter adapter = new Adapter();
    private int activeToken = AccessTokenManager.getActiveTokenIndex();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        //init tokens
        for (AccessTokenManager.Token t : AccessTokenManager.tokens) {
            AccessTokenManager.Token newToken = new AccessTokenManager.Token(t.name, t.token, t.isActive);
            adapter.elements.add(newToken);
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        //setup FAB
        FloatingActionButton fab = (FloatingActionButton) getActivity().findViewById(R.id.fab);
        fab.setOnClickListener(view -> {
            AccessTokenManager.Token token = new AccessTokenManager.Token("", "", false);
            adapter.elements.add(token);
            adapter.notifyItemInserted(adapter.getItemCount() - 1);
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        App.saveFilter();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_access_token_manager, container, false);
        Toolbar toolbar = (Toolbar) v.findViewById(R.id.toolbar);
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        rv = (RecyclerView) v.findViewById(R.id.rv);
        layoutManager = new LinearLayoutManager(getContext());
        rv.setLayoutManager(layoutManager);
        rv.setAdapter(adapter);
        rv.setItemAnimator(new DefaultItemAnimator());
        return v;
    }

    @Override
    public void onPause() {
        AccessTokenManager.tokens.clear();
        //add non-empty tokens
        for (AccessTokenManager.Token e : adapter.elements) {
            if (!e.name.isEmpty() && !e.token.isEmpty())
                AccessTokenManager.tokens.add(e);
        }
        if (activeToken < AccessTokenManager.tokens.size())
            AccessTokenManager.setActiveToken(activeToken);
        new Thread(AccessTokenManager::save).start();
        super.onPause();
    }

    public class Adapter extends RecyclerView.Adapter<Adapter.ViewHolder> {
        public ArrayList<AccessTokenManager.Token> elements = new ArrayList<>();

        private Button.OnClickListener setActiveListener = v -> {
            int position = v.getId();

            AccessTokenManager.Token token = elements.get(position);
            if (!token.isActive) {
                //deactivate all other tokens
                for (int i = 0; i < elements.size(); i++) {
                    boolean b = elements.get(i).isActive;
                    if (b && position != i) {
                        elements.get(i).isActive = false;
                        notifyItemChanged(i);
                    }
                }
                //activate selected token
                token.isActive = true;
                notifyItemChanged(position);
                activeToken = position;
            }
        };

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.element_access_token, parent, false));
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            final AccessTokenManager.Token token = elements.get(position);
            holder.bg.setImageDrawable(new ColorDrawable(token.isActive ? 0xff00ff00 : 0xffffffff));
            if (!token.isActive) {
                holder.setActive.setId(position);
                holder.setActive.setOnClickListener(setActiveListener);
            } else {
                holder.setActive.setActivated(false);
            }
            holder.name.setText(token.name);
            holder.name.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    token.name = s.toString();
                }

                @Override
                public void afterTextChanged(Editable s) {
                }
            });
            holder.token.setText(token.token);
            holder.token.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    token.token = s.toString();
                }

                @Override
                public void afterTextChanged(Editable s) {
                }
            });
        }

        @Override
        public int getItemCount() {
            return elements.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            public ImageView bg;
            public Button setActive;
            public EditText name, token;

            public ViewHolder(View v) {
                super(v);
                bg = (ImageView) v.findViewById(R.id.bg);
                setActive = (Button) v.findViewById(R.id.set_active);
                name = (EditText) v.findViewById(R.id.et_name);
                token = (EditText) v.findViewById(R.id.et_token);
            }
        }
    }
}
