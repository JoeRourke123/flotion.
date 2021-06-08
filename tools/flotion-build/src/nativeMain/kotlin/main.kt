import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import kotlinx.cinterop.*
import platform.posix.*
import utils.buildCaddyConfig
import utils.buildSystemdConfig
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

	val tools by option("-t", "--tools", help="Rebuild flotion tools")
		.flag("-nt", "--no-tools", default=false)

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

	private val n = "> /dev/null"

	/**
	 * Runs the Clikt command main flow.
	 */
	override fun run() {
		if (checkEnvVars()) {
			return
		}

		val flotionDir = "$envHomeDir/flotion/"

		system("cd $flotionDir && git pull $n")
		println("----\n Latest changes pulled. \n")

		if(opendir(flotionDir) == null) {
			println("\n---- Error")
			println("Flotion source could not be found at \"$flotionDir\"")
			return
		}

		if (initialSetup) {
			runSystemdSetup()
			runCaddySetup()
		}

		if(tools) {
			buildTools()
		}

		if (backend) {
			buildBackend()
		}

		if (frontend) {
			buildFrontend()
		}

		if (caddy) {
			reloadCaddy("$envHomeDir/.config/caddy/flotion/Caddyfile")
		}
	}

	/**
	 * Checks that all the required fields are given by either options or environment variables.
	 * returns: true if any of the checks fail.
	 */
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

	/**
	 * Sets up the systemd service of the project
	 */
	private fun runSystemdSetup() {
		println("---")

		// Set up Systemd service
		val flotionDir = "$envHomeDir/flotion/"
		val configDir = "$envHomeDir/.config/systemd/user/"

		// Create the config directory if it doesn't exist
		if(opendir(configDir) == null) {
			mkdir("$envHomeDir/.config/systemd", S_IRWXU)
			mkdir(configDir, S_IRWXU)
			println("Making configuration directories...")
		}

		val fp = fopen("$configDir/flotion.service", "w")

		if(fp != null) {
			// Write the systemd unit service configuration to disk.
			fprintf(fp, buildSystemdConfig(clientID, clientSecret, redirectURI, flotionDir))
			fclose(fp)

			println("Configuration file written.")

			// Reload and restart the service.
			system("systemctl --user daemon-reload $n")
			system("systemctl --user enable --now flotion.service $n")

			println("----\n Systemd service set up complete. \n")
		} else {
			println("Couldn't set up flotion systemd service (service may not start or persist).")
		}
	}

	/**
	 * Runs initial setup of the Caddy configuration.
	 */
	private fun runCaddySetup() {
		// Setup Caddy configuration
		println("---")
		val caddyConfigDir = "$envHomeDir/.config/caddy/flotion"
		val caddyConfig = "$caddyConfigDir/Caddyfile"

		if(opendir(caddyConfigDir) == null) {
			mkdir("$envHomeDir/.config/caddy", S_IRWXU)
			mkdir(caddyConfigDir, S_IRWXU)
			println("Caddy configuration directories created...")
		}

		val fp = fopen(caddyConfig, "w")

		if(fp != null) {
			// Write the configuration to the Caddyfile
			fprintf(fp, buildCaddyConfig())
			fclose(fp)

			println("New flotion Caddyfile written.")

			reloadCaddy(caddyConfig)

			println("Caddy initialised. \n")
		} else {
			println("Couldn't set up flotion Caddy service (reverse proxy/domain name may not be available).\n")
		}

	}

	/**
	 * Simply reloads the Caddy instance with the specified config file.
	 */
	private fun reloadCaddy(configFile: String) {
		system("caddy reload --config $configFile $n")
	}

	/**
	 * Runs commands to rebuild the Kotlin Spring backend and restart the systemd service.
	 */
	private fun buildBackend() {
		val flotionDir = "$envHomeDir/flotion/"

		println("Building backend project... this may take a few minutes.")
		system("cd $flotionDir && ./gradlew build $n")

		println("Backend build completed. Restarting service.\n")
		system("systemctl --user restart flotion.service $n")

		println("---")
		println("Backend initialised")
	}

	/**
	 * Runs commands to rebuild and restart the React instance.
	 */
	private fun buildFrontend() {
		val frontendDir = "$envHomeDir/flotion/src/react/flotion"

		println("Building React project, stick the kettle on...")
		system("cd $frontendDir && yarn build $n")

		if(initialSetup) {
			system("cd $frontendDir && pm2 start \"serve -s build\" $n && pm2 save $n")
		} else {
			system("pm2 restart $PM2_REACT_ID $n")
		}

		println("----")
		println("Frontend deployed!")
	}

	/**
	 * Rebuilds the flotion tools.
	 */
	private fun buildTools() {
		val toolsDir = "$envHomeDir/flotion/tools/"
		val toolsPaths = listOf("$toolsDir/flotion-build/build/bin/native/releaseExecutable")

		for (tool in listOf("flotion-build")) {
			system("cd $toolsDir$tool && ./gradlew nativeBinaries $n")
		}

		val path = getenv("PATH")!!.toKString()
		val splitPath = path.split(":")

		val fp = fopen("$envHomeDir/.bashrc", "a")

		for(tool in toolsPaths) {
			if (!splitPath.contains(tool)) {
				fprintf(fp, "export PATH=\$PATH:$tool\n")
			}
		}

		fclose(fp)
		system("source $envHomeDir/.bashrc $n")

		println("----")
		println("Tools built!")
	}
}

fun main(args: Array<String>) = FlotionBuild().main(args)
