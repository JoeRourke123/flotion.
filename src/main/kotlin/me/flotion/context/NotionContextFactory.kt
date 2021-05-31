package me.flotion.context

import com.expediagroup.graphql.server.spring.execution.SpringGraphQLContextFactory
import io.jsonwebtoken.JwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import me.flotion.config.FLOTION_SECRET
import me.flotion.exceptions.UnvalidatedUserException
import me.flotion.model.NotionUser
import me.flotion.model.NotionUserFactory
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest

@Component
class NotionContextFactory : SpringGraphQLContextFactory<NotionContext>() {

	override suspend fun generateContext(request: ServerRequest): NotionContext {
		var user: NotionUser? = null

		try {
			val authToken = request.headers().firstHeader("Authorization")

			if (authToken != null) {
				val key = Keys.hmacShaKeyFor(FLOTION_SECRET.toByteArray())
				val accessToken = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(authToken).body.subject

				user = NotionUserFactory.fromToken(accessToken)
			}
		} catch(exc: JwtException) {
			println(exc.message)
		} catch(exc: UnvalidatedUserException) {
			println(exc.message)
		}

		return NotionContext(
			request,
			user
		)
	}
}
