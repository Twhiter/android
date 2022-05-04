package com.example.mobilepay.ui.lib

import android.app.AlertDialog
import android.content.Context
import android.content.res.ColorStateList
import android.text.InputType
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import com.example.mobilepay.R
import com.example.mobilepay.databinding.FragmentPayKeyboardBinding


class PaymentDialog(private val context: Context, inflater: LayoutInflater) {

    private val builder: AlertDialog.Builder = AlertDialog.Builder(context, R.style.Theme_MobilePay)
    private val binding: FragmentPayKeyboardBinding = FragmentPayKeyboardBinding.inflate(inflater)
    private var payHandler: PayHandler = PayHandler.Companion.DefaultHandler
    private lateinit var dialog: AlertDialog
    private val password: CharArray = CharArray(6)


    private var title: String = "Pay"

    private var forgetText = "forget password?"

    fun setTitle(title: String) {
        this.title = title
    }

    fun setForgetText(forgetText: String) {
        this.forgetText = forgetText
    }

    fun getTitle(): String {
        return this.title
    }

    fun getForgetText(): String {
        return this.forgetText
    }


    fun getBuilder(): AlertDialog.Builder {
        return builder
    }

    fun getBinding(): FragmentPayKeyboardBinding {
        return binding
    }

    fun getDialog(): AlertDialog {
        return this.dialog
    }

    fun setHandler(payHandler: PayHandler): PaymentDialog {
        this.payHandler = payHandler
        return this
    }

    fun show() {

        val numbers: List<Button> = listOf(
            binding.number0, binding.number1, binding.number2, binding.number3, binding.number4,
            binding.number5, binding.number6, binding.number7, binding.number8, binding.number9
        )

        val digits: List<EditText> = listOf(
            binding.in1, binding.in2, binding.in3, binding.in4, binding.in5, binding.in6)

        digits.map {
            it.inputType = InputType.TYPE_NULL
        }


        var currentPos = 0
        turnOn(digits[0], context)

        //set action for number button
        numbers.forEachIndexed { idx, btn ->
            btn.setOnClickListener {

                if (currentPos == 6)
                    return@setOnClickListener

                digits[currentPos].setText("*")
                this.password[currentPos] = ((idx + '0'.code).toChar())

                turnOff(digits[currentPos], context)
                currentPos++

                if (currentPos != 6)
                    turnOn(digits[currentPos], context)


                //finish inputing
                if (currentPos == 6) {
                    payHandler.onFinish(this.password.joinToString(""))
                }

            }
        }

        //set action for remove number
        binding.numberCancel.setOnClickListener {

            if (currentPos == 0)
                return@setOnClickListener

            if (currentPos != 6)
                turnOff(digits[currentPos], context)

            currentPos--

            digits[currentPos].text.clear()
            turnOn(digits[currentPos], context)

        }

        binding.close.setOnClickListener {
            this.payHandler.onClose()
            this.dialog.dismiss()
        }

        binding.forgetPassword.setOnClickListener {
            payHandler.onForgetPassword()
        }

        binding.forgetPassword.text = this.forgetText
        binding.title.text = this.title


        this.dialog = builder.setView(binding.root).create()

        this.dialog.window?.apply {
            setLayout(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT)
            setDimAmount(0.4f)
            setGravity(Gravity.BOTTOM)
        }

        this.dialog.show()
    }

    fun dismiss() {
        this.dialog.dismiss()
    }

    companion object {
        private fun turnOn(editText: EditText, context: Context) {
            editText.backgroundTintList = ColorStateList.valueOf(
                context.getColor(R.color.alipay))
        }

        private fun turnOff(editText: EditText, context: Context) {
            editText.backgroundTintList = ColorStateList.valueOf(
                context.getColor(R.color.black))
        }
    }

}

interface PayHandler {

    fun onFinish(password: String)
    fun onClose()
    fun onForgetPassword()

    companion object {
        object DefaultHandler : PayHandler {
            override fun onFinish(password: String) {}
            override fun onClose() {}
            override fun onForgetPassword() {}
        }
    }
}