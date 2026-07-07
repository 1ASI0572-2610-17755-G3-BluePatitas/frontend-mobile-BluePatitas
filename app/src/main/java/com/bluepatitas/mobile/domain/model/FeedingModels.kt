package com.bluepatitas.mobile.domain.model

data class FeedingPlan(
    val id: String,
    val animalId: String,
    val dietType: DietType,
    val foodAmount: FoodAmount,
    val schedule: FeedingSchedule,
    val status: FeedingPlanStatus,
    val createdAt: String?,
    val updatedAt: String?
)

data class DietType(
    val name: String,
    val nutritionalNotes: String?
)

data class FoodAmount(
    val quantity: Double?,
    val unit: String
)

data class FeedingSchedule(
    val timesPerDay: Int?,
    val scheduledTimes: String,
    val toleranceMinutes: Int?
)

enum class FeedingPlanStatus {
    Draft,
    Active,
    Inactive,
    Unknown;

    companion object {
        fun fromApiValue(value: String?): FeedingPlanStatus =
            when (value?.trim()?.uppercase()) {
                "DRAFT" -> Draft
                "ACTIVE" -> Active
                "INACTIVE" -> Inactive
                else -> Unknown
            }
    }
}

data class FeedingPlanForm(
    val animalId: String,
    val dietName: String,
    val nutritionalNotes: String,
    val foodQuantity: Double,
    val foodUnit: String,
    val timesPerDay: Int,
    val scheduledTimes: String,
    val toleranceMinutes: Int
)
