package com.xingoxu.pixivapi;

/**
 * Created by xingo on 1/15/2016.
 */
public class pixivLoginUser {
    private String access_token ;
    private String refresh_token;
    private String pixivId ;
    private String name ;

    /**
     * <p/>0 small
     * <p/>
     * 1 middle
     * <p/>
     * 2 big
     */
    private String[] avatar;
    private int expires_time ;

    public String getAccess_token() {
        return access_token;
    }

    public void setAccess_token(String access_token) {
        this.access_token = access_token;
    }

    public String getRefresh_token() {
        return refresh_token;
    }

    public void setRefresh_token(String refresh_token) {
        this.refresh_token = refresh_token;
    }

    /**
     *
     * @return <p/>0 small<p/> 1 middle<p/> 2 big
     */
    public String[] getAvatar() {
        return avatar;
    }

    /**
     *
     * @param avatar <p/>0 small<p/> 1 middle<p/> 2 big
     */
    public void setAvatar(String[] avatar) {
        this.avatar = avatar;
    }

    public int getExpires_time() {
        return expires_time;
    }

    public void setExpires_time(int expires_time) {
        this.expires_time = expires_time;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPixivId() {
        return pixivId;
    }

    public void setPixivId(String pixivId) {
        this.pixivId = pixivId;
    }
}
