package fastcampus.part3.chapter06.home

data class ArticleModel(
    val sellerId : String,
    val title : String,
    val createdAt : Long,
    val price : String,
    val imageUrl : String
){
    // realtime데이터베이스는 빈생성자가 잇어야함.
    constructor(): this("","",0,"","")
}