/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.displayingbitmaps.util;

import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.media.ThumbnailUtils;
import android.provider.MediaStore;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;

import com.example.android.displayingbitmaps.BuildConfig;
import com.example.android.displayingbitmaps.R;

import java.lang.ref.WeakReference;
import java.util.Locale;

/**
 * This class wraps up completing some arbitrary long running work when loading a bitmap to an
 * ImageView. It handles things like using a memory and disk cache, running the work in a background
 * thread and setting a placeholder image.
 */
public class ImageWorker {
    private static final String TAG = "ImageWorker";
    private static final int FADE_IN_TIME = 200;

    private ImageCache mImageCache;
    private ImageCache.ImageCacheParams mImageCacheParams;
    private Bitmap mLoadingBitmap;
    private boolean mFadeInBitmap = true;
    private boolean mExitTasksEarly = false;
    protected boolean mPauseWork = false;
    private final Object mPauseWorkLock = new Object();

    protected Resources mResources;

    private static final int MESSAGE_CLEAR = 0;
    private static final int MESSAGE_INIT_DISK_CACHE = 1;
    private static final int MESSAGE_FLUSH = 2;
    private static final int MESSAGE_CLOSE = 3;


    private ContentResolver mContentResolver;
    private String[] mArrayPath;
    private String[] mFolderArrayPath;
    private DisplayMetrics mMetrics;
    private View mnoMediaView;
    private ImageView m_IconView;
    private Boolean[] misVideoSupported;

    private int mCountImage = 0;
    private int mCountVideo = 0 ;
    private long[] mArrayId = null;
    private long[] mFolderArrayId;
    private boolean mFolder = false;

    private long mID;
    private boolean is_Image;

    private String m_ArrayPath;

    private static final String[] IMAGE_EXTENSIONS = {".jpg", ".png", ".jpeg"};
    private static final String[] VIDEO_EXTENSIONS = {".mp4", ".3gp", ".3gpp", ".avi", ".mov", ".wmv"};


    protected ImageWorker(Context context) {
        mResources = context.getResources();
    }

    public ImageWorker(Context context, ContentResolver contentResolver, int nCountImage, int nCountVideo, long[] arrayId, String[] arrayPath,
                      DisplayMetrics metrics) {


        mResources = context.getResources();
        mContentResolver = contentResolver;
        mCountImage = nCountImage;
        mCountVideo = nCountVideo;
        mArrayPath = arrayPath;
        mArrayId = arrayId;
        mFolder = false;
        mMetrics = metrics;
        mLoadingBitmap = BitmapFactory.decodeResource(mResources, R.drawable.empty_photo);
        Log.d(TAG, "Image Worker Constructor is called " + "Images Count :" + mCountImage + " Video Count" + mCountVideo);

    }

    /**
     * Load an image specified by the data parameter into an ImageView (override
     * {@link ImageWorker#processBitmap(com.example.android.displayingbitmaps.util.ImageWorker.AsyncParam)}  to define the processing logic). A memory and
     * disk cache will be used if an {@link ImageCache} has been added using
     * {@link ImageWorker#addImageCache(android.support.v4.app.FragmentManager, ImageCache.ImageCacheParams)}. If the
     * image is found in the memory cache, it is set immediately, otherwise an {@link AsyncTask}
     * will be created to asynchronously load the bitmap.
     *
     * @param imageView The ImageView to bind the downloaded image to.
     */
/*    public void loadImage(Object data, ImageView imageView) {
        if (data == null) {
            return;
        }

        BitmapDrawable value = null;

        if (mImageCache != null) {
            value = mImageCache.getBitmapFromMemCache(String.valueOf(data));
        }

        if (value != null) {
            // Bitmap found in memory cache
            imageView.setImageDrawable(value);
        } else if (cancelPotentialWork(data, imageView)) {
            //BEGIN_INCLUDE(execute_background_task)
            final BitmapWorkerTask task = new BitmapWorkerTask(data, imageView);
            final AsyncDrawable asyncDrawable =
                    new AsyncDrawable(mResources, mLoadingBitmap, task);
            imageView.setImageDrawable(asyncDrawable);

            // NOTE: This uses a custom version of AsyncTask that has been pulled from the
            // framework and slightly modified. Refer to the docs at the top of the class
            // for more info on what was changed.

            AsyncParam param = new AsyncParam(position, id, arrayPath, noMediaView);

            task.executeOnExecutor(AsyncTask.DUAL_THREAD_EXECUTOR);
            //END_INCLUDE(execute_background_task)
        }
    }*/

