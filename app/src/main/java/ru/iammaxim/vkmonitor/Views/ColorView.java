package ru.iammaxim.vkmonitor.Views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by maxim on 12/5/16.
 */

public class ColorView extends View {
    private Paint p;

    public void setColor(int color) {
        p.setColor(color);
    }

    public int getColor() {
        return p.getColor();
    }

    public ColorView(Context context, AttributeSet attrs) {
        super(context, attrs);
        p = new Paint();
        p.setStyle(Paint.Style.FILL);
        p.setColor(0xFF000000);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int parentWidth = MeasureSpec.getSize(widthMeasureSpec);
        int parentHeight = MeasureSpec.getSize(heightMeasureSpec);
        this.setMeasuredDimension(parentWidth, parentHeight);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawRect(0, 0, canvas.getWidth(), canvas.getHeight(), p);
    }

    public ColorView(Context context) {
        this(context, null);
    }
}
