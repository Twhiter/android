package com.example.mobilepay.ui.mainPage

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.mobilepay.*
import com.example.mobilepay.databinding.*
import com.example.mobilepay.entity.BillRecord
import com.example.mobilepay.entity.BillType
import com.example.mobilepay.entity.Page
import com.example.mobilepay.network.MerchantApi
import com.example.mobilepay.network.UserApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.SimpleDateFormat
import java.util.*


class BillFragment : Fragment() {

    private val args: BillFragmentArgs by navArgs()

    private lateinit var binding: FragmentBillBinding
    private val viewModel: BillFragmentViewModel by viewModels()

    private var pageNum = 1
    private var headers: Set<String> = TreeSet()
    private var records = mutableListOf<Record?>()
    private var isDataOver = false
    private lateinit var filterData: FilterData


    private lateinit var adapter: BillAdapter


    val isUser: Boolean get() = args.isUser


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentBillBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        clear()

        binding.viewModel = viewModel
        binding.lifecycleOwner = this

        //set adapter for recycleview
        adapter = BillAdapter(requireContext())
        binding.billRecords.adapter = adapter
        binding.billRecords.layoutManager = WrapContentLinearLayoutManager(requireContext())

        loadData()

        //set recyclerview scroll down action
        binding.billRecords.setOnScrollChangeListener { _, _, _, _, _ ->


            if (records.lastOrNull() != null &&
                !isDataOver &&
                !binding.billRecords.canScrollVertically(1)
            ) {
                loadData()
            }
        }

