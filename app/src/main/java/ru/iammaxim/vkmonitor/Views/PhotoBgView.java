package ru.iammaxim.vkmonitor.Views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by maxim on 11.09.2016.
 */
public class PhotoBgView extends View {
    Paint bgPaint = new Paint();

    public void setColor(int color) {
        bgPaint.setColor(color);
    }

    public int getColor() {
        return bgPaint.getColor();
    }

    public PhotoBgView(Context context, AttributeSet attrs) {
        super(context, attrs);
        bgPaint.setAntiAlias(true);
        bgPaint.setColor(0xff000000);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int w = canvas.getWidth(), h = canvas.getHeight();
        canvas.drawCircle(w/2, h/2, Math.min(w/2, h/2), bgPaint);
    }
}
