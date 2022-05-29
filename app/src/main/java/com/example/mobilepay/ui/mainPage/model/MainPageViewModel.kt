package com.example.mobilepay.ui.mainPage.model

import android.util.Log
import androidx.lifecycle.*
import com.example.mobilepay.MainApplication
import com.example.mobilepay.Util
import com.example.mobilepay.entity.Merchant
import com.example.mobilepay.entity.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainPageViewModel : ViewModel() {
    var user: MutableLiveData<User> = MutableLiveData(User.mock)
    var merchant: MutableLiveData<Merchant> = MutableLiveData(Merchant.mock)

    val userLoading: LiveData<Boolean> = Transformations.map(user) {
        (it == null || it == User.mock)
    }
    val merchantExistAndVerified: LiveData<Boolean> = Transformations.map(merchant) {
        !(it == null || it == Merchant.mock || it.state == "unverified")
    }

    val merchantExist: LiveData<Boolean> = Transformations.map(merchant) {
        !(it == null || it == Merchant.mock )
    }




    val userMoneyFormatted: LiveData<String> = Transformations.map(user) {
        if (it != null)
            Util.formatBigDecimalToStr(it.moneyAmount)
        else ""
    }
    val userFrozenMoneyFormatted: LiveData<String> = Transformations.map(user) {
        if (it != null)
            Util.formatBigDecimalToStr(it.frozenMoney)
        else ""
    }

    val merchantMoneyFormatted: LiveData<String> = Transformations.map(merchant) {
        if (it != null)
            Util.formatBigDecimalToStr(it.moneyAmount)
        else ""
    }

    val merchantFrozenMoneyFormatted: LiveData<String> = Transformations.map(user) {
        if (it != null)
            Util.formatBigDecimalToStr(it.frozenMoney)
        else ""
    }

    init {
        viewModelScope.launch {
            val userId = MainApplication.db().kvDao().get("userId") ?: return@launch

            CoroutineScope(Dispatchers.IO).launch {
                MainApplication.db().userDao().get(userId.toInt()).collect {

                    withContext(Dispatchers.Main) {
                        user.value = it
                    }
                }
            }


            viewModelScope.launch(Dispatchers.IO) {
                MainApplication.db().merchantDao().get(userId.toInt()).collect {
                    withContext(Dispatchers.Main) {
                        merchant.value = it
                        Log.d("Mainss", if (it == null) "" else it.toString())
                    }
                }
            }
        }
    }

}