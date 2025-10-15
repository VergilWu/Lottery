package com.vergil.lottery.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.vergil.lottery.domain.model.DrawResult
import com.vergil.lottery.domain.model.WinnerDetail


@Entity(tableName = "draw_history")
data class DrawHistoryEntity(
    @PrimaryKey
    val id: String,              
    val type: String,
    val name: String,
    val code: String,
    val issue: String,
    val redNumbers: String,      
    val blueNumbers: String,     
    val drawDate: String,
    val timeRule: String,
    val saleMoney: String?,
    val prizePool: String?,
    val createdAt: Long,
    val updatedAt: Long
) {

    fun toDomain(): DrawResult {
        return DrawResult(
            type = type,
            name = name,
            code = code,
            issue = issue,
            red = redNumbers.split(" ").filter { it.isNotBlank() },
            blue = blueNumbers.split(" ").filter { it.isNotBlank() },
            drawDate = drawDate,
            timeRule = timeRule,
            saleMoney = saleMoney,
            prizePool = prizePool,
            winnerDetail = null  
        )
    }

    companion object {

        fun fromDomain(drawResult: DrawResult): DrawHistoryEntity {
            val currentTime = System.currentTimeMillis()
            return DrawHistoryEntity(
                id = "${drawResult.code}_${drawResult.issue}",
                type = drawResult.type,
                name = drawResult.name,
                code = drawResult.code,
                issue = drawResult.issue,
                redNumbers = drawResult.red.joinToString(" "),
                blueNumbers = drawResult.blue.joinToString(" "),
                drawDate = drawResult.drawDate,
                timeRule = drawResult.timeRule,
                saleMoney = drawResult.saleMoney,
                prizePool = drawResult.prizePool,
                createdAt = currentTime,
                updatedAt = currentTime
            )
        }
    }
}