    public void loadImage(Integer position, ImageView imageView, Long id, String arrayPath, View noMediaView, boolean isImage)
    {
        mID = id;
        m_ArrayPath = arrayPath;
        //	misVideoSupported = isVideoSupported;
        // check position value
        mnoMediaView = noMediaView;
        is_Image = isImage;
        Log.v(TAG, " Video/Image Received from NexVMediaBrowser is : " + is_Image + " Path : " + m_ArrayPath + " id: " + mID);
        boolean isSupported = false;
        Boolean misImage = false;

        if( position < 0)
        {
            return ;
        }

        Bitmap bitmap = null;
        Boolean isVideoSupport;
        ImageCache.CacheEntry mCacheEntry;


        if (mImageCache != null)
        {
            mCacheEntry = mImageCache.getBitmapFromMemCache(id.toString());
            if(mCacheEntry != null) {
                bitmap = mCacheEntry.mBitmap;
                Log.v(TAG, "loadImage  : getting Image from mImageCache" );
            }
        }

        if (bitmap != null)
        {

            // Bitmap found in memory cache
            imageView.setImageBitmap(bitmap);

            //      Log.v(TAG, "loadImage :  Bitmap found in memory cache " + bitmap );

        }
        else if (cancelPotentialWork(position.toString(), imageView))
        {
            final BitmapWorkerTask task = new BitmapWorkerTask(imageView);
            final AsyncDrawable asyncDrawable = new AsyncDrawable(mResources, mLoadingBitmap, task);
            imageView.setImageDrawable(asyncDrawable);

            // NOTE: This uses a custom version of AsyncTask that has been pulled from the
            // framework and slightly modified. Refer to the docs at the top of the class
            // for more info on what was changed.
            AsyncParam param = new AsyncParam(position, id, arrayPath);

            task.executeOnExecutor(AsyncTask.DUAL_THREAD_EXECUTOR, param);


        }



    }

    /**
     * Set placeholder bitmap that shows when the the background thread is running.
     *
     * @param bitmap
     */
    public void setLoadingImage(Bitmap bitmap) {
        mLoadingBitmap = bitmap;
    }

    /**
     * Set placeholder bitmap that shows when the the background thread is running.
     *
     * @param resId
     */
    public void setLoadingImage(int resId) {
        mLoadingBitmap = BitmapFactory.decodeResource(mResources, resId);
    }

    /**
     * Adds an {@link ImageCache} to this {@link ImageWorker} to handle disk and memory bitmap
     * caching.
     * @param fragmentManager
     * @param cacheParams The cache parameters to use for the image cache.
     */
    public void addImageCache(FragmentManager fragmentManager,
            ImageCache.ImageCacheParams cacheParams) {
        mImageCacheParams = cacheParams;
        mImageCache = ImageCache.findOrCreateCache(fragmentManager, mImageCacheParams);
        new CacheAsyncTask().execute(MESSAGE_INIT_DISK_CACHE);
    }

    /**
     * Adds an {@link ImageCache} to this {@link ImageWorker} to handle disk and memory bitmap
     * caching.
     * @param activity
     * @param diskCacheDirectoryName See
     * {@link ImageCache.ImageCacheParams#ImageCacheParams(android.content.Context, String)}.
     */
    public void addImageCache(FragmentActivity activity, String diskCacheDirectoryName) {
        mImageCacheParams = new ImageCache.ImageCacheParams(activity, diskCacheDirectoryName);
        mImageCache = ImageCache.findOrCreateCache(activity.getSupportFragmentManager(), mImageCacheParams);
        new CacheAsyncTask().execute(MESSAGE_INIT_DISK_CACHE);
    }

    /**
     * If set to true, the image will fade-in once it has been loaded by the background thread.
     */
    public void setImageFadeIn(boolean fadeIn) {
        mFadeInBitmap = fadeIn;
    }

