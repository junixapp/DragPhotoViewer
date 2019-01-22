package com.lxj.dragphotoviewer;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ArgbEvaluator;
import android.animation.IntEvaluator;
import android.animation.ValueAnimator;
import android.app.Dialog;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.lxj.dragphotoviewer.interf.OnDragChangeListener;
import com.lxj.dragphotoviewer.interf.OnLoadImageListener;
import com.lxj.dragphotoviewer.interf.OnUpdateSrcViewListener;

/**
 * process move animation between src view and DragContainer.
 */
public class DragPhotoViewer extends Dialog implements OnDragChangeListener {
    ViewPager pager;
    DragContainer dragContainer;
    private IntEvaluator intEvaluator = new IntEvaluator();
    private ArgbEvaluator argbEvaluator = new ArgbEvaluator();
    OnUpdateSrcViewListener updateSrcViewListener;
    private OnLoadImageListener loadImageListener;
    private int currentItem;
    private int imageSize = 1;

    private AnimInfo showAnimInfo, hideAnimInfo = new AnimInfo();
    private ImageView snapshot;
    private Bitmap srcBmp;
    public DragPhotoViewer(@NonNull Context context) {
        super(context, R.style.DragPhotoDialog);
    }

    public DragPhotoViewer(@NonNull Context context, int themeResId) {
        super(context, themeResId);
    }

