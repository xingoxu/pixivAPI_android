package com.xingoxu.pixivapi_in_android.Logic;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Looper;
import android.util.Log;
import android.util.LruCache;

import com.loopj.android.http.AsyncHttpResponseHandler;
import com.xingoxu.pixivapi.pixivOAuth;

import java.io.ByteArrayInputStream;
import java.lang.ref.WeakReference;

import cz.msebera.android.httpclient.Header;

/**
 * Created by xingo on 1/19/2016.
 */
public class pixivImageCacheHelper {
    private final static String TAG = "pixivImageCacheHelper";


    private static WeakReference<notifyHandler> notifyHandler;

    private static pixivImageCacheHelper ourInstance = new pixivImageCacheHelper();

    public static pixivImageCacheHelper getInstance(notifyHandler handler) {
        notifyHandler = new WeakReference<>(handler);
        return ourInstance;
    }

    private LruCache<String, Bitmap> memoryCache;


    private pixivImageCacheHelper() {
        int maxMemorySize = (int) (Runtime.getRuntime().maxMemory() / 8);

        memoryCache = new LruCache<String, Bitmap>(maxMemorySize) {
            @Override
            protected int sizeOf(String key, Bitmap value) {
                return value.getByteCount() / 1024;
            }
        };


    }


    public Bitmap getBitMapFromMemCache(String key, String imageURL, int position) {
        if (key == null) return null;
        //1. getbitMapFromMemCache return to Adapter


        Bitmap bitmap = this.memoryCache.get(key);
        if (bitmap != null)
            Log.v(TAG, key + " Image loaded from Cache");
        else {
            //2. if cache doesn't have, return the loading picture(null) and let downloader download the image
            pixivOAuth oAuth = pixivAPISingleton.getInstance().getOAuth();
            notifyHandler handler = notifyHandler.get();
            if (handler == null) return bitmap;
            Thread startDownloadImage = new downloadImageThread(oAuth, this, handler, imageURL, key, position);
            startDownloadImage.start();
        }
        return bitmap;
        //3. when loading complete, notify the adapter to get the bitmap from this
        //see handle message
    }


    public void putBitMapToMemCache(String key, Bitmap value) {
        if (key == null || value == null)
            return;
        if (memoryCache.get(key) == null) {
            memoryCache.put(key, value);
            Log.v("ImageCacheHelper", "Cache added");
        } else {
            memoryCache.remove(key);
            memoryCache.put(key, value);
            Log.v("ImageCacheHelper", "Cache added");
        }
    }

    private static class downloadImageThread extends Thread {
        private final WeakReference<pixivOAuth> weakOauth;
        private final WeakReference<pixivImageCacheHelper> helper;
        private final WeakReference<notifyHandler> notifyHandlerWeakRef;
        private final String key;
        private final String imageURL;
        private final int position;

        downloadImageThread(pixivOAuth oAuth, pixivImageCacheHelper helper, notifyHandler handler, String imageURL, String key, int position) {
            this.weakOauth = new WeakReference<>(oAuth);
            this.helper = new WeakReference<>(helper);
            this.notifyHandlerWeakRef = new WeakReference<>(handler);
            this.position = position;
            this.imageURL = imageURL;
            this.key = key;
        }

        @Override
        public void run() {
            pixivOAuth oAuth = this.weakOauth.get();
            pixivImageCacheHelper helper = this.helper.get();
            notifyHandler handler = this.notifyHandlerWeakRef.get();
            if (oAuth == null) return;
            if (helper == null) return;
            if (handler == null) return;

            Looper.prepare();
            Looper looper = Looper.myLooper();
            Log.v(key, "start post request to download image");
            oAuth.GetAsync(imageURL, null, null, new downloaderHandler(helper, handler, key, position, looper), null);
            Looper.loop();
        }
    }


    private static class downloaderHandler extends AsyncHttpResponseHandler {
        private final WeakReference<pixivImageCacheHelper> refer;
        private final WeakReference<notifyHandler> notifyHandlerWeakRef;
        private final WeakReference<Looper> looperWeakRef;
        private final String workid;
        private final int position;

        downloaderHandler(pixivImageCacheHelper caller, notifyHandler handler, String workid, int position, Looper looper) {
            this.refer = new WeakReference<>(caller);
            this.notifyHandlerWeakRef = new WeakReference<>(handler);
            this.workid = workid;
            this.position = position;
            this.looperWeakRef = new WeakReference<Looper>(looper);
        }

        @Override
        public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
            //put the request in a new Thread
            Bitmap bitmapResult = BitmapFactory.decodeStream(new ByteArrayInputStream(responseBody));


            pixivImageCacheHelper refer = this.refer.get();
            notifyHandler handler = this.notifyHandlerWeakRef.get();
            if (refer == null) return;
            if (handler == null) return;

            refer.putBitMapToMemCache(workid, bitmapResult);
            handler.sendEmptyMessage(position);

            Looper looper = this.looperWeakRef.get();
            if (looper == null) return;
            looper.quit();

        }

        @Override
        public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {


        }
    }

}
