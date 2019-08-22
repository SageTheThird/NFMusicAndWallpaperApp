package com.obcomdeveloper.realmusic.Utils;

import java.lang.ref.WeakReference;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Build;
import android.util.LruCache;
import android.widget.ImageView;

public class BitmapUtils {
    public static LruCache<String, Bitmap> memoryCache;

    public static Bitmap decodeSampledBitmapFromResource(Resources res,
                                                         int resId, int reqWidth, int reqHeight, boolean agresiveMode) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(res, resId, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth,
                reqHeight, agresiveMode);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeResource(res, resId, options);
    }

    public static int calculateInSampleSize(BitmapFactory.Options options,
                                            int reqWidth, int reqHeight, boolean agressiveMode) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and
            // keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }

            if (agressiveMode) {
                // This offers some additional logic in case the image has a
                // strange
                // aspect ratio. For example, a panorama may have a much larger
                // width than height. In these cases the total pixels might
                // still
                // end up being too large to fit comfortably in memory, so we
                // should
                // be more aggressive with sample down the image (=larger
                // inSampleSize).
                long totalPixels = width * height / inSampleSize;

                // Anything more than 2x the requested pixels we'll sample down
                // further
                final long totalReqPixelsCap = reqWidth * reqHeight * 2;

                while (totalPixels > totalReqPixelsCap) {
                    inSampleSize *= 2;
                    totalPixels /= 2;
                }
            }
        }
        return inSampleSize;
    }

    private static void createCache() {
        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
        // Use 1/8th of the available memory for this memory cache.
        final int cacheSize = maxMemory / 8;
        createCache(cacheSize);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
    private static void createCache(int cacheSize) {
        memoryCache = new LruCache<String, Bitmap>(cacheSize) {
            @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                // The cache size will be measured in kilobytes rather than
                // number of items.
                if (Build.VERSION_CODES.HONEYCOMB_MR1 < Build.VERSION.SDK_INT) {
                    return bitmap.getByteCount() / 1024;
                } else {
                    return bitmap.getRowBytes() * bitmap.getHeight() / 1024;
                }
            }
        };
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
    public static void loadResourceAsyncWithCache(Context context,
                                                  ImageView imageView, int redId, int reqWidth, int reqHeight,
                                                  boolean agresiveMode) {
        if (memoryCache == null) {
            createCache();
        }
        Bitmap bitmap = memoryCache.get(redId + "");
        if (bitmap != null) {
            imageView.setImageBitmap(bitmap);
        } else {
            BitmapWorkerTask async = (BitmapWorkerTask) imageView.getTag();
            if (async != null) {
                async.cancel(true);
            }
            async = new BitmapWorkerTask(imageView, context, agresiveMode);
            async.execute(redId, reqWidth, reqHeight);
            imageView.setTag(async);
        }
    }

    static class BitmapWorkerTask extends AsyncTask<Integer, Void, Bitmap> {
        private final WeakReference<ImageView> imageViewReference;
        private final WeakReference<Context> contextReference;
        private int data = 0;
        private boolean agresiveMode;

        public BitmapWorkerTask(ImageView imageView, Context context,
                                boolean agresiveMode) {
            // Use a WeakReference to ensure the ImageView can be garbage
            // collected
            contextReference = new WeakReference<Context>(context);
            imageViewReference = new WeakReference<ImageView>(imageView);
            this.agresiveMode = agresiveMode;
        }

        // Decode image in background.
        @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
        @Override
        protected Bitmap doInBackground(Integer... params) {
            data = params[0];
            Bitmap bitmap = decodeSampledBitmapFromResource(contextReference
                            .get().getResources(), data, params[1], params[2],
                    agresiveMode);
            memoryCache.put(data + "", bitmap);
            return bitmap;
        }

        // Once complete, see if ImageView is still around and set bitmap.
        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (imageViewReference != null && bitmap != null) {
                final ImageView imageView = imageViewReference.get();
                if (imageView != null) {
                    imageView.setImageBitmap(bitmap);
                }
            }
        }
    }

}