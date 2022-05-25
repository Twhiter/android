package com.example.mobilepay.ui.mainPage

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.mobilepay.databinding.FragmentBusinessDealBinding
import com.example.mobilepay.ui.mainPage.model.MainPageViewModel
import com.example.mobilepay.ui.merchantRegister.MerchantRegisterActivity


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

    }

    private fun toBill() {
        val action = BusinessDealFragmentDirections.actionBusinessDealFragmentToBillFragment()
        findNavController().navigate(action)
    }
}