    public void setExitTasksEarly(boolean exitTasksEarly) {
        mExitTasksEarly = exitTasksEarly;
        setPauseWork(false);
    }

    /**
     * Subclasses should override this to define any processing or work that must happen to produce
     * the final bitmap. This will be executed in a background thread and be long running. For
     * example, you could resize a large bitmap here, or pull down an image from the network.
     *
     * @param params The data to identify which image to process, as provided by
     *            {@link ImageWorker#loadImage(Integer, android.widget.ImageView, Long, String, android.view.View, boolean)}
     * @return The processed bitmap
     */
    private Bitmap processBitmap(AsyncParam params) {
        int position = params.mPosition;
        Long ImageId = params.mImageId;
        String imagePath = params.mImagePath;

/*       if(isImage(imagePath)) {
           BitmapFactory.Options opt = new BitmapFactory.Options();
           opt.inJustDecodeBounds = true;
           BitmapFactory.decodeFile(imagePath, opt);
           opt.inJustDecodeBounds = false;

           int thumbHeight = 120;
           int thumbWidth = Math.min((int)(200),(int)((float)opt.outWidth/(float)opt.outHeight*(float)thumbHeight));

           if( opt.outHeight > thumbHeight*2 && opt.outWidth > thumbWidth*2 ) {
               opt.inSampleSize = Math.min(opt.outHeight/thumbHeight, opt.outWidth/thumbWidth);
           }
           Log.d(TAG, "   bounds decoded : width,height=" + opt.outWidth + "," + opt.outHeight + "; target w,h=" + thumbWidth + "," + thumbHeight + "; sampleSize=" + opt.inSampleSize);
           Bitmap bm = BitmapFactory.decodeFile(imagePath, opt);
           if( bm != null ) {
               Bitmap scaledBm = Bitmap.createScaledBitmap(bm, thumbWidth, thumbHeight, true);
               if(scaledBm != bm)
                   bm.recycle();
               return scaledBm;
           }
           return null;

       } else if(isVideo(imagePath)) {



       }*/

        Bitmap bm = null;
        final BitmapFactory.Options options = new BitmapFactory.Options();
        if(position < mCountImage ){

            options.inSampleSize = calculateInSampleSize(options, dipToPx(106), dipToPx(68));

                bm = MediaStore.Images.Thumbnails.getThumbnail(mContentResolver, ImageId, MediaStore.Images.Thumbnails.MINI_KIND, options);
                if(bm == null)
                    bm = ThumbnailUtils.extractThumbnail(BitmapFactory.decodeFile(imagePath, options), dipToPx(106), dipToPx(68));

            if(bm == null) {
                BitmapFactory.Options opt = new BitmapFactory.Options();
                opt.inJustDecodeBounds = true;
                BitmapFactory.decodeFile(imagePath, opt);
                opt.inJustDecodeBounds = false;

                int thumbHeight = 120;
                int thumbWidth = Math.min((int)(200),(int)((float)opt.outWidth/(float)opt.outHeight*(float)thumbHeight));

                if( opt.outHeight > thumbHeight*2 && opt.outWidth > thumbWidth*2 ) {
                    opt.inSampleSize = Math.min(opt.outHeight/thumbHeight, opt.outWidth/thumbWidth);
                }
                Log.d(TAG, "   bounds decoded : width,height=" + opt.outWidth + "," + opt.outHeight + "; target w,h=" + thumbWidth + "," + thumbHeight + "; sampleSize=" + opt.inSampleSize);
                Bitmap bitmap = BitmapFactory.decodeFile(imagePath, opt);
                if( bitmap != null ) {
                    Bitmap scaledBm = Bitmap.createScaledBitmap(bm, thumbWidth, thumbHeight, true);
                    if(scaledBm != bitmap)
                        bitmap.recycle();
                    return scaledBm;
                }

            }

        }
        else if (position == mCountImage || position > mCountImage && position<mCountImage+mCountVideo )
        {

            options.inSampleSize = calculateInSampleSize(options, dipToPx(106), dipToPx(68));

            bm = MediaStore.Video.Thumbnails.getThumbnail(mContentResolver, ImageId , MediaStore.Video.Thumbnails.MINI_KIND, options);

            if(bm == null){

                bm = ThumbnailUtils.createVideoThumbnail(imagePath, MediaStore.Video.Thumbnails.MICRO_KIND );
                if(bm == null) {
                    bm = ThumbnailUtils.createVideoThumbnail(imagePath, MediaStore.Video.Thumbnails.MINI_KIND);
                }
                if (bm == null) {
                }

            } else {

                Log.d(TAG, "=========== bm/Video is not NULL and Thumbnail is created!!! ");
            }


        }

        return bm;
    }

