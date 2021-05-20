package me.flotion.mutation

import com.expediagroup.graphql.generator.annotations.GraphQLDescription
import com.expediagroup.graphql.server.operations.Query
import me.flotion.config.CORRECT_PAGE_KEY
import me.flotion.config.NotionSingleton
import me.flotion.config.ResponseMessages
import me.flotion.context.NotionContext
import org.jraf.klibnotion.client.NotionClient
import org.jraf.klibnotion.model.property.value.PropertyValueList
import org.springframework.stereotype.Component

@Component
class CorrectCardMutation : Query {
	class CorrectCardResponse(
		val response: Int = 200,
		val message: String = ResponseMessages.SUCCESS.message,
		val card: String? = null
	)

	@GraphQLDescription("marks a specified card as having been answered correctly")
	suspend fun gotCorrect(card: String, context: NotionContext): CorrectCardResponse {
		return if (context.user != null) {
			val userToken: String = context.user.accessToken
			val client: NotionClient = NotionSingleton.userClient(userToken)

			try {
				// Find the current correct value in the card's page.
				val currentCorrect: Int? =
					client.pages.getPage(card).propertyValues.find { it.name == CORRECT_PAGE_KEY }?.value as Int?

				if (currentCorrect == null) {
					CorrectCardResponse(400, ResponseMessages.MALFORMED_CARD.message)
				} else {
					client.pages.updatePage(card, PropertyValueList().number(CORRECT_PAGE_KEY, currentCorrect + 1))

					// Successful response.
					CorrectCardResponse(card = card)
				}
			} catch (exc: Exception) {
				CorrectCardResponse(404, ResponseMessages.MISSING_CARD.message)
			}
		} else CorrectCardResponse(401, ResponseMessages.NOT_LOGGED_IN.message)
	}
}
