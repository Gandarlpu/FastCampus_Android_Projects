package fastcampus.part3.chapter06.chatDetail

data class ChatItem (
    val senderId : String,
    val message : String
){
    constructor() : this("" , "")
}