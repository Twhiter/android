package com.example.mobilepay.ui.mainPage

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.mobilepay.R
import com.example.mobilepay.databinding.FragmentTransferChooseBinding


private var headerPos:MutableList<Int> = mutableListOf()

class TransferChooseFragment : Fragment() {

    private lateinit var binding:FragmentTransferChooseBinding


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentTransferChooseBinding.inflate(inflater,container,false)
        return binding.root
    }
}




