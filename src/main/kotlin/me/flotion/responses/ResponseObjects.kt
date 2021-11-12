package me.flotion.responses

import me.flotion.config.ResponseMessages
import me.flotion.model.Flashcard
import me.flotion.model.NotionClientDetails

object ResponseObjects {
    // ----
    // QUERIES
    // ----
    class UserDetailsResponse(
        val response: Int = 200,
        val message: String = "OK",
        val user: NotionClientDetails? = null
    )

    class FlashcardResponse(
        val response: Int = 200,
        val message: String = ResponseMessages.SUCCESS.message,
        val card: Flashcard.FlashcardDetails? = null
    )

    class ModulesResponse(
        val response: Int = 200, val message: String = ResponseMessages.SUCCESS.message,
        val modules: List<String>? = null,
        val colours: List<String>? = null,
    )

    class StatData(val name: String, val module: String, val amount: Int)
    class StatsResponse(
        val response: Int = 200, val message: String = ResponseMessages.SUCCESS.message,
        var modules: List<String>? = null,
        var overall: StatData? = null,
        var moduleRed: List<StatData>? = null,
        var moduleYellow: List<StatData>? = null,
        var moduleGreen: List<StatData>? = null,
        var overallRed: Int? = null, var overallYellow: Int? = null, var overallGreen: Int? = null,
        var moduleCount: Int? = null
    )
}