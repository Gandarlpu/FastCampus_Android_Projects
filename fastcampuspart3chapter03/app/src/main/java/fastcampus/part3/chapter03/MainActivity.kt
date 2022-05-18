package fastcampus.part3.chapter03

import android.app.AlarmManager
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import java.util.*
import kotlin.math.min

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // step0 뷰 초기화 해주기
        initOnOffButton()
        initChangeAlarmTimeButton()

        // step1 데이터 가져오기
        val model = fetchDataFromSharedPreferences()

        // step2 뷰에 데이터를 그려주기
        renderView(model)


    }

    private fun fetchDataFromSharedPreferences(): AlarmDisplayModel {
        val sharedPreference = getSharedPreferences(SHARED_PREFERENCE_NAME , Context.MODE_PRIVATE)
        // 데이터 가져오기
        val timeDBValue = sharedPreference.getString(ALARM_KEY , "9:30") ?: "9:30"
        val onOffDBValue = sharedPreference.getBoolean(ONOFF_KEY , false)
        val alarmData = timeDBValue.split(":")

        val alarmModel = AlarmDisplayModel(
            hour = alarmData[0].toInt(),
            minute = alarmData[1].toInt(),
            onOff = onOffDBValue
        )

        // 보정
        // 1. sharepreference에 알람이 켜져잇다고해서 앱을 켯는데 알람이 꺼져잇을 경우 Off로 바꿔줘야 한다. = View가 Off기 때문에
        // 2. 알람이 등록이 되어잇는데 sharepreference에는 꺼져잇다 그럴 때도 꺼져야 한다.
        // 알람이 커져있는지 꺼져잇는지 확인하기 위해서는 브로드캐스트리시버 필요
        val pendingIntent = PendingIntent.getBroadcast(this , ALARM_REQUEST_CODE , Intent(this , AlarmReceiver::class.java), PendingIntent.FLAG_NO_CREATE)

        if((pendingIntent == null) and alarmModel.onOff){
            // 알람은 꺼져있는데, 데이터는 켜져있는 경우
            alarmModel.onOff = false
        }else if((pendingIntent != null) and alarmModel.onOff.not()){
            // 알람은 켜저있는데, 데이터는 꺼져있는 경우
            // 알람을 취소함
            cancelAlarm()
        }
        return alarmModel
    }

    private fun initChangeAlarmTimeButton() {
        val changeAlarmButton = findViewById<Button>(R.id.changeAlarmTimeButton)
        changeAlarmButton.setOnClickListener {

            // TimePickDialog를 띄워줘서 시간을 설정을 하도록 하게끔하고, 그 시간을 가져와서
            val calendar = Calendar.getInstance()

            TimePickerDialog(this , { picker , hour , minute ->
                // 이 람다식 안에는 시간이 설정 된 이후에 설정 됨 //
                // 데이터를 저장
                val model = saveAlarmModel(hour , minute , false)

                // 뷰를 업데이트
                renderView(model)

                // 기존에 있던 알람을 삭제한다.
                cancelAlarm()

            }, calendar.get(Calendar.HOUR_OF_DAY) , calendar.get(Calendar.MINUTE) , false)
                .show()


        }
    }

    private fun initOnOffButton() {
        val onOffButton = findViewById<Button>(R.id.onOffButton)
        onOffButton.setOnClickListener {
            // 저장한 데이터를 확인한다.
            val model = it.tag as? AlarmDisplayModel ?: return@setOnClickListener
            val newModel = saveAlarmModel(model.hour , model.minute , model.onOff.not())
            renderView(newModel)

            // 온오프에 따라 On인지 Off인지 작업 처리
            if(newModel.onOff){
                // newModel이 켜진 경우 -> 알람을 등록
                val calendar = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY , newModel.hour)
                    set(Calendar.MINUTE , newModel.minute)

                    // 알람시간이 지난 시간으로 설정되어 있으면 다음날 해당 시간으로 설정 되도록록
                   if(before(Calendar.getInstance())){
                        add(Calendar.DATE, 1)
                    }
                }
                val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
                val intent = Intent(this , AlarmReceiver::class.java)
                val pendingIntent = PendingIntent.getBroadcast(this , ALARM_REQUEST_CODE ,
                        intent , PendingIntent.FLAG_UPDATE_CURRENT)

                // 알람 매니저 설정
                // 반복적으로발생해야되기때문에 핸드폰입장에서는 베터리소모와 자원소모가 빠름
                // 따라서 비정확한반복 함수를 사용함.
                // 정확한 주기로 사용하고 싶다면 setExact()를 사용하면 된다.
                alarmManager.setInexactRepeating(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    AlarmManager.INTERVAL_DAY,
                    pendingIntent
                )

            }else{
                // 꺼진경우 -> 알람을 제거
                cancelAlarm()
            }
        }
    }

    private fun saveAlarmModel(
        hour : Int , minute : Int , onOff : Boolean
    ) : AlarmDisplayModel {
        // 데이터를 저장한다.
        val model = AlarmDisplayModel(
            hour = hour,
            minute = minute,
            onOff = onOff
        )

        val sharedPreference = getSharedPreferences(SHARED_PREFERENCE_NAME , Context.MODE_PRIVATE)
        // sharedPreference를 어떻게 사용할 것인지 with 스코프 안에 하나로 묶음
        // edit을 사용했기 대문에 마지막에 commit()으로 적용시키자
        with(sharedPreference.edit()){
            putString("alarm", model.makeDataForDB())
            putBoolean("onOff" , model.onOff)
            commit()
        }

        return model
    }

    private fun renderView(model : AlarmDisplayModel){
        findViewById<TextView>(R.id.ampmTextView).apply {
            text = model.ampmText
        }

        findViewById<TextView>(R.id.timeTextView).apply {
            text = model.timeText
        }

        findViewById<Button>(R.id.onOffButton).apply {
            text = model.onOffText
            tag = model
        }
    }

    private fun cancelAlarm(){
        val pendingIntent = PendingIntent.getBroadcast(this , ALARM_REQUEST_CODE , Intent(this , AlarmReceiver::class.java), PendingIntent.FLAG_NO_CREATE)
        pendingIntent?.cancel()
    }

    // sharedpreference에서 key값을 보기 쉽게 설정하는 방법
    companion object{
        private const val SHARED_PREFERENCE_NAME = "time"
        private const val ALARM_KEY = "alarm"
        private const val ONOFF_KEY = "onOFF"
        private const val ALARM_REQUEST_CODE = 1000
    }
}