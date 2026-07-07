package com.bluepatitas.mobile.data.remote.feeding

data class CreateFeedingPlanRequestDto(
    val animalId: String,
    val dietName: String,
    val nutritionalNotes: String,
    val foodQuantity: Double,
    val foodUnit: String,
    val timesPerDay: Int,
    val scheduledTimes: String,
    val toleranceMinutes: Int
)

data class UpdateFeedingPlanRequestDto(
    val dietName: String?,
    val nutritionalNotes: String?,
    val foodQuantity: Double?,
    val foodUnit: String?,
    val timesPerDay: Int?,
    val scheduledTimes: String?,
    val toleranceMinutes: Int?
)

data class FeedingPlanDto(
    val id: String?,
    val animalId: String?,
    val dietType: DietTypeDto?,
    val foodAmount: FoodAmountDto?,
    val schedule: FeedingScheduleDto?,
    val status: String?,
    val createdAt: String?,
    val updatedAt: String?
)

data class DietTypeDto(
    val name: String?,
    val nutritionalNotes: String?
)

data class FoodAmountDto(
    val quantity: Double?,
    val unit: String?
)

data class FeedingScheduleDto(
    val timesPerDay: Int?,
    val scheduledTimes: String?,
    val toleranceMinutes: Int?
)
