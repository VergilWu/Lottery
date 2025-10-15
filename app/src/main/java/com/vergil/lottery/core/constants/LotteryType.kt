package com.vergil.lottery.core.constants


enum class LotteryType(
    val code: String,
    val displayName: String,
    val type: String,
    val redCount: Int,
    val redRange: IntRange,
    val blueCount: Int,
    val blueRange: IntRange?
) {
    SSQ(
        code = "ssq",
        displayName = "双色球",
        type = "福彩",
        redCount = 6,
        redRange = 1..33,
        blueCount = 1,
        blueRange = 1..16
    ),
    QLC(
        code = "qlc",
        displayName = "七乐彩",
        type = "福彩",
        redCount = 7,
        redRange = 1..30,
        blueCount = 0,
        blueRange = null
    ),
    FC3D(
        code = "fc3d",
        displayName = "福彩3D",
        type = "福彩",
        redCount = 3,
        redRange = 0..9,
        blueCount = 0,
        blueRange = null
    ),
    CJDLT(
        code = "cjdlt",
        displayName = "超级大乐透",
        type = "体彩",
        redCount = 5,
        redRange = 1..35,
        blueCount = 2,
        blueRange = 1..12
    ),
    QXC(
        code = "7xc",
        displayName = "七星彩",
        type = "体彩",
        redCount = 7,
        redRange = 0..9,
        blueCount = 0,
        blueRange = null
    ),
    PL3(
        code = "pl3",
        displayName = "排列3",
        type = "体彩",
        redCount = 3,
        redRange = 0..9,
        blueCount = 0,
        blueRange = null
    ),
    PL5(
        code = "pl5",
        displayName = "排列5",
        type = "体彩",
        redCount = 5,
        redRange = 0..9,
        blueCount = 0,
        blueRange = null
    ),
    KL8(
        code = "kl8",
        displayName = "快乐8",
        type = "福彩",
        redCount = 20,
        redRange = 1..80,
        blueCount = 0,
        blueRange = null
    );

    companion object {

        fun fromCode(code: String): LotteryType? {
            return entries.find { it.code == code }
        }
    }
}

