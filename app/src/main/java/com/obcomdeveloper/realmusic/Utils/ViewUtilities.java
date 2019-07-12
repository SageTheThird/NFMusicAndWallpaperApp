package com.obcomdeveloper.realmusic.Utils;


import android.view.View;
import android.view.ViewTreeObserver;

public final class ViewUtilities {
    public static void waitForLayout(final View view, final Runnable runnable) {
        view.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                //noinspection deprecation
                view.getViewTreeObserver().removeGlobalOnLayoutListener(this);

                runnable.run();
            }
        });
    }
}