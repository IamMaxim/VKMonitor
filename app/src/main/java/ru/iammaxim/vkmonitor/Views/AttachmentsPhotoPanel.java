package ru.iammaxim.vkmonitor.Views;

import android.content.Context;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;

import ru.iammaxim.vkmonitor.API.Objects.Attachments.AttachmentPhoto;

/**
 * Created by maxim on 10/17/17.
 */

public class AttachmentsPhotoPanel extends HorizontalScrollView {
    protected final int height = 64;
    protected ArrayList<AttachmentPhoto> photos = new ArrayList<>();
    protected ArrayList<ImageView> photoViews = new ArrayList<>();
    protected LinearLayout layout;

    public AttachmentsPhotoPanel(Context context, AttributeSet attrs) {
        super(context, attrs);

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        setLayoutParams(lp);
        setPadding(dpToPx(4), dpToPx(2), dpToPx(4), dpToPx(2));

        setMinimumHeight(dpToPx(height));
        layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.HORIZONTAL);
        addView(layout);
    }

    public AttachmentsPhotoPanel(Context context) {
        this(context, null);
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

        view.setMinimumHeight((int) (dm.density * height));
        view.setMaxHeight((int) (dm.density * height));

        view.setMinimumWidth((int) (dm.density * height));
        view.setMaxWidth((int) (dm.density * height));

        view.setAdjustViewBounds(true);
        if (photo.localFilepath != null)
            Picasso.with(getContext()).load(new File(photo.localFilepath)).fit().centerCrop().into(view);
        else
            Picasso.with(getContext()).load(photo.getBestURL()).fit().centerCrop().into(view);
        layout.addView(view);
        photoViews.add(view);
    }

    private int dpToPx(int dp) {
        DisplayMetrics displayMetrics = getContext().getResources().getDisplayMetrics();
        return Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
    }

    public void clear() {
        photos.clear();
        photoViews.clear();
        layout.removeAllViews();
    }
}
