package ru.iammaxim.vkmonitor.Views;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;

/**
 * Created by maxim on 5/9/17.
 */

public class RecyclerViewWrapper extends RecyclerView {
    public Adapter adapter;
    public ImprovedLinearLayoutManager layoutManager;

    public RecyclerViewWrapper(Context context) {
        this(context, null);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (onTheBottom())
            scrollToBottom();
    }

    public RecyclerViewWrapper(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        layoutManager = new ImprovedLinearLayoutManager(context);
        setLayoutManager(layoutManager);
    }

    public void smoothScrollToBottom() {
        try {
            layoutManager.smoothScrollToPosition(this, null, adapter.getItemCount() - 1);
        } catch (IllegalArgumentException e) {
        }
    }

    public void smoothScrollToTop() {
        try {
            layoutManager.scrollToPositionWithOffset(0, 0);
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

    public boolean onTheBottom() {
        return layoutManager.findLastVisibleItemPosition() > adapter.getItemCount() - 3;
    }
}
