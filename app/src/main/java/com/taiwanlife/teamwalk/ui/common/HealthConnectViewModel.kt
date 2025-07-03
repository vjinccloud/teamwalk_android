package com.taiwanlife.teamwalk.ui.common

import androidx.health.connect.client.records.SleepSessionRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.records.TotalCaloriesBurnedRecord
import androidx.health.connect.client.records.metadata.Device
import androidx.health.connect.client.records.metadata.Metadata
import androidx.health.connect.client.units.Energy
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.taiwanlife.teamwalk.remote.HealthConnectRepository
import kotlinx.coroutines.launch
import timber.log.Timber
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit
import kotlin.random.Random

class HealthConnectViewModel(
    private val healthConnectRepository: HealthConnectRepository
) : ViewModel() {
    // 設定結束時間為現在
    private val defaultEndTime = Instant.now()
    // 設定一個足夠早的開始時間。
    // 這裡使用 2020 年 1 月 1 日作為範例，因為這通常遠遠早於 30 天的預設限制。
    private val earliestPossibleStartTime =
        LocalDateTime.of(2020, 1, 1, 0, 0, 0).toInstant(ZoneOffset.UTC)



    fun readSleepData(
        startTime: Instant = earliestPossibleStartTime,
        endTime: Instant = defaultEndTime,
        callback: (List<SleepSessionRecord>) -> Unit
    ) {
        viewModelScope.launch {
            val result = healthConnectRepository.readSleepData(startTime, endTime)
            callback.invoke(result)
        }
    }

    fun readStepsData(
        startTime: Instant = earliestPossibleStartTime,
        endTime: Instant = defaultEndTime,
        callback: (List<StepsRecord>) -> Unit
    ) {
        viewModelScope.launch {
            val result = healthConnectRepository.readStepData(startTime, endTime)
            callback.invoke(result)
        }
    }

    fun readTotalCaloriesBurnedData(
        startTime: Instant = earliestPossibleStartTime,
        endTime: Instant = defaultEndTime,
        callback: (List<TotalCaloriesBurnedRecord>) -> Unit
    ) {
        viewModelScope.launch {
            val result = healthConnectRepository.readTotalCaloriesBurnedData(startTime, endTime)
            callback.invoke(result)
        }
    }

    /**
     *  模擬30天前的資料 插入之前會先把所有來自我們APP的插入資料都先刪除 避免混亂
     */
    fun writeAndCleanDummyHealthDataForPast30Days(callback: () -> Unit) {
        viewModelScope.launch {
            try {
                healthConnectRepository.deleteStepsDataByTimeRange(
                    earliestPossibleStartTime,
                    defaultEndTime
                )
                healthConnectRepository.deleteSleepDataByTimeRange(
                    earliestPossibleStartTime,
                    defaultEndTime
                )
//                healthConnectRepository.deleteTotalCaloriesBurnedDataByTimeRange(
//                    earliestPossibleStartTime,
//                    defaultEndTime
//                )

                val stepsRecords = mutableListOf<StepsRecord>()
                val now = Instant.now()

                for (i in 1 until 31) {
                    val dayStart =
                        now.minus(i.toLong(), ChronoUnit.DAYS).truncatedTo(ChronoUnit.DAYS)
                    val stepsStartTime = dayStart.plus(8, ChronoUnit.HOURS)
                    val stepsEndTime = dayStart.plus(20, ChronoUnit.HOURS)
                    val stepsCount = Random.Default.nextLong(3000, 15000)

                    val record = StepsRecord(
                        count = stepsCount,
                        startTime = stepsStartTime,
                        endTime = stepsEndTime,
                        startZoneOffset = ZoneOffset.UTC,
                        endZoneOffset = ZoneOffset.UTC,
                        metadata = Metadata.Companion.autoRecorded(
                            device = Device(type = Device.Companion.TYPE_WATCH)
                        )
                    )
                    stepsRecords.add(record)
                }
                healthConnectRepository.writeData(stepsRecords) {
                    Timber.Forest.d("完成步數假資料")
                }

                val sleepRecords = mutableListOf<SleepSessionRecord>()

                for (i in 1 until 31) {
                    val dayStart =
                        now.minus(i.toLong(), ChronoUnit.DAYS).truncatedTo(ChronoUnit.DAYS)
                    val sleepStartTime = dayStart.plus(22, ChronoUnit.HOURS)
                    val sleepEndTime = sleepStartTime.plus(Random.Default.nextLong(6, 9), ChronoUnit.HOURS)

                    val record = SleepSessionRecord(
                        startTime = sleepStartTime,
                        endTime = sleepEndTime,
                        startZoneOffset = ZoneOffset.UTC,
                        endZoneOffset = ZoneOffset.UTC,
                        title = "每日睡眠記錄 - ${i + 1}天前",
                        notes = "自動生成模擬睡眠數據",
                        stages = listOf(
                            SleepSessionRecord.Stage(
                                startTime = sleepStartTime,
                                endTime = sleepEndTime,
                                stage = SleepSessionRecord.Companion.STAGE_TYPE_SLEEPING
                            )
                        ),
                        metadata = Metadata.Companion.autoRecorded(
                            device = Device(type = Device.Companion.TYPE_WATCH)
                        )
                    )
                    sleepRecords.add(record)
                }
                healthConnectRepository.writeData(sleepRecords) {
                    Timber.Forest.d("完成睡眠假資料")
                }
//                val totalCaloriesRecords = mutableListOf<TotalCaloriesBurnedRecord>()
//
//                for (i in 0 until 30) {
//                    val dayStart =
//                        now.minus(i.toLong(), ChronoUnit.DAYS).truncatedTo(ChronoUnit.DAYS)
//                    val totalCaloriesCount =
//                        Random.Default.nextDouble(1800.0, 2500.0) // 每天總熱量 1800 到 2500 大卡
//
//                    val record = TotalCaloriesBurnedRecord(
//                        energy = Energy.Companion.calories(totalCaloriesCount),
//                        startTime = dayStart.plus(0, ChronoUnit.HOURS), // 從當天午夜開始
//                        endTime = dayStart.plus(23, ChronoUnit.HOURS)
//                            .plus(59, ChronoUnit.MINUTES), // 到當天午夜前
//                        startZoneOffset = ZoneOffset.systemDefault().rules.getOffset(dayStart),
//                        endZoneOffset = ZoneOffset.systemDefault().rules.getOffset(
//                            dayStart.plus(
//                                23,
//                                ChronoUnit.HOURS
//                            ).plus(59, ChronoUnit.MINUTES)
//                        ),
//                        metadata = Metadata.Companion.autoRecorded(
//                            device = Device(type = Device.Companion.TYPE_WATCH)
//                        )
//                    )
//                    totalCaloriesRecords.add(record)
//                }
//                healthConnectRepository.writeData(totalCaloriesRecords) {
//                    Timber.Forest.d("完成卡洛里假資料")
//                }

                callback()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}