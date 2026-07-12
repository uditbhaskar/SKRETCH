package com.example.skretch.demo

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.Color
import com.example.skretch.R
import kotlin.random.Random

internal data class DemoScratchReward(
    val emoji: String,
    @param:StringRes val titleRes: Int,
    @param:StringRes val subtitleRes: Int,
    val isWin: Boolean,
    @param:StringRes val labelRes: Int? = R.string.demo_prize_won_label,
    val backgroundColor: Color = Color.White,
    val titleColor: Color = Color(0xFF1A73E8),
    val subtitleColor: Color = Color(0xFF5F6368),
    val labelColor: Color = Color(0xFF34A853),
)

internal fun randomDemoScratchReward(random: Random = Random.Default): DemoScratchReward {
    return when (random.nextInt(100)) {
        in 0..14 -> DemoScratchReward(
            emoji = "🏆",
            titleRes = R.string.demo_prize_10,
            subtitleRes = R.string.demo_prize_10_subtitle,
            isWin = true,
        )
        in 15..27 -> DemoScratchReward(
            emoji = "🎁",
            titleRes = R.string.demo_prize_25,
            subtitleRes = R.string.demo_prize_25_subtitle,
            isWin = true,
        )
        in 28..38 -> DemoScratchReward(
            emoji = "💰",
            titleRes = R.string.demo_prize_50,
            subtitleRes = R.string.demo_prize_50_subtitle,
            isWin = true,
        )
        in 39..44 -> DemoScratchReward(
            emoji = "🎉",
            titleRes = R.string.demo_prize_100,
            subtitleRes = R.string.demo_prize_100_subtitle,
            isWin = true,
            titleColor = Color(0xFFEA8600),
            labelColor = Color(0xFFEA8600),
        )
        in 45..54 -> DemoScratchReward(
            emoji = "🏷️",
            titleRes = R.string.demo_prize_cashback,
            subtitleRes = R.string.demo_prize_cashback_subtitle,
            isWin = true,
            titleColor = Color(0xFF9334E6),
            labelColor = Color(0xFF9334E6),
        )
        in 55..62 -> DemoScratchReward(
            emoji = "🚚",
            titleRes = R.string.demo_prize_free_delivery,
            subtitleRes = R.string.demo_prize_free_delivery_subtitle,
            isWin = true,
            titleColor = Color(0xFF188038),
            labelColor = Color(0xFF188038),
        )
        else -> DemoScratchReward(
            emoji = "🎫",
            titleRes = R.string.demo_better_luck,
            subtitleRes = R.string.demo_better_luck_subtitle,
            isWin = false,
            labelRes = null,
            backgroundColor = Color(0xFFF3F4F6),
            titleColor = Color(0xFF5F6368),
            subtitleColor = Color(0xFF80868B),
        )
    }
}
