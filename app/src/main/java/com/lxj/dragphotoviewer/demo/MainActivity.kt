package com.lxj.dragphotoviewer.demo

import android.animation.FloatEvaluator
import android.animation.IntEvaluator
import android.animation.ValueAnimator
import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.Rect
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.github.chrisbanes.photoview.OnViewTapListener
import com.lxj.dragphotoviewer.DragPhotoViewer
import com.lxj.easyadapter.CommonAdapter
import com.lxj.easyadapter.MultiItemTypeAdapter
import com.lxj.easyadapter.ViewHolder
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val list = arrayListOf<String>()
        list.add("https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1537520263340&di=67fac95f069738c5d7828f893703e961&imgtype=0&src=http%3A%2F%2Fimgsrc.baidu.com%2Fimgad%2Fpic%2Fitem%2Ff9dcd100baa1cd11ded7491bb212c8fcc3ce2da3.jpg")
        list.add("https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1537520263339&di=a96184cab3fa48b8040188e37a9af9c9&imgtype=0&src=http%3A%2F%2Fimgsrc.baidu.com%2Fimgad%2Fpic%2Fitem%2F908fa0ec08fa513de2383be4366d55fbb2fbd97e.jpg")
        list.add("https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1537520263339&di=f682e82b937b4b29fc8865f391a73524&imgtype=0&src=http%3A%2F%2Fimgsrc.baidu.com%2Fimgad%2Fpic%2Fitem%2F03087bf40ad162d983b9fe211adfa9ec8a13cdbc.jpg")
        list.add("https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1537520263339&di=b4937b4a817cc8e066a969c3c7a3aa42&imgtype=0&src=http%3A%2F%2Fimgsrc.baidu.com%2Fimage%2Fc0%253Dshijue1%252C0%252C0%252C294%252C40%2Fsign%3D4821b0b654df8db1a8237427614ab721%2Fca1349540923dd54420f1646db09b3de9c82489b.jpg")

        recyclerView.apply {
            layoutManager = GridLayoutManager(context, 2, GridLayoutManager.VERTICAL, false)
            adapter = object : CommonAdapter<String>(R.layout.item_list, list) {
                override fun convert(holder: ViewHolder, t: String, position: Int) {
                    Glide.with(context).load(t).into(holder.getView(R.id.image))
                }

            }
            (adapter as CommonAdapter<String>).setOnItemClickListener(object : MultiItemTypeAdapter.OnItemClickListener {
                override fun onItemLongClick(view: View?, holder: RecyclerView.ViewHolder?, position: Int) = false

                override fun onItemClick(view: View, holder: RecyclerView.ViewHolder?, position: Int) {
                    DragPhotoViewer(context)
                            .setSrcView(view)
                            .setCurrentItem(position)
                            .setImageSize(list.size)
                            .setLoadImageListener { position, imageView ->
                                Glide.with(context).load(list[position]).into(imageView)
                            }
                            .setOnUpdateSrcViewListener { viewer, position ->
                                //set new src view.
                                viewer.setSrcView(recyclerView.getChildAt(position))
                            }
                            .show()
                }
            })
        }

        //单张图片使用
        val list2 = arrayListOf<String>()
        list2.add("https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1539170196600&di=60afed2abdd6a3375b3a3c86266480e2&imgtype=0&src=http%3A%2F%2Fimgsrc.baidu.com%2Fimgad%2Fpic%2Fitem%2F728da9773912b31bd8396f298c18367adab4e15d.jpg")
        Glide.with(this).asBitmap().load(list2[0]).into(simpleTarget)
        Glide.with(this).load(list2[0]).into(piv)

//        iv.setOnClickListener {
//            DragPhotoViewer(it.context)
//                    .setSrcView(iv)
//                    .setLoadImageListener { position, imageView ->
//                        Glide.with(it.context).load(list2[position]).into(imageView)
//                    }
//                    .show()
//        }


//        piv.translationX = iv.left.toFloat()
//        val matrix = Matrix(piv.imageMatrix)
//        matrix.postTranslate(400f, 300f)
//        piv.imageMatrix = matrix

        iv.setOnClickListener {
            val imageView = it as ImageView





            Log.e("tag", " w: ${width}  h: ${height} ")
//            piv.scaleType = imageView.scaleType
            //show
//            piv.layoutParams.apply {
//                width = 800
//                height = 900
////                width = windowManager.defaultDisplay.width
////                height = windowManager.defaultDisplay.height
//                piv.layoutParams = this
//            }
//            piv.translationX = iv.left.toFloat()
//            val matrix = Matrix(piv.imageMatrix)
//            matrix.postTranslate(100f, 100f)
//
//            piv.imageMatrix = matrix


        }

        piv.setOnViewTapListener { view, x, y ->
            val targetW = windowManager.defaultDisplay.width
            val targetHeight = targetW / (width*1f/height)
            Log.e("tag", piv.displayRect.toShortString())
            Log.e("tag", "targetW: ${targetW}   targetH: ${targetHeight}")

            val animator = ValueAnimator.ofInt(piv.width, targetW)
            val start = piv.height
            val startX = piv.translationX.toInt()

            piv.scaleType = ImageView.ScaleType.CENTER
            animator.addUpdateListener {
                piv.layoutParams.apply {
                    width = it.animatedValue as Int
                    height = evaluator.evaluate(it.animatedFraction, start*1f, targetHeight).toInt()
                    piv.layoutParams = this
                }
                piv.requestLayout()
//                piv.translationX = evaluator.evaluate(it.animatedFraction, startX, 0).toFloat()
            }
            animator.duration = 500
            animator.start()
        }

    }

    val evaluator = FloatEvaluator()
    var width: Int = 0
    var height: Int = 0
    val simpleTarget = object : SimpleTarget<Bitmap>() {
        override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
            width = resource.width
            height = resource.height
            iv.setImageBitmap(resource)
        }
    }

}
