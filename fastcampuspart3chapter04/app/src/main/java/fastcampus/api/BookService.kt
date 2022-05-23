package fastcampus.api

import fastcampus.model.BestSellerDto
import fastcampus.model.SearchBookDto
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface BookService {

    // 책 검색 api
    @GET("/api/search.api?output=json")
    fun getBooksByName(
        @Query("key") apiKey : String,
        @Query("query") keyword : String
    ) : Call<SearchBookDto>

    // 베스트셀러 api
    @GET("/api/bestSeller.api?output=json&categoryId=100")
    fun getBestSellerBooks(
        @Query("key") apiKey : String,
    ):Call<BestSellerDto>

}