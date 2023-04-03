package me.flotion.query

import com.expediagroup.graphql.generator.annotations.GraphQLDescription
import com.expediagroup.graphql.server.operations.Query
import me.flotion.config.*
import me.flotion.context.NotionContext
import me.flotion.exceptions.UnauthorisedUserException
import me.flotion.model.Flashcard
import me.flotion.model.FlashcardFactory
import me.flotion.model.Understanding
import me.flotion.responses.ResponseObjects
import me.flotion.services.FlashcardService
import me.flotion.services.ModulesService
import org.jraf.klibnotion.model.database.query.DatabaseQuery
import org.jraf.klibnotion.model.database.query.filter.DatabaseQueryPredicate
import org.jraf.klibnotion.model.database.query.filter.DatabaseQueryPropertyFilter
import org.jraf.klibnotion.model.page.Page
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.util.*
import kotlin.random.Random

@Component
class FlashcardQuery @Autowired constructor(
    private val modulesService: ModulesService,
    private val cardService: FlashcardService
) : Query {


    @GraphQLDescription("returns a random flashcard from the user's card database")
    suspend fun randomCard(
        modules: List<String> = emptyList(),
        understanding: List<Understanding> = emptyList(),
        context: NotionContext
    ): ResponseObjects.FlashcardResponse {
        return try {
            if(context.user == null) throw UnauthorisedUserException(ResponseMessages.NOT_LOGGED_IN.message)

            val moduleFilter = modulesService.buildModuleFilter(modules)
            val flashcard = cardService.getRandomFlashcard(moduleFilter, understanding, context.user)

            if(flashcard != null) {
                ResponseObjects.FlashcardResponse(card = flashcard.cardDetails)
            } else {
                ResponseObjects.FlashcardResponse(204, ResponseMessages.NO_CARDS_HERE.message)
            }
        } catch (exc: UnauthorisedUserException) {
            ResponseObjects.FlashcardResponse(401, ResponseMessages.NOT_LOGGED_IN.message)
        } catch (exc: Exception) {
            exc.printStackTrace()
            ResponseObjects.FlashcardResponse(500, ResponseMessages.SERVER_ERROR.message)
        }
    }
}
