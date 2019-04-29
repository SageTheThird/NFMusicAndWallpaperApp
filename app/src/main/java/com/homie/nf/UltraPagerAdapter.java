package com.homie.nf;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

public class UltraPagerAdapter extends PagerAdapter {

    Context context;
    private boolean isMultiScr;
    private String mData;


    public UltraPagerAdapter(boolean isMultiScr, String mData, Context context) {

        this.isMultiScr = isMultiScr;
        this.mData = mData;

    }

   @Override
    public int getCount() {
        return 1;
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object o) {
        return view == o;
    }


    @Override
    public Object instantiateItem(final ViewGroup container, final int position) {

        LayoutInflater mInflater = LayoutInflater.from(container.getContext());
        View item_view = mInflater.inflate(R.layout.fullscreen_image, container, false);

        final ImageView imageView = item_view.findViewById(R.id.fullscreen_imageView);

        Picasso
                .with(container.getContext())
                .load(mData)
                .fetch(new Callback() {

                    @Override
                    public void onSuccess() {
                        Picasso
                                .with(container.getContext())
                                .load(mData)
                                .networkPolicy(NetworkPolicy.OFFLINE)
                                .placeholder(R.drawable.ic_change_history_black_24dp)
                                .into(imageView);


                    }

                    @Override
                    public void onError() {

                    }
                });
        container.addView(item_view);
//        linearLayout.getLayoutParams().width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 180, container.getContext().getResources().getDisplayMetrics());
//        linearLayout.getLayoutParams().height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 400, container.getContext().getResources().getDisplayMetrics());
        return item_view;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((LinearLayout) object);
    }


}
