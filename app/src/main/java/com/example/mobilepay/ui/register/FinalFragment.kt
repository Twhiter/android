package com.example.mobilepay.ui.register

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat.finishAffinity
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.example.mobilepay.databinding.FragmentFinalBinding

class FinalFragment : Fragment() {

    private lateinit var _binding: FragmentFinalBinding

    val binding get() = _binding

    val args: FinalFragmentArgs by navArgs()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {

        _binding = FragmentFinalBinding.inflate(inflater, container, false)
        return _binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.lifecycleOwner = this
        binding.email = args.email
        binding.phoneNumber = args.phoneNumber

        binding.close.setOnClickListener {
            finishAffinity(requireActivity())
        }
    }
}