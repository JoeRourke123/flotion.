package me.flotion.model

import me.flotion.config.CORRECT_PAGE_KEY
import me.flotion.config.NotionSingleton
import me.flotion.config.UNDERSTANDING_SELECT_KEY
import org.jraf.klibnotion.model.page.Page
import org.jraf.klibnotion.model.property.value.PropertyValueList
import toFormattedString
import java.lang.Exception

data class NotionID(val id: String) {
	/**
	 * Returns the ID in a URL format for easy access to pages.
	 */
	val url: String = "https://notion.so/$id"

	suspend fun getPage(user: NotionUser): Page {
		val client = NotionSingleton.userClient(user.accessToken)

		return client.pages.getPage(id)
	}

	suspend fun getContents(user: NotionUser): String {
		val client = NotionSingleton.userClient(user.accessToken)

		val blocks = client.blocks.getAllBlockListRecursively(id)

		return blocks.toFormattedString()
	}
}

enum class Understanding {
	RED, YELLOW, GREEN
}

class Flashcard(
	private val cardID: NotionID,
	private val question: String,
	private val user: NotionUser,
	private val answer: String,
	private val modules: List<String>,
	private var correct: Int,
	page: Page
) : Page by page {
	data class FlashcardDetails(val id: String, val question: String, val answer: String, val modules: List<String>, val understanding: Understanding)

	val cardDetails: FlashcardDetails
		get() = FlashcardDetails(cardID.id, question, answer, modules, understanding)

	private val understanding: Understanding
		get() {
			return user.limits.getUnderstandingLevel(correct)
		}

	suspend fun incrementCorrect() {
		val token = user.accessToken
		val client = NotionSingleton.userClient(token)

		client.pages.updatePage(cardID.id, PropertyValueList().number(CORRECT_PAGE_KEY, ++correct))

		val newUnderstanding = user.limits.getUnderstandingLevel(correct)

		if (newUnderstanding != understanding) {
			client.pages.updatePage(cardID.id, PropertyValueList().selectByName(
				UNDERSTANDING_SELECT_KEY, newUnderstanding.name.capitalize()
			))
		}
	}
}
