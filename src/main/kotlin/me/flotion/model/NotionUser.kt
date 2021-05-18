package me.flotion.model

import org.jraf.klibnotion.model.user.Person

data class NotionClientDetails(
	val firstName: String,
	val limits: UnderstandingLimits
)

data class NotionUser private constructor(
	val accessToken: String,
	val email: String,
	val firstName: String,

	val limits: UnderstandingLimits,
	val excludedModules: ExcludedModules
) {
	companion object {
		fun fromPerson(token: String, person: Person): NotionUser {
			val modules = ExcludedModules.loadFromDB(token)
			val lims = UnderstandingLimits.loadFromDB(token)

			return NotionUser(
				token,
				person.email,
				person.name,
				lims,
				modules
			)
		}
	}

	val clientDetails get() = NotionClientDetails(firstName, limits)
}
