package me.flotion.config

import org.jraf.klibnotion.client.*
import org.jraf.klibnotion.model.oauth.OAuthCredentials
import org.yaml.snakeyaml.error.MissingEnvironmentVariableException

object NotionSingleton {
	private val authentication = Authentication()

	val oAuthCredentials: OAuthCredentials by lazy {
		OAuthCredentials(
			clientId = System.getenv("flotion_client_id")
				?: throw MissingEnvironmentVariableException("No client ID env. var set"),
			clientSecret = System.getenv("flotion_client_secret")
				?: throw MissingEnvironmentVariableException("No client secret env. var set"),
			redirectUri = System.getenv("flotion_redirect_uri")
				?: "http://localhost:5000/auth"    // Gets redirect URL from env. var if production, but uses localhost otherwise
		)
	}

	val client: NotionClient by lazy {
		buildNotionClient()
	}

	private fun buildNotionClient(auth: Authentication= authentication) =
		NotionClient.newInstance(
			ClientConfiguration(
				auth,
				HttpConfiguration(
					// Uncomment to see more logs
					// loggingLevel = HttpLoggingLevel.BODY,
					loggingLevel = HttpLoggingLevel.INFO,
					// This is only needed to debug with, e.g., Charles Proxy
					bypassSslChecks = true
				)
			)
		)

	fun userClient(token: String) = buildNotionClient(Authentication(token))

	suspend fun getUserCardDB(token: String): String? {
		val client: NotionClient = NotionSingleton.userClient(token)

		val databases = client.search.searchDatabases().results

		print(databases)

		val cardDatabase = databases.find { it.title.plainText == CARD_DATABASE_TITLE }

		return cardDatabase?.id
	}
}
