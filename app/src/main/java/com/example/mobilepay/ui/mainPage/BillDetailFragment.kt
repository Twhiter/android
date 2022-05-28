package com.example.mobilepay.ui.mainPage

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.mobilepay.MainApplication
import com.example.mobilepay.R
import com.example.mobilepay.Util
import com.example.mobilepay.databinding.FragmentBillDetailBinding
import com.example.mobilepay.entity.BillType
import com.example.mobilepay.network.PayApi
import com.fasterxml.jackson.databind.util.StdDateFormat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.math.BigDecimal
import java.util.*


class BillDetailFragment : Fragment() {

    private lateinit var binding: FragmentBillDetailBinding
    val args: BillDetailFragmentArgs by navArgs()

    val billRecord get() = args.billRecord
    val isRefunded: Boolean get() = billRecord.billType == BillType.refunded_pay
    val refundedTimeString: String
        get() = run {
            val date: Date?
            if (billRecord.extraData["refundedTime"] == null)
                date = null
            else
                date = StdDateFormat().parse(billRecord.extraData["refundedTime"] as String)
            Util.toBillShowDateFormat(date) ?: ""
        }

    val amountColor
        get() =
            when {
                billRecord.billType == BillType.refunded_pay -> requireContext().getColor(R.color.color_danger)
                billRecord.amount > BigDecimal.ZERO -> requireContext().getColor(R.color.color_success)
                else -> requireContext().getColor(R.color.black)
            }


    val shouldRefundBtnShow: Boolean
        get() =
            billRecord.billType == BillType.pay && !args.isUser

    val remarks: String
        get() = run {
            if (billRecord.extraData.containsKey("remarks"))
                billRecord.extraData["remarks"] as String
            else
                ""
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentBillDetailBinding.inflate(layoutInflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.billDetailFragment = this
        binding.lifecycleOwner = this

        binding.back.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.refund.setOnClickListener {


            lifecycleScope.launch(Dispatchers.IO) {

                val token = MainApplication.db().kvDao().get("token")
                if (token == null) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(requireContext(), "token expired, can't process",
                            Toast.LENGTH_SHORT).show()
                    }
                    return@launch
                }
                val resp = PayApi.service.refundPay(token, billRecord.recordId)
                var prompt = ""
                resp.handleOneWithDefault(requireContext()) {
                    prompt = it.data!!
                    true
                }


                if (prompt != "") {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(requireContext(), prompt,
                            Toast.LENGTH_SHORT).show()
                    }
                    return@launch
                }

                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "Refund Success",
                        Toast.LENGTH_SHORT).show()
                    findNavController().navigateUp()
                }
            }
        }


    }


}