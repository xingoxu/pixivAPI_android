package com.xingoxu.pixivapi_in_android;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.Checkable;
import android.widget.RelativeLayout;

/**
 * Created by xingo on 1/14/2016.
 */
public class MediumPictureSquareViewLayoutBase extends RelativeLayout implements Checkable {
    public MediumPictureSquareViewLayoutBase(Context context) {
        super(context);
    }

    public MediumPictureSquareViewLayoutBase(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MediumPictureSquareViewLayoutBase(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // For simple implementation, or internal size is always 0.
        // We depend on the container to specify the layout size of
        // our view. We can't really know what it is since we will be
        // adding and removing different arbitrary views and do not
        // want the layout to change as this happens.
        setMeasuredDimension(getDefaultSize(0, widthMeasureSpec), getDefaultSize(0, heightMeasureSpec));

        // Children are just made to fill our space.
        int childWidthSize = getMeasuredWidth();
        int childHeightSize = getMeasuredHeight();
        //ensure the height is the same with the width
        heightMeasureSpec = widthMeasureSpec = MeasureSpec.makeMeasureSpec(childWidthSize, MeasureSpec.EXACTLY);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    //Checkable Realize Start
    private boolean mChecked;

    @Override
    public void setChecked(boolean checked) {
        mChecked = checked;

//        if (this.getTag()==null) {
//            ViewHolder viewHolder = new ViewHolder();
//            viewHolder.mediumPicSqrView_chooseModeImage = (MediumPictureSquareView) this.findViewById(R.id.mediumPicSqrView_chooseModeImage);
//            viewHolder.mediumPicSqrView_pixivImage = (MediumPictureSquareView) this.findViewById(R.id.mediumPicSqrView_pixivImage);
//            this.setTag(viewHolder);
//        }


//        ViewHolder viewHolder = (ViewHolder) this.getTag();

        if (chooseModeImageView == null)
            chooseModeImageView = (MediumPictureSquareView) this.findViewById(R.id.mediumPicSqrView_chooseModeImage);
        if (pixivImageView == null)
            pixivImageView = (MediumPictureSquareView) this.findViewById(R.id.mediumPicSqrView_pixivImage);

        if (mChecked) {
            chooseModeImageView.setVisibility(VISIBLE);
            pixivImageView.setImageAlpha(100);
        } else {
            chooseModeImageView.setVisibility(GONE);
            pixivImageView.setImageAlpha(255);
        }


    }

    @Override
    public boolean isChecked() {
        return mChecked;
    }

    @Override
    public void toggle() {
        setChecked(!mChecked);
    }

    private MediumPictureSquareView chooseModeImageView = (MediumPictureSquareView) this.findViewById(R.id.mediumPicSqrView_chooseModeImage);
    private MediumPictureSquareView pixivImageView = (MediumPictureSquareView) this.findViewById(R.id.mediumPicSqrView_pixivImage);

}
