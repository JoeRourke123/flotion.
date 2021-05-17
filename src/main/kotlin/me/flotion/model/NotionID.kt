package me.flotion.model

data class NotionID (val id: String) {
	/**
	 * Returns the ID in a URL format for easy access to pages.
	 */
	fun getURL() = "https://notion.so/$id"
}
