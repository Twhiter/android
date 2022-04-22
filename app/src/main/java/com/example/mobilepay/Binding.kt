package com.example.mobilepay

import android.graphics.Bitmap
import android.widget.AutoCompleteTextView
import android.widget.ImageView
import androidx.core.net.toUri
import androidx.databinding.BindingAdapter
import coil.load
import com.example.mobilepay.network.BASE_URL


@BindingAdapter("imageBitmap")
fun loadImage(iv: ImageView, bitmap: Bitmap?) {
    if (bitmap != null)
        iv.setImageBitmap(bitmap)
}

@BindingAdapter("imageUrl")
fun bindImage(imgView: ImageView, imgUrl: String?) {

    imgUrl?.takeIf { it.isNotEmpty() }?.let {
        val imgUri = (BASE_URL + imgUrl).toUri().buildUpon().scheme("http").build()
        imgView.load(imgUri) {
            placeholder(R.drawable.loading_animation)
            error(R.drawable.ic_broken_image)
        }
    }
    if(imgUrl == null) {
        imgView.setImageResource(R.drawable.ic_broken_image)
    }
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


