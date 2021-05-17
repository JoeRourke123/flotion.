package me.flotion.models

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
