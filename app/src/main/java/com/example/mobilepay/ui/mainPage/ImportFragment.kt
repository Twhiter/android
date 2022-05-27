package com.example.mobilepay.ui.mainPage

import android.os.Bundle
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
import com.example.mobilepay.databinding.FragmentImportBinding
import com.example.mobilepay.network.ExportAndImportApi
import com.example.mobilepay.ui.mainPage.model.MainPageViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.math.BigDecimal
import java.math.RoundingMode


class ImportFragment : Fragment() {

    private lateinit var binding:FragmentImportBinding
    private val viewModel:ImportViewModel by viewModels()
    private val args:ImportFragmentArgs by navArgs()


    override fun onResume() {
        super.onResume()
        MainPageActivity.getInstance()?.setFullScreenVisibility(View.GONE)
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentImportBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        binding.viewModel = viewModel
        binding.lifecycleOwner = this


        binding.cardNumber.addTextChangedListener {
            checkCardNumber()
        }

        binding.month.addTextChangedListener {
            checkMM()
        }

        binding.year.addTextChangedListener {
            checkYY()
        }

        binding.CVC.addTextChangedListener {
            checkCVC()
        }

        binding.name.addTextChangedListener {
            checkName()
        }

        binding.amount.addTextChangedListener {
            checkAmount()
        }


        binding.importBtn.setOnClickListener {
            if (!checkAll())
                return@setOnClickListener

            viewModel.isLoading.value = true

            lifecycleScope.launch(Dispatchers.IO) {

                val token = MainApplication.db().kvDao().get("token")!!
                val userType: String = if (args.isUser) "user" else "merchant"
                val amount = BigDecimal(binding.amount.text.toString()).setScale(2, RoundingMode.UNNECESSARY)

                val resp = ExportAndImportApi.service.importFundsFromBank(token, userType, amount)

                withContext(Dispatchers.Main) {
                    resp.handleOneWithDefault(requireContext()) {_ ->

                        val prompt = resp.data!!

                        if (resp.data == "")
                            Toast.makeText(requireContext(),"Success",Toast.LENGTH_SHORT).show()
                        else
                            Toast.makeText(requireContext(),prompt,Toast.LENGTH_SHORT).show()

                        viewModel.isLoading.value = false
                        true
                    }
                }

            }
        }
    }

    private fun checkAll() =
        checkCardNumber() && checkMM() && checkYY() && checkCVC() && checkName() && checkAmount()


    private fun checkCardNumber():Boolean {

        if (binding.cardNumber.text.isNullOrBlank()) {
            binding.cardNumberInputLayout.error = "Please Input"
            return false
        }else {
            binding.cardNumberInputLayout.error = null
            return true
        }
    }

    private fun checkMM():Boolean {

        if (binding.month.text.isNullOrBlank()) {
            binding.month.error = "Please Input"
            return false
        }else {
            binding.month.error = null
            return true
        }
    }

    private fun checkYY():Boolean {
        if (binding.year.text.isNullOrBlank()) {
            binding.year.error = "Please Input"
            return false
        }else {
            binding.year.error = null
            return true
        }
    }

    private fun checkCVC():Boolean {
        if (binding.CVC.text.isNullOrBlank()) {
            binding.CVC.error = "Please Input"
            return false
        } else if (binding.CVC.text.length < 3) {
            binding.CVC.error = "Enter 3 digits CVC"
            return false
        }
        else {
            binding.CVC.error = null
            return true
        }
    }

    private fun checkName():Boolean {
        if (binding.name.text.isNullOrBlank()) {
            binding.nameInputLayout.error = "Please Input"
            return false
        }
        else {
            binding.nameInputLayout.error = null
            return true
        }
    }

    private fun checkAmount():Boolean {
        if (binding.amount.text.isNullOrBlank()) {
            binding.amountInputLayout.error = "Please Input"
            return false
        }
        else {
            binding.amountInputLayout.error = null
            return true
        }
    }




}


class ImportViewModel:ViewModel() {
    val isLoading = MutableLiveData(false)
}