package com.xingoxu.pixivapi_in_android;


import android.content.Context;
import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


import com.xingoxu.pixivapi.pixivAPI;
import com.xingoxu.pixivapi_in_android.Logic.pixivImage;
import com.xingoxu.pixivapi_in_android.Logic.pixivImageCacheHelper;

import java.util.List;

/**
 * Created by xingo on 1/18/2016.
 */
public class myRecyclerViewAdapter extends RecyclerView.Adapter<MyViewHolder> {
    private LayoutInflater minflater;
    private Context mcontext;

    private List<pixivImage> pixivImages;

    private static pixivAPI pixivAPI; //should be a singleton

    public myRecyclerViewAdapter(Context context, List<pixivImage> pixivImages, pixivAPI api) {
        this.mcontext = context;
        this.pixivImages = pixivImages;
        this.pixivAPI = api;
        minflater = LayoutInflater.from(context);
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = minflater.inflate(R.layout.mediumpicsqrviewlayout, parent, false);
        MyViewHolder viewHolder = new MyViewHolder(view);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {


        pixivImageCacheHelper helper = pixivImageCacheHelper.getInstance(pixivAPI, this);


        Bitmap photo = helper.getBitMapFromMemCache(pixivImages.get(position).pixivID, pixivImages.get(position).image_url, position);


        if (photo != null) {
            holder.pixivImage.setImageBitmap(photo);//get imagesource from cache
        }

    }

    @Override
    public int getItemCount() {
        return pixivImages.size();
    }
}

class MyViewHolder extends RecyclerView.ViewHolder {
    MediumPictureSquareView pixivImage;
    MediumPictureSquareView chooseModeImage;

    public MyViewHolder(View itemView) {
        super(itemView);

        this.pixivImage = (MediumPictureSquareView) itemView.findViewById(R.id.mediumPicSqrView_pixivImage);
        this.chooseModeImage = (MediumPictureSquareView) itemView.findViewById(R.id.mediumPicSqrView_chooseModeImage);
    }
}