    /**
     * @return The {@link ImageCache} object currently being used by this ImageWorker.
     */
    protected ImageCache getImageCache() {
        return mImageCache;
    }

    /**
     * Cancels any pending work attached to the provided ImageView.
     * @param imageView
     */
    public static void cancelWork(ImageView imageView) {
        final BitmapWorkerTask bitmapWorkerTask = getBitmapWorkerTask(imageView);
        if (bitmapWorkerTask != null) {
            bitmapWorkerTask.cancel(true);
                final Object bitmapData = bitmapWorkerTask.imageId;
                Log.d(TAG, "cancelWork - cancelled work for " + bitmapData);
        }
    }

    /**
     * Returns true if the current work has been canceled or if there was no work in
     * progress on this image view.
     * Returns false if the work in progress deals with the same data. The work is not
     * stopped in that case.
     */
    public static boolean cancelPotentialWork(String position, ImageView imageView) {
        //BEGIN_INCLUDE(cancel_potential_work)
        final BitmapWorkerTask bitmapWorkerTask = getBitmapWorkerTask(imageView);

        if (bitmapWorkerTask != null) {
            final Long bitmapData = bitmapWorkerTask.imageId;
            if (bitmapData == null || !bitmapData.equals(position)) {
                bitmapWorkerTask.cancel(true);
                if (BuildConfig.DEBUG) {
                    Log.d(TAG, "cancelPotentialWork - cancelled work for " + position);
                }
            } else {
                // The same work is already in progress.
                return false;
            }
        }
        return true;
        //END_INCLUDE(cancel_potential_work)
    }

    /**
     * @param imageView Any imageView
     * @return Retrieve the currently active work task (if any) associated with this imageView.
     * null if there is no such task.
     */
    private static BitmapWorkerTask getBitmapWorkerTask(ImageView imageView) {
        if (imageView != null) {
            final Drawable drawable = imageView.getDrawable();
            if (drawable instanceof AsyncDrawable) {
                final AsyncDrawable asyncDrawable = (AsyncDrawable) drawable;
                return asyncDrawable.getBitmapWorkerTask();
            }
        }
        return null;
    }

    /**
     * The actual AsyncTask that will asynchronously process the image.
     */
    private class BitmapWorkerTask extends AsyncTask<AsyncParam, Void, BitmapDrawable> {

        private Long imageId;
        private String imagePath;
        private int position;
        private boolean supported;
        private final WeakReference<ImageView> imageViewReference;

        public BitmapWorkerTask(ImageView imageView) {
            imageViewReference = new WeakReference<ImageView>(imageView);
        }

        /**
         * Background processing.
         */
        @Override
        protected BitmapDrawable doInBackground(AsyncParam... params) {
            //BEGIN_INCLUDE(load_bitmap_in_background)
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "doInBackground - starting work");
            }


            if (BuildConfig.DEBUG) {
                Log.d(TAG, "doInBackground - starting work");
            }
            Bitmap bitmap = null;
            AsyncParam aparam = (AsyncParam)params[0];
            imageId = aparam.mImageId;
            imagePath = aparam.mImagePath;
            position = aparam.mPosition;
            ImageCache.CacheEntry mcacheEntry2;

            BitmapDrawable drawable = null;

            // Wait here if work is paused and the task is not cancelled
            synchronized (mPauseWorkLock) {
                while (mPauseWork && !isCancelled()) {
                    try {
                        mPauseWorkLock.wait();
                    } catch (InterruptedException e) {}
                }
            }

