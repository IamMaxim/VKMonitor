package ru.iammaxim.vkmonitor.Views;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;

import ru.iammaxim.vkmonitor.Activities.LogActivity;

/**
 * Created by maxim on 5/9/17.
 */

public class RecyclerViewWrapper extends RecyclerView {
    public Adapter adapter;
    public WrapLinearLayoutManager layoutManager;

    public RecyclerViewWrapper(Context context) {
        this(context, null);
    }

    public RecyclerViewWrapper(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        layoutManager = new WrapLinearLayoutManager(context);
        setLayoutManager(layoutManager);
    }

    public void smoothScrollToBottom() {
        try {
            layoutManager.smoothScrollToPosition(this, null, adapter.getItemCount() - 1);
        } catch (IllegalArgumentException e) {
        }
    }

    public void scrollToBottom() {
        layoutManager.scrollToPosition(adapter.getItemCount() - 1);
    }

    public void setAdapter(Adapter adapter) {
        this.adapter = adapter;
        super.setAdapter(adapter);
    }
}
