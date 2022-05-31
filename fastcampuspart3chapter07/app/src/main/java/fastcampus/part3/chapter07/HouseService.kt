package fastcampus.part3.chapter07

import retrofit2.Call
import retrofit2.http.GET

interface HouseService {

    @GET("/v3/5cc9317f-49c9-48be-8d3a-c293d73d9e32") // API주소값 입력
    fun getHouseList() : Call<HouseDTO> // json으로 데이터값을 입력했는데, items에 여러항목으로 묶여있기 때문에
    //List로 묶어줌

}