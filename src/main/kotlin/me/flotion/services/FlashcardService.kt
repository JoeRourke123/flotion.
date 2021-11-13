package me.flotion.services

import me.flotion.config.CORRECT_PAGE_KEY
import me.flotion.config.NotionSingleton
import me.flotion.config.ResponseMessages
import me.flotion.model.Flashcard
import me.flotion.model.FlashcardFactory
import me.flotion.model.NotionUser
import me.flotion.model.Understanding
import me.flotion.query.FlashcardQuery
import org.jraf.klibnotion.model.database.query.DatabaseQuery
import org.jraf.klibnotion.model.page.Page
import org.springframework.stereotype.Component
import java.util.*
import kotlin.random.Random

@Component
class FlashcardService {
    suspend fun incrementCardCorrectCount(card: String, user: NotionUser): Flashcard {
        val flashcard = FlashcardFactory.buildCardWithoutContents(card, user)
        flashcard.incrementCorrect()

        return flashcard
    }

    suspend fun getRandomFlashcard(
        moduleFilter: DatabaseQuery,
        includedUnderstandings: List<Understanding>,
        user: NotionUser
    ): Flashcard? {
        val client = NotionSingleton.userClient(
            user.accessToken
        )

        val cardDB = user.databaseID
        val understandingSet = setOf(*includedUnderstandings.toTypedArray());

        var query = client.databases.queryDatabase(cardDB, moduleFilter)

        val pages: MutableList<Page> = ArrayList(query.results).toMutableList()

        while (query.nextPagination != null) {
            query = client.databases.queryDatabase(cardDB, moduleFilter, pagination = query.nextPagination!!)

            pages += query.results
        }

        val filteredPages = if (includedUnderstandings.isEmpty()) {
            pages
        } else {
            pages.filter {
                user.limits.getUnderstandingLevel(
                    (it.propertyValues.find { p -> p.name == CORRECT_PAGE_KEY }?.value as Long).toInt()
                ) in understandingSet
            }
        }

        if (filteredPages.isEmpty()) {
            return null
        }

        val currentTime = Date().time
        val selectedPage = filteredPages[Random(currentTime.toInt()).nextInt(filteredPages.size)]

        return FlashcardFactory.buildCard(selectedPage, user)
    }
}