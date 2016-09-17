package ru.iammaxim.vkmonitor.Activities;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatRadioButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Map;

import ru.iammaxim.vkmonitor.AccessTokenManager;
import ru.iammaxim.vkmonitor.R;

public class AccessTokenManagerActivity extends AppCompatActivity {
    private RecyclerView rv;
    private LinearLayoutManager layoutManager;
    private Adapter adapter = new Adapter();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_access_token_manager);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        layoutManager = new LinearLayoutManager(this);
        rv.setLayoutManager(layoutManager);
        rv.setAdapter(adapter);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AccessTokenManager.Token token = new AccessTokenManager.Token("", "", false);
                AccessTokenManager.tokens.put(token.name, token);
                adapter.elements.add(new Element(token));
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        new Thread(new Runnable() {
            @Override
            public void run() {
                for (Map.Entry<String, AccessTokenManager.Token> entry : AccessTokenManager.tokens.entrySet()) {
                    adapter.elements.add(new Element(entry.getValue()));
                }
            }
        }).start();
    }

    public class Adapter extends RecyclerView.Adapter<Adapter.ViewHolder> {
        public ArrayList<Element> elements = new ArrayList<>();
        private CompoundButton.OnCheckedChangeListener radioListener = new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                AppCompatRadioButton v = (AppCompatRadioButton) buttonView;
                int id = v.getId();
                Element token = elements.get(id);
                token.isActive = isChecked;
                AccessTokenManager.tokens.get(token.name).isActive = isChecked;
                v.setChecked(isChecked);
            }
        };

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ViewHolder(View.inflate(parent.getContext(), R.layout.access_token_element, null));
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            Element element = elements.get(position);
            holder.rb.setChecked(element.isActive);
            holder.rb.setId(position);
            holder.rb.setOnCheckedChangeListener(radioListener);
            holder.name.setText(element.name);
            holder.name.setId(position);
            holder.name.setOnKeyListener(new View.OnKeyListener() {
                @Override
                public boolean onKey(View v, int keyCode, KeyEvent event) {
                    return false;
                }
            });
            holder.token.setText(element.token);
        }

        @Override
        public int getItemCount() {
            return elements.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            public AppCompatRadioButton rb;
            public EditText name, token;

            public ViewHolder(View v) {
                super(v);
                rb = (AppCompatRadioButton) v.findViewById(R.id.rb);
                name = (EditText) v.findViewById(R.id.et_name);
                token = (EditText) v.findViewById(R.id.et_token);
            }
        }
    }

    class Element {
        public boolean isActive = false;
        public String name, token;

        public Element(String name, String token, boolean isActive) {
            this.name = name;
            this.token = token;
            this.isActive = isActive;
        }

        public Element(AccessTokenManager.Token token) {
            this.name = token.name;
            this.token = token.token;
            this.isActive = token.isActive;
        }
    }
}
