package ru.iammaxim.vkmonitor.Views;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageButton;

import ru.iammaxim.vkmonitor.R;

/**
 * Created by maxim on 14.09.2016.
 */
public class ScrollDownButton extends ImageButton {
    public boolean isShown = true;

    private Runnable hideRunnable = new Runnable() {
        @Override
        public void run() {
            setVisibility(INVISIBLE);
        }
    }, showRunnable = new Runnable() {
        @Override
        public void run() {
            setVisibility(VISIBLE);
        }
    };

    public ScrollDownButton(Context context) {
        this(context, null);
    }

    public ScrollDownButton(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ScrollDownButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public ScrollDownButton(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
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
