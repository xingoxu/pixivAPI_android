package com.xingoxu.pixivapi_in_android.Logic;

import android.os.Handler;
import android.os.Message;

import com.xingoxu.pixivapi_in_android.myRecyclerViewAdapter;

import java.lang.ref.WeakReference;

/**
 * Created by xingo on 1/19/2016.
 */
public class notifyHandler extends Handler {
    private WeakReference<myRecyclerViewAdapter> adapterWeakReference;

    public notifyHandler(myRecyclerViewAdapter adapter) {
        this.adapterWeakReference = new WeakReference<myRecyclerViewAdapter>(adapter);
    }


    @Override
    public void handleMessage(Message msg) {
        myRecyclerViewAdapter adapter = adapterWeakReference.get();
        if (adapter == null) return;
        int position = msg.what;
        adapter.notifyItemChanged(position);
    }
}
