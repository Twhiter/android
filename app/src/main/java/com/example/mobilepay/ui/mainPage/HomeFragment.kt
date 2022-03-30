package com.example.mobilepay.ui.mainPage

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.mobilepay.R
import com.example.mobilepay.databinding.FragmentEmailAndPhoneVerifyBinding
import com.example.mobilepay.databinding.FragmentHomeBinding


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
}