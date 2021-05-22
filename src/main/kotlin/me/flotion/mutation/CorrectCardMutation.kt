package me.flotion.mutation

import com.expediagroup.graphql.generator.annotations.GraphQLDescription
import com.expediagroup.graphql.server.operations.Mutation
import me.flotion.config.CORRECT_PAGE_KEY
import me.flotion.config.NotionSingleton
import me.flotion.config.ResponseMessages
import me.flotion.context.NotionContext
import me.flotion.model.Flashcard
import me.flotion.model.FlashcardFactory
import org.jraf.klibnotion.client.NotionClient
import org.jraf.klibnotion.model.property.value.PropertyValueList
import org.springframework.stereotype.Component

@Component
class CorrectCardMutation : Mutation {
	class CorrectCardResponse(
		val response: Int = 200,
		val message: String = ResponseMessages.SUCCESS.message,
		val card: Flashcard.FlashcardDetails? = null
	)

	@GraphQLDescription("marks a specified card as having been answered correctly")
	suspend fun gotCorrect(card: String, context: NotionContext): CorrectCardResponse {
		return if (context.user != null) {
			try {
				val flashcard = FlashcardFactory.buildCardWithoutContents(card, context)
				flashcard.incrementCorrect()

				CorrectCardResponse(card = flashcard.cardDetails)
			} catch (exc: Exception) {
				CorrectCardResponse(404, ResponseMessages.MISSING_CARD.message)
			}
		} else CorrectCardResponse(401, ResponseMessages.NOT_LOGGED_IN.message)
	}
}
