package ru.iammaxim.vkmonitor.Views;

import android.content.Context;
import android.view.MotionEvent;
import android.webkit.WebView;

/**
 * Created by maxim on 9/17/2017.
 */

public class MyWebView extends WebView {
    public MyWebView(Context context) {
        super(context);
    }

    @Override
    public boolean onCheckIsTextEditor() {
        return true;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_UP:
                if (!hasFocus())
                    requestFocus();
                break;
        }

        return super.onTouchEvent(ev);
    }
}