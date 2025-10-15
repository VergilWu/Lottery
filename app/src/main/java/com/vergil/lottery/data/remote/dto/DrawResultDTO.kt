package com.vergil.lottery.data.remote.dto

import com.vergil.lottery.domain.model.BetWinner
import com.vergil.lottery.domain.model.DrawResult
import com.vergil.lottery.domain.model.WinnerDetail
import kotlinx.serialization.*
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.encoding.*
import kotlinx.serialization.json.*


@Serializable
data class DrawResultDTO(
    val type: String,
    val name: String,
    val code: String,
    val issue: String,
    val red: String,              
    val blue: String,
    @SerialName("drawdate")
    val drawDate: String,
    @SerialName("time_rule")
    val timeRule: String,
    @SerialName("sale_money")
    val saleMoney: String? = null,
    @SerialName("prize_pool")
    val prizePool: String? = null,
    @SerialName("red_order")
    val redOrder: String? = null,
    @SerialName("blue_order")
    val blueOrder: String? = null,
    @SerialName("winner_detail")
    val winnerDetail: List<WinnerDetailDTO>? = null
) {

    fun toDomain(): DrawResult {
        return DrawResult(
            type = type,
            name = name,
            code = code,
            issue = issue,
            red = red.split(" ").filter { it.isNotBlank() },
            blue = blue.split(" ").filter { it.isNotBlank() },
            drawDate = drawDate,
            timeRule = timeRule,
            saleMoney = saleMoney,
            prizePool = prizePool,
            winnerDetail = winnerDetail?.map { it.toDomain() }
        )
    }
}


@Serializable
data class WinnerDetailDTO(
    @SerialName("awardEtc")
    val awardEtc: String,
    @SerialName("baseBetWinner")
    @Serializable(with = NullableStringOrObjectSerializer::class)
    val baseBetWinner: BetWinnerDTO? = null,
    @SerialName("addToBetWinner")
    @Serializable(with = NullableStringOrObjectSerializer::class)
    val addToBetWinner: BetWinnerDTO? = null,  
    @SerialName("addToBetWinner2")
    @Serializable(with = NullableStringOrObjectSerializer::class)
    val addToBetWinner2: BetWinnerDTO? = null,
    @SerialName("addToBetWinner3")
    @Serializable(with = NullableStringOrObjectSerializer::class)
    val addToBetWinner3: BetWinnerDTO? = null
) {
    fun toDomain(): WinnerDetail {
        return WinnerDetail(
            awardEtc = awardEtc,
            baseBetWinner = baseBetWinner?.toDomain(),
            addToBetWinner = addToBetWinner?.toDomain(),
            addToBetWinner2 = addToBetWinner2?.toDomain(),
            addToBetWinner3 = addToBetWinner3?.toDomain()
        )
    }
}


@Serializable
data class BetWinnerDTO(
    val remark: String = "",
    @SerialName("awardNum")
    val awardNum: String = "",
    @SerialName("awardMoney")
    val awardMoney: String = "",
    @SerialName("totalMoney")
    val totalMoney: String = ""
) {
    fun toDomain(): BetWinner {
        return BetWinner(
            remark = remark,
            awardNum = awardNum,
            awardMoney = awardMoney,
            totalMoney = totalMoney
        )
    }
}


@OptIn(InternalSerializationApi::class, ExperimentalSerializationApi::class)
object NullableStringOrObjectSerializer : KSerializer<BetWinnerDTO?> {
    override val descriptor: SerialDescriptor = buildSerialDescriptor(
        "NullableStringOrObject",
        PolymorphicKind.SEALED
    )

    override fun serialize(encoder: Encoder, value: BetWinnerDTO?) {
        if (value == null) {
            encoder.encodeNull()
        } else {
            encoder.encodeSerializableValue(BetWinnerDTO.serializer(), value)
        }
    }

    override fun deserialize(decoder: Decoder): BetWinnerDTO? {
        return when (val jsonElement = (decoder as JsonDecoder).decodeJsonElement()) {
            is JsonNull -> null
            is JsonPrimitive -> {

                if (jsonElement.isString) null else null
            }
            is JsonObject -> {

                decoder.json.decodeFromJsonElement(BetWinnerDTO.serializer(), jsonElement)
            }
            else -> null
        }
    }
}

