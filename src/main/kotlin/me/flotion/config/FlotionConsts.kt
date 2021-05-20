package me.flotion.config

const val YELLOW_LIMIT = 3
const val GREEN_LIMIT = 5

const val YELLOW_LIM_KEY = "yellow_limit"
const val GREEN_LIM_KEY = "green_limit"

const val CORRECT_PAGE_KEY = "Correct"

val FLOTION_SECRET = System.getenv("flotion_client_secret")

enum class ResponseMessages(val message: String) {
	EXPIRED_TOKEN("Your token has expired, please try re-authorising"),
	AUTH_ERROR("Uh oh! We were unable to authorise you, please try again."),
	PERSONAL_WORKSPACE_ONLY("You can only use Flotion on a personal workspace - try authorising on a different workspace"),
	INVALID_LIMITS("Oops - make sure the yellow limit is less than the green limit!"),
	NOT_LOGGED_IN("This is awkward - you don't appear to be logged in."),
	MALFORMED_CARD("Hmm - this card doesn't seem to match the format we were expecting! Maybe try recopying the template?"),
	MISSING_CARD("We can't seem to find the card you specified - has it been deleted?"),
	SUCCESS("OK")
}
