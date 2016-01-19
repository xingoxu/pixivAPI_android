package com.xingoxu.pixivapi_in_android.Logic;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.util.LruCache;

import com.loopj.android.http.AsyncHttpResponseHandler;
import com.xingoxu.pixivapi.*;
import com.xingoxu.pixivapi_in_android.myRecyclerViewAdapter;


import java.io.ByteArrayInputStream;
import java.lang.ref.WeakReference;

import cz.msebera.android.httpclient.Header;

/**
 * Created by xingo on 1/19/2016.
 */
public class pixivImageCacheHelper {
    private static pixivImageCacheHelper ourInstance = new pixivImageCacheHelper();

    private static pixivAPI pixivAPI;

    private static notifyHandler handler;

    public void setHandler(notifyHandler handler) {
        pixivImageCacheHelper.handler = handler;
    }

    public static pixivImageCacheHelper getInstance() {
        return ourInstance;
    }

    public static pixivImageCacheHelper getInstance(pixivAPI api, myRecyclerViewAdapter myRecyclerViewAdapter) {
        pixivAPI = api;
        return ourInstance;
    }

    private final LruCache<String, Bitmap> memoryCache;

    private static String TAG = "pixivImageCacheHelper";

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
            pixivOAuth oAuth = pixivAPI.getOAuth();
            Thread startDownloadImage = new downloadImageThread(oAuth, this, imageURL, key, position);
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
        private final String key;
        private final String imageURL;
        private final int position;

        downloadImageThread(pixivOAuth oAuth, pixivImageCacheHelper helper, String imageURL, String key, int position) {
            this.weakOauth = new WeakReference<pixivOAuth>(oAuth);
            this.helper = new WeakReference<pixivImageCacheHelper>(helper);
            this.position = position;
            this.imageURL = imageURL;
            this.key = key;
        }

        @Override
        public void run() {
            pixivOAuth oAuth = this.weakOauth.get();
            pixivImageCacheHelper helper = this.helper.get();
            if (oAuth == null) return;
            if (helper == null) return;

            Looper.prepare();
            Log.d("DownloadImage", "start post request to download image");
            oAuth.GetAsync(imageURL, null, null, new downloaderHandler(helper, key, position), null);
            Looper.loop();
        }
    }


    private static class downloaderHandler extends AsyncHttpResponseHandler {
        private final WeakReference<pixivImageCacheHelper> refer;
        private final String workid;
        private final int position;

        downloaderHandler(pixivImageCacheHelper caller, String workid, int position) {
            this.refer = new WeakReference<pixivImageCacheHelper>(caller);
            this.workid = workid;
            this.position = position;
        }

        @Override
        public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
            //put the request in a new Thread
            Bitmap bitmapResult = BitmapFactory.decodeStream(new ByteArrayInputStream(responseBody));

            pixivImageCacheHelper refer = this.refer.get();
            if (refer == null) return;
            if (workid == null) return;
            refer.putBitMapToMemCache(workid, bitmapResult);

            handler.sendEmptyMessage(position);

            //adapter.notifyDataSetChanged();//improve
        }

        @Override
        public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {


        }
    }

}
