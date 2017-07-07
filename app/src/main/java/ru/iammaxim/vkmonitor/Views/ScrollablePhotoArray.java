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
    private LinearLayout layout;

    public ScrollablePhotoArray(Context context) {
        super(context);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        setLayoutParams(lp);

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

        photos.add(photo);
        ImageView view = new ImageView(getContext());
        view.setMaxHeight((int) (dm.density * 200));
        view.setAdjustViewBounds(true);
        Picasso.with(getContext()).load(photo.getBestURL()).into(view);
        layout.addView(view);
    }
}
