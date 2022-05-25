package com.example.mobilepay.ui.mainPage

import android.app.ProgressDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.*
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.mobilepay.DecimalDigitsInputFilter
import com.example.mobilepay.MainApplication
import com.example.mobilepay.R
import com.example.mobilepay.Util
import com.example.mobilepay.databinding.FragmentPayBinding
import com.example.mobilepay.entity.*
import com.example.mobilepay.network.MerchantApi
import com.example.mobilepay.network.PayApi
import com.example.mobilepay.network.TransferApi
import com.example.mobilepay.network.UserApi
import com.example.mobilepay.ui.lib.PayHandler
import com.example.mobilepay.ui.lib.PaymentDialog
import com.example.mobilepay.ui.mainPage.model.MainPageViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.math.BigDecimal
import java.math.RoundingMode


class PayFragment : Fragment() {

    private lateinit var binding: FragmentPayBinding
    private val viewModel: PayViewModel by viewModels()
    private val args: PayFragmentArgs by navArgs()
    private val activityViewModel: MainPageViewModel by activityViewModels()

    private lateinit var paymentDialog: PaymentDialog


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentPayBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.amount.value = if (args.qrCodeContent.amount != null)
            args.qrCodeContent.amount!!.setScale(2, RoundingMode.UNNECESSARY).toString()
        else null

        binding.viewModel = viewModel
        binding.lifecycleOwner = this

        renderPayeeInfo()

        binding.amount.filters = arrayOf(DecimalDigitsInputFilter(5, 2))


        binding.amount.addTextChangedListener(object : TextWatcher {
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
                    Toast.makeText(requireContext(),
                        "Can't pay to self merchant",
                        Toast.LENGTH_SHORT)
                        .show()
                    return@setOnClickListener
                }
            } else {
                if (activityViewModel.user.value!!.userId == args.qrCodeContent.id) {
                    Toast.makeText(requireContext(), "Can't transfer to self", Toast.LENGTH_SHORT)
                        .show()
                    return@setOnClickListener
                }
            }

