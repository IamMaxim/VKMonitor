package ru.iammaxim.vkmonitor.Views.Attachments;

import android.content.Context;
import android.graphics.Typeface;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import ru.iammaxim.vkmonitor.API.Objects.Attachments.AttachmentDoc;
import ru.iammaxim.vkmonitor.API.Users.Users;
import ru.iammaxim.vkmonitor.App;
import ru.iammaxim.vkmonitor.R;

/**
 * Created by maxim on 9/17/2017.
 */

public class AttachmentDocumentView extends LinearLayout {
    public AttachmentDocumentView(Context context, AttachmentDoc doc) {
        super(context);
        setOrientation(HORIZONTAL);
        setPadding(dpToPx(2), dpToPx(4), dpToPx(2), dpToPx(4));

        ImageView docIconView = new ImageView(context);
        LinearLayout.LayoutParams docIconParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        docIconParams.gravity = Gravity.CENTER_VERTICAL;
        docIconView.setLayoutParams(docIconParams);
        docIconView.setImageResource(R.drawable.icon_doc);

        LinearLayout layout1 = new LinearLayout(context);
        layout1.setOrientation(VERTICAL);
        layout1.setPadding(dpToPx(6), 0, 0, 0);

        TextView title = new TextView(context);
        SpannableString titleSS = new SpannableString(doc.title);
        titleSS.setSpan(new StyleSpan(Typeface.BOLD), 0, titleSS.length(), 0);
        title.setText(titleSS);
        title.setTextSize(13);
        title.setTextColor(getResources().getColor(R.color.messageAttachmentDocTitleColor));

        TextView size = new TextView(context);
        size.setTextSize(12);
        size.setTextColor(getResources().getColor(R.color.messageAttachmentDocSubtitleColor));
        size.setText("Size: " + App.getFilesizeString(doc.size));

        TextView owner = new TextView(context);
        owner.setTextSize(12);
        owner.setTextColor(getResources().getColor(R.color.messageAttachmentDocSubtitleColor));
        owner.setText("Owner: " + Users.get(doc.owner_id).getTitle());

        TextView date = new TextView(context);
        date.setTextSize(12);
        date.setTextColor(getResources().getColor(R.color.messageAttachmentDocSubtitleColor));
        date.setText("Date: " + App.formatDateAndTime(doc.date * 1000));

        layout1.addView(title);
        layout1.addView(size);
        layout1.addView(date);
        layout1.addView(owner);

        addView(docIconView);
        addView(layout1);
    }

    private int dpToPx(float dp) {
        return (int) (getResources().getDisplayMetrics().density * dp);
    }
}
