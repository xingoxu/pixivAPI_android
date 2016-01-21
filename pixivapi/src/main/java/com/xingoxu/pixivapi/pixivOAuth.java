package com.xingoxu.pixivapi;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.loopj.android.http.*;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


/**
 * Created by xingo on 1/15/2016.
 */
public class pixivOAuth {
    private pixivLoginUser user;
    private pixivLoginUser outerUser;
    private static AsyncHttpClient httpClient;
    private static Lock lock;//ensure the headers thread-safe :)

    public pixivLoginUser getUser() {
        return this.outerUser;
    }


    public pixivOAuth() {
        this.httpClient = new AsyncHttpClient();
        this.lock = new ReentrantLock();
        Log.i("pixivAPI.OAuth", "Please do authAsync first if you want to use pixivAPI");
    }

    /**
     * Start OAuth from here, reAuth by leaving the username and password null.
     *
     * @param username leave it null when you reAuth
     * @param password leave it null when you reAuth
     * @param handler  whatever success or failed, a message contains information will be sent to handler<p /> Success: message.obj=(pixivLoginUser)user<p />Failed: message.obj=(String) errorMessage
     * @param context
     * @return
     * @throws Exception Missing both username and refresh_token
     */
    public RequestHandle authAsync(String username, String password, final Handler handler, Context context) {


        String api = "https://oauth.secure.pixiv.net/auth/token";
        Map<String, String> header = new HashMap<>();
        header.put("Referer", "http://www.pixiv.net/");

        RequestParams parameters = new RequestParams();

        parameters.put("client_id", "bYGKuGVw91e0NMfPGp44euvGt59s");
        parameters.put("client_secret", "HP3RmkgAmEGro0gn1x9ioawQE8WMfvLXDz3ZqxpK");

        if (username == null || password == null) {
            if (this.user == null) {
                throw new RuntimeException("No username or refresh_token is set!");
            }
            parameters.put("grant_type", "refresh_token");
            parameters.put("refresh_token", user.getRefresh_token());
        } else {
            parameters.put("grant_type", "password");
            parameters.put("username", username);
            parameters.put("password", password);
        }

        //handle message
        pixivOAuthResponseHandler mhandler = new pixivOAuthResponseHandler() {
            @Override
            protected void handleMessage(Message message) {
                super.handleMessage(message);


                if (message.arg1 != pixivOAuthResponseHandler.OAuthResponseMessage_ID1)
                    return;

                Message messageWaitToSent = new Message();
                messageWaitToSent.arg1 = pixivOAuthResponseHandler.OAuthResponseMessage_ID1;

                if (message.arg2 == pixivOAuthResponseHandler.OAuthSuccessMessage_ID) {
                    if (message.obj instanceof pixivLoginUser) {
                        pixivOAuth.this.user = (pixivLoginUser) message.obj;
                        pixivLoginUser _outerUser = new pixivLoginUser();
                        _outerUser.setAvatar(pixivOAuth.this.user.getAvatar());
                        _outerUser.setPixivId(pixivOAuth.this.user.getPixivId());
                        _outerUser.setName(pixivOAuth.this.user.getName());
                        _outerUser.setExpires_time(pixivOAuth.this.user.getExpires_time());
                        pixivOAuth.this.outerUser = _outerUser;

                        messageWaitToSent.arg2 = pixivOAuthResponseHandler.OAuthSuccessMessage_ID;
                        messageWaitToSent.obj = _outerUser;
                    } else {
                        throw new RuntimeException("unknown error");
                    }
                } else {
                    messageWaitToSent.arg2 = pixivOAuthResponseHandler.OAuthFailedMessage_ID;
                    Log.w("pixivOAuth", "Auth Failed, reason has logged.");
                }

                handler.sendMessage(messageWaitToSent);
            }
        };


        return this.PostAsync(api, header, parameters, mhandler, context);
    }

    //base api start


    /**
     * @param api             api URL
     * @param header          @Nullable
     * @param parameters      @Nullable
     * @param responseHandler
     * @param context         Android Context bind with. After binding with context, you can cancel the request with cancelRequests(Context, boolean)
     * @return
     */
    public RequestHandle PostAsync(String api, Map<String, String> header, RequestParams parameters, ResponseHandlerInterface responseHandler, Context context) {
        Map<String, String> req_header = new HashMap<>();
        req_header.put("Referer", "http://spapi.pixiv.net/");
        req_header.put("UserAgent", "PixivIOSApp/5.8.3");

        if (user != null) req_header.put("Authorization", ("Bearer " + user.getAccess_token()));

        if (header == null) header = new HashMap<>();


        for (Map.Entry<String, String> x : header.entrySet()) {
            if (req_header.containsKey(x.getKey()))
                req_header.remove(x.getKey());

            req_header.put(x.getKey(), x.getValue());
        }

        //use lock and add headers
        lock.lock();

        httpClient.removeAllHeaders();

        for (Map.Entry<String, String> x : req_header.entrySet()) {
            httpClient.addHeader(x.getKey(), x.getValue());
        }


        RequestHandle result = httpClient.post(context, api, parameters, responseHandler);

        lock.unlock();


        return result;

    }

    /**
     * @param api             api URL
     * @param header          @Nullable
     * @param parameters      @Nullable
     * @param responseHandler
     * @param context         Android Context bind with. After binding with context, you can cancel the request with cancelRequests(Context, boolean)
     * @return
     */
    public RequestHandle GetAsync(String api, Map<String, String> header, RequestParams parameters, ResponseHandlerInterface responseHandler, Context context) {
        Map<String, String> req_header = new HashMap<>();
        req_header.put("Referer", "http://spapi.pixiv.net/");
        req_header.put("UserAgent", "PixivIOSApp/5.8.3");

        if (user != null) req_header.put("Authorization", ("Bearer " + user.getAccess_token()));

        if (header == null) header = new HashMap<>();

        for (Map.Entry<String, String> x : header.entrySet()) {
            if (req_header.containsKey(x.getKey()))
                req_header.remove(x.getKey());

            req_header.put(x.getKey(), x.getValue());
        }

        //use lock and add headers
        lock.lock();

        httpClient.removeAllHeaders();

        for (Map.Entry<String, String> x : req_header.entrySet()) {
            httpClient.addHeader(x.getKey(), x.getValue());
        }

        RequestHandle result = httpClient.get(context, api, parameters, responseHandler);

        lock.unlock();

        return result;


    }

    /**
     * @param api             api URL
     * @param header          @Nullable
     * @param parameters      @Nullable
     * @param responseHandler
     * @param context         Android Context bind with. After binding with context, you can cancel the request with cancelRequests(Context, boolean)
     * @return
     */
    public RequestHandle DeleteAsync(String api, Map<String, String> header, RequestParams parameters, ResponseHandlerInterface responseHandler, Context context) {

        Map<String, String> req_header = new HashMap<>();
        req_header.put("Referer", "http://spapi.pixiv.net/");
        req_header.put("UserAgent", "PixivIOSApp/5.8.3");

        if (user != null) req_header.put("Authorization", ("Bearer " + user.getAccess_token()));

        if (header == null) header = new HashMap<>();

        for (Map.Entry<String, String> x : header.entrySet()) {
            if (req_header.containsKey(x.getKey()))
                req_header.remove(x.getKey());

            req_header.put(x.getKey(), x.getValue());
        }

        //use lock and add headers
        lock.lock();

        httpClient.removeAllHeaders();

        for (Map.Entry<String, String> x : req_header.entrySet()) {
            httpClient.addHeader(x.getKey(), x.getValue());
        }

        RequestHandle result = httpClient.delete(context, api, null, parameters, responseHandler);

        lock.unlock();

        return result;

    }

    //base api end


}
