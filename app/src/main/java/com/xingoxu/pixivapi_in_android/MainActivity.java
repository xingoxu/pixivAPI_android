package com.xingoxu.pixivapi_in_android;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

import com.xingoxu.pixivapi.*;
import com.xingoxu.pixivapi_in_android.Logic.notifyHandler;
import com.xingoxu.pixivapi_in_android.Logic.pixivImage;
import com.xingoxu.pixivapi_in_android.Logic.pixivImageCacheHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.Header;


public class MainActivity extends AppCompatActivity {
    /*
    the content deleted in gradle build:
        compile fileTree(include: ['*.jar'], dir: 'libs')
     */

    private RecyclerView mRecyclerView;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mRecyclerView = (RecyclerView) this.findViewById(R.id.recyclerView);



        final notifyHandler handler = new notifyHandler();

        (new Thread() {
            @Override
            public void run() {
                Looper.prepare();
                final pixivOAuth oAuth = new pixivOAuth();
                oAuth.authAsync(
                        "username",
                        "password",
                        new Handler() {
                            @Override
                            public void handleMessage(Message msg) {
                                super.handleMessage(msg);
                                final pixivAPI pixivAPI = new pixivAPI(oAuth);
                                pixivAPI.my_following_worksAsync(
                                        1,
                                        new AbstractPixivResponseHandler() {
                                            @Override
                                            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {

                                                super.onSuccess(statusCode, headers, response);

                                                JSONArray responses = null;

                                                try {
                                                    responses = response.getJSONArray("response");
                                                } catch (JSONException e) {
                                                    e.printStackTrace();
                                                }

                                                if (responses == null) return;

                                                final List<pixivImage> images = new ArrayList<>();

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
                                                        // Log.v("getInfo",image.pixivID);
                                                        image.image_url = responseItem
                                                                .getJSONObject("image_urls")
                                                                .getString("px_128x128");
                                                    } catch (JSONException e) {
                                                        e.printStackTrace();
                                                    }
                                                    if (image.image_url == null) return;
                                                    Log.d("getJSON", image.image_url);
                                                    images.add(image);
                                                }

                                                mRecyclerView.post(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        myRecyclerViewAdapter adapter = new myRecyclerViewAdapter(MainActivity.this, images, pixivAPI);
                                                        pixivImageCacheHelper helper = pixivImageCacheHelper.getInstance();
                                                        handler.setAdapter(adapter);
                                                        helper.setHandler(handler);
                                                        mRecyclerView.setAdapter(adapter);
                                                        mRecyclerView.setLayoutManager(new GridLayoutManager(MainActivity.this, 3));
                                                    }
                                                });
                                            }
                                        },
                                        MainActivity.this);
                            }
                        },
                        MainActivity.this);


                Looper.loop();
            }
        }).start();


//        Button button;
//
//        button.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                pixivOAuth oAuth = new pixivOAuth();
//                try {
//                    oAuth.authAsync("username", "password", handler, MainActivity.this);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//                Log.d("MainActivity", "OAuth request has been sent");
//            }
//        });


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
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