        binding.filter.setOnClickListener {
            FilterDialog(this.filterData).show()
        }

    }

    private fun loadData() {

        // add loading
        records.add(null)
        adapter.submitList(records.toMutableList())

        lifecycleScope.launch(Dispatchers.IO) {

            val pageData = fetchBill()
            isDataOver = pageData.currentPage == pageData.maxPage

            val addedRecords = formRecordsFromPageData(pageData)
            val newRecords = records.toMutableList()
            newRecords.removeLast()
            newRecords.addAll(addedRecords)

            withContext(Dispatchers.Main) {
                adapter.submitList(newRecords)
            }
            records = newRecords
        }
    }

    private fun clear() {
        filterData = FilterData(getFullBillTypes(isUser).toMutableList(),
            null, null, null, null)
        pageNum = 1
        records.clear()
        isDataOver = false
        headers = TreeSet()
        viewModel.isResultNotFound.value = false
    }


    private suspend fun fetchBill(): Page<BillRecord> {
        val token = MainApplication.db().kvDao().get("token")

        var pageData: Page<BillRecord>? = null

        val resp = if (args.isUser)
            UserApi.service.getUserBills(token!!, 10, pageNum, this.filterData.min,
                this.filterData.max,
                Util.toBillRequestDataFormat(this.filterData.start),
                Util.toBillRequestDataFormat(this.filterData.end),
                this.filterData.billTypes)
        else
            MerchantApi.service.getMerchantBills(token!!, 10, pageNum, this.filterData.min,
                this.filterData.max,
                Util.toBillRequestDataFormat(this.filterData.start),
                Util.toBillRequestDataFormat(this.filterData.end),

                this.filterData.billTypes)

        resp.handleOneWithDefault(requireContext()) { r ->
            pageData = r.data!!
            true
        }

        pageNum++
        return pageData!!
    }


    private fun formRecordsFromPageData(pageData: Page<BillRecord>): List<Record> {

        val returnedRecords = mutableListOf<Record>()

        pageData.data.forEach {

            val key = "${it.date.year}:${it.date.month}"
            if (!headers.contains(key)) {
                headers = headers.plus(key)
                val headerStr = SimpleDateFormat("yyyy MMM").format(it.date)
                returnedRecords.add(HeaderRecord(headerStr))
            }
            returnedRecords.add(it)
        }

        return returnedRecords
    }


    override fun onResume() {
        super.onResume()
        MainPageActivity.getInstance()?.setFullScreenVisibility(View.GONE)
    }

    companion object {
        private val userBillTypes =
            listOf(BillType.export_to_merchant, BillType.import_from_merchant,
                BillType.pay, BillType.refunded_pay, BillType.transfer_in, BillType.transfer_out)

        private val merchantBillTypes = listOf(BillType.import_from_user, BillType.export_to_user,
            BillType.pay, BillType.refunded_pay)

        private fun getFullBillTypes(isUser: Boolean) =
            if (isUser) userBillTypes else merchantBillTypes


        const val LOADING = 0
        const val HEADER = 1
        const val RECORD = 2


        private class Diff : DiffUtil.ItemCallback<Record>() {
            override fun areItemsTheSame(
                oldItem: Record,
                newItem: Record,
            ): Boolean {
                return oldItem === newItem
            }


            override fun areContentsTheSame(
                oldItem: Record,
                newItem: Record,
            ): Boolean {
                return if (oldItem is HeaderRecord && newItem is HeaderRecord) {
                    oldItem.head == newItem.head
                } else if (oldItem is BillRecord && newItem is BillRecord) {
                    oldItem == newItem
                } else
                    false
            }
        }
    }


    inner class FilterDialog(filterObj: FilterData) {


        private val selected = filterObj.copy(billTypes = filterObj.billTypes.toMutableList())
        private val binding = FilterBinding.inflate(this@BillFragment.layoutInflater)
        private lateinit var dialog: AlertDialog

        fun show() {

            binding.min.filters = arrayOf(DecimalDigitsInputFilter(5, 2))
            binding.max.filters = arrayOf(DecimalDigitsInputFilter(5, 2))

            binding.billFragment = this@BillFragment
            binding.filterDialog = this
            binding.lifecycleOwner = this@BillFragment

            dialog = AlertDialog.Builder(requireContext(), R.style.Theme_MobilePay)
                .setView(binding.root)
                .setPositiveButton("Confirm") { _, _ ->
                    this.dialog.dismiss()
                    this@BillFragment.clear()


                    binding.min.text.takeIf { !it.isNullOrBlank() }?.let {
                        selected.min = BigDecimal(it.toString())
                    }

                    binding.max.text.takeIf { !it.isNullOrBlank() }?.let {
                        selected.max = BigDecimal(it.toString())
                    }

                    this@BillFragment.filterData = this.selected.copy(
                        billTypes = this.selected.billTypes.toMutableList())
                    loadData()
                }
                .setNegativeButton("Cancel") { _, _ ->
                    this.dialog.dismiss()
                }
                .setNeutralButton("Reset") { _, _ ->
                    this.dialog.dismiss()
                    this@BillFragment.clear()
                    this@BillFragment.loadData()
                }.create()
            dialog.show()
        }

        fun startClick() {

            val startMaxCalendar = Calendar.getInstance()
            if (selected.end == null)
                startMaxCalendar.add(Calendar.DAY_OF_MONTH, -1)
            else {
                startMaxCalendar.time = selected.end!!
                startMaxCalendar.add(Calendar.DAY_OF_MONTH, -1)
            }


            val datePicker = DatePickerDialog(requireContext(), { _, y, m, d ->

                val calendar = Calendar.getInstance()
                calendar.set(Calendar.YEAR, y)
                calendar.set(Calendar.MONTH, m)
                calendar.set(Calendar.DAY_OF_MONTH, d)

                calendar.set(Calendar.HOUR, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)

                selected.start = calendar.time
                binding.startDate.setText(
                    SimpleDateFormat("yyyy/MM/dd").format(calendar.time))

            }, startMaxCalendar.get(Calendar.YEAR), startMaxCalendar.get(Calendar.MONTH),
                startMaxCalendar.get(Calendar.DAY_OF_MONTH))

            datePicker.datePicker.maxDate = startMaxCalendar.time.time
            datePicker.show()
        }

        fun endClick() {

            val endMinCalendar = Calendar.getInstance()
            if (selected.start == null)
                endMinCalendar.time = Date(0)
            else {
                endMinCalendar.time = selected.start!!
                endMinCalendar.add(Calendar.DAY_OF_MONTH, 1)
            }
            val now = Calendar.getInstance()

            val datePicker = DatePickerDialog(requireContext(), { _, y, m, d ->

                val calendar = Calendar.getInstance()
                calendar.set(Calendar.YEAR, y)
                calendar.set(Calendar.MONTH, m)
                calendar.set(Calendar.DAY_OF_MONTH, d)

                calendar.set(Calendar.HOUR, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)

                selected.end = calendar.time
                binding.endDate.setText(
                    SimpleDateFormat("yyyy/MM/dd").format(calendar.time))

            }, now.get(Calendar.YEAR), now.get(Calendar.MONTH),
                now.get(Calendar.DAY_OF_MONTH))

            datePicker.datePicker.minDate = endMinCalendar.time.time
            datePicker.datePicker.maxDate = now.time.time

            datePicker.show()
        }

        fun importClick(btnView: View) {
            generalClick(BillType.import_from_merchant, BillType.import_from_user, btnView)
        }

        fun exportClick(btnView: View) {
            generalClick(BillType.export_to_merchant, BillType.export_to_user, btnView)
        }

        fun payClick(btnView: View) {
            generalClick(BillType.pay, BillType.pay, btnView)
        }

        fun refundedClick(btn: View) {
            generalClick(BillType.refunded_pay, BillType.refunded_pay, btn)
        }

        fun transferInClick(btnView: View) {
            generalClick(BillType.transfer_in, BillType.transfer_in, btnView)
        }

        fun transferOutClick(btnView: View) {
            generalClick(BillType.transfer_out, BillType.transfer_out, btnView)
        }

        fun setClickedColor(one: BillType, other: BillType?): Int {
            val billType = if (isUser) one else other
            return if (selected.billTypes.contains(billType))
                requireContext().getColor(R.color.white)
            else
                requireContext().getColor(R.color.gray_e5)
        }


        private fun generalClick(one: BillType, other: BillType, btnView: View) {
            val billType = if (isUser) one else other

            if (selected.billTypes.contains(billType)) {
                selected.billTypes.remove(billType)
                (btnView as Button).setTextColor(requireContext().getColor(R.color.gray_e5))
            } else {
                selected.billTypes.add(billType)
                (btnView as Button).setTextColor(requireContext().getColor(R.color.white))
            }
        }
    }


    inner class BillAdapter(val context: Context) :
        ListAdapter<Record, RecyclerView.ViewHolder>(Diff()) {


        inner class Head(val binding: ListBillsRecordsHeadBinding) :
            RecyclerView.ViewHolder(binding.root) {

            fun onBind(title: String) {
                binding.head.text = title
            }
        }

        inner class Record(val binding: ListBillsRecordsItemBinding) :
            RecyclerView.ViewHolder(binding.root) {

            fun onBind(bill: BillRecord) {
                binding.action.text = context.getString(R.string.action_and_name,
                    bill.billType.prompt,
                    bill.overviewInfo.name)

                //show avatar
                bindImage(binding.avatar, bill.overviewInfo.avatar)

                // show money amount
                binding.money.text = bill.amount.setScale(2, RoundingMode.UNNECESSARY)
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

                binding.container.setOnClickListener {
                    val action = BillFragmentDirections.actionBillFragmentToBillDetailFragment(bill,
                        isUser)
                    findNavController().navigate(action)
                }

            }
        }

        inner class Loading(val binding: LoadingItemBinding) : RecyclerView.ViewHolder(binding.root)


        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

            when (viewType) {
                LOADING -> {
                    val binding = LoadingItemBinding.inflate(
                        LayoutInflater.from(parent.context), parent, false)
                    return Loading(binding)
                }
                HEADER -> {
                    val binding = ListBillsRecordsHeadBinding.inflate(
                        LayoutInflater.from(parent.context), parent, false)
                    return Head(binding)
                }
                else -> {
                    val binding = ListBillsRecordsItemBinding.inflate(
                        LayoutInflater.from(parent.context), parent, false)
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
}


class BillFragmentViewModel : ViewModel() {
    val isResultNotFound = MutableLiveData(false)
}

data class FilterData(
    val billTypes: MutableList<BillType>,
    var min: BigDecimal?,
    var max: BigDecimal?,
    var start: Date?,
    var end: Date?,
)


interface Record

data class HeaderRecord(val head: String) : Record





