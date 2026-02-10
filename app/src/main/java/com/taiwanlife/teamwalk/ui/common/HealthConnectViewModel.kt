package com.taiwanlife.teamwalk.ui.common

import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.records.TotalCaloriesBurnedRecord
import androidx.health.connect.client.records.metadata.Device
import androidx.health.connect.client.records.metadata.Metadata
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.taiwanlife.teamwalk.Config
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
            callback(sleepData, stepData)

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
}