package ru.iammaxim.vkmonitor.Fragments;

import android.annotation.SuppressLint;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import org.json.JSONObject;

import java.util.ArrayList;

import ru.iammaxim.vkmonitor.AccessTokenManager;
import ru.iammaxim.vkmonitor.App;
import ru.iammaxim.vkmonitor.Net;
import ru.iammaxim.vkmonitor.R;
import ru.iammaxim.vkmonitor.Views.MyWebView;

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

    // needed to be outside of method to access from anonymous class
    AlertDialog dialog;

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        //setup FAB
        FloatingActionButton fab = getActivity().findViewById(R.id.fab);
        fab.setOnClickListener(view -> {
            new AlertDialog.Builder(getContext())
                    .setItems(new String[]{"Auth via VK", "Enter token manually"}, (di, i) -> {
                        if (i == 0) { // auth via VK
                            MyWebView wv = new MyWebView(getContext());
                            wv.loadUrl("https://oauth.vk.com/authorize?client_id=5129616&display=page&scope=friends,messages,offline,status,wall,notifications,photos,audio,groups&response_type=token&v=5.68");
                            wv.setWebViewClient(new WebViewClient() {
                                @Override
                                public boolean shouldOverrideUrlLoading(WebView view, String url) {
                                    if (url.contains("access_token=")) {
                                        String tokenStr = url.substring(url.indexOf("access_token=") + 13, url.indexOf("&", url.indexOf("access_token=") + 13));
                                        AccessTokenManager.Token token = new AccessTokenManager.Token("Name", tokenStr, false);
                                        adapter.elements.add(token);
                                        adapter.notifyItemInserted(adapter.getItemCount() - 1);
                                        dialog.dismiss();
                                    } else {
                                        wv.loadUrl(url);
                                    }
                                    return true;
                                }
                            });
                            (dialog = new AlertDialog.Builder(getContext()).setView(wv).create()).show();
                        } else { // enter token manually
                            AccessTokenManager.Token token = new AccessTokenManager.Token("", "", false);
                            adapter.elements.add(token);
                            adapter.notifyItemInserted(adapter.getItemCount() - 1);
                            di.dismiss();
                        }
                    }).create().show();
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
        Toolbar toolbar = v.findViewById(R.id.toolbar);
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        rv = v.findViewById(R.id.rv);
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

        private Button.OnClickListener deleteListener = v -> {
            new AlertDialog.Builder(getContext()).setPositiveButton("OK", (di, v1) -> {
                int position = v.getId();

                if (AccessTokenManager.getActiveToken() == AccessTokenManager.tokens.remove(position)) {
                    if (AccessTokenManager.tokens.size() > 0)
                        AccessTokenManager.setActiveToken(0);
                    else
                        AccessTokenManager.removeActiveToken();
                }
                adapter.elements.remove(position);
                adapter.notifyItemRemoved(position);
                AccessTokenManager.save();
            })
                    .setNegativeButton("Cancel", null)
                    .setMessage("Delete this token?").create().show();
        };

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.element_access_token, parent, false));
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            final AccessTokenManager.Token token = elements.get(position);
            holder.bg.setImageDrawable(new ColorDrawable(token.isActive ? 0xff00ff00 : 0xffffffff));

            holder.setActive.setId(position);
            holder.setActive.setOnClickListener(setActiveListener);
            if (!token.isActive) {
                holder.setActive.setActivated(true);
            } else {
                holder.setActive.setActivated(false);
            }

            holder.delete.setId(position);
            holder.delete.setOnClickListener(deleteListener);

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
            public Button setActive, delete;
            public EditText name, token;

            public ViewHolder(View v) {
                super(v);
                bg = v.findViewById(R.id.bg);
                setActive = v.findViewById(R.id.set_active);
                delete = v.findViewById(R.id.delete);
                name = v.findViewById(R.id.et_name);
                token = v.findViewById(R.id.et_token);
            }
        }
    }
}
