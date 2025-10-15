package com.vergil.lottery.data.remote.dto

import kotlinx.serialization.Serializable


@Serializable
data class ApiResponse<T>(
    val code: Int,
    val msg: String,
    val time: String,
    val data: T
)

