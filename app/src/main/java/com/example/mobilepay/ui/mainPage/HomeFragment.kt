package com.example.mobilepay.ui.mainPage

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.findNavController
import com.example.mobilepay.R
import com.example.mobilepay.databinding.FragmentHomeBinding


class HomeFragment : Fragment() {

    private val viewModel:MainPageViewModel by activityViewModels()

    private lateinit var _binding:FragmentHomeBinding

    val binding get() = _binding

    override fun onResume() {
        super.onResume()
        MainPageActivity.getInstance()?.setFullScreenVisibility(View.VISIBLE)
    }



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {



        _binding = FragmentHomeBinding.inflate(inflater,container,false);
        return _binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        binding.payLinearLayout.setOnClickListener{ toPay() }
        binding.receiveLinearLayout.setOnClickListener {  }
        binding.transferLinearLayout.setOnClickListener {  }

        binding.payBtn.setOnClickListener { toPay() }


    }


    private fun toPay() {
        requireView().findNavController().navigate(R.id.scanFragment)
    }


}