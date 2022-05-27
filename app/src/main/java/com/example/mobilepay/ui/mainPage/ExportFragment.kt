package com.example.mobilepay.ui.mainPage

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import com.example.mobilepay.MainApplication
import com.example.mobilepay.databinding.FragmentExportBinding
import com.example.mobilepay.entity.Type
import com.example.mobilepay.network.ExportAndImportApi
import com.example.mobilepay.ui.lib.PayHandler
import com.example.mobilepay.ui.lib.PaymentDialog
import com.example.mobilepay.ui.mainPage.model.MainPageViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.math.BigDecimal
import java.math.RoundingMode

class ExportFragment : Fragment() {


    private lateinit var binding:FragmentExportBinding
    private val args: ExportFragmentArgs by navArgs()
    private val viewModel:ExportViewModel by viewModels()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentExportBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        MainPageActivity.getInstance()?.setFullScreenVisibility(View.GONE)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        binding.viewModel = viewModel
        binding.lifecycleOwner = this

        binding.amount.addTextChangedListener {
            checkAmount()
        }

        binding.cardNumber.addTextChangedListener {
            checkCardNumber()
        }

        binding.export.setOnClickListener {

            if (!(checkAmount() && checkCardNumber()))
                return@setOnClickListener

            val paymentDialog = PaymentDialog(requireContext(),layoutInflater)
            paymentDialog.setTitle("Export")
            paymentDialog.setForgetText("forget password?")

            paymentDialog.setHandler(object :PayHandler {
                override fun onFinish(password: String) {

                    lifecycleScope.launch(Dispatchers.IO) {

                        val token = MainApplication.db().kvDao().get("token")!!
                        val userType: String = if (args.isUser) "user" else "merchant"
                        val amount = BigDecimal(binding.amount.text.toString()).setScale(2,RoundingMode.UNNECESSARY)

                        withContext(Dispatchers.Main) {
                            viewModel.isLoading.value = true
                        }
                        val resp = ExportAndImportApi.
                        service.exportFundsToBank(token,userType,amount,password)



                        withContext(Dispatchers.Main) {
                            resp.handleOneWithDefault(requireContext()) {
                                val prompt = it.data!!
                                if (prompt == "")
                                    Toast.makeText(requireContext(),"Success",Toast.LENGTH_SHORT)
                                        .show()
                                else
                                    Toast.makeText(requireContext(),prompt,Toast.LENGTH_SHORT)
                                        .show()

                                paymentDialog.dismiss()
                                viewModel.isLoading.value = false
                                true
                            }
                        }


                    }
                }

                override fun onClose() {}

                override fun onForgetPassword() {}
            }).show()
        }



    }


    private fun checkCardNumber():Boolean {
        return if (binding.cardNumber.text.isNullOrBlank()) {
            binding.cardNumberInputLayout.error = "Please Input"
            false
        }else {
            binding.cardNumberInputLayout.error = null
            true
        }

    }

    private fun checkAmount():Boolean {
        return if (binding.amount.text.isNullOrBlank()) {
            binding.amountInputLayout.error = "Please Input"
            false
        } else {
            binding.amountInputLayout.error = null
            true
        }
    }

}

class ExportViewModel:ViewModel() {

    val isLoading = MutableLiveData(false)


}