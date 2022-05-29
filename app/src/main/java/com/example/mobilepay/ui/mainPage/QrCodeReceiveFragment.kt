package com.example.mobilepay.ui.mainPage

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.VectorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.*
import com.example.mobilepay.MainApplication
import com.example.mobilepay.R
import com.example.mobilepay.Util
import com.example.mobilepay.databinding.FragmentQrCodeBinding
import com.example.mobilepay.entity.QrCodeContent
import com.example.mobilepay.entity.Type
import com.example.mobilepay.network.BASE_URL
import com.example.mobilepay.ui.mainPage.model.MainPageViewModel
import com.fasterxml.jackson.databind.ObjectMapper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.URL


class QrCodeReceiveFragment : Fragment() {

    private lateinit var binding: FragmentQrCodeBinding
    private val activityViewModel: MainPageViewModel by activityViewModels()
    private val viewModel: QrCodeReceiveModel by viewModels {
        QrCodeReceiveViewModelFactory(activityViewModel)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentQrCodeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.viewModel = viewModel
        binding.lifecycleOwner = this

        viewModel.avatarUrl.observe(viewLifecycleOwner) {

            lifecycleScope.launch(Dispatchers.IO) {
                val logo = try {
                    val url = URL(it)
                    BitmapFactory.decodeStream(url.openConnection().getInputStream())
                } catch (e: Exception) {
                    e.printStackTrace()
                    (ResourcesCompat.getDrawable(requireContext().resources,
                        R.drawable.ic_broken_image,
                        null) as VectorDrawable).toBitmap()
                }

                val id: Int
                val type: Type

                if (viewModel.isUser.value!!) {
                    id = activityViewModel.user.value!!.userId
                    type = Type.User
                } else {
                    id = activityViewModel.merchant.value!!.merchantId
                    type = Type.Merchant
                }

                val qrCodeContentStr = ObjectMapper().writeValueAsString(QrCodeContent(id, type))
                val bitmap = try {
                    Util.getQrCodeBitmapWithLogo(qrCodeContentStr, logo)
                } catch (e: Exception) {
                    e.printStackTrace()
                    (ResourcesCompat.getDrawable(requireContext().resources,
                        R.drawable.ic_broken_image,
                        null) as VectorDrawable).toBitmap()
                }

                withContext(Dispatchers.Main) {
                    viewModel.qrCodeBitMap.value = bitmap
                }

            }
        }

        binding.switchBtn.setOnClickListener {
            viewModel.isUser.value = !viewModel.isUser.value!!
        }


    }

    override fun onResume() {
        super.onResume()
        MainPageActivity.getInstance()?.setFullScreenVisibility(View.GONE)
    }
}

class QrCodeReceiveModel(private val activityViewModel: MainPageViewModel) : ViewModel() {

    val isUser = MutableLiveData(true)

    val title: LiveData<String> = Transformations.map(isUser) {
        if (it)
            MainApplication.applicationContext().getString(R.string.user_receive_qr_code)
        else
            MainApplication.applicationContext().getString(R.string.merchant_receive_qr_code)
    }

    val isBtnEnabled: LiveData<Boolean> = activityViewModel.merchantExistAndVerified
    val avatarUrl: LiveData<String> = Transformations.map(isUser) {
        if (it)
            BASE_URL + activityViewModel.user.value!!.avatar
        else
            BASE_URL + activityViewModel.merchant.value!!.merchantLogo
    }

    val qrCodeBitMap: MutableLiveData<Bitmap> = MutableLiveData(null)
}

class QrCodeReceiveViewModelFactory(private val activityViewModel: MainPageViewModel) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return (QrCodeReceiveModel(activityViewModel)) as T
    }

}

