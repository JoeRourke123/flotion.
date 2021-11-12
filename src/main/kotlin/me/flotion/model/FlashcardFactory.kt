package me.flotion.model

import me.flotion.config.CORRECT_PAGE_KEY
import me.flotion.config.MODULE_SELECT_KEY
import me.flotion.config.ResponseMessages
import me.flotion.context.NotionContext
import me.flotion.exceptions.MalformedCardException
import me.flotion.exceptions.UnauthorisedUserException
import org.jraf.klibnotion.model.page.Page
import org.jraf.klibnotion.model.property.SelectOption
import org.jraf.klibnotion.model.property.value.MultiSelectPropertyValue
import org.jraf.klibnotion.model.richtext.RichTextList

class FlashcardFactory {
	companion object {
		suspend fun buildCard(id: String, user: NotionUser): Flashcard {
			val notionID = NotionID(id)
			val page: Page = notionID.getPage(user)

			return buildCard(page, user)
		}

		suspend fun buildCardWithoutContents(id: String, user: NotionUser): Flashcard {
			val notionID = NotionID(id)
			val page: Page = notionID.getPage(user)

			return buildCardWithoutContents(page, user)
		}

		suspend fun buildCard(page: Page, user: NotionUser) : Flashcard {
			val notionID = NotionID(page.id)
			val pageContents: String = notionID.getContents(user)

			return buildCardWithContents(page, pageContents, user)
		}

		fun buildCardWithoutContents(page: Page, user: NotionUser): Flashcard = buildCardWithContents(page, "", user)

		private fun buildCardWithContents(page: Page, contents: String, user: NotionUser): Flashcard {
			val notionID = NotionID(page.id)

			val pageProperty = (page.propertyValues.find { it.name == "Name" }?.value ?: throw MalformedCardException(ResponseMessages.MALFORMED_CARD.message)) as RichTextList
			val pageName = pageProperty.plainText ?: "No name"

			val corrects: Int = ((page.propertyValues.find { it.name == CORRECT_PAGE_KEY }?.value ?: 0) as Long).toInt()

			val moduleProperty = (page.propertyValues.find { it.name == MODULE_SELECT_KEY }?.value ?: throw MalformedCardException(ResponseMessages.MALFORMED_CARD.message)) as ArrayList<SelectOption>
			val modules = moduleProperty.map { it.name }

			return Flashcard(
				notionID,
				pageName,
				user,
				contents,
				modules,
				corrects,
				page
			)
		}
	}
}
