package com.xingoxu.pixivapi;

import android.content.Context;

import com.loopj.android.http.RequestHandle;
import com.loopj.android.http.RequestParams;

import java.util.*;


/**
 * Created by xingo on 1/16/2016.
 */
public class pixivAPI {
    private pixivOAuth OAuth;

    private boolean checkIsID(String id) {
        if (id == null) throw new NullPointerException();
        if (id.equals("")) throw new RuntimeException("ID is not correct!");
        for (char x : id.toCharArray()) {
            if (!Character.isDigit(x))
                throw new RuntimeException("ID contains non-numeric!");
            int y = (int) x;
        }
        return true;
    }

    private pixivAPI() {
        //no OAuth can't use API
    }

    public pixivAPI(pixivOAuth OAuth) {
        this.OAuth = OAuth;
    }

    public RequestHandle badwordAsync(AbstractPixivResponseHandler responseHandler, Context context) {
        String api = "https://public-api.secure.pixiv.net/v1.1/bad_words.json";
        return OAuth.GetAsync(api, null, (RequestParams) null, responseHandler, context);
    }

    public RequestHandle workDetailAsync(String pixivWorkid, AbstractPixivResponseHandler responseHandler, Context context) {

        checkIsID(pixivWorkid);

        String api = "https://public-api.secure.pixiv.net/v1/works/" + pixivWorkid + ".json";

        RequestParams params = new RequestParams();
        params.put("image_sizes", "px_128x128,small,medium,large,px_480mw");
        params.put("include_stats", "true");

        return OAuth.GetAsync(api, null, params, responseHandler, context);
    }

    public RequestHandle user_profileAsync(String user_id, AbstractPixivResponseHandler responseHandler, Context context) {

        checkIsID(user_id);

        String api = "https://public-api.secure.pixiv.net/v1/users/" + user_id + ".json";
        RequestParams parameters = new RequestParams();

        {
            parameters.put("profile_image_sizes", "px_170x170,px_50x50");
            parameters.put("image_sizes", "px_128x128,small,medium,large,px_480mw");
            parameters.put("include_stats", 1);
            parameters.put("include_profile", 1);
            parameters.put("include_workspace", 1);
            parameters.put("include_contacts", 1);
        }


        return OAuth.GetAsync(api, null, parameters, responseHandler, context);
    }

    /**
     * Feeds 动态 フィード
     *
     * @param show_r18
     * @param max_id   start from illust_id
     * @return
     */
    public RequestHandle my_feedsAsync(boolean show_r18, String max_id, AbstractPixivResponseHandler responseHandler, Context context) {
        String api = "https://public-api.secure.pixiv.net/v1/me/feeds.json";

        RequestParams parameters = new RequestParams();

        int r18 = 0;
        if (show_r18) r18 = 1;

        {
            parameters.put("relation", "all");
            parameters.put("type", "touch_nottext");
            parameters.put("show_r18", r18);
        }

        if (max_id != null) parameters.put("max_id", max_id);

        return OAuth.GetAsync(api, null, parameters, responseHandler, context);
    }

    public enum publicity {
        PUBLIC, PRIVATE
    }

    public RequestHandle my_favourite_worksAsync(int page, publicity _publicity, AbstractPixivResponseHandler responseHandler, Context context) {
        String api = "https://public-api.secure.pixiv.net/v1/me/favorite_works.json";

        String publicity = "public";
        if (_publicity.equals(pixivAPI.publicity.PRIVATE)) publicity = "private";

        RequestParams parameters = new RequestParams();

        {
            parameters.put("page", page);
            parameters.put("per_page", 50);
            parameters.put("publicity", publicity);
            parameters.put("image_sizes", "px_128x128,px_480mw,large");
        }

        return OAuth.GetAsync(api, null, parameters, responseHandler, context);
    }

