package com.example.mobilepay.ui.register

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.mobilepay.R
import com.example.mobilepay.databinding.FragmentEmailAndPhoneVerifyBinding
import com.example.mobilepay.databinding.FragmentRegisterPasswordSetBinding


class PasswordSetFragment : Fragment() {

    private lateinit var _binding: FragmentRegisterPasswordSetBinding

    val binding get() = _binding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentRegisterPasswordSetBinding.inflate(inflater, container, false)
        return _binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        //check password and repeat password the same
        binding.confirmPassword.setOnFocusChangeListener {_,_ ->
            val confirmPwd = binding.confirmPassword.text.toString()
            val password = binding.password.text.toString()
            if (confirmPwd != password)
                binding.confirmPasswordLayout.error = getString(R.string.password_not_same_prompt)
            else
                binding.confirmPasswordLayout.error = null
        }




    }



}