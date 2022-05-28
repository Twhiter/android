package com.example.mobilepay.ui.mainPage

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.mobilepay.MainApplication
import com.example.mobilepay.R
import com.example.mobilepay.Util
import com.example.mobilepay.databinding.FragmentBusinessDealBinding
import com.example.mobilepay.network.ExportAndImportApi
import com.example.mobilepay.ui.mainPage.model.MainPageViewModel
import com.example.mobilepay.ui.merchantRegister.MerchantRegisterActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.math.BigDecimal
import java.math.RoundingMode


class BusinessDealFragment : Fragment() {


    private val viewModel: MainPageViewModel by activityViewModels()
    private lateinit var binding: FragmentBusinessDealBinding

    override fun onResume() {
        super.onResume()
        MainPageActivity.getInstance()?.setFullScreenVisibility(View.VISIBLE)
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentBusinessDealBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        binding.viewModel = viewModel
        binding.lifecycleOwner = this

        binding.apply.setOnClickListener {
            val intent = Intent(requireContext(), MerchantRegisterActivity::class.java)
            startActivity(intent)
        }

        binding.bill.setOnClickListener {
            toBill()
        }

        binding.importBtn.setOnClickListener {
            val items = arrayOf("Import from Credit Card", "Import from Individual Account")
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Import funds from individual account")
                .setItems(items) { _, which ->
                    when (which) {
                        0 -> toImport()
                        1 -> importFromSelfAccount()
                    }
                }
                .show()
        }

        binding.exportBnt.setOnClickListener {
            val items = arrayOf("Export to Credit Card", "Export to Individual Account")
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Export funds to individual account")
                .setItems(items) { _, which ->

                    when (which) {
                        0 -> toExport()
                        1 -> exportToSelfAccount()
                    }
                }
                .show()
        }
    }

    private fun toImport() {
        val action = BusinessDealFragmentDirections.actionBusinessDealFragmentToImportFragment()
        findNavController().navigate(action)
    }

    private fun toExport() {
        val action = BusinessDealFragmentDirections.actionBusinessDealFragmentToExportFragment()
        findNavController().navigate(action)
    }


    private fun toBill() {
        val action = BusinessDealFragmentDirections.actionBusinessDealFragmentToBillFragment()
        findNavController().navigate(action)
    }


    private fun exportToSelfAccount() {

        SelfExportAndImportDialog.showSelfExportImportDialog(requireContext(),
            layoutInflater, viewModel.merchant.value!!
                .moneyAmount.setScale(2,RoundingMode.FLOOR).toString(),
            "Export to Individual Account") {viewBinding,dialog ->

            Util.importFromMerchant(lifecycleScope,viewBinding,requireContext(),dialog)

        }


    }

    private fun importFromSelfAccount() {
        SelfExportAndImportDialog.showSelfExportImportDialog(requireContext(),
            layoutInflater,
            viewModel.user.value!!.moneyAmount
                .setScale(2, RoundingMode.FLOOR).toString(),
            "Import from Individual Account") { viewBinding, dialog ->
            Util.export(lifecycleScope,viewBinding,requireContext(),dialog)
        }
    }


}