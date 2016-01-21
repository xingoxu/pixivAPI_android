package com.xingoxu.pixivapi_in_android;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;

import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.xingoxu.pixivapi.*;
import com.xingoxu.pixivapi_in_android.Logic.*;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.Header;


public class MainActivity extends AppCompatActivity {
    /*
    the content deleted in gradle build:
        compile fileTree(include: ['*.jar'], dir: 'libs')
     */

    private RecyclerView mRecyclerView;

    private SwipeRefreshLayout mRefreshLayout;

    List<pixivImage> images = new ArrayList<>();

    private int nowPage = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mRecyclerView = (RecyclerView) this.findViewById(R.id.recyclerView);
        mRefreshLayout = (SwipeRefreshLayout) this.findViewById(R.id.refreshLayout);


        final myRecyclerViewAdapter adapter = new myRecyclerViewAdapter(this, images);

        pixivOAuth oAuth = new pixivOAuth();
        oAuth.authAsync("username", "password", new oAuthHandler(oAuth, this, mRefreshLayout, adapter, images), MainActivity.this);

        final GridLayoutManager layoutManager = new GridAutofitLayoutManager(this, 100);

        mRecyclerView.setAdapter(adapter);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            private int previousTotal = 0;
            private boolean loading = true;
            private int firstVisibleItem, visibleItemCount, totalItemCount;

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                visibleItemCount = recyclerView.getChildCount();
                totalItemCount = layoutManager.getItemCount();
                firstVisibleItem = layoutManager.findFirstVisibleItemPosition();

                if (totalItemCount == 0) return;

                if (loading) {
                    if (totalItemCount > previousTotal) {
                        loading = false;
                        previousTotal = totalItemCount;
                    }
                }
                if (!loading
                        && (totalItemCount - visibleItemCount) <= firstVisibleItem) {
                    pixivAPI api = pixivAPISingleton.getInstance();
                    if (api == null) return;
                    nowPage++;
                    api.my_following_worksAsync(nowPage, new showRecyclerViewHandler(adapter, mRefreshLayout, images, nowPage), MainActivity.this);
                    loading = true;
                }
            }
        });

        mRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                pixivAPI api = pixivAPISingleton.getInstance();
                if (api == null) return;
                api.my_following_worksAsync(1, new showRecyclerViewHandler(adapter, mRefreshLayout, images, 1), MainActivity.this);
                nowPage = 1;
            }
        });


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }

    private static class oAuthHandler extends Handler {
        private final WeakReference<pixivOAuth> oAuth;
        private final WeakReference<MainActivity> mainActivity;
        private final WeakReference<List<pixivImage>> imagesWeakRef;
        private final WeakReference<myRecyclerViewAdapter> adapterWeakRef;
        private final WeakReference<SwipeRefreshLayout> refreshLayoutWeakRef;


        public oAuthHandler(pixivOAuth oAuth, MainActivity mainActivity, SwipeRefreshLayout refreshLayout, myRecyclerViewAdapter adapter, List<pixivImage> images) {
            this.oAuth = new WeakReference<pixivOAuth>(oAuth);
            this.mainActivity = new WeakReference<MainActivity>(mainActivity);
            this.imagesWeakRef = new WeakReference<List<pixivImage>>(images);
            this.adapterWeakRef = new WeakReference<myRecyclerViewAdapter>(adapter);
            this.refreshLayoutWeakRef = new WeakReference<SwipeRefreshLayout>(refreshLayout);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            pixivOAuth oAuth = this.oAuth.get();
            MainActivity mainActivity = this.mainActivity.get();
            List<pixivImage> images = this.imagesWeakRef.get();
            myRecyclerViewAdapter adapter = this.adapterWeakRef.get();
            SwipeRefreshLayout swipeRefreshLayout = this.refreshLayoutWeakRef.get();
            if (oAuth == null) return;
            if (mainActivity == null) return;
            if (images == null) return;
            if (adapter == null) return;
            if (swipeRefreshLayout == null) return;
            pixivAPI pixivAPI = pixivAPISingleton.getInstance(oAuth);
            pixivAPI.my_following_worksAsync(
                    1,
                    new showRecyclerViewHandler(adapter, swipeRefreshLayout, images, 1),
                    mainActivity);
        }

    }

    private static class showRecyclerViewHandler extends AbstractPixivResponseHandler {
        private final WeakReference<List<pixivImage>> imagesWeakReference;
        private final WeakReference<myRecyclerViewAdapter> adapterWeakRef;
        private final WeakReference<SwipeRefreshLayout> swipeRefreshLayoutWeakRef;
        private final int page;

        public showRecyclerViewHandler(myRecyclerViewAdapter adapter, SwipeRefreshLayout swipeRefreshLayout, List<pixivImage> images, int page) {
            this.imagesWeakReference = new WeakReference<List<pixivImage>>(images);
            this.adapterWeakRef = new WeakReference<myRecyclerViewAdapter>(adapter);
            this.swipeRefreshLayoutWeakRef = new WeakReference<SwipeRefreshLayout>(swipeRefreshLayout);
            this.page = page;
        }

        @Override
        public void onSuccess(int statusCode, Header[] headers, JSONObject response) {

            super.onSuccess(statusCode, headers, response);

            List<pixivImage> images = this.imagesWeakReference.get();
            myRecyclerViewAdapter adapter = this.adapterWeakRef.get();

            if (images == null) return;
            if (adapter == null) return;

            if (page == 1) {
                int size = images.size();
                images.clear();
                adapter.notifyItemRangeRemoved(0, size);
            }

            JSONArray responses = null;

            try {
                responses = response.getJSONArray("response");
            } catch (JSONException e) {
                e.printStackTrace();
            }

            if (responses == null) return;

            for (int i = 0; i < responses.length(); i++) {
                JSONObject responseItem = null;
                try {
                    responseItem = responses.getJSONObject(i);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                if (responseItem == null) return;

                pixivImage image = new pixivImage();
                try {
                    image.pixivID = responseItem.getString("id");
                    image.image_url = responseItem
                            .getJSONObject("image_urls")
                            .getString("px_128x128");
                } catch (JSONException e) {
                    e.printStackTrace();
                }


                images.add(image);
                adapter.notifyItemInserted(images.size() - 1);

            }

            SwipeRefreshLayout swipeRefreshLayout = this.swipeRefreshLayoutWeakRef.get();
            if (swipeRefreshLayout == null) return;
            swipeRefreshLayout.setRefreshing(false);

        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("MainActivity", "Destroying...");
    }
}


