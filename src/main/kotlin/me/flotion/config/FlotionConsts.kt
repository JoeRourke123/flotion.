package me.flotion.config

const val YELLOW_LIMIT = 3
const val GREEN_LIMIT = 5

const val YELLOW_LIM_KEY = "yellow_limit"
const val GREEN_LIM_KEY = "green_limit"

val FLOTION_SECRET = System.getenv("flotion_client_secret")

enum class ResponseMessages(val message: String) {
	EXPIRED_TOKEN("Your token has expired, please try re-authorising"),
	AUTH_ERROR("Uh oh! We were unable to authorise you, please try again."),
	PERSONAL_WORKSPACE_ONLY("You can only use Flotion on a personal workspace - try authorising on a different workspace"),
	SUCCESS("OK")
}
