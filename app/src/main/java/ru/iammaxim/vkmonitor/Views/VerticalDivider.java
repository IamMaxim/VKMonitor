package ru.iammaxim.vkmonitor.Views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;

/**
 * Created by maxim on 12/5/16.
 */

public class VerticalDivider extends View {
    private Paint p;

    public VerticalDivider(Context context) {
        this(context, null);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int height = getMeasuredHeight();
        DisplayMetrics dm = getContext().getResources().getDisplayMetrics();
        setMeasuredDimension((int) dm.density, height); // 1 dp width
    }

    public VerticalDivider(Context context, AttributeSet attrs) {
        super(context, attrs);
        p = new Paint();
        p.setStyle(Paint.Style.FILL);
        p.setColor(0x1F000000);
    }

    public void setColor(int color) {
        p.setColor(color);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawRect(0, 0, canvas.getWidth(), canvas.getHeight(), p);
    }
}
