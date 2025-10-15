package com.vergil.lottery.data.remote

import com.vergil.lottery.core.constants.AppConstants
import com.vergil.lottery.data.remote.dto.ApiResponse
import com.vergil.lottery.data.remote.dto.DrawResultDTO
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter


class LotteryApiService(private val httpClient: HttpClient) {


    suspend fun getLatestDraw(lotteryCode: String): ApiResponse<DrawResultDTO> {
        return httpClient.get("kjxx") {
            parameter("apikey", AppConstants.API_KEY)
            parameter("code", lotteryCode)
        }.body()
    }


    suspend fun getDrawByIssue(issue: String, lotteryCode: String): ApiResponse<DrawResultDTO> {
        return httpClient.get("issue") {
            parameter("apikey", AppConstants.API_KEY)
            parameter("issue", issue)
            parameter("code", lotteryCode)
        }.body()
    }


    suspend fun getHistory(lotteryCode: String, size: Int = 100): ApiResponse<List<DrawResultDTO>> {
        return httpClient.get("history") {
            parameter("apikey", AppConstants.API_KEY)
            parameter("code", lotteryCode)
            parameter("size", size)
        }.body()
    }
}

