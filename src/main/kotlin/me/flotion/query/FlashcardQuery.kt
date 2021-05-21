package me.flotion.query

import com.expediagroup.graphql.generator.annotations.GraphQLDescription
import com.expediagroup.graphql.server.operations.Query
import me.flotion.config.MODULE_SELECT_KEY
import me.flotion.config.NotionSingleton
import me.flotion.config.ResponseMessages
import me.flotion.config.UNDERSTANDING_SELECT_KEY
import me.flotion.context.NotionContext
import me.flotion.exceptions.UnauthorisedUserException
import me.flotion.model.Flashcard
import me.flotion.model.FlashcardFactory
import me.flotion.model.Understanding
import org.jraf.klibnotion.model.database.query.DatabaseQuery
import org.jraf.klibnotion.model.database.query.filter.DatabaseQueryPredicate
import org.jraf.klibnotion.model.database.query.filter.DatabaseQueryPropertyFilter
import org.springframework.stereotype.Component
import java.util.*
import kotlin.random.Random

@Component
class FlashcardQuery : Query {
	class FlashcardResponse(val response: Int = 200, val message: String = ResponseMessages.SUCCESS.message, val card: Flashcard.FlashcardDetails? = null)

	@GraphQLDescription("returns a random flashcard from the user's card database")
	suspend fun randomCard(modules: List<String> = emptyList(), understanding: List<Understanding> = emptyList(), context: NotionContext): FlashcardResponse {
		return try {
			val client = NotionSingleton.userClient(context.user?.accessToken ?: throw UnauthorisedUserException(ResponseMessages.NOT_LOGGED_IN.message))
			val cardDB = context.user.databaseID

			val moduleFilters = modules.map { DatabaseQueryPropertyFilter.MultiSelect(
				MODULE_SELECT_KEY,
				DatabaseQueryPredicate.MultiSelect.Contains(it)
			) }

			val understandingFilters = understanding.map { DatabaseQueryPropertyFilter.Select(
				UNDERSTANDING_SELECT_KEY,
				DatabaseQueryPredicate.Select.Equals(it.name.capitalize())
			) }

			val pages = client.databases.queryDatabase(cardDB,
				DatabaseQuery().any(
					*moduleFilters.toTypedArray()
				).any(
					*understandingFilters.toTypedArray()
				)
			).results

			if(pages.isEmpty()) {
				return FlashcardResponse(204, ResponseMessages.NO_CARDS_HERE.message)
			}

			val currentTime = Date().time
			val selectedPage = pages[Random(currentTime.toInt()).nextInt(pages.size)]

			FlashcardResponse(card = FlashcardFactory.buildCard(selectedPage, context).cardDetails)
		} catch(exc: UnauthorisedUserException) {
			FlashcardResponse(401, ResponseMessages.NOT_LOGGED_IN.message)
//		} catch(exc: Exception) {
//			println(exc.message)
//			FlashcardResponse(500, ResponseMessages.SERVER_ERROR.message)
		}
	}
}