    public RequestHandle my_favourite_work_add(String pixivWorkid, publicity _publicity, AbstractPixivResponseHandler responseHandler, Context context) {
        String api = "https://public-api.secure.pixiv.net/v1/me/favorite_works.json";

        checkIsID(pixivWorkid);

        String publicity = "public";
        if (_publicity.equals(pixivAPI.publicity.PRIVATE)) publicity = "private";

        RequestParams parameters = new RequestParams();

        {
            parameters.put("work_id", pixivWorkid);
            parameters.put("publicity", publicity);
        }

        return OAuth.PostAsync(api, null, parameters, responseHandler, context);
    }


    private String ListParamsToString(List<String> list) {
        StringBuilder buffer = new StringBuilder(list.size() * 16);
        Iterator<String> it = list.iterator();
        while (it.hasNext()) {
            String next = it.next();
            buffer.append(next);

            if (it.hasNext()) {
                buffer.append(",");
            }
        }
        return buffer.toString();
    }

    /**
     * @param pixivWorkids if only one id ,just put id in List
     * @param _publicity   @nullable Recommended if we can get the publicity
     * @return
     */
    public RequestHandle my_favourite_works_delete(List<String> pixivWorkids, publicity _publicity, AbstractPixivResponseHandler responseHandler, Context context) {
        String api = "https://public-api.secure.pixiv.net/v1/me/favorite_works.json";


        String publicity = null;
        if (_publicity != null) {
            publicity = "public";
            if (_publicity.equals(pixivAPI.publicity.PRIVATE)) publicity = "private";
        }

        RequestParams parameters = new RequestParams();

        {
            parameters.put("ids", this.ListParamsToString(pixivWorkids));
            if (publicity != null)
                parameters.put("publicity", publicity);
        }


        return OAuth.DeleteAsync(api, null, parameters, responseHandler, context);

    }

    public RequestHandle my_following_works(int page, AbstractPixivResponseHandler responseHandler, Context context) {
        String api = "https://public-api.secure.pixiv.net/v1/me/following/works.json";

        RequestParams params = new RequestParams();

        {
            params.put("page", page);
            params.put("per_page", 30);
            params.put("image_sizes", "px_128x128,px_480mw,large");
            params.put("include_stats", true);
            params.put("include_sanity_level", true);
        }

        return OAuth.GetAsync(api, null, params, responseHandler, context);
    }

    public RequestHandle my_following_user(int page, publicity _publicity, AbstractPixivResponseHandler responseHandler, Context context) {
        String api = "https://public-api.secure.pixiv.net/v1/me/following.json";

        String publicity = "public";
        if (_publicity.equals(pixivAPI.publicity.PRIVATE)) publicity = "private";


        RequestParams params = new RequestParams();

        {
            params.put("page", page);
            params.put("per_page", 30);
            params.put("publicity", publicity);
        }

        return OAuth.GetAsync(api, null, params, responseHandler, context);
    }

    public RequestHandle my_following_user_follow(String user_id, publicity _publicity, AbstractPixivResponseHandler responseHandler, Context context) {
        String api = "https://public-api.secure.pixiv.net/v1/me/favorite-users.json";

        checkIsID(user_id);

        String publicity = "public";
        if (_publicity.equals(pixivAPI.publicity.PRIVATE)) publicity = "private";

        RequestParams parameters = new RequestParams();

        {
            parameters.put("target_user_id", user_id);
            parameters.put("publicity", publicity);
        }

        return OAuth.PostAsync(api, null, parameters, responseHandler, context);
    }

    /**
     * @param user_ids        if only one id ,just put id in List
     * @param _publicity      @nullable Recommended if we can get the publicity
     * @param responseHandler
     * @param context
     * @return
     */
    public RequestHandle my_following_user_unfollow(List<String> user_ids, publicity _publicity, AbstractPixivResponseHandler responseHandler, Context context) {
        String api = "https://public-api.secure.pixiv.net/v1/me/favorite-users.json";

        String publicity = null;
        if (_publicity != null) {
            publicity = "public";
            if (_publicity.equals(pixivAPI.publicity.PRIVATE)) publicity = "private";
        }

        RequestParams parameters = new RequestParams();

        {
            parameters.put("ids", this.ListParamsToString(user_ids));
            if (publicity != null)
                parameters.put("publicity", publicity);
        }


        return OAuth.DeleteAsync(api, null, parameters, responseHandler, context);
    }

}
