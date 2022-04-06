package com.example.mobilepay.ui.login

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.example.mobilepay.MainActivity
import com.example.mobilepay.MainApplication
import com.example.mobilepay.databinding.FragmentLoginBinding
import com.example.mobilepay.entity.ResponseData
import com.example.mobilepay.network.UserApi
import com.example.mobilepay.room.roomEntity.KV
import kotlinx.coroutines.*

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        //set click action
        //set click action for register
        binding.register.setOnClickListener {
            val action = LoginFragmentDirections.actionLoginFragmentToRegisterFragment()
            view.findNavController().navigate(action)
        }

        //set click action for login
        binding.login.setOnClickListener {
            CoroutineScope(Dispatchers.Default).launch {
                login()
            }
        }
    }

    private suspend fun login() {

        val phoneAndPwd = HashMap<String, String>()

        phoneAndPwd["phone"] = binding.phone.text.toString()
        phoneAndPwd["password"] = binding.password.text.toString()

        val job = CoroutineScope(Dispatchers.IO).launch {
            val resp = UserApi.service.login(phoneAndPwd)

            if (resp.status != ResponseData.OK) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), resp.errorPrompt, Toast.LENGTH_SHORT)
                        .show()
                }
                return@launch
            }


            resp.data?.let {

                if (!it.isOkay) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(requireContext(), it.prompt, Toast.LENGTH_SHORT).show()
                    }
                    return@launch
                }

                withContext(Dispatchers.Main) {
                    //store the token inside
                    withContext(Dispatchers.IO) {
                        MainApplication.db().KVDao().set(KV("token",it.token))
                    }
                    Toast.makeText(requireContext(), "Successfully Login!", Toast.LENGTH_SHORT)
                        .show()
                    delay(300)
                    MainActivity.toMainPage(requireActivity())
                }
            }
        }

        withContext(Dispatchers.Main){
            binding.loginLoading.visibility = View.VISIBLE
        }
        job.join()

        withContext(Dispatchers.Main) {
            binding.loginLoading.visibility = View.GONE
        }
    }
}