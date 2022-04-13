package com.example.mobilepay.ui.mainPage

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.*
import androidx.navigation.fragment.navArgs
import com.example.mobilepay.DecimalDigitsInputFilter
import com.example.mobilepay.R
import com.example.mobilepay.Util
import com.example.mobilepay.databinding.FragmentPayBinding
import com.example.mobilepay.entity.OverviewInfo
import com.example.mobilepay.entity.RespHandler
import com.example.mobilepay.entity.ResponseData
import com.example.mobilepay.entity.Type
import com.example.mobilepay.network.MerchantApi
import com.example.mobilepay.network.UserApi
import com.example.mobilepay.ui.mainPage.model.MainPageViewModel
import com.google.android.material.snackbar.Snackbar
import com.lzj.pass.dialog.PayPassDialog
import com.lzj.pass.dialog.PayPassView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PayFragment : Fragment() {

    private lateinit var binding: FragmentPayBinding
    private val viewModel:PayViewModel by viewModels()
    private val args:PayFragmentArgs by navArgs()
    private val activityViewModel:MainPageViewModel by activityViewModels()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPayBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.viewModel = viewModel
        binding.lifecycleOwner = this
        renderPayeeInfo()

        binding.amount.filters = arrayOf(DecimalDigitsInputFilter(5,2))


        binding.amount.addTextChangedListener(object :TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                if (s.isNullOrBlank())
                    binding.amount.error = requireContext().getString(R.string.empty_amount_prompt)
                else
                    binding.amount.error = null
            }

        })

        binding.payBtn.setOnClickListener {

            if (binding.amount.text.toString().isBlank()) {
                binding.amount.error = requireContext().getString(R.string.empty_amount_prompt)
                return@setOnClickListener
            }

            if (viewModel.type.value!! == Type.Merchant) {
                if (activityViewModel.merchant.value!!.merchantId == args.qrCodeContent.id) {
                    Toast.makeText(requireContext(),"Can't pay to self merchant",Toast.LENGTH_SHORT)
                        .show()
                    return@setOnClickListener
                }
            }else {
                if (activityViewModel.user.value!!.userId == args.qrCodeContent.id) {
                    Toast.makeText(requireContext(),"Can't transfer to self",Toast.LENGTH_SHORT)
                        .show()
                    return@setOnClickListener
                }
            }

            val dialog = PayPassDialog(requireContext(),R.style.dialog_pay_theme)

            Util.showPayDialog(requireContext(),"Pay","Forget Password?",
                object :PayPassView.OnPayClickListener{
                    override fun onPassFinish(password: String?) {




                    }

                    override fun onPayClose() {dialog.dismiss()}
                    override fun onPayForget() {}
                },dialog)
        }
    }






    private fun renderPayeeInfo() {
        val qrCodeContent = args.qrCodeContent
        viewModel.type.value = qrCodeContent.type

        lifecycleScope.launch(Dispatchers.IO) {

            val resp = when(qrCodeContent.type) {
                Type.User -> UserApi.service.fetchOverviewInfo(qrCodeContent.id)
                Type.Merchant -> MerchantApi.service.fetchOverviewInfo(qrCodeContent.id)
            }

            //handle response
            val isOkay = resp.handleOneWithDefault(requireContext()){
                    r ->
                if (r.data == null) {
                    lifecycleScope.launch(Dispatchers.Main) {
                        Toast.makeText(
                            requireContext(), "User doesn't exist",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    false
                }else {
                    true
                }
            }

            if (isOkay) {
                withContext(Dispatchers.Main) {
                    viewModel.overview.value = resp.data
                }
            }
        }
    }

}


class PayViewModel:ViewModel() {

    val type:MutableLiveData<Type> = MutableLiveData(Type.User)
    val overview = MutableLiveData(OverviewInfo.mock)
    val isLoading:LiveData<Boolean> = Transformations.map(overview) { it == OverviewInfo.mock }


    val btnTextResId:LiveData<Int> = Transformations.map(type) {
        if (it == Type.Merchant)
            R.string.pay_prompt
        else
            R.string.transfer_prompt
    }
}
