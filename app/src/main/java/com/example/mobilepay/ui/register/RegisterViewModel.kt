package com.example.mobilepay.ui.register

import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class RegisterViewModel : ViewModel() {

    private val _firstName = MutableLiveData<String>()
    val firstName: LiveData<String> get() = _firstName

    private val _familyName = MutableLiveData<String>()
    val familyName: LiveData<String> get() = _familyName

    private val _nationality = MutableLiveData<String>()
    val nationality: LiveData<String> get() = _nationality

    private val _IdNumber = MutableLiveData<String>()
    val IdNumber: LiveData<String> get() = _IdNumber

    private val _IdPhoto = MutableLiveData<Bitmap>(null)
    val IdPhoto: LiveData<Bitmap> get() = _IdPhoto

    private val _phoneCode = MutableLiveData<String>()
    val phoneCode: LiveData<String> get() = _phoneCode

    private val _phone = MutableLiveData<String>()
    val phone: LiveData<String> get() = _phone

    private val _email = MutableLiveData<String>()
    val email: LiveData<String> get() = _email

    private val _password = MutableLiveData<String>()
    val password: LiveData<String> get() = _password

    private val _paymentPassword = MutableLiveData<String>()
    val paymentPassword: LiveData<String> get() = _paymentPassword

    val phoneNumber: String get() = phoneCode.value + phone.value


    fun setFirstName(firstName: String) {
        _firstName.value = firstName
    }

    fun setFamilyName(familyName: String) {
        _familyName.value = familyName
    }

    fun setNationality(nationality: String) {
        _nationality.value = nationality
    }

    fun setIdNumber(IdNumber: String) {
        _IdNumber.value = IdNumber
    }

    fun setIdPhoto(IdPhoto: Bitmap) {
        _IdPhoto.value = IdPhoto
    }

    fun setPhoneCode(phoneCode: String) {
        _phoneCode.value = phoneCode
    }

    fun setPhone(phone: String) {
        _phone.value = phone
    }

    fun setEmail(email: String) {
        _email.value = email
    }

    fun setPassword(password: String) {
        _password.value = password
    }

    fun setPaymentPassword(paymentPassword: String) {
        _paymentPassword.value = paymentPassword
    }
}