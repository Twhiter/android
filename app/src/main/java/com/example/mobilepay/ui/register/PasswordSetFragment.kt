package com.example.mobilepay.ui.register

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import com.example.mobilepay.R
import com.example.mobilepay.databinding.FragmentRegisterPasswordSetBinding
import com.lzj.pass.dialog.PayPassDialog
import com.lzj.pass.dialog.PayPassView


class PasswordSetFragment : Fragment() {

    private lateinit var _binding: FragmentRegisterPasswordSetBinding

    val binding get() = _binding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentRegisterPasswordSetBinding.inflate(inflater, container, false)
        return _binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        //check password and repeat password the same
        binding.confirmPassword.setOnFocusChangeListener {_,_ ->
            val confirmPwd = binding.confirmPassword.text.toString()
            val password = binding.password.text.toString()
            if (confirmPwd != password)
                binding.confirmPasswordLayout.error = getString(R.string.password_not_same_prompt)
            else
                binding.confirmPasswordLayout.error = null
        }

        binding.paymentPassword.setOnClickListener {
            payDialog()
        }

    }


    private fun payDialog() {

        val dialog = PayPassDialog(requireContext())
        dialog.payViewPass
            .setRandomNumber(true)
            .setHintText("Please Input password")
            .setForgetText("Forget Password?")
            .setPayClickListener(object :PayPassView.OnPayClickListener {
                override fun onPassFinish(password: String?) {

                }

                override fun onPayClose() {
                    dialog.dismiss()
                }

                override fun onPayForget() {

                }

            })

        dialog.payViewPass.setBackgroundColor(requireContext().getColor(R.color.white))
    }



}