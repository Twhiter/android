package com.example.mobilepay.ui.mainPage

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.example.mobilepay.R
import com.example.mobilepay.databinding.FragmentHomeBinding
import com.example.mobilepay.ui.mainPage.model.MainPageViewModel


class HomeFragment : Fragment() {

    private val viewModel: MainPageViewModel by activityViewModels()

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
        binding.receiveLinearLayout.setOnClickListener { toReceive() }
        binding.transferLinearLayout.setOnClickListener { toTransfer() }
        binding.payBtn.setOnClickListener { toPay() }
        binding.requestBtn.setOnClickListener { toTransfer()}

        binding.viewModel = viewModel
        binding.lifecycleOwner = this
    }


    private fun toPay() {
        requireView().findNavController().navigate(R.id.scanFragment)
    }

    private fun toTransfer() {
        val action = HomeFragmentDirections.actionHomeFragmentToTransferChooseFragment()
        findNavController().navigate(action)
    }

    private fun toReceive() {
        val action = HomeFragmentDirections.actionHomeFragmentToQrCodeReceiveFragment2()
        findNavController().navigate(action)

    }




}