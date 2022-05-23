package fastcampus.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

// API에서 받아온 데이터들 중 Item안에서 어떠한 항목들을 받아올 것인지 데이터 형식을 담음.

@Parcelize
data class Book (
        // 서버에서는 itemId로 주는데 우리는 id로 키값을 받아오기 때문에 매칭해주기 위해 SerivalzedName사용
        @SerializedName("itemId") val id : Long,
        @SerializedName("title") val title : String,
        @SerializedName("description") val description : String,
        @SerializedName("coverSmallUrl") val coverSmallUrl : String,

) : Parcelable