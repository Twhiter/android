package com.example.mobilepay.ui.mainPage

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.mobilepay.databinding.FragmentSettingMerchantBinding
import com.example.mobilepay.ui.mainPage.model.MainPageViewModel


class MerchantSettingDetailFragment : Fragment() {

    private val viewModel: MainPageViewModel by activityViewModels()
    private lateinit var binding: FragmentSettingMerchantBinding


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentSettingMerchantBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.viewModel = viewModel
        binding.lifecycleOwner = this
    }

    override fun onResume() {
        super.onResume()
        MainPageActivity.getInstance()?.setFullScreenVisibility(View.GONE)
    }


}