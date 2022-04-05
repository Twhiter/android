package com.example.mobilepay.ui.mainPage

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.example.mobilepay.R
import com.example.mobilepay.databinding.FragmentPersonalDealBinding


class PersonalDealFragment : Fragment() {


    private val viewModel:MainPageViewModel by activityViewModels()
    private lateinit var bining:FragmentPersonalDealBinding

    override fun onResume() {
        super.onResume()
        MainPageActivity.getInstance()?.setFullScreenVisibility(View.VISIBLE)
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        bining = FragmentPersonalDealBinding.inflate(inflater,container,false)

        return bining.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    }


}