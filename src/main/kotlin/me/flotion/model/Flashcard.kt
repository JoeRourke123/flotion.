package me.flotion.model

data class NotionID (val id: String) {
	/**
	 * Returns the ID in a URL format for easy access to pages.
	 */
	fun getURL() = "https://notion.so/$id"
}

data class Flashcard(
	val id: NotionID,
	val correct: Int,
	val topic: String,
	val coverURL: String,

	val question: String,
	val content: String
) {
	/**
	 * Increments the correct property on the Notion database.
	 */
	fun incrementCorrect() {

	}

	/**
	 * Updates the cover of the card on the Notion database.
	 */
	private fun updateCover() {

	}
}
