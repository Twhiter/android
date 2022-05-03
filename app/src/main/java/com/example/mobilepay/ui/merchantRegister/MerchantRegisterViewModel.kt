package com.example.mobilepay.ui.merchantRegister

import android.graphics.Bitmap
import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import androidx.databinding.library.baseAdapters.BR
import androidx.lifecycle.ViewModel

class MerchantRegisterViewModel:ViewModel() {

    val merchantRegisterInfo = MerchantRegisterInfo()
}


class MerchantRegisterInfo :BaseObservable() {


    @get:Bindable
    var companyName = ""
    set(value) {
        field = value
        notifyPropertyChanged(BR.companyName)
    }

    @get:Bindable
    var licenseNumber = ""
    set(value) {
        field = value
        notifyPropertyChanged(BR.licenseNumber)
    }

    @get:Bindable
    var licensePhoto:Bitmap? = null
    set(value) {
        field = value
        notifyPropertyChanged(BR.licensePhoto)
    }

    @get:Bindable
    var phoneCode:String?=null
    set(value) {
        field = value
        notifyPropertyChanged(BR.phoneCode)
    }


    @get:Bindable
    var phoneDigits:String? = null
    set(value) {
        field = value
        notifyPropertyChanged(BR.phoneDigits)
    }


    @get:Bindable
    var email:String? = ""
    set(value) {
        field = value
        notifyPropertyChanged(BR.email)
    }


    val phoneNumber:String? get() {
        return if (phoneCode == null || phoneDigits == null)
            null
        else
            phoneCode + phoneDigits
    }


}