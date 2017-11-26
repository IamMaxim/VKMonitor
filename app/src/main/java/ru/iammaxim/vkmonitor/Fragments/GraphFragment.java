package ru.iammaxim.vkmonitor.Fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import org.json.JSONException;

import java.io.IOException;

import ru.iammaxim.graphlib.Graph;
import ru.iammaxim.vkmonitor.API.Messages.Messages;
import ru.iammaxim.vkmonitor.R;

/**
 * Created by maxim on 11/25/17.
 */

public class GraphFragment extends mFragment {
    public Graph graph;
    public Button dump_btn;
    public TextView tv;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_graph, null);

        graph = root.findViewById(R.id.graph);
        dump_btn = root.findViewById(R.id.dump);
        tv = root.findViewById(R.id.tv);

        if (Messages.isDumping())
            addDumpCallback();

        dump_btn.setOnClickListener(v -> {
            addDumpCallback();

            new Thread(() -> {
                try {
                    Messages.dump();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }).start();
        });

        return root;
    }

    private void addDumpCallback() {
        Messages.addDumpCallback((curDialog, totalDialogs, curMessage, totalMessages) -> {
            if (tv == null)
                return;

            if (getActivity() != null)
                getActivity().runOnUiThread(() -> {
                    tv.setText("Dialog " + (curDialog + 1) + " of " + totalDialogs + "\nLoaded " + curMessage + " of " + totalMessages + " messages");
                });

        });
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

}
