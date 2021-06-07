import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.MissingOption
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import kotlinx.cinterop.*
import platform.posix.*
import utils.getHomeDirectory

class FlotionBuild : CliktCommand() {
	val envClientID: String? = getenv("flotion_client_id")?.toKString()
	val envClientSecret: String? = getenv("flotion_client_secret")?.toKString()
	val envRedirectURI: String? = getenv("flotion_redirect_uri")?.toKString()
	val envHomeDir: String? = getHomeDirectory()

	val frontend by option("-f", "--frontend", help = "Don't build the frontend.")
		.flag("-nf", "--no-frontend", default = true)
	val backend by option("-b", "--backend", help = "Don't build the backend")
		.flag("-nb", "--no-backend", default = true)

	val caddy by option("-c", "--caddy", help = "Restart the caddy instance")
		.flag("-nc", "--no-caddy", default = false)

	val initialSetup by option("-s", "--setup", help = "Run initial setup of Caddy/Systemd/Builds")
		.flag("-ns", "--no-setup", default = false)

	val clientID by option("--id", help = "Set the Notion API client ID environment variable").default(
		envClientID ?: ""
	)
	val clientSecret by option("--secret", help = "Set the Notion API client secret environment variable").default(
		envClientSecret ?: ""
	)
	val redirectURI by option("--redirect", help = "Set the Notion API redirect URL environment variable").default(
		envRedirectURI ?: ""
	)

	override fun run() {
		if (checkEnvVars()) {
			return
		}

		val flotionDir = "$envHomeDir/flotion/"

		system("cd $flotionDir && git pull")

		if(opendir(flotionDir) == null) {
			println("Flotion source could not be found at \"$flotionDir\"")
			return
		}

		if (initialSetup) {
			runSystemdSetup()
			runCaddySetup()
		}

		if (backend) {
			buildBackend()
		}

		if (frontend) {
			buildFrontend()
		}

		if (caddy) {
			reloadCaddy("$envHomeDir$CADDY_FILE_LOC")
		}
	}

	private fun checkEnvVars(): Boolean {
		val hasID = envClientID != null || clientID.isNotEmpty()
		val hasSecret = envClientSecret != null || clientSecret.isNotEmpty()
		val hasURI = envRedirectURI != null || redirectURI.isNotEmpty()

		if(envHomeDir == null) {
			println("Could not find the location of your flotion source...")
			return true
		}

		if (!hasID) {
			println("You must use the --id parameter to set a Notion API client ID environment variable!")
		}
		if (!hasSecret) {
			println("You must use the --secret parameter to set a Notion API client secret environment variable!")
		}
		if (!hasURI) {
			println("You must use the --redirect parameter to set a Notion API redirect URL environment variable!")
		}

		return !hasID || !hasSecret || !hasURI
	}

	private fun runSystemdSetup() {
		// Set up Systemd service
		val flotionDir = "$envHomeDir/flotion/"
		val configDir = "$envHomeDir/.config/systemd/user/"

		if(opendir(configDir) == null) {
			mkdir(configDir, S_IRWXU)
		}

		val fp = fopen("$configDir/flotion.service", "w")

		if(fp != null) {
			fprintf(fp, buildSystemdConfig(clientID, clientSecret, redirectURI, flotionDir))
			fclose(fp)

			system("systemctl --user daemon-reload")
			system("systemctl --user enable --now flotion.service")
			system("systemctl --user restart flotion.service")
		} else {
			println("Couldn't set up flotion systemd service (service may not start or persist).")
		}
	}

	private fun runCaddySetup() {
		// Setup Caddy configuration
		val caddyConfig = "$envHomeDir$CADDY_FILE_LOC"

		val fp = fopen(caddyConfig, "w")

		if(fp != null) {
			fprintf(fp, buildCaddyConfig())
			fclose(fp)

			reloadCaddy(caddyConfig)
		} else {
			println("Couldn't set up flotion Caddy service (reverse proxy/domain name may not be available).")
		}
	}

	private fun reloadCaddy(configFile: String) {
		system("caddy reload --config $configFile")
	}

	private fun buildBackend() {
		val flotionDir = "$envHomeDir/flotion/"

		system("cd $flotionDir && rm -r build")
		system("cd $flotionDir && ./gradlew buildRun")
		system("systemctl --user restart flotion.service")
	}

	private fun buildFrontend() {
		val frontendDir = "$envHomeDir/flotion/src/react/flotion"

		system("cd $frontendDir && yarn build")
		system("pm2 restart $PM2_REACT_ID")
	}

	private fun buildTools() {

	}
}

fun main(args: Array<String>) = FlotionBuild().parse(args)
