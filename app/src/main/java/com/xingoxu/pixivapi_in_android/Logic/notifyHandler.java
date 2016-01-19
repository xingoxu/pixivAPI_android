package com.xingoxu.pixivapi_in_android.Logic;

import android.os.Handler;
import android.os.Message;

import com.xingoxu.pixivapi_in_android.myRecyclerViewAdapter;

/**
 * Created by xingo on 1/19/2016.
 */
public class notifyHandler extends Handler {

    public void setAdapter(myRecyclerViewAdapter adapter) {
        this.adapter = adapter;
    }

    private myRecyclerViewAdapter adapter;


    @Override
    public void handleMessage(Message msg) {
        int position = msg.what;
        adapter.notifyItemChanged(position);

    }
}
