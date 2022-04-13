package com.example.mobilepay.ui.mainPage.model

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
import java.math.BigDecimal

class MainPageViewModel:ViewModel() {
    var user:MutableLiveData<User> = MutableLiveData(User.mock)
    var merchant:MutableLiveData<Merchant> = MutableLiveData(Merchant.mock)

    val userLoading:MutableLiveData<Boolean> = MutableLiveData(true)
    val merchantExist:MutableLiveData<Boolean> = MutableLiveData(false)

    val userMoneyFormatted:LiveData<String> = Transformations.map(user) {
        Util.formatBigDecimalToStr(user.value!!.moneyAmount)
    }
    val userFrozenMoneyFormatted:LiveData<String> = Transformations.map(user) {
        Util.formatBigDecimalToStr(user.value!!.frozenMoney)
    }

    val merchantMoneyFormatted:LiveData<String> = Transformations.map(user) {
        Util.formatBigDecimalToStr(merchant.value!!.moneyAmount)
    }

    val merchantFrozenMoneyFormatted:LiveData<String> = Transformations.map(user) {
        Util.formatBigDecimalToStr(merchant.value!!.frozenMoney)
    }

    init {
        viewModelScope.launch {
            val userId = MainApplication.db().kvDao().get("userId")!!

            CoroutineScope(Dispatchers.IO).launch {
                MainApplication.db().userDao().get(userId.toInt()).collect {

                    withContext(Dispatchers.Main) {
                        user.value = it
                        userLoading.value = false
                    }
                }
            }


            CoroutineScope(Dispatchers.IO).launch {
                MainApplication.db().merchantDao().get(userId.toInt()).collect {

                    withContext(Dispatchers.Main) {
                        merchant.value = it
                        merchantExist.value = true
                    }

                }
            }
        }
    }

}