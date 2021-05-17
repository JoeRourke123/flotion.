package me.flotion.model

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
		NotionClient.newInstance(
			ClientConfiguration(
				authentication,
				HttpConfiguration(
					// Uncomment to see more logs
					// loggingLevel = HttpLoggingLevel.BODY,
					loggingLevel = HttpLoggingLevel.INFO,
					// This is only needed to debug with, e.g., Charles Proxy
					httpProxy = if(System.getenv("flotion_redirect_uri") == null) HttpProxy("localhost", 8888) else null,
					// Can be useful in certain circumstances, but unwise to use in production
					bypassSslChecks = System.getenv("flotion_redirect_uri") == null
				)
			)
		)
	}
}
