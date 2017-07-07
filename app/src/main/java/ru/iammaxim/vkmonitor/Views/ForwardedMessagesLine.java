package ru.iammaxim.vkmonitor.Views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;

/**
 * Created by maxim on 07.07.2017.
 */

public class ForwardedMessagesLine extends View {
    private Paint p;

    public ForwardedMessagesLine(Context context) {
        this(context, null);
    }

    public ForwardedMessagesLine(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        p = new Paint();
        p.setStyle(Paint.Style.FILL);
        p.setColor(0xFF6B9ECE);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int height = getMeasuredHeight();
        DisplayMetrics dm = getContext().getResources().getDisplayMetrics();
        setMeasuredDimension((int) (2 * dm.density), height); // 2 dp width
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawRect(0, 0, canvas.getWidth(), canvas.getHeight(), p);
    }
}
