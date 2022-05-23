package fastcampus.model

import com.google.gson.annotations.SerializedName

// 데이터를 꺼내올 수 있게 연결시켜줌

data class BestSellerDto (
    @SerializedName("title") val title : String,
    @SerializedName("item") val books : List<Book>,

)