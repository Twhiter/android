package com.example.mobilepay.ui.register

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat.finishAffinity
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.mobilepay.databinding.FragmentFinalBinding


/**
 * A simple [Fragment] subclass.
 * Use the [FinalFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class FinalFragment : Fragment() {

    private lateinit var _binding: FragmentFinalBinding

    val binding get() = _binding

    private val viewModel:RegisterViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentFinalBinding.inflate(inflater, container, false)
        return _binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.lifecycleOwner = this
        binding.viewModel = viewModel


        binding.close.setOnClickListener {
            finishAffinity(requireActivity())
        }
    }
}