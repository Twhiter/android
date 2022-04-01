package com.example.mobilepay.ui.mainPage

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.findNavController
import com.example.mobilepay.R
import com.example.mobilepay.databinding.FragmentEmailAndPhoneVerifyBinding
import com.example.mobilepay.databinding.FragmentHomeBinding
import com.example.mobilepay.ui.scan.ScanActivity


class HomeFragment : Fragment() {


    private lateinit var _binding:FragmentHomeBinding

    val binding get() = _binding



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

    }


    private fun toPay() {
        val intent = Intent(requireContext(),ScanActivity::class.java)
        requireActivity().startActivity(intent)
    }


}