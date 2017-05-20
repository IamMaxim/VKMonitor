package ru.iammaxim.vkmonitor.Views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import ru.iammaxim.vkmonitor.R;

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
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.PhotoBgView, 0, 0);
        try {
            bgPaint.setColor(a.getColor(R.styleable.PhotoBgView_color, 0xff000000));
        } finally {
            a.recycle();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int w = canvas.getWidth(), h = canvas.getHeight();
        canvas.drawCircle(w/2, h/2, Math.min(w/2, h/2), bgPaint);
    }
}
