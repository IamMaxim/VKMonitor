package ru.iammaxim.vkmonitor.Views;

import android.content.Context;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;

import ru.iammaxim.vkmonitor.Fragments.LogFragment;
import ru.iammaxim.vkmonitor.R;

/**
 * Created by maxim on 15.09.2016.
 */
public class FABHideOnScrollBehavior extends FloatingActionButton.Behavior {
    LinearLayoutManager rvLayoutManager;
    LogFragment.Adapter rvAdapter;
    private LinearInterpolator linearInterpolator = new LinearInterpolator();

    public FABHideOnScrollBehavior(Context context, AttributeSet attrs) {
        super();
    }

    @Override
    public void onNestedScroll(CoordinatorLayout coordinatorLayout, FloatingActionButton child, View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed) {
        if (rvLayoutManager == null) {
            RecyclerView rv = coordinatorLayout.findViewById(R.id.rv);
            rvLayoutManager = ((LinearLayoutManager) rv.getLayoutManager());
            rvAdapter = (LogFragment.Adapter) rv.getAdapter();
        }

        if (dyConsumed > 0) {
            CoordinatorLayout.LayoutParams layoutParams = (CoordinatorLayout.LayoutParams) child.getLayoutParams();
            int fab_bottomMargin = layoutParams.bottomMargin;
            child.animate().translationY(child.getHeight() + fab_bottomMargin).setDuration(200).start();
        } else if (dyConsumed < 0 && rvLayoutManager.findLastVisibleItemPosition() + 3 < rvAdapter.getItemCount()) {
            child.animate().translationY(0).setDuration(200).start();
        }
    }

    @Override
    public boolean onStartNestedScroll(CoordinatorLayout coordinatorLayout, FloatingActionButton child, View directTargetChild, View target, int nestedScrollAxes) {
        return nestedScrollAxes == ViewCompat.SCROLL_AXIS_VERTICAL;
    }
}
