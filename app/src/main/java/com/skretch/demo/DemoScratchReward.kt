package com.skretch.demo

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.Color
import com.skretch.R
import kotlin.random.Random

/**
 * Prize copy and colors shown under the demo scratch cover.
 */
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

private val demoScratchRewards = listOf(
    DemoScratchReward(
        emoji = "🏆",
        titleRes = R.string.demo_prize_10,
        subtitleRes = R.string.demo_prize_10_subtitle,
        isWin = true,
    ),
    DemoScratchReward(
        emoji = "🎁",
        titleRes = R.string.demo_prize_25,
        subtitleRes = R.string.demo_prize_25_subtitle,
        isWin = true,
    ),
    DemoScratchReward(
        emoji = "💰",
        titleRes = R.string.demo_prize_50,
        subtitleRes = R.string.demo_prize_50_subtitle,
        isWin = true,
    ),
    DemoScratchReward(
        emoji = "🎉",
        titleRes = R.string.demo_prize_100,
        subtitleRes = R.string.demo_prize_100_subtitle,
        isWin = true,
        titleColor = Color(0xFFEA8600),
        labelColor = Color(0xFFEA8600),
    ),
    DemoScratchReward(
        emoji = "🏷️",
        titleRes = R.string.demo_prize_cashback,
        subtitleRes = R.string.demo_prize_cashback_subtitle,
        isWin = true,
        titleColor = Color(0xFF9334E6),
        labelColor = Color(0xFF9334E6),
    ),
    DemoScratchReward(
        emoji = "🚚",
        titleRes = R.string.demo_prize_free_delivery,
        subtitleRes = R.string.demo_prize_free_delivery_subtitle,
        isWin = true,
        titleColor = Color(0xFF188038),
        labelColor = Color(0xFF188038),
    ),
    DemoScratchReward(
        emoji = "🎫",
        titleRes = R.string.demo_better_luck,
        subtitleRes = R.string.demo_better_luck_subtitle,
        isWin = false,
        labelRes = null,
        backgroundColor = Color(0xFFF3F4F6),
        titleColor = Color(0xFF5F6368),
        subtitleColor = Color(0xFF80868B),
    ),
)

/** Returns a reward by index, wrapping with a positive modulo. */
internal fun demoScratchRewardAt(index: Int): DemoScratchReward {
    val safeIndex = index.floorMod(demoScratchRewards.size)
    return demoScratchRewards[safeIndex]
}

/** Random index into the demo reward pool. */
internal fun randomDemoScratchRewardIndex(random: Random = Random.Default): Int =
    random.nextInt(demoScratchRewards.size)

private fun Int.floorMod(modulus: Int): Int {
    val remainder = this % modulus
    return if (remainder >= 0) remainder else remainder + modulus
}
