package com.taiwanlife.teamwalk.ui.common

import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.records.TotalCaloriesBurnedRecord
import androidx.health.connect.client.records.metadata.Device
import androidx.health.connect.client.records.metadata.Metadata
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.taiwanlife.teamwalk.remote.HealthConnectRepository
import com.taiwanlife.teamwalk.ui.common.model.TeamWalkRecordModel
import com.taiwanlife.teamwalk.utils.toSleepTeamWalkRecord
import com.taiwanlife.teamwalk.utils.toStepTeamWalkRecord
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

class HealthConnectViewModel(
    private val healthConnectRepository: HealthConnectRepository
) : ViewModel() {
    private val zoneId = ZoneId.systemDefault()

    // 設定一個足夠早的開始時間。
    // 這裡使用 2020 年 1 月 1 日作為範例，因為這通常遠遠早於 30 天的預設限制。
    private val earliestPossibleStartTime =
        LocalDateTime.of(2020, 1, 1, 0, 0, 0).toInstant(ZoneOffset.UTC)


    fun getAllData(
        startTime: Instant = Instant.now().minus(30, ChronoUnit.DAYS).truncatedTo(ChronoUnit.DAYS),
//        startTime: Instant = LocalDate.now(zoneId).atStartOfDay(zoneId).toInstant(),
        endTime: Instant =  Instant.now(),
        callback: (List<TeamWalkRecordModel>, List<TeamWalkRecordModel>) -> Unit
    ) {
        viewModelScope.launch {
//            val sleepDataDeferred =
//                async { healthConnectRepository.readSleepData(startTime, endTime) }
//            val stepDataDeferred =
//                async { healthConnectRepository.readStepData(startTime, endTime) }
//
//            val sleepData = sleepDataDeferred.await().filter { record ->
//                record.metadata.recordingMethod != Metadata.RECORDING_METHOD_MANUAL_ENTRY
//            }.map { it.toTeamWalkRecord() }
//            val stepData = stepDataDeferred.await().filter { record ->
//                record.metadata.recordingMethod != Metadata.RECORDING_METHOD_MANUAL_ENTRY
//            }.map { it.toTeamWalkRecord() }

//            val formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss")
//                .withZone(ZoneId.systemDefault()) // 使用系統時區
//            Timber.d(
//                "第${i}次 呼叫時間 => ${formatter.format(Instant.now())} / startTime = ${
//                    formatter.format(
//                        startTime
//                    )
//                } endTime = ${formatter.format(endTime)}"
//            )

            val sleepDataDeferred =
                async { healthConnectRepository.readSleepDataAsBuckets(startTime, endTime) }
            val sleepData = sleepDataDeferred.await().map { bucket ->
                bucket.toSleepTeamWalkRecord()
            }.filter { record -> record.data > 0 }

            val stepDataDeferred =
                async { healthConnectRepository.readStepDataAsBuckets(startTime, endTime) }
            val stepData = stepDataDeferred.await().map { bucket ->
                bucket.toStepTeamWalkRecord()
            }.filter { record -> record.data > 0 }
//            Timber.d(
//                "第${i}次 步數共 ${stepData.sumOf { it.data }}"
//            )
//
//            delay(1000)
//            getAllData { _, _ -> }
            callback(sleepData, stepData)

        }
    }

//
//    fun readSleepData(
//        startTime: Instant = earliestPossibleStartTime,
//        endTime: Instant = defaultEndTime,
//        callback: (List<SleepSessionRecord>) -> Unit
//    ) {
//        viewModelScope.launch {
//            val result = healthConnectRepository.readSleepData(startTime, endTime)
//            callback.invoke(result)
//        }
//    }
//
//    fun readStepsData(
//        startTime: Instant = earliestPossibleStartTime,
//        endTime: Instant = defaultEndTime,
//        callback: (List<StepsRecord>) -> Unit
//    ) {
//        viewModelScope.launch {
//            val result = healthConnectRepository.readStepData(startTime, endTime)
//            callback.invoke(result)
//        }
//    }

    fun readTotalCaloriesBurnedData(
        startTime: Instant = earliestPossibleStartTime,
        endTime: Instant =  Instant.now(),
        callback: (List<TotalCaloriesBurnedRecord>) -> Unit
    ) {
        viewModelScope.launch {
            val result = healthConnectRepository.readTotalCaloriesBurnedData(startTime, endTime)
            callback.invoke(result)
        }
    }


    fun deleteAllOurData(callback: () -> Unit) {
        viewModelScope.launch {
            try {
                healthConnectRepository.deleteStepsDataByTimeRange(
                    earliestPossibleStartTime,
                    Instant.now()
                )
                healthConnectRepository.deleteSleepDataByTimeRange(
                    earliestPossibleStartTime,
                    Instant.now()
                )

                callback()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun insertManualSteps(
        startStr: String = "2026/01/30 14:05:20", // 格式: "yyyy/MM/dd HH:mm:ss"
        endStr: String = "2026/01/30 14:40:20",   // 格式: "yyyy/MM/dd HH:mm:ss"
        steps: Int = 30000
    ) {
        viewModelScope.launch {
            try {
                val formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss")
                val zoneId = ZoneId.systemDefault()

                // 1. 將字串解析為 LocalDateTime，並根據系統時區轉為 Instant
                val sTime = LocalDateTime.parse(startStr, formatter).atZone(zoneId).toInstant()
                val eTime = LocalDateTime.parse(endStr, formatter).atZone(zoneId).toInstant()

                // 2. 取得時區偏移量 (Offset)
                val sOffset = zoneId.rules.getOffset(sTime)
                val eOffset = zoneId.rules.getOffset(eTime)

                // 3. 建立 StepsRecord
                val record = StepsRecord(
                    count = steps.toLong(),
                    startTime = sTime,
                    endTime = eTime,
                    startZoneOffset = sOffset,
                    endZoneOffset = eOffset,
                    // 標記為手動輸入或由手錶紀錄
                    metadata = Metadata.manualEntry(device = Device(type = Device.TYPE_WATCH))
                )

                // 4. 寫入 Health Connect
                healthConnectRepository.writeData(listOf(record)) {
                    Timber.d("成功手動插入步數: $steps 步 ($startStr ~ $endStr)")
                }
            } catch (e: Exception) {
                Timber.e(e, "插入步數資料失敗")
            }
        }
    }
    /**
     *  模擬30天前的資料 插入之前會先把所有來自我們APP的插入資料都先刪除 避免混亂
     */
//    fun writeAndCleanDummyHealthData(callback: () -> Unit) {
//        viewModelScope.launch {
//            try {
//                healthConnectRepository.deleteStepsDataByTimeRange(
//                    earliestPossibleStartTime,
//                    defaultEndTime
//                )
//                healthConnectRepository.deleteSleepDataByTimeRange(
//                    earliestPossibleStartTime,
//                    defaultEndTime
//                )
////                healthConnectRepository.deleteTotalCaloriesBurnedDataByTimeRange(
////                    earliestPossibleStartTime,
////                    defaultEndTime
////                )
//                val now = Instant.now().atZone(ZoneId.systemDefault())
//                val stepsRecords = mutableListOf<StepsRecord>()
//                val sleepRecords = mutableListOf<SleepSessionRecord>()
//
//                for (i in 1 until 100) {
//                    val targetDay = now.minusDays(i.toLong())
//
//                    val periods = listOf(8 to 10, 12 to 14, 18 to 21) // 定義活動時段
//                    periods.forEach { (startHour, endHour) ->
//                        // 在時段內隨機取時間點
//                        val randomStartHour = Random.nextInt(startHour, endHour)
//                        val randomStartMin = Random.nextInt(0, 60)
//                        val durationMin = Random.nextInt(10, 45) // 每次走 10~45 分鐘
//
//                        val sTime = targetDay.withHour(randomStartHour).withMinute(randomStartMin)
//                            .toInstant()
//                        val eTime = sTime.plus(durationMin.toLong(), ChronoUnit.MINUTES)
//
//                        stepsRecords.add(
//                            StepsRecord(
//                                count = Random.nextLong(500, 3000), // 該時段步數
//                                startTime = sTime,
//                                endTime = eTime,
//                                startZoneOffset = ZoneOffset.systemDefault().rules.getOffset(sTime),
//                                endZoneOffset = ZoneOffset.systemDefault().rules.getOffset(eTime),
//                                metadata = Metadata.autoRecorded(device = Device(type = Device.TYPE_WATCH))
//                            )
//                        )
//                    }
//
//                    val sleepStartHour = if (Random.nextBoolean()) 22 + Random.nextInt(0, 2) else 0
//                    val sleepStartMin = Random.nextInt(0, 60)
//                    val sleepDurationHours = Random.nextInt(6, 9) // 睡 6~9 小時
//
//                    val sleepStart =
//                        targetDay.minusDays(1).withHour(sleepStartHour).withMinute(sleepStartMin)
//                            .toInstant()
//                    val sleepEnd = sleepStart.plus(sleepDurationHours.toLong(), ChronoUnit.HOURS)
//                        .plus(Random.nextInt(0, 60).toLong(), ChronoUnit.MINUTES)
//
//                    val stages = mutableListOf<SleepSessionRecord.Stage>()
//                    var stageStart = sleepStart
//
//                    val part = Duration.between(sleepStart, sleepEnd).dividedBy(3)
//
//                    stages.add(
//                        SleepSessionRecord.Stage(
//                            stageStart,
//                            stageStart.plus(part),
//                            SleepSessionRecord.STAGE_TYPE_LIGHT
//                        )
//                    )
//                    stageStart = stageStart.plus(part)
//                    stages.add(
//                        SleepSessionRecord.Stage(
//                            stageStart,
//                            stageStart.plus(part),
//                            SleepSessionRecord.STAGE_TYPE_DEEP
//                        )
//                    )
//                    stageStart = stageStart.plus(part)
//                    stages.add(
//                        SleepSessionRecord.Stage(
//                            stageStart,
//                            sleepEnd,
//                            SleepSessionRecord.STAGE_TYPE_REM
//                        )
//                    )
//
//                    sleepRecords.add(
//                        SleepSessionRecord(
//                            startTime = sleepStart,
//                            endTime = sleepEnd,
//                            startZoneOffset = ZoneOffset.systemDefault().rules.getOffset(sleepStart),
//                            endZoneOffset = ZoneOffset.systemDefault().rules.getOffset(sleepEnd),
//                            stages = stages,
//                            metadata = Metadata.autoRecorded(device = Device(type = Device.TYPE_WATCH))
//                        )
//                    )
//                }
//
//                // 寫入資料
//                healthConnectRepository.writeData(stepsRecords) { Timber.d("完成分段步數假資料") }
//                healthConnectRepository.writeData(sleepRecords) { Timber.d("完成多階段睡眠假資料") }
//
//                callback()
//            } catch (e: Exception) {
//                e.printStackTrace()
//            }
//        }
//    }
}