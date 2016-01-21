package com.xingoxu.pixivapi;

import android.os.Message;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

/**
 * Created by xingo on 1/16/2016.
 * A ResponseHandler extends from JSONResponseHandler.
 * <p/>
 * Can't ensure the everything in runnable is thread-safe.
 */
public class pixivOAuthResponseHandler extends AbstractPixivResponseHandler {

    public static final int OAuthResponseMessage_ID1 = 0x87284395;
    public static final int OAuthResponseMessage_IDwhat = 0x32940581;

    public static final int OAuthSuccessMessage_ID = 0x75439206;
    public static final int OAuthFailedMessage_ID = 0x26339039;

    @Override
    public void onSuccess(int statusCode, Header[] headers, String responseString) {
        super.onSuccess(statusCode, headers, responseString);
        Log.e("pixivOAuth", "Response Comes From ResponseString Method. Api Return may has changed!");
    }

    /**
     * Returns when request succeeds
     * WHEN YOU OVERRIDE DON'T FORGET TO DO super.OnSuccess(statusCode,headers,response) FIRST!!!
     *
     * @param statusCode http response status line
     * @param headers    response headers if any
     * @param response   parsed response if any
     */
    @Override
    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
        super.onSuccess(statusCode, headers, response);

        String access_token = null;
        int expires_in = 0;
        String refresh_token = null;
        String pixivName = null;
        String pixivID = null;
        String[] avatar = new String[3];


        try {
            //should let them be independent and then
            //get all we can get after error occurs before modified code
            JSONObject responseObject = response.getJSONObject("response");
            access_token = responseObject.getString("access_token");
            refresh_token = responseObject.getString("refresh_token");
            expires_in = responseObject.getInt("expires_in");

            JSONObject user_response = responseObject.getJSONObject("user");
            pixivID = user_response.getString("id");
            pixivName = user_response.getString("name");

            JSONObject profile_image_response = user_response.getJSONObject("profile_image_urls");
            avatar[0] = profile_image_response.getString("px_16x16");//small
            avatar[1] = profile_image_response.getString("px_50x50");//middle
            avatar[2] = profile_image_response.getString("px_170x170");//large


        } catch (JSONException e) {
            Log.e("pixivOAuth", "Resolving json occurs error, API result might has been changed!", e);
        }

        if (access_token == null) return;

        pixivLoginUser user = new pixivLoginUser();

        user.setAccess_token(access_token);
        user.setRefresh_token(refresh_token);
        user.setAvatar(avatar);
        user.setExpires_time(expires_in);
        user.setName(pixivName);
        user.setPixivId(pixivID);

        Message message = new Message();
        message.what = OAuthResponseMessage_IDwhat;
        message.arg1 = OAuthResponseMessage_ID1;
        message.arg2 = OAuthSuccessMessage_ID;
        message.obj = user;

        this.sendMessage(message);
    }

    /**
     * Returns when request succeeds
     *
     * @param statusCode http response status line
     * @param headers    response headers if any
     * @param response   parsed response if any
     */
    @Override
    public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
        super.onSuccess(statusCode, headers, response);
        Log.e("pixivOAuth", "Response Comes From ResponseString Method. Api Return may has changed!");
    }

    /**
     * Returns when request failed
     *
     * @param statusCode    http response status line
     * @param headers       response headers if any
     * @param throwable     throwable describing the way request failed
     * @param errorResponse parsed response if any
     */
    @Override
    public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
        super.onFailure(statusCode, headers, throwable, errorResponse);

        String errorMessage = null;
        try {
            errorMessage
                    = errorResponse
                    .getJSONObject("errors")
                    .getJSONObject("system")
                    .getString("message");
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e("pixivOAuth", "Resolving json occurs error, API result might has been changed!", e);
        }

        Message message = new Message();
        message.what = OAuthResponseMessage_IDwhat;
        message.arg1 = OAuthResponseMessage_ID1;
        message.arg2 = OAuthFailedMessage_ID;

        if (errorMessage != null)
            message.obj = errorMessage;
        else
            message.obj = errorResponse;


        sendMessage(message);
    }

    /**
     * Returns when request failed
     *
     * @param statusCode    http response status line
     * @param headers       response headers if any
     * @param throwable     throwable describing the way request failed
     * @param errorResponse parsed response if any
     */
    @Override
    public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONArray errorResponse) {
        super.onFailure(statusCode, headers, throwable, errorResponse);
    }

    @Override
    public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
        super.onFailure(statusCode, headers, responseString, throwable);
    }
}