    protected DragPhotoViewer(@NonNull Context context, boolean cancelable, @Nullable OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_drag_photoviewer);
        dragContainer = findViewById(R.id.dragContainer);
        dragContainer.setVisibility(View.INVISIBLE);
        dragContainer.setOnDragChangeListener(this);
        pager = findViewById(R.id.pager);
        PhotoViewAdapter photoViewAdapter = new PhotoViewAdapter(imageSize, loadImageListener);
        pager.setAdapter(photoViewAdapter);
        pager.setCurrentItem(currentItem);
        pager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener(){
            @Override
            public void onPageSelected(int position) {
                if(updateSrcViewListener!=null){
                    updateSrcViewListener.updateSrcView(DragPhotoViewer.this, position);
                }
            }
        });
        addSnapshotView();
        // set dialog width and height
        applyFullscreen();
    }

    /**
     * add snapshot view to current window.
     */
    private void addSnapshotView() {
        snapshot = new ImageView(getContext());
        snapshot.setScaleType(ImageView.ScaleType.FIT_XY);
        snapshot.setImageBitmap(srcBmp);
        ViewGroup group = (ViewGroup) dragContainer.getParent();
        group.setBackgroundColor(Color.TRANSPARENT);
        // if have added, remove it.
        if (group.indexOfChild(snapshot) == -1) {
            group.addView(snapshot, new FrameLayout.LayoutParams(srcBmp.getWidth(), srcBmp.getHeight()));
        }
        //start x,y
        snapshot.setTranslationX(hideAnimInfo.x);
        snapshot.setTranslationY(hideAnimInfo.y);
    }

    private void applyFullscreen() {
        int width = getWindow().getWindowManager().getDefaultDisplay().getWidth();
        int height = getWindow().getWindowManager().getDefaultDisplay().getHeight();
        getWindow().setLayout(width, height);
        //让Dialog摆放在整个屏幕上，并且会将当前Window的statusbar颜色清除
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
    }

    public DragPhotoViewer setSrcView(View view) {
        if (view == null) return this;
        showAnimInfo = createShowAnimInfo(view);
        hideAnimInfo = createHideAnimInfo(view);
        srcBmp = createSrcBmp(view);
        return this;
    }

    /**
     * create snapshot view to move.
     *
     * @param src
     */
    private Bitmap createSrcBmp(View src) {
        Bitmap bmp = Bitmap.createBitmap(src.getWidth(),
                src.getHeight(),
                Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bmp);
        src.draw(c);
        return bmp;
    }

    /**
     * create entity class that hold move anim info.
     *
     * @param view
     * @return
     */
    private AnimInfo createShowAnimInfo(View view) {
        float ratio = view.getWidth() * 1f / view.getHeight();
        int windowWidth = getWindow().getWindowManager().getDefaultDisplay().getWidth();
        int windowHeight = getWindow().getWindowManager().getDefaultDisplay().getHeight();
        int endHeight = (int) (windowWidth / ratio);
        int endY = (windowHeight - endHeight) / 2;
        return new AnimInfo(0, endY, windowWidth, endHeight);
    }

    private AnimInfo createHideAnimInfo(View view) {
        int[] locations = new int[2];
        view.getLocationOnScreen(locations);
        return new AnimInfo(locations[0], locations[1]-getStatusBarHeight(), view.getWidth(), view.getHeight());
    }

    /**
     * do move animation
     * @param isShow
     * @param animInfo the entity class that holds animation data.
     */
    public void doMoveAnim(final boolean isShow, final AnimInfo animInfo) {
        ValueAnimator animator = ValueAnimator.ofFloat(0, 1);
        final int currentX = (int) snapshot.getTranslationX();
        final int currentY = (int) snapshot.getTranslationY();
        final int currentWidth = snapshot.getWidth();
        final int currentHeight = snapshot.getHeight();
        final ViewGroup parent = ((ViewGroup) dragContainer.getParent());
        final int currentColor = ((ColorDrawable) parent.getBackground()).getColor();
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float fraction = animation.getAnimatedFraction();
                snapshot.setTranslationX(intEvaluator.evaluate(fraction, currentX, animInfo.x));
                snapshot.setTranslationY(intEvaluator.evaluate(fraction, currentY, animInfo.y));
                ViewGroup.LayoutParams params = snapshot.getLayoutParams();
                params.width = intEvaluator.evaluate(fraction, currentWidth, animInfo.width);
                params.height = intEvaluator.evaluate(fraction, currentHeight, animInfo.height);
                snapshot.setLayoutParams(params);
                // set bg
                parent.setBackgroundColor((Integer) argbEvaluator.evaluate(fraction, currentColor,
                        isShow ? Color.BLACK : Color.TRANSPARENT));
            }
        });
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                snapshot.setVisibility(View.INVISIBLE);
                dragContainer.setVisibility(View.VISIBLE);

                if (!isShow) {
                    //close
                    dismiss();
                }
            }
        });
        animator.setInterpolator(isShow ? new LinearInterpolator() : new FastOutSlowInInterpolator());
        animator.setDuration(350).start();
    }

    @Override
    protected void onStart() {
        super.onStart();
        dragContainer.post(new Runnable() {
            @Override
            public void run() {
                doMoveAnim(true, showAnimInfo);
            }
        });
    }

    @Override
    public void onBackPressed() {
        onRelease();
    }

    @Override
    public void onRelease() {
        snapshot.setVisibility(View.VISIBLE);
        dragContainer.setVisibility(View.INVISIBLE);

        snapshot.post(new Runnable() {
            @Override
            public void run() {
                doMoveAnim(false, hideAnimInfo);
            }
        });
    }

    @Override
    public void onDragChange(int dy, float pageScale) {
        snapshot.setImageBitmap(srcBmp);
        snapshot.setLayoutParams(new FrameLayout.LayoutParams((int) (showAnimInfo.width * pageScale),
                (int) (showAnimInfo.height * pageScale)));
        // calucate relative translation with ViewPager scale value.
        snapshot.setTranslationX(pager.getWidth() * (1 - pageScale) / 2);
        snapshot.setTranslationY(snapshot.getTranslationY() + dy * pageScale);
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    public DragPhotoViewer setOnUpdateSrcViewListener(OnUpdateSrcViewListener updateSrcViewListener){
        this.updateSrcViewListener = updateSrcViewListener;
        return this;
    }

    public DragPhotoViewer setImageSize(int count) {
        this.imageSize = count;
        return this;
    }

    public DragPhotoViewer setLoadImageListener(OnLoadImageListener loadImageListener) {
        this.loadImageListener = loadImageListener;
        return this;
    }

    public DragPhotoViewer setCurrentItem(int position) {
        currentItem = position;
        return this;
    }

    public int getStatusBarHeight(){
        Resources resources = Resources.getSystem();
        int resourceId = resources.getIdentifier("status_bar_height", "dimen", "android");
        return resources.getDimensionPixelSize(resourceId);
    }
}
