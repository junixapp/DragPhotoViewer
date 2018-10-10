package com.lxj.dragphotoviewer;

import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;

import com.github.chrisbanes.photoview.PhotoView;
import com.lxj.dragphotoviewer.interf.OnLoadImageListener;

public class PhotoViewAdapter extends PagerAdapter {
    private int count;
    private OnLoadImageListener loadImageListener;

    public PhotoViewAdapter(int count, OnLoadImageListener loadImageListener) {
        this.count = count;
        this.loadImageListener = loadImageListener;
    }

    @Override
    public int getCount() {
        return count;
    }
    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object o) {
        return o == view;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        PhotoView photoView = new PhotoView(container.getContext());
        // call LoadImageListener
        if(loadImageListener!=null){
            loadImageListener.onLoadImage(position, photoView);
        }
        container.addView(photoView);
        return photoView;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View) object);
    }
}
