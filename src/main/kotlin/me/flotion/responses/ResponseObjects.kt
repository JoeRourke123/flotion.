package me.flotion.responses

import me.flotion.config.ResponseMessages
import me.flotion.model.Flashcard
import me.flotion.model.NotionClientDetails
import me.flotion.model.UnderstandingLimits

object ResponseObjects {
    // ----
    // QUERIES
    // ----
    data class UserDetailsResponse(
        val response: Int = 200,
        val message: String = "OK",
        val user: NotionClientDetails? = null
    )

    data class FlashcardResponse(
        val response: Int = 200,
        val message: String = ResponseMessages.SUCCESS.message,
        val card: Flashcard.FlashcardDetails? = null
    )

    data class ModulesResponse(
        val response: Int = 200, val message: String = ResponseMessages.SUCCESS.message,
        val modules: List<String>? = null,
        val colours: List<String>? = null,
    )

    data class StatData(val name: String, val module: String, val amount: Int)
    data class StatsResponse(
        val response: Int = 200, val message: String = ResponseMessages.SUCCESS.message,
        var modules: List<String>? = null,
        var overall: StatData? = null,
        var moduleRed: List<StatData>? = null,
        var moduleYellow: List<StatData>? = null,
        var moduleGreen: List<StatData>? = null,
        var overallRed: Int? = null, var overallYellow: Int? = null, var overallGreen: Int? = null,
        var moduleCount: Int? = null
    )


    // ----
    // MUTATIONS
    // ----

    data class AuthorisationResponse(
        val response: Int = 200,
        val user: NotionClientDetails? = null,
        val message: String = ResponseMessages.SUCCESS.message
    )

    data class CorrectCardResponse(
        val response: Int = 200,
        val message: String = ResponseMessages.SUCCESS.message,
        val card: Flashcard.FlashcardDetails? = null
    )

    data class AlterLimitResponse(
        val response: Int = 200,
        val message: String = ResponseMessages.SUCCESS.message,
        val limits: UnderstandingLimits? = null
    )
}