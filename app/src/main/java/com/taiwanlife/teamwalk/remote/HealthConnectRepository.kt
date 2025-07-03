package com.taiwanlife.teamwalk.remote

import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.deleteRecords
import androidx.health.connect.client.records.Record
import androidx.health.connect.client.records.SleepSessionRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.records.TotalCaloriesBurnedRecord
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import java.time.Instant

class HealthConnectRepository(private val healthConnectClient: HealthConnectClient) {

    suspend fun readSleepData(
        startTime: Instant,
        endTime: Instant
    ): List<SleepSessionRecord> {
        try {
            val response = healthConnectClient.readRecords(
                ReadRecordsRequest(
                    SleepSessionRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
                )
            )
            return response.records
        } catch (e: Exception) {
            // Run error handling here
            e.printStackTrace()
        }
        return emptyList()
    }

    suspend fun readStepData(
        startTime: Instant,
        endTime: Instant
    ): List<StepsRecord> {
        try {
            val response = healthConnectClient.readRecords(
                ReadRecordsRequest(
                    StepsRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
                )
            )
            return response.records
        } catch (e: Exception) {
            // Run error handling here
            e.printStackTrace()
        }
        return emptyList()
    }

    suspend fun readTotalCaloriesBurnedData(
        startTime: Instant,
        endTime: Instant
    ): List<TotalCaloriesBurnedRecord> {
        try {
            val response = healthConnectClient.readRecords(
                ReadRecordsRequest(
                    TotalCaloriesBurnedRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
                )
            )
            return response.records
        } catch (e: Exception) {
            // Run error handling here
            e.printStackTrace()
        }
        return emptyList()
    }

    suspend fun writeData(records: List<Record>, callback: () -> Unit) {
        try {
            healthConnectClient.insertRecords(records)
            callback()
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }

    suspend fun deleteSleepDataByTimeRange(startTime: Instant, endTime: Instant) {
        try {
            healthConnectClient.deleteRecords<SleepSessionRecord>(
                timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
            )
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }

    suspend fun deleteStepsDataByTimeRange(startTime: Instant, endTime: Instant) {
        try {
            healthConnectClient.deleteRecords<StepsRecord>(
                timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
            )
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }

    suspend fun deleteTotalCaloriesBurnedDataByTimeRange(startTime: Instant, endTime: Instant) {
        try {
            healthConnectClient.deleteRecords<TotalCaloriesBurnedRecord>(
                timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
            )
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }
}