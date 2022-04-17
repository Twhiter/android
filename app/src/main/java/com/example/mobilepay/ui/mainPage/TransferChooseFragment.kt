package com.example.mobilepay.ui.mainPage

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.mobilepay.MainApplication
import com.example.mobilepay.R
import com.example.mobilepay.databinding.FragmentTransferChooseBinding
import com.example.mobilepay.entity.OverviewInfo
import com.example.mobilepay.room.roomEntity.SearchHistory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TransferChooseFragment : Fragment() {

    private lateinit var binding:FragmentTransferChooseBinding
    private val viewModel:TransferChooseViewModel by viewModels()


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

        //initialize history






    }
}

class TransferChooseViewModel:ViewModel() {

    val historyRecords = MainApplication.db().searchHistoryDao().queryAll().asLiveData()
    val isSearching = MutableLiveData(false)
    val searchResults:MutableLiveData<List<OverviewInfo>> = MutableLiveData(listOf())
}

class HistoryItemAdapter(private val searchHistories:List<SearchHistory>)
    : ListAdapter<SearchHistory, RecyclerView.ViewHolder>(Diff()) {


    class HistoryRecord(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val clearBtn: Button = itemView.findViewById(R.id.clear)

        fun onBind() {
            clearBtn.setOnClickListener {
                val dao = MainApplication.db().searchHistoryDao()
                CoroutineScope(Dispatchers.IO).launch {
                    dao.deleteAll()
                }
            }
        }
    }

    class TailButton(itemView: View):RecyclerView.ViewHolder(itemView) {
        val crossing:ImageView = itemView.findViewById(R.id.crossing)
        val history:TextView = itemView.findViewById(R.id.history)

        fun onBind(searchHistory:SearchHistory) {
            crossing.setOnClickListener {
                val dao = MainApplication.db().searchHistoryDao()
                CoroutineScope(Dispatchers.IO).launch {
                    dao.delete(searchHistory)
                }
            }

            history.setOnClickListener {

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
            (holder as HistoryRecord).onBind()
        else
            (holder as TailButton).onBind(searchHistories[position])
    }

    override fun getItemCount(): Int {
        return if (searchHistories.isEmpty())
            0
        else searchHistories.size + 1
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == searchHistories.size)
            1
        else
            0
    }


    class Diff: DiffUtil.ItemCallback<SearchHistory>() {
        override fun areItemsTheSame(oldItem: SearchHistory, newItem: SearchHistory): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: SearchHistory, newItem: SearchHistory): Boolean {
            return oldItem.id == newItem.id
        }
    }
}




