package fastcampus.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import fastcampus.model.Review

@Dao
interface ReviewDao {

    @Query("SELECT * FROM review WHERE id == :id")
    fun getOneReview(id : Int) : Review

    @Insert(onConflict = OnConflictStrategy.REPLACE) // 같은 리뷰는 덮어쓰기
    fun saveReview(review : Review)

}