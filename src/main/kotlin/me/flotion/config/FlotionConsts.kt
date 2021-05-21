package me.flotion.config

const val YELLOW_LIMIT = 3
const val GREEN_LIMIT = 5

const val YELLOW_LIM_KEY = "yellow_limit"
const val GREEN_LIM_KEY = "green_limit"
const val CARD_DB_KEY = "cards_database"

const val CORRECT_PAGE_KEY = "Correct"
const val CARD_DATABASE_TITLE = "Flashcards"
const val MODULE_SELECT_KEY = "Module(s)"
const val UNDERSTANDING_SELECT_KEY = "Understanding"

val FLOTION_SECRET = System.getenv("flotion_client_secret")

enum class ResponseMessages(val message: String) {
	EXPIRED_TOKEN("Your token has expired, please try re-authorising"),
	AUTH_ERROR("Uh oh! We were unable to authorise you, please try again."),
	PERSONAL_WORKSPACE_ONLY("You can only use Flotion on a personal workspace - try authorising on a different workspace"),
	INVALID_LIMITS("Oops - make sure the yellow limit is less than the green limit!"),
	NOT_LOGGED_IN("This is awkward - you don't appear to be logged in."),
	MALFORMED_CARD("Hmm - this card doesn't seem to match the format we were expecting! Maybe try recopying the template?"),
	MISSING_CARD("We can't seem to find the card you specified - has it been deleted?"),
	CARD_DB_NOT_FOUND("Your flashcards database could not be found - did you tick the Flotion page when authorising?"),
	NO_CARDS_HERE("We can't find the cards you're looking for! Try tweaking your parameters."),
	SERVER_ERROR("Um... Houston we have a (server) problem."),
	SUCCESS("OK")
}