            // If the image cache is available and this task has not been cancelled by another
            // thread and the ImageView that was originally bound to this task is still bound back
            // to this task and our "exit early" flag is not set then try and fetch the bitmap from
            // the cache
            if (mImageCache != null && !isCancelled() && getAttachedImageView() != null
                    && !mExitTasksEarly) {
//                bitmap = mImageCache.getBitmapFromDiskCache(dataString);
                mcacheEntry2 = mImageCache.getBitmapFromDiskCache(imageId.toString());
                if(mcacheEntry2 != null)
                    bitmap = mcacheEntry2.mBitmap;
                Log.d(TAG, " getBitmapfromDiskCache :" + bitmap);
            }


            // If the bitmap was not found in the cache and this task has not been cancelled by
            // another thread and the ImageView that was originally bound to this task is still
            // bound back to this task and our "exit early" flag is not set, then call the main
            // process method (as implemented by a subclass)
            if (bitmap == null && !isCancelled() && getAttachedImageView() != null
                    && !mExitTasksEarly) {
                bitmap = processBitmap(params[0]);
            }

            if(bitmap != null && mImageCache != null) {
                if(imagePath.toLowerCase().endsWith("jpg") || imagePath.toLowerCase().endsWith("jpeg") || imagePath.toLowerCase().endsWith("png") ||
                        imagePath.toLowerCase().endsWith("gif")) {

                    is_Image = true;


                }else {
                    is_Image = false;

                }
            }

            // If the bitmap was processed and the image cache is available, then add the processed
            // bitmap to the cache for future use. Note we don't check if the task was cancelled
            // here, if it was, and the thread is still running, we may as well add the processed
            // bitmap to our cache as it might be used again in the future
            if (bitmap != null) {
                if (Utils.hasHoneycomb()) {
                    // Running on Honeycomb or newer, so wrap in a standard BitmapDrawable
                    drawable = new BitmapDrawable(mResources, bitmap);
                } else {
                    // Running on Gingerbread or older, so wrap in a RecyclingBitmapDrawable
                    // which will recycle automagically
                    drawable = new RecyclingBitmapDrawable(mResources, bitmap);
                }

                if (mImageCache != null) {
                    mImageCache.addBitmapToCache(imageId.toString(), bitmap);
                }
            }

            if (BuildConfig.DEBUG) {
                Log.d(TAG, "doInBackground - finished work");
            }

