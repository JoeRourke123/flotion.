package me.flotion.mutation

import com.expediagroup.graphql.generator.annotations.GraphQLDescription
import com.expediagroup.graphql.server.operations.Mutation
import me.flotion.config.*
import me.flotion.context.NotionContext
import me.flotion.model.UnderstandingLimits
import org.springframework.stereotype.Component

@Component
class LimitAlterMutation : Mutation {
	data class AlterLimitResponse(
		val response: Int = 200,
		val message: String = ResponseMessages.SUCCESS.message,
		val limits: UnderstandingLimits? = null
	)

	@GraphQLDescription("alters the current user's card colour limits")
	suspend fun alterLimits(yellow: Int?, green: Int?, context: NotionContext): AlterLimitResponse {
		if(context.user == null) return AlterLimitResponse(401, ResponseMessages.NOT_LOGGED_IN.message)

		try {
			val token = context.user.accessToken

			val newLimits = UnderstandingLimits(
				yellow ?: (context.user.limits.yellowLimit),
				green ?: (context.user.limits.yellowLimit)
			)

			return if (newLimits.isValid()) {
				newLimits.saveToUser(token)

				AlterLimitResponse(limits = newLimits)
			} else {
				AlterLimitResponse(400, ResponseMessages.INVALID_LIMITS.message, context.user.limits)
			}
		} catch(e: Exception) {
			return AlterLimitResponse(500, ResponseMessages.SERVER_ERROR.message)
		}
	}
}
