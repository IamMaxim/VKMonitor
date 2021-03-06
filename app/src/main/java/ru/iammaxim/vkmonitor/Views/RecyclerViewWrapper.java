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
    public Runnable onScrolledToTop;
    public Runnable onScrolledToBottom;

    public RecyclerViewWrapper(Context context) {
        this(context, null);
    }

    public RecyclerViewWrapper(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        layoutManager = new ImprovedLinearLayoutManager(context);
        setLayoutManager(layoutManager);
    }

    public void initOnScrolledToTopListener() {
        addOnScrollListener(new OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (onScrolledToTop != null && layoutManager.findFirstVisibleItemPosition() == 0)
                    onScrolledToTop.run();
            }
        });
    }

    public void initOnScrolledToBottomListener() {
        addOnScrollListener(new OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (onScrolledToBottom != null && layoutManager.findLastVisibleItemPosition() == adapter.getItemCount() - 1)
                    onScrolledToBottom.run();
            }
        });
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (nearTheBottom())
            scrollToBottom();
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

    public boolean nearTheBottom() {
        return layoutManager.findLastVisibleItemPosition() > adapter.getItemCount() - 3;
    }
}
