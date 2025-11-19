package com.btl.tinder

import android.util.Log
import com.btl.tinder.data.InterestData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object FinalInterestValidator {

    sealed class Result {
        data class ExactMatch(val interest: InterestData) : Result()

        data class TypoSuggestion(
            val original: String,
            val suggested: InterestData,
            val distance: Int
        ) : Result()

        data class NewInterest(
            val name: String,
            val needsReview: Boolean = true
        ) : Result()

        data class Invalid(val reason: String) : Result()
    }

    suspend fun validate(
        input: String,
        existingInterests: List<InterestData>
    ): Result = withContext(Dispatchers.Default) {

        val trimmed = input.trim()

        if (trimmed.length < 2) {
            return@withContext Result.Invalid("Too short (min 2 characters)")
        }

        if (trimmed.length > 50) {
            return@withContext Result.Invalid("Too long (max 50 characters)")
        }

        // TIER 1: Exact match (99%)
        val exactMatch = existingInterests.find {
            it.name.equals(trimmed, ignoreCase = true)
        }
        if (exactMatch != null) {
            Log.d("Validator", "Tier 1: Exact match '${exactMatch.name}'")
            return@withContext Result.ExactMatch(exactMatch)
        }

        // TIER 2: Typo detection (0.9%)
        val typo = findTypo(trimmed, existingInterests, maxDistance = 2)
        if (typo != null) {
            val distance = levenshteinDistance(trimmed.lowercase(), typo.name.lowercase())
            Log.d("Validator", "Tier 2: Typo '$trimmed' -> '${typo.name}' (distance=$distance)")
            return@withContext Result.TypoSuggestion(
                original = trimmed,
                suggested = typo,
                distance = distance
            )
        }

        // TIER 3: New interest (0.1%)
        val capitalized = trimmed.split(" ")
            .joinToString(" ") { it.replaceFirstChar { c -> c.uppercase() } }

        Log.d("Validator", "Tier 3: New interest '$capitalized'")
        return@withContext Result.NewInterest(
            name = capitalized,
            needsReview = true
        )
    }

    private fun findTypo(
        input: String,
        interests: List<InterestData>,
        maxDistance: Int
    ): InterestData? {
        val lower = input.lowercase()

        return interests
            .map { it to levenshteinDistance(lower, it.name.lowercase()) }
            .filter { it.second in 1..maxDistance }
            .minByOrNull { it.second }
            ?.first
    }

    private fun levenshteinDistance(s1: String, s2: String): Int {
        val len1 = s1.length
        val len2 = s2.length

        val dp = Array(len1 + 1) { IntArray(len2 + 1) }

        for (i in 0..len1) dp[i][0] = i
        for (j in 0..len2) dp[0][j] = j

        for (i in 1..len1) {
            for (j in 1..len2) {
                val cost = if (s1[i - 1] == s2[j - 1]) 0 else 1
                dp[i][j] = minOf(
                    dp[i - 1][j] + 1,
                    dp[i][j - 1] + 1,
                    dp[i - 1][j - 1] + cost
                )
            }
        }

        return dp[len1][len2]
    }
}