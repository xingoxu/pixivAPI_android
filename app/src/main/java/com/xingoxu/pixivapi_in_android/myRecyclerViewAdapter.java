package com.xingoxu.pixivapi_in_android;


import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import com.bignerdranch.android.multiselector.ModalMultiSelectorCallback;
import com.bignerdranch.android.multiselector.MultiSelector;
import com.bignerdranch.android.multiselector.SwappingHolder;
import com.xingoxu.pixivapi_in_android.Logic.notifyHandler;
import com.xingoxu.pixivapi_in_android.Logic.pixivImage;
import com.xingoxu.pixivapi_in_android.Logic.pixivImageCacheHelper;


import java.util.List;

/**
 * Created by xingo on 1/18/2016.
 */
public class myRecyclerViewAdapter extends RecyclerView.Adapter<MyViewHolder> {
    private LayoutInflater minflater;
    private Context mContext;

    private List<pixivImage> pixivImages;

    public myRecyclerViewAdapter(Context context, List<pixivImage> pixivImages) {
        this.mContext = context;
        this.pixivImages = pixivImages;
        minflater = LayoutInflater.from(this.mContext);
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = minflater.inflate(R.layout.mediumpicsqrviewlayout, parent, false);

        return new MyViewHolder(view, (MainActivity) this.mContext);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {


        pixivImageCacheHelper helper = pixivImageCacheHelper.getInstance(notifyHandler);


        Bitmap photo = helper.getBitMapFromMemCache(pixivImages.get(position).pixivID, pixivImages.get(position).image_url, position);

        if (photo != null) {
            holder.pixivImage.setImageBitmap(photo);//get imagesource from cache
        }


    }

    @Override
    public int getItemCount() {
        return pixivImages.size();
    }

    private final notifyHandler notifyHandler = new notifyHandler(this);

}

class MyViewHolder extends SwappingHolder implements View.OnClickListener, View.OnLongClickListener {
    MediumPictureSquareView pixivImage;
    MediumPictureSquareView chooseModeImage;

    private static MultiSelector multiSelector = new MultiSelector();
    private static ModalMultiSelectorCallback testMode = new ModalMultiSelectorCallback(multiSelector) {
        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            return false;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
            super.onPrepareActionMode(actionMode, menu);
            activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                activity.getWindow().setStatusBarColor(ContextCompat.getColor(activity, R.color.colorPrimary));
            return true;
        }

        @Override
        public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
            super.onCreateActionMode(actionMode, menu);
            return true;
        }

        @Override
        public void onDestroyActionMode(ActionMode actionMode) {
            multiSelector.clearSelections();
            super.onDestroyActionMode(actionMode);
            activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            MyViewHolder.mode = null;
        }
    };

    private static MainActivity activity;

    private static ActionMode mode;

    public MyViewHolder(View itemView, MainActivity activity) {
        super(itemView, multiSelector);

        this.pixivImage = (MediumPictureSquareView) itemView.findViewById(R.id.mediumPicSqrView_pixivImage);
        this.chooseModeImage = (MediumPictureSquareView) itemView.findViewById(R.id.mediumPicSqrView_chooseModeImage);

        MyViewHolder.activity = activity;


        itemView.setOnClickListener(this);
        itemView.setLongClickable(true);
        itemView.setOnLongClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (MyViewHolder.multiSelector.tapSelection(this)) {
            //handle actionmode here
            if (MyViewHolder.mode == null) return;
            MyViewHolder.mode.setTitle("" + MyViewHolder.multiSelector.getSelectedPositions().size());

        } else {
            //handle not actionmode here
            MyViewHolder.mode = null;

        }


    }

    @Override
    public boolean onLongClick(View v) {
        if (MyViewHolder.mode == null)
            MyViewHolder.mode = MyViewHolder.activity.startSupportActionMode(MyViewHolder.testMode);

        MyViewHolder.multiSelector.setSelected(this, true);
        return true;
    }
}
