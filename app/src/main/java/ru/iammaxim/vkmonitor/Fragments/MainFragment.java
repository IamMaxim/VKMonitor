package ru.iammaxim.vkmonitor.Fragments;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Messenger;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import ru.iammaxim.vkmonitor.AccessTokenManager;
import ru.iammaxim.vkmonitor.App;
import ru.iammaxim.vkmonitor.LongPollService;
import ru.iammaxim.vkmonitor.R;
import ru.iammaxim.vkmonitor.RequestGenerator.RequestGeneratorMain;

/**
 * Created by maxim on 5/14/17.
 */

public class MainFragment extends Fragment implements View.OnClickListener {
    private static boolean started = false;
    private View start, stop;
    private TextView state;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_main, container, false);
        Toolbar toolbar = (Toolbar) v.findViewById(R.id.toolbar);
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        state = (TextView) v.findViewById(R.id.state);
        start = v.findViewById(R.id.start);
        stop = v.findViewById(R.id.stop);

        setupState();
        setupButtons();
        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        setupState();
    }

    private void setupState() {
        AccessTokenManager.Token currentToken = AccessTokenManager.getActiveToken();
        if (currentToken != null) {
            state.setText(getString(R.string.current_token, currentToken.name));
            state.setBackgroundColor(getResources().getColor(R.color.colorStateBgOK));
        } else {
            state.setText(R.string.current_token_not_set);
            state.setBackgroundColor(getResources().getColor(R.color.colorStateBgError));
        }
    }

    private void setupButtons() {
        if (started) {
            start.setEnabled(false);
            start.setAlpha(0.5f);
            stop.setEnabled(true);
            stop.setAlpha(1);
        } else {
            start.setEnabled(true);
            start.setAlpha(1);
            stop.setEnabled(false);
            stop.setAlpha(0.5f);
        }
    }

    private void addFragment(Fragment fragment) {
        FragmentManager fm = getFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();
        transaction.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left, R.anim.slide_in_left, R.anim.slide_out_right);
        transaction.replace(R.id.container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.start:
                onClickStart();
                started = true;
                setupButtons();
                break;
            case R.id.open_log:
                addFragment(new LogFragment());
                break;
            case R.id.change_filter:
                addFragment(new ChangeFilterFragment());
                break;
            case R.id.stop:
                getContext().stopService(new Intent(getContext(), LongPollService.class));
                started = false;
                setupButtons();
                break;
            case R.id.manage_tokens:
                addFragment(new AccessTokenManagerFragment());
                break;
            case R.id.clear_filter:
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle("Clear filter");
                builder.setMessage("Are you sure you want to clear filter?");
                builder.setPositiveButton("OK", (dialog, which) -> {
                    App.clearFilter();
                    Toast.makeText(getContext(), "Filter cleared", Toast.LENGTH_SHORT).show();
                });
                builder.setNegativeButton("Cancel", null);
                builder.show();
                break;
            case R.id.request_generator:
                startActivity(new Intent(getContext(), RequestGeneratorMain.class));
                break;
            case R.id.clear_log:
                AlertDialog.Builder builder1 = new AlertDialog.Builder(getContext());
                builder1.setTitle("Clear log");
                builder1.setMessage("Are you sure you want to clear log?");
                builder1.setPositiveButton("OK", (dialog, which) -> {
                    App.clearLog();
                    Toast.makeText(getContext(), "Log cleared", Toast.LENGTH_SHORT).show();
                });
                builder1.setNegativeButton("Cancel", null);
                builder1.show();
                break;
            case R.id.open_dialogs:
                addFragment(new DialogsFragment());
                break;
        }
    }

    private void onClickStart() {
        getContext().startService(new Intent(getContext(), LongPollService.class).putExtra("MESSENGER", new Messenger(App.handler)));
    }
}
