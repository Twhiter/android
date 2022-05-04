package com.example.mobilepay.ui.mainPage

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.mobilepay.R
import com.example.mobilepay.databinding.FragmentPaymentSuccessBinding

class PaymentSuccess : Fragment() {


    private val args: PaymentSuccessArgs by navArgs()
    private lateinit var binding: FragmentPaymentSuccessBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentPaymentSuccessBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.balanceAmount.text = getString(R.string.Byn_format, args.balanceAmount)
        binding.moneyAmount.text = getString(R.string.Byn_format, args.moneyAmount)
        binding.receiverName.text = args.receiverName


        binding.okay.setOnClickListener {
            val action = PaymentSuccessDirections.actionPaymentSuccessToHomeFragment2()
            findNavController().navigate(action)
        }


    }
}