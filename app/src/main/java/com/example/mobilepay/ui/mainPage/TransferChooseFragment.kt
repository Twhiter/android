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
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.mobilepay.*
import com.example.mobilepay.databinding.FragmentTransferChooseBinding
import com.example.mobilepay.entity.OverviewInfo
import com.example.mobilepay.entity.QrCodeContent
import com.example.mobilepay.entity.Type
import com.example.mobilepay.network.UserApi
import com.example.mobilepay.room.roomEntity.SearchHistory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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
        viewModel.page.value = 1
        viewModel.searchingLoading.value = true
        lifecycleScope.launch(Dispatchers.Default) {
            val resp = withContext(Dispatchers.IO) {
                UserApi.service.searchUsers(binding.searchText.text.toString(),1,10)
            }

            withContext(Dispatchers.Main) {
                resp.handleOneWithDefault(requireContext()) { r ->
                    if (r.data == null)
                        false
                    else {
                        // render data and update maxPage
                        viewModel.searchResults.value = r.data.data
                        viewModel.maxPage.value = r.data.maxPage
                        true
                    }
                }
                withContext(Dispatchers.Main) {
                    viewModel.searchingLoading.value = false
                }
            }
        }
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

         inner class SearchResultItem(view: View):RecyclerView.ViewHolder(view) {

            private val avatar:ImageView = itemView.findViewById(R.id.avatar)
            private val name:TextView = itemView.findViewById(R.id.name)
            private val phoneNumber:TextView = itemView.findViewById(R.id.phoneNumber)
            private val email:TextView = itemView.findViewById(R.id.email)
            private val layout:ConstraintLayout = itemView.findViewById(R.id.resultItem)

            fun bind(overviewInfo: OverviewInfo) {
                bindImage(avatar,overviewInfo.avatar)
                name.text = overviewInfo.name
                phoneNumber.text = overviewInfo.phoneNumber
                email.text = overviewInfo.email

                layout.setOnClickListener {
                    val qrCodeContent = QrCodeContent(overviewInfo.id,Type.User)
                    val action = TransferChooseFragmentDirections
                        .actionTransferChooseFragmentToPayFragment(qrCodeContent)
                    findNavController().navigate(action)
                }
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

    val historyRecords: MutableLiveData<List<SearchHistory>> = MutableLiveData(listOf())
    val onSearchMode = MutableLiveData(false)
    val searchResults:MutableLiveData<List<OverviewInfo>> = MutableLiveData(listOf())
    val searchingLoading = MutableLiveData(false)
    val page = MutableLiveData(1)
    val maxPage = MutableLiveData(1)

    val searchResultsReady:LiveData<Boolean> = CombinedLiveData(onSearchMode,searchingLoading) {
        (it[0] as Boolean) && (!(it[1] as Boolean))
    }

    val searchResultsFound: LiveData<Boolean> = CombinedLiveData(searchResultsReady,searchResults) {
        (it[0] as Boolean) && (it[1] as List<OverviewInfo>).isNotEmpty()
    }

    val searchResultsNotFound:LiveData<Boolean> = CombinedLiveData(searchResultsReady,searchResults) {
        (it[0] as Boolean) && (it[1] as List<OverviewInfo>).isEmpty()
    }

}






