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
		val token = context.user?.accessToken ?: "bad_token"

		val newLimits = UnderstandingLimits(
			yellow ?: (context.user?.limits?.yellowLimit ?: YELLOW_LIMIT),
			green ?: (context.user?.limits?.yellowLimit ?: GREEN_LIMIT)
		)

		return if (newLimits.isValid()) {
			newLimits.saveToUser(token)

			AlterLimitResponse(limits = newLimits)
		} else {
			AlterLimitResponse(400, ResponseMessages.INVALID_LIMITS.message, context.user?.limits)
		}
	}
}
