package com.example.mobilepay.ui.mainPage

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.*
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.mobilepay.*
import com.example.mobilepay.databinding.FragmentTransferChooseBinding
import com.example.mobilepay.entity.OverviewInfo
import com.example.mobilepay.room.roomEntity.SearchHistory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class TransferChooseFragment : Fragment() {

     lateinit var binding:FragmentTransferChooseBinding
     val viewModel:TransferChooseViewModel by viewModels()

    init {
        lifecycleScope.launchWhenCreated {
            MainApplication.db().searchHistoryDao().queryAll().collect {
                viewModel.historyRecords.value = it
            }
        }
    }

    companion object {

        class Diff: DiffUtil.ItemCallback<SearchHistory>() {
            override fun areItemsTheSame(oldItem: SearchHistory, newItem: SearchHistory): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: SearchHistory, newItem: SearchHistory): Boolean {
                return oldItem.id == newItem.id
            }
        }

        class SearchResultDiff:DiffUtil.ItemCallback<OverviewInfo>() {
            override fun areItemsTheSame(oldItem: OverviewInfo, newItem: OverviewInfo): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: OverviewInfo, newItem: OverviewInfo): Boolean {
                return oldItem == newItem
            }
        }



    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentTransferChooseBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        MainPageActivity.getInstance()?.setFullScreenVisibility(View.GONE)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.viewmodel = viewModel
        binding.lifecycleOwner = this



        //initialize history
        val historyAdapter = HistoryItemAdapter()
        binding.history.adapter = historyAdapter
        binding.history.layoutManager = WrapContentLinearLayoutManager(requireContext())
        //viewModel.historyRecords.value?.let(historyAdapter::submitList)

        //observe history and fresh recycleview when it updates
        viewModel.historyRecords.observe(viewLifecycleOwner,historyAdapter::submitList)

        //initialize search
        val searchResultAdapter = SearchResultAdapter()
        binding.result.adapter = searchResultAdapter
        binding.result.layoutManager = WrapContentLinearLayoutManager(requireContext())

        //observe search result and fresh recycleview when it updates
        viewModel.searchResults.observe(viewLifecycleOwner,searchResultAdapter::submitList)


        binding.searchText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_NEXT
                && binding.searchText.text!!.isNotBlank()) {
                search()
                addHistory()
            }
            true
        }

        //set on change of the search input
        binding.searchText.addTextChangedListener(
            object :TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

                override fun afterTextChanged(s: Editable?) {
                    viewModel.onSearchMode.value = false
                }
            }
        )
    }


    fun search() {
        viewModel.onSearchMode.value = true
        viewModel.searchingLoading.value = true
        val overviewInfo = OverviewInfo(1,"user","/avatar/1.png"
            ,"Cheng Cheng Liang","+8619917910891","1065582542@qq.com")
        viewModel.searchingLoading.value = false
        viewModel.searchResults.value = listOf(overviewInfo)
    }

    fun addHistory() {
        binding.searchText.text.toString().takeIf {it.isNotBlank()}?.let {
            lifecycleScope.launch(Dispatchers.IO) {
                MainApplication.db().searchHistoryDao().insert(SearchHistory(0,it))
            }
        }
    }





    inner class HistoryItemAdapter
        : ListAdapter<SearchHistory, RecyclerView.ViewHolder>(Diff()) {



        inner class HistoryRecord(itemView: View) : RecyclerView.ViewHolder(itemView) {

            private val crossing:ImageView = itemView.findViewById(R.id.crossing)
            private val history:TextView = itemView.findViewById(R.id.history)

            fun onBind(searchHistory:SearchHistory) {

                history.text = searchHistory.keyword

                crossing.setOnClickListener {
                    val dao = MainApplication.db().searchHistoryDao()
                    lifecycleScope.launch(Dispatchers.IO) {
                        dao.delete(searchHistory)
                    }
                }

                history.setOnClickListener {
                    binding.searchText.setText(searchHistory.keyword)
                    search()
                    addHistory()
                }
            }

        }

        inner class TailButton(itemView: View):RecyclerView.ViewHolder(itemView) {
            val clearBtn: Button = itemView.findViewById(R.id.clear)

            fun onBind() {
                clearBtn.setOnClickListener {
                    val dao = MainApplication.db().searchHistoryDao()
                    lifecycleScope.launch(Dispatchers.IO) {
                        dao.deleteAll()
                    }
                }
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            val view:View
            if (viewType == 0) {
                view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.list_history_item,parent,false)
                return HistoryRecord(view)
            }else {
                view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.list_history_item_tail,parent,false)
                return TailButton(view)
            }
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            if (holder.itemViewType == 0)
                (holder as HistoryRecord).onBind(getItem(position))
            else
                (holder as TailButton).onBind()
        }

        override fun getItemCount(): Int {
            return if (currentList.isEmpty())
                0
            else currentList.size + 1
        }

        override fun getItemViewType(position: Int): Int {
            return if (position == currentList.size)
                1
            else
                0
        }
    }



    inner class SearchResultAdapter
        :ListAdapter<OverviewInfo, SearchResultAdapter.SearchResultItem>(SearchResultDiff()) {


        inner class SearchResultItem(itemView: View):RecyclerView.ViewHolder(itemView) {

            private val avatar:ImageView = itemView.findViewById(R.id.avatar)
            private val name:TextView = itemView.findViewById(R.id.name)
            private val phoneNumber:TextView = itemView.findViewById(R.id.phoneNumber)
            private val email:TextView = itemView.findViewById(R.id.email)

            fun bind(overviewInfo: OverviewInfo) {

                bindImage(avatar,overviewInfo.avatar)
                name.text = overviewInfo.name
                phoneNumber.text = overviewInfo.phoneNumber
                email.text = overviewInfo.email
            }

        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchResultItem {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.list_search_result_item,parent,false)
            return SearchResultItem(view)
        }

        override fun onBindViewHolder(holder: SearchResultItem, position: Int) {
            holder.bind(getItem(position))
        }
    }
}

class TransferChooseViewModel: ViewModel() {

    val historyRecords:MutableLiveData<List<SearchHistory>> = MutableLiveData(listOf())
    val onSearchMode = MutableLiveData(false)
    val searchResults:MutableLiveData<List<OverviewInfo>> = MutableLiveData(listOf())
    val searchingLoading = MutableLiveData(false)

    val searchResultsReady:LiveData<Boolean> = CombinedLiveData(onSearchMode,searchingLoading) {
        (it[0] as Boolean) && (!(it[1] as Boolean))
    }
}