            return drawable;
            //END_INCLUDE(load_bitmap_in_background)
        }

        /**
         * Once the image is processed, associates it to the imageView
         */
        @Override
        protected void onPostExecute(BitmapDrawable value) {
            //BEGIN_INCLUDE(complete_background_work)
            // if cancel was called on this task or the "exit early" flag is set then we're done
            if (isCancelled() || mExitTasksEarly) {
                value = null;
            }

            final ImageView imageView = getAttachedImageView();
            if (value != null && imageView != null) {
                if (BuildConfig.DEBUG) {
                    Log.d(TAG, "onPostExecute - setting bitmap");
                }
                setImageDrawable(imageView, value);
            }
            //END_INCLUDE(complete_background_work)
        }

        @Override
        protected void onCancelled(BitmapDrawable value) {
            super.onCancelled(value);
            synchronized (mPauseWorkLock) {
                mPauseWorkLock.notifyAll();
            }
        }

        /**
         * Returns the ImageView associated with this task as long as the ImageView's task still
         * points to this task as well. Returns null otherwise.
         */
        private ImageView getAttachedImageView() {
            final ImageView imageView = imageViewReference.get();
            final BitmapWorkerTask bitmapWorkerTask = getBitmapWorkerTask(imageView);

            if (this == bitmapWorkerTask) {
                return imageView;
            }

            return null;
        }
    }

    /**
     * A custom Drawable that will be attached to the imageView while the work is in progress.
     * Contains a reference to the actual worker task, so that it can be stopped if a new binding is
     * required, and makes sure that only the last started worker process can bind its result,
     * independently of the finish order.
     */
    private static class AsyncDrawable extends BitmapDrawable {
        private final WeakReference<BitmapWorkerTask> bitmapWorkerTaskReference;

        public AsyncDrawable(Resources res, Bitmap bitmap, BitmapWorkerTask bitmapWorkerTask) {
            super(res, bitmap);
            bitmapWorkerTaskReference =
                    new WeakReference<BitmapWorkerTask>(bitmapWorkerTask);
        }

        public BitmapWorkerTask getBitmapWorkerTask() {
            return bitmapWorkerTaskReference.get();
        }
    }

    /**
     * Called when the processing is complete and the final drawable should be
     * set on the ImageView.
     *
     * @param imageView
     * @param drawable
     */
    private void setImageDrawable(ImageView imageView, Drawable drawable) {
        if (mFadeInBitmap) {
            // Transition drawable with a transparent drawable and the final drawable
            final TransitionDrawable td =
                    new TransitionDrawable(new Drawable[] {
                            new ColorDrawable(android.R.color.transparent),
                            drawable
                    });
            // Set background to loading bitmap
            imageView.setBackgroundDrawable(
                    new BitmapDrawable(mResources, mLoadingBitmap));

            imageView.setImageDrawable(td);
            td.startTransition(FADE_IN_TIME);
        } else {
            imageView.setImageDrawable(drawable);
        }
    }

    /**
     * Pause any ongoing background work. This can be used as a temporary
     * measure to improve performance. For example background work could
     * be paused when a ListView or GridView is being scrolled using a
     * {@link android.widget.AbsListView.OnScrollListener} to keep
     * scrolling smooth.
     * <p>
     * If work is paused, be sure setPauseWork(false) is called again
     * before your fragment or activity is destroyed (for example during
     * {@link android.app.Activity#onPause()}), or there is a risk the
     * background thread will never finish.
     */
    public void setPauseWork(boolean pauseWork) {
        synchronized (mPauseWorkLock) {
            mPauseWork = pauseWork;
            if (!mPauseWork) {
                mPauseWorkLock.notifyAll();
            }
        }
    }

    protected class CacheAsyncTask extends AsyncTask<Object, Void, Void> {

        @Override
        protected Void doInBackground(Object... params) {
            switch ((Integer)params[0]) {
                case MESSAGE_CLEAR:
                    clearCacheInternal();
                    break;
                case MESSAGE_INIT_DISK_CACHE:
                    initDiskCacheInternal();
                    break;
                case MESSAGE_FLUSH:
                    flushCacheInternal();
                    break;
                case MESSAGE_CLOSE:
                    closeCacheInternal();
                    break;
            }
            return null;
        }
    }

    protected void initDiskCacheInternal() {
        if (mImageCache != null) {
            mImageCache.initDiskCache();
        }
    }

    protected void clearCacheInternal() {
        if (mImageCache != null) {
            mImageCache.clearCache();
        }
    }

    protected void flushCacheInternal() {
        if (mImageCache != null) {
            mImageCache.flush();
        }
    }

    protected void closeCacheInternal() {
        if (mImageCache != null) {
            mImageCache.close();
            mImageCache = null;
        }
    }

    public void clearCache() {
        new CacheAsyncTask().execute(MESSAGE_CLEAR);
    }

    public void flushCache() {
        new CacheAsyncTask().execute(MESSAGE_FLUSH);
    }

    public void closeCache() {
        new CacheAsyncTask().execute(MESSAGE_CLOSE);
    }


    // Additional methods

    public class AsyncParam
    {
        public int mPosition = 0;
        public Long mImageId = 0L;
        public String mImagePath = "";

        public AsyncParam(int position, Long id, String path)
        {
            mPosition = position;
            mImageId = id;
            mImagePath = path;
        }

    }

    private static boolean isImage(String fileName) {
        String name = fileName.toLowerCase(Locale.US);

        for (String s: IMAGE_EXTENSIONS) {
            if(name.endsWith(s))
                return true;
        }
        return false;

    }
    private static boolean isVideo(String fileName) {
        String name = fileName.toLowerCase(Locale.US);

        for (String s: VIDEO_EXTENSIONS) {
            if(name.endsWith(s))
                return true;
        }
        return false;

    }


    private static int calculateInSampleSize(BitmapFactory.Options options,
                                             int reqWidth, int reqHeight) {

        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            // Calculate ratios of height and width to requested height and
            // width
            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);

            // Choose the smallest ratio as inSampleSize value, this will
            // guarantee
            // a final image with both dimensions larger than or equal to the
            // requested height and width.
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }

        return inSampleSize;

    }

    private int dipToPx( int sp ) {


        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, sp, mMetrics);
    }


}
