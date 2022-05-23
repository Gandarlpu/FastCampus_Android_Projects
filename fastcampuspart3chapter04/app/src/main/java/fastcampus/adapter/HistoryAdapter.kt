package fastcampus.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import fastcampus.model.Book
import fastcampus.model.History
import fastcampus.part3.chapter04.databinding.ItemBookBinding
import fastcampus.part3.chapter04.databinding.ItemHistoryBinding

class HistoryAdapter(val historyDeleteClickListener : (String) -> Unit) : ListAdapter<History, HistoryAdapter.HistoryItemViewHolder>(diffUtil) {

    // 리사이클러뷰는 몇개 뷰를 미리 만들어 놓고 데이터만 집어넣는 방식인데
    // ViewHolder가 그 몇개의 뷰 이다.
    inner class HistoryItemViewHolder(private val binding : ItemHistoryBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(historyModel : History){
            binding.historyKeywordTextView.text = historyModel.keyword

            binding.historyKeywordDeleteButton.setOnClickListener {
                historyDeleteClickListener(historyModel.keyword.orEmpty())
            }
        }
    }

    // 미리 만들어진 뷰 홀더가 없을 경우 새로 생성하는 함수
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryItemViewHolder {
        return HistoryItemViewHolder(
            ItemHistoryBinding.inflate(
            LayoutInflater.from(parent.context)
            , parent
            , false))
    }

    // 뷰에 그려질 때 데이터를 그리는 함수
    override fun onBindViewHolder(holder: HistoryItemViewHolder, position: Int) {
        holder.bind(currentList[position])
    }

    //리사이클러뷰가 포지션 변경 시 새로운 값을 할당할지 말지 결정
    companion object{
        val diffUtil = object : DiffUtil.ItemCallback<History>(){
            override fun areItemsTheSame(oldItem: History, newItem: History): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(oldItem: History, newItem: History): Boolean {
                return oldItem.keyword == newItem.keyword
            }

        }
    }
}