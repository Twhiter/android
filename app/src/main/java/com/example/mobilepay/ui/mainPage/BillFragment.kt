package com.example.mobilepay.ui.mainPage

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.mobilepay.R
import com.example.mobilepay.bindImage
import com.example.mobilepay.databinding.FragmentBillBinding
import com.example.mobilepay.databinding.ListBillsRecordsHeadBinding
import com.example.mobilepay.databinding.ListBillsRecordsItemBinding
import com.example.mobilepay.databinding.LoadingItemBinding
import com.example.mobilepay.entity.BillRecord
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.SimpleDateFormat

class BillFragment : Fragment() {


    private lateinit var binding:FragmentBillBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentBillBinding.inflate(inflater,container,false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onResume() {
        super.onResume()
        MainPageActivity.getInstance()?.setFullScreenVisibility(View.GONE)
    }
}
interface Record{}

data class HeaderRecord(val head:String):Record


class BillAdapter(val context: Context): ListAdapter<Record, RecyclerView.ViewHolder>(Diff()) {

    companion object {

        const val LOADING = 0
        const val HEADER = 1
        const val RECORD = 2


        private class Diff: DiffUtil.ItemCallback<com.example.mobilepay.ui.mainPage.Record>() {
            override fun areItemsTheSame(
                oldItem: com.example.mobilepay.ui.mainPage.Record,
                newItem: com.example.mobilepay.ui.mainPage.Record,
            ): Boolean {
                return oldItem === newItem
            }


            override fun areContentsTheSame(
                oldItem: com.example.mobilepay.ui.mainPage.Record,
                newItem: com.example.mobilepay.ui.mainPage.Record,
            ): Boolean {
                return if (oldItem is HeaderRecord && newItem is HeaderRecord) {
                    oldItem.head == newItem.head
                }else if (oldItem is BillRecord && newItem is BillRecord) {
                    oldItem == newItem
                }else
                    false
            }
        }
    }


    inner class Head(val binding:ListBillsRecordsHeadBinding)
        : RecyclerView.ViewHolder(binding.root) {

        fun onBind(title:String) {
            binding.head.text = title
        }
    }

    inner class Record(val binding: ListBillsRecordsItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

            fun onBind(bill:BillRecord) {
                binding.action.text = context.getString(R.string.action_and_name
                    ,bill.billType.prompt,bill.overviewInfo.name)

                //show avatar
                bindImage(binding.avatar,bill.overviewInfo.avatar)

                // show money amount
                binding.money.text = bill.amount.setScale(2,RoundingMode.UNNECESSARY)
                    .toString()

                // show money color
                if (bill.extraData.containsKey("refundedTime"))
                    binding.money.setTextColor(context.getColor(R.color.color_danger))
                else {
                    if (bill.amount < BigDecimal.ZERO)
                        binding.money.setTextColor(context.getColor(R.color.black))
                    else
                        binding.money.setTextColor(context.getColor(R.color.color_success))
                }

                //show transaction date
                binding.time.text = SimpleDateFormat("MMM dd hh:mm").format(bill.date)
            }
    }

    class Loading(val binding:LoadingItemBinding): RecyclerView.ViewHolder(binding.root)


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        when (viewType) {
            LOADING -> {
                val binding = LoadingItemBinding.inflate(
                    LayoutInflater.from(parent.context),parent,false)
                return Loading(binding)
            }
            HEADER -> {
                val binding = ListBillsRecordsHeadBinding.inflate(
                    LayoutInflater.from(parent.context),parent,false)
                return Head(binding)
            }
            else -> {
                val binding = ListBillsRecordsItemBinding.inflate(
                    LayoutInflater.from(parent.context),parent,false)
                return Record(binding)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        when (holder) {
            is Head -> holder.onBind((currentList[position] as HeaderRecord).head)
            is Record -> holder.onBind((currentList[position] as BillRecord))
            else -> return
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when {
            currentList[position] == null -> LOADING
            currentList[position] is HeaderRecord -> HEADER
            else -> RECORD
        }
    }
}



