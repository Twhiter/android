package com.example.mobilepay.ui.mainPage

import android.app.AlertDialog
import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.core.widget.addTextChangedListener
import com.example.mobilepay.R
import com.example.mobilepay.databinding.FragmentSelfExportImportBinding

class SelfExportAndImportDialog(val context: Context, inflater: LayoutInflater) {


    val binding = FragmentSelfExportImportBinding.inflate(inflater)
    private val builder: AlertDialog.Builder = AlertDialog.Builder(context, R.style.Theme_MobilePay)
    private lateinit var dialog: AlertDialog
    private lateinit var handler: () -> Unit

    fun setAvailable(amount: String): SelfExportAndImportDialog {
        binding.available.text = context.getString(R.string.available_amount, amount)
        return this
    }


    fun setOnOkay(text: String, handler: () -> Unit): SelfExportAndImportDialog {
        builder.setPositiveButton(text) { _, _ -> }
            .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
        this.handler = handler
        return this
    }

    fun show() {
        dialog = builder.setView(binding.root).create()

        dialog.window?.apply {
            setLayout(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT)
            setDimAmount(0.4f)
            setGravity(Gravity.CENTER)
        }

        dialog.show()
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            this.handler()
        }
    }

    fun dismiss() {
        dialog.dismiss()
    }

    companion object {

        fun showSelfExportImportDialog(
            context: Context,
            inflater: LayoutInflater,
            amountStr: String,
            text: String,
            handler: (FragmentSelfExportImportBinding, SelfExportAndImportDialog) -> Unit,
        ) {
            val dialog = SelfExportAndImportDialog(context, inflater)
            val viewBinding = dialog.binding

            fun checkAmount(): Boolean {
                return if (viewBinding.amount.text.isNullOrBlank()) {
                    viewBinding.amount.error = "Please Input"
                    false
                } else {
                    viewBinding.amount.error = null
                    true
                }
            }
            viewBinding.amount.addTextChangedListener { checkAmount() }

            dialog.setOnOkay(text) {
                if (!checkAmount())
                    return@setOnOkay
                handler(viewBinding, dialog)
            }.setAvailable(amountStr)
                .show()
        }

    }

}









