import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.MissingOption
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option

import platform.posix.*
import kotlinx.cinterop.*

class FlotionBuild : CliktCommand() {
	val envClientID: String? = getenv("flotion_client_id")?.toKString()
	val envClientSecret: String? = getenv("flotion_client_secret")?.toKString()
	val envRedirectURI: String? = getenv("flotion_redirect_uri")?.toKString()

	val frontend by option("-f", "--frontend", help="Don't build the frontend.")
		.flag("-nf", "--no-frontend", default=true)
	val backend by option("-b", "--backend", help="Don't build the backend")
		.flag("-nb", "--no-backend", default=true)

	val caddy by option("-c", "--caddy", help="Restart the caddy instance")
		.flag("-nc", "--no-caddy", default=false)

	val initialSetup by option("-s", "--setup", help="Run initial setup of Caddy/Systemd/Builds")
		.flag("-ns", "--no-setup", default=false)

	val clientID by option("--id", help="Set the Notion API client ID environment variable").default(envClientID ?: "")
	val clientSecret by option("--secret", help="Set the Notion API client secret environment variable").default(envClientSecret ?: "")
	val redirectURI by option("--redirect", help="Set the Notion API redirect URL environment variable").default(envRedirectURI ?: "")

	override fun run() {
		checkEnvVars()
		setEnvVars()

		if(initialSetup) {

		}

		if(backend) {

		}

		if(caddy) {

		}

		if(frontend) {

		}
	}

	private fun checkEnvVars() {
		val hasID = envClientID != null || clientID.isNotEmpty()
		val hasSecret = envClientSecret != null || clientSecret.isNotEmpty()
		val hasURI = envRedirectURI != null || redirectURI.isNotEmpty()

		if(!hasID) {
			println("You must use the --id parameter to set a Notion API client ID environment variable!")
			throw MissingOption(option("--id"))
		}
		if(!hasSecret) {
			println("You must use the --secret parameter to set a Notion API client secret environment variable!")
			throw MissingOption(option("--secret"))
		}
		if(!hasURI) {
			println("You must use the --redirect parameter to set a Notion API redirect URL environment variable!")
			throw MissingOption(option("--redirect"))
		}
	}

	private fun setEnvVars() {
		
	}
}

fun main(args: Array<String>) = FlotionBuild().parse(args)
