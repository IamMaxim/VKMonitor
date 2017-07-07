package ru.iammaxim.vkmonitor.Views;

import android.content.Context;
import android.util.AttributeSet;

/**
 * Created by maxim on 14.09.2016.
 */
public class ScrollDownButton extends android.support.v7.widget.AppCompatImageButton {
    public boolean isShown = true;

    private Runnable
            hideRunnable = () -> setVisibility(INVISIBLE),
            showRunnable = () -> setVisibility(VISIBLE);

    public ScrollDownButton(Context context) {
        this(context, null);
    }

    public ScrollDownButton(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ScrollDownButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void hide() {
        isShown = false;
        animate().translationY(getHeight() * 2).setDuration(300).withEndAction(hideRunnable).withLayer().start();
    }

    public void show() {
        isShown = true;
        animate().translationY(0).setDuration(300).withStartAction(showRunnable).withLayer().start();
    }
}
