package com.example.mobilepay.ui.mainPage

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.mobilepay.MainActivity
import com.example.mobilepay.MainApplication
import com.example.mobilepay.databinding.FragmentSettingBinding
import com.example.mobilepay.ui.mainPage.model.MainPageViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class SettingFragment : Fragment() {

    private lateinit var binding:FragmentSettingBinding
    private val viewModel: MainPageViewModel by activityViewModels()

    override fun onResume() {
        super.onResume()
        MainPageActivity.getInstance()?.setFullScreenVisibility(View.GONE)
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSettingBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.viewModel = viewModel
        binding.lifecycleOwner = this

        binding.logout.setOnClickListener {
            //clear token
            lifecycleScope.launch(Dispatchers.IO) {
                MainApplication.db().kvDao().deleteAll()
                withContext(Dispatchers.Main) {
                    MainActivity.toLoginPage(this@SettingFragment.requireActivity())
                }
            }
        }

        binding.userDetail.setOnClickListener {
            val action = SettingFragmentDirections.actionSettingFragmentToUserSettingDetailFragment()
            findNavController().navigate(action)
        }

        binding.merchantDetail.setOnClickListener {
            val action = SettingFragmentDirections.actionSettingFragmentToMerchantSettingDetailFragment()
            findNavController().navigate(action)
        }

    }



}