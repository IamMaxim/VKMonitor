package ru.iammaxim.vkmonitor.Views;

import android.content.Context;
import android.util.DisplayMetrics;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import ru.iammaxim.vkmonitor.API.Objects.Attachments.AttachmentPhoto;

/**
 * Created by maxim on 07.07.2017.
 */

public class ScrollablePhotoArray extends HorizontalScrollView {
    private ArrayList<AttachmentPhoto> photos = new ArrayList<>();
    private ArrayList<ImageView> photoViews = new ArrayList<>();
    private LinearLayout layout;
    private int height;

    public ScrollablePhotoArray(Context context) {
        super(context);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        setLayoutParams(lp);
        setPadding(0, dpToPx(2), 0, dpToPx(2));

        height = 204;
        setMinimumHeight(dpToPx(height));
        layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.HORIZONTAL);
        addView(layout);
    }

    public void add(AttachmentPhoto photo) {
        DisplayMetrics dm = getResources().getDisplayMetrics();

        if (photos.size() > 0) { // add a divider
            VerticalDivider divider = new VerticalDivider(getContext());

            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT);
            lp.setMargins((int) (dm.density * 4), 0, (int) (dm.density * 4), 0);
            divider.setLayoutParams(lp);

            layout.addView(divider);
        }

        if (photo.height < dm.density * (height - 4)) {
            height = photo.height + 4;
            setMinimumHeight(height);

            for (ImageView iv : photoViews) {
                iv.setMaxHeight((int) (dm.density * height));
            }
        }

        photos.add(photo);
        ImageView view = new ImageView(getContext());
        view.setMaxHeight((int) (dm.density * height));
        view.setAdjustViewBounds(true);
        Picasso.with(getContext()).load(photo.getBestURL()).into(view);
        layout.addView(view);
        photoViews.add(view);
    }

    private int dpToPx(int dp) {
        DisplayMetrics displayMetrics = getContext().getResources().getDisplayMetrics();
        return Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
    }
}
