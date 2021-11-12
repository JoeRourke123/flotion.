package me.flotion.mutation

import com.expediagroup.graphql.generator.annotations.GraphQLDescription
import com.expediagroup.graphql.server.operations.Mutation
import me.flotion.config.ResponseMessages
import me.flotion.context.NotionContext
import me.flotion.model.Flashcard
import me.flotion.services.FlashcardService
import org.jraf.klibnotion.model.exceptions.NotionClientException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class CorrectCardMutation @Autowired constructor(private val cardService: FlashcardService) : Mutation {
	class CorrectCardResponse(
		val response: Int = 200,
		val message: String = ResponseMessages.SUCCESS.message,
		val card: Flashcard.FlashcardDetails? = null
	)

	@GraphQLDescription("marks a specified card as having been answered correctly")
	suspend fun gotCorrect(card: String, context: NotionContext): CorrectCardResponse {
		return if (context.user != null) {
			try {
				val flashcard = cardService.incrementCardCorrectCount(card, context.user)

				CorrectCardResponse(card = flashcard.cardDetails)
			} catch (exc: NotionClientException) {
				CorrectCardResponse(404, ResponseMessages.MISSING_CARD.message)
			} catch(exc: Exception) {
				CorrectCardResponse(500, ResponseMessages.SERVER_ERROR.message)
			}
		} else CorrectCardResponse(401, ResponseMessages.NOT_LOGGED_IN.message)
	}
}
