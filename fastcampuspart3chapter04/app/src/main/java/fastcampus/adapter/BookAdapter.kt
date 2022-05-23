package fastcampus.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import fastcampus.model.Book
import fastcampus.part3.chapter04.databinding.ItemBookBinding

class BookAdapter(private val itemClickListener : (Book) -> Unit) : ListAdapter<Book, BookAdapter.BookItemViewHolder>(diffUtil) {

    // 리사이클러뷰는 몇개 뷰를 미리 만들어 놓고 데이터만 집어넣는 방식인데
    // ViewHolder가 그 몇개의 뷰 이다.
    inner class BookItemViewHolder(private val binding : ItemBookBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(bookModel : Book){
            binding.titleTextView.text = bookModel.title
            binding.descriptionTextView.text = bookModel.description

            binding.root.setOnClickListener{
                itemClickListener(bookModel)
            }

            Glide
                .with(binding.coverImageView.context)
                .load(bookModel.coverSmallUrl)
                .into(binding.coverImageView)
        }
    }

    // 미리 만들어진 뷰 홀더가 없을 경우 새로 생성하는 함수
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookItemViewHolder {
        return BookItemViewHolder(ItemBookBinding.inflate(
                LayoutInflater.from(parent.context)
                , parent
                , false))
    }

    // 뷰에 그려질 때 데이터를 그리는 함수
    override fun onBindViewHolder(holder: BookItemViewHolder, position: Int) {
        holder.bind(currentList[position])
    }

    //리사이클러뷰가 포지션 변경 시 새로운 값을 할당할지 말지 결정
    companion object{
        val diffUtil = object : DiffUtil.ItemCallback<Book>(){
            override fun areItemsTheSame(oldItem: Book, newItem: Book): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(oldItem: Book, newItem: Book): Boolean {
                return oldItem.id == newItem.id
            }

        }
    }

}