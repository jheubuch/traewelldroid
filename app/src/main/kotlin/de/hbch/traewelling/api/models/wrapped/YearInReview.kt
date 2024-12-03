package de.hbch.traewelling.api.models.wrapped

import de.hbch.traewelling.api.models.user.LightUser

data class YearInReview(
    val user: LightUser,
    val count: Int,
    val distance: YearInReviewSumStats,
    val duration: YearInReviewSumStats,
    val totalDelay: Int,
)

data class YearInReviewSumStats(
    val total: Long,
    val averagePerDay: Float
)
