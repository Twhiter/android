package com.example.mobilepay.ui.mainPage

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.mobilepay.databinding.FragmentPersonalDealBinding
import com.example.mobilepay.ui.mainPage.model.MainPageViewModel


class PersonalDealFragment : Fragment() {


    private val viewModel: MainPageViewModel by activityViewModels()
    private lateinit var binding: FragmentPersonalDealBinding

    override fun onResume() {
        super.onResume()
        MainPageActivity.getInstance()?.setFullScreenVisibility(View.VISIBLE)
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {

        binding = FragmentPersonalDealBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.viewModel = viewModel
        binding.lifecycleOwner = this

        binding.transferBtn.setOnClickListener { toTransfer() }
        binding.bill.setOnClickListener { toBill() }


        binding.importBtn.setOnClickListener {
            val action = PersonalDealFragmentDirections.actionPersonalDealFramentToImportFragment()
            findNavController().navigate(action)
        }

        binding.exportBnt.setOnClickListener {
            val action = PersonalDealFragmentDirections.actionPersonalDealFramentToExportFragment()
            findNavController().navigate(action)
        }

    }

    private fun toTransfer() {
        val action =
            PersonalDealFragmentDirections.actionPersonalDealFramentToTransferChooseFragment()
        findNavController().navigate(action)
    }

    private fun toBill() {
        val action = PersonalDealFragmentDirections.actionPersonalDealFramentToBillFragment(true)
        findNavController().navigate(action)
    }


}