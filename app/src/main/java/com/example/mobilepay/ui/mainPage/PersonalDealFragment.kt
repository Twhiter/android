package com.example.mobilepay.ui.mainPage

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.mobilepay.Util
import com.example.mobilepay.databinding.FragmentPersonalDealBinding
import com.example.mobilepay.entity.Merchant
import com.example.mobilepay.ui.mainPage.model.MainPageViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.math.RoundingMode


class PersonalDealFragment : Fragment() {


    private val viewModel: MainPageViewModel by activityViewModels()
    private lateinit var binding: FragmentPersonalDealBinding

    override fun onResume() {
        super.onResume()
        MainPageActivity.getInstance()?.setFullScreenVisibility(View.VISIBLE)
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {

        binding = FragmentPersonalDealBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.viewModel = viewModel
        binding.lifecycleOwner = this

        binding.transferBtn.setOnClickListener { toTransfer() }
        binding.bill.setOnClickListener { toBill() }


        binding.importBtn.setOnClickListener {
            val items = arrayOf("Import from Credit Card", "Import from Merchant Account")
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Import")
                .setItems(items) { _, which ->
                    when (which) {
                        0 -> toImport()
                        1 -> trySelfImport()
                    }
                }
                .show()
        }

        binding.exportBnt.setOnClickListener {
            val items = arrayOf("Export to Credit Card", "Export to Merchant Account")
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Export")
                .setItems(items) { _, which ->

                    when (which) {
                        0 -> toExport()
                        1 -> trySelfExport()
                    }
                }
                .show()
        }
    }

    private val merchantExist
        get() = run {
            !(viewModel.merchant.value == Merchant.mock || viewModel.merchant.value == null)
        }


    private fun trySelfExport() {

        if (merchantExist)
            exportToSelfAccount()
        else
            Toast.makeText(requireContext(),
                "You need to register a merchant account", Toast.LENGTH_SHORT)
                .show()

    }

    private fun trySelfImport() {
        if (merchantExist)
            importFromSelfAccount()
        else
            Toast.makeText(requireContext(),
                "You need to register a merchant account", Toast.LENGTH_SHORT)
                .show()
    }


    private fun toImport() {
        val action = PersonalDealFragmentDirections.actionPersonalDealFramentToImportFragment()
        findNavController().navigate(action)
    }

    private fun toExport() {
        val action = PersonalDealFragmentDirections.actionPersonalDealFramentToExportFragment()
        findNavController().navigate(action)
    }

    private fun toTransfer() {
        val action =
            PersonalDealFragmentDirections.actionPersonalDealFramentToTransferChooseFragment()
        findNavController().navigate(action)
    }

    private fun toBill() {
        val action = PersonalDealFragmentDirections.actionPersonalDealFramentToBillFragment(true)
        findNavController().navigate(action)
    }

    private fun exportToSelfAccount() {

        SelfExportAndImportDialog.showSelfExportImportDialog(requireContext(),
            layoutInflater,
            viewModel.user.value!!
                .moneyAmount.setScale(2, RoundingMode.FLOOR).toString(),
            "Export to Merchant") { viewBinding, dialog ->
            Util.export(lifecycleScope, viewBinding, requireContext(), dialog)

        }


    }

    private fun importFromSelfAccount() {


        SelfExportAndImportDialog.showSelfExportImportDialog(requireContext(),
            layoutInflater,
            viewModel.merchant.value!!.moneyAmount.setScale(2, RoundingMode.FLOOR).toString(),
            "Import from Merchant") { viewBinding, dialog ->
            Util.importFromMerchant(lifecycleScope, viewBinding, requireContext(), dialog)
        }


    }

}