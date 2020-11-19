package com.king.app.jgallery.view

import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.king.app.jgallery.model.GlideApp


/**
 * Desc:
 * @author：Jing Yang
 * @date: 2020/11/19 9:26
 */
@BindingAdapter("itemUrl")
fun setImageUrl(view: ImageView, url: String) {
    GlideApp.with(view.context)
        .load(url)
        .into(view)
}