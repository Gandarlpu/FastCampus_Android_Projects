package fastcampus.part3.chapter06.chatList

data class ChatListItem(
    val buyerId : String,
    val sellerId : String,
    val itemTitle : String,
    val key : Long
){
    constructor() : this("" , "" , "" , 0)
}
