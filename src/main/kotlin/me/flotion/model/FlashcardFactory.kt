package me.flotion.model

import me.flotion.config.CORRECT_PAGE_KEY
import me.flotion.config.ResponseMessages
import me.flotion.context.NotionContext
import me.flotion.exceptions.MalformedCardException
import me.flotion.exceptions.UnauthorisedUserException
import org.jraf.klibnotion.model.page.Page
import org.jraf.klibnotion.model.richtext.RichTextList

class FlashcardFactory {
	companion object {
		suspend fun buildCard(id: String, context: NotionContext): Flashcard {
			val notionID = NotionID(id)
			val page: Page = notionID.getPage(
				context.user ?: throw UnauthorisedUserException(ResponseMessages.NOT_LOGGED_IN.message)
			)

			return buildCard(page, context)
		}

		suspend fun buildCard(page: Page, context: NotionContext) : Flashcard {
			val notionID = NotionID(page.id)
			val pageContents: String = notionID.getContents(context.user ?: throw UnauthorisedUserException(ResponseMessages.NOT_LOGGED_IN.message))

			val pageProperty = (page.propertyValues.find { it.name == "Name" }?.value ?: throw MalformedCardException(ResponseMessages.MALFORMED_CARD.message)) as RichTextList
			val pageName = pageProperty.plainText ?: "No name"

			val corrects: Int = ((page.propertyValues.find { it.name == CORRECT_PAGE_KEY }?.value ?: 0) as Long).toInt()

			return Flashcard(
				notionID,
				pageName,
				context.user,
				pageContents,
				corrects,
				page
			)
		}
	}
}
