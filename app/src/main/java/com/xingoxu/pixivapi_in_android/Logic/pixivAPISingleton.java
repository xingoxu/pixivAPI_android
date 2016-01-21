package com.xingoxu.pixivapi_in_android.Logic;

import com.xingoxu.pixivapi.pixivAPI;
import com.xingoxu.pixivapi.pixivOAuth;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by xingo on 1/20/2016.
 */
public class pixivAPISingleton {
    private static pixivAPI pixivAPI = null;

    public static pixivAPI getInstance(pixivOAuth oAuth) {
        pixivAPISingleton.pixivAPI = new pixivAPI(oAuth);
        return pixivAPI;
    }

    public static pixivAPI getInstance() {
        return pixivAPI;
    }
}
