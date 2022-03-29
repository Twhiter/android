package com.example.mobilepay

import android.graphics.Bitmap
import android.widget.AutoCompleteTextView
import android.widget.ImageView

import androidx.databinding.BindingAdapter


@BindingAdapter("imageBitmap")
fun loadImage(iv: ImageView, bitmap: Bitmap?) {
    if (bitmap != null)
        iv.setImageBitmap(bitmap)
}

@BindingAdapter("selectedItem")
fun setSelection(atv: AutoCompleteTextView,value:String?) {
    value?.apply {
        atv.adapter.autofillOptions?.apply {
            atv.listSelection = indexOf(value)
        }
    }
}

fun string(resourceId: Int,vararg formatArgs:Any) = MainApplication.applicationContext()
    .getString(resourceId,formatArgs)


