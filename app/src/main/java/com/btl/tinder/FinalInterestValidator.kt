package com.btl.tinder

import android.util.Log
import com.btl.tinder.data.InterestData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object FinalInterestValidator {

    sealed class Result {
        data class ExactMatch(val interest: InterestData) : Result()

        data class NewInterest(
            val name: String,
            val needsReview: Boolean = true
        ) : Result()
    }

    suspend fun validate(
        input: String,
        existingInterests: List<InterestData>
    ): Result = withContext(Dispatchers.Default) {

        val trimmed = input.trim()

        // TIER 1: Exact match (99%)
        val exactMatch = existingInterests.find {
            it.name.equals(trimmed, ignoreCase = true)
        }
        if (exactMatch != null) {
            Log.d("Validator", "Tier 1: Exact match '${exactMatch.name}'")
            return@withContext Result.ExactMatch(exactMatch)
        }

        // TIER 2: New interest - cho phép thêm tất cả
        val capitalized = trimmed.split(" ")
            .joinToString(" ") { it.replaceFirstChar { c -> c.uppercase() } }

        Log.d("Validator", "Tier 2: New interest '$capitalized'")
        return@withContext Result.NewInterest(
            name = capitalized,
            needsReview = true
        )
    }

}