            paymentDialog = PaymentDialog(requireContext(), layoutInflater)
            paymentDialog.setTitle("Pay")
            paymentDialog.setForgetText("forget password?")
            paymentDialog.setHandler(object : PayHandler {
                override fun onFinish(password: String) {
                    when (args.qrCodeContent.type) {
                        Type.Merchant -> pay(password)
                        Type.User -> transfer(password)
                        Type.MerchantWithConfirmation -> payWithConfirm(password)
                    }
                }

                override fun onClose() {}
                override fun onForgetPassword() {}
            }).show()

        }
    }



    private fun transfer(paymentPassword: String) {

        val db = MainApplication.db()
        lifecycleScope.launch(Dispatchers.IO) {
            val token = db.kvDao().get("token")
            val amount =
                BigDecimal(binding.amount.text.toString()).setScale(2, RoundingMode.UNNECESSARY)

            if (token == null)
                return@launch


            val progressDialog: ProgressDialog
            withContext(Dispatchers.Main) {
                progressDialog = ProgressDialog.show(requireContext(), "Waiting for Transfer",
                    "", false)
            }

            withContext(Dispatchers.IO) {
                val resp = TransferApi.service.transfer(token,
                    args.qrCodeContent.id,
                    amount,
                    paymentPassword,
                    binding.remarks.text.toString())

                handleTransferResp(resp)
            }

            withContext(Dispatchers.Main) {
                progressDialog.dismiss()
                paymentDialog.dismiss()
            }

        }
    }
    private fun pay(paymentPassword: String) {
        val db = MainApplication.db()
        lifecycleScope.launch(Dispatchers.IO) {

            val token = db.kvDao().get("token")
            val amount =
                BigDecimal(binding.amount.text.toString()).setScale(2, RoundingMode.UNNECESSARY)

            if (token == null)
                return@launch


            val progressDialog: ProgressDialog
            withContext(Dispatchers.Main) {
                progressDialog = ProgressDialog.show(requireContext(), "Waiting for Payment",
                        "", false)
            }

            // to let execute serially
            withContext(Dispatchers.IO) {
                val resp = PayApi.service.pay(token,
                    args.qrCodeContent.id,
                    amount,
                    paymentPassword,
                    binding.remarks.text.toString())
                handlePaymentResp(resp)
            }

            withContext(Dispatchers.Main) {
                progressDialog.dismiss()
                paymentDialog.dismiss()
            }

        }
    }
    private fun payWithConfirm(paymentPassword: String) {
        val db = MainApplication.db()


        lifecycleScope.launch(Dispatchers.IO) {
            val token = db.kvDao().get("token")
            val amount =
                BigDecimal(binding.amount.text.toString()).setScale(2, RoundingMode.UNNECESSARY)

            if (token == null)
                return@launch

            val progressDialog: ProgressDialog
            withContext(Dispatchers.Main) {
                progressDialog = ProgressDialog.show(requireContext(), "Waiting for Payment",
                    "", false)
            }

            // to let execute serially
            withContext(Dispatchers.IO) {
                val resp = PayApi.service.payWithConfirm(token,
                    args.qrCodeContent.sessionId!!,
                    paymentPassword,
                    binding.remarks.text.toString())
                handlePaymentResp(resp)
            }

            withContext(Dispatchers.Main) {
                progressDialog.dismiss()
                paymentDialog.dismiss()
            }


        }

    }

    private fun handleTransferResp(resp: ResponseData<TransferResp>) {

        resp.handleOneWithDefault(requireContext()) { r ->

            if (r.data == null)
                false
            else {
                if (r.data.transfer == null) {
                    lifecycleScope.launch(Dispatchers.Main) {
                        Toast.makeText(requireContext(),
                            r.data.prompt,
                            Toast.LENGTH_SHORT).show()
                    }
                    false
                } else {
                    lifecycleScope.launch(Dispatchers.Main) {
                        Toast.makeText(requireContext(),
                            "Transfer successfully",
                            Toast.LENGTH_SHORT).show()
                        Util.updateSelfInfo()
                        val action = PayFragmentDirections
                            .actionPayFragmentToPaymentSuccess(
                                binding.amount.text.toString(),
                                viewModel.overview.value!!.name,
                                activityViewModel.user.value!!.moneyAmount.setScale(2)
                                    .toString()
                            )
                        findNavController().navigate(action)
                    }
                    true
                }
            }
        }



    }



    private fun handlePaymentResp(resp:ResponseData<PayResp>) {

        resp.handleOneWithDefault(requireContext()) { r ->

            if (r.data == null)
                false
            else {
                if (r.data.payOverview == null) {
                    lifecycleScope.launch(Dispatchers.Main) {
                        Toast.makeText(requireContext(),
                            r.data.prompt,
                            Toast.LENGTH_SHORT).show()
                    }
                    false
                } else {
                    lifecycleScope.launch(Dispatchers.Main) {
                        Toast.makeText(requireContext(),
                            "Pay successfully",
                            Toast.LENGTH_SHORT).show()
                        Util.updateSelfInfo()

                        val action = PayFragmentDirections
                            .actionPayFragmentToPaymentSuccess(
                                binding.amount.text.toString(),
                                viewModel.overview.value!!.name,
                                activityViewModel.user.value!!.moneyAmount.setScale(2)
                                    .toString()
                            )
                        findNavController().navigate(action)
                    }
                    true
                }
            }
        }
    }






    private fun renderPayeeInfo() {
        val qrCodeContent = args.qrCodeContent
        viewModel.type.value = qrCodeContent.type

        lifecycleScope.launch(Dispatchers.IO) {

            val resp = when (qrCodeContent.type) {
                Type.User -> UserApi.service.fetchOverviewInfo(qrCodeContent.id)
                Type.Merchant -> MerchantApi.service.fetchOverviewInfo(qrCodeContent.id)
                Type.MerchantWithConfirmation -> MerchantApi.service.fetchOverviewInfo(qrCodeContent.id)
            }

            //handle response
            val isOkay = resp.handleOneWithDefault(requireContext()) { r ->
                if (r.data == null) {
                    lifecycleScope.launch(Dispatchers.Main) {
                        Toast.makeText(
                            requireContext(), "User doesn't exist",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    false
                } else {
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

class PayViewModel : ViewModel() {

    val type: MutableLiveData<Type> = MutableLiveData(Type.User)
    val overview = MutableLiveData(OverviewInfo.mock)
    val isLoading: LiveData<Boolean> = Transformations.map(overview) { it == OverviewInfo.mock }
    val amount:MutableLiveData<String> = MutableLiveData(null)


    val btnTextResId: LiveData<Int> = Transformations.map(type) {
        if (it == Type.Merchant || it == Type.MerchantWithConfirmation)
            R.string.pay_prompt
        else
            R.string.transfer_prompt
    }
}
