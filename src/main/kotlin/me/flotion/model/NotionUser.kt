package me.flotion.model

data class NotionUser constructor(
	val accessToken: String,
	val email: String,
	val firstName: String,

	val limits: UnderstandingLimits,
	val excludedModules: ExcludedModules
) {

	val clientDetails get() = NotionClientDetails(firstName, limits)
}

data class NotionClientDetails(
	val firstName: String,
	val limits: UnderstandingLimits
)
