package me.flotion.mutation

import com.expediagroup.graphql.generator.annotations.GraphQLDescription
import com.expediagroup.graphql.server.operations.Mutation
import me.flotion.config.*
import me.flotion.context.NotionContext
import me.flotion.model.UnderstandingLimits
import me.flotion.services.UserSettingsService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class LimitAlterMutation @Autowired constructor(private val userService: UserSettingsService) : Mutation {
	data class AlterLimitResponse(
		val response: Int = 200,
		val message: String = ResponseMessages.SUCCESS.message,
		val limits: UnderstandingLimits? = null
	)

	@GraphQLDescription("alters the current user's card colour limits")
	suspend fun alterLimits(yellow: Int?, green: Int?, context: NotionContext): AlterLimitResponse {
		if(context.user == null) return AlterLimitResponse(401, ResponseMessages.NOT_LOGGED_IN.message)

		return try {
			val newLimits = userService.setUnderstandingLimits(yellow, green, context.user)

			if(newLimits != null) {
				AlterLimitResponse(limits = newLimits)
			} else {
				AlterLimitResponse(400, ResponseMessages.INVALID_LIMITS.message, context.user.limits)
			}
		} catch(e: Exception) {
			AlterLimitResponse(500, ResponseMessages.SERVER_ERROR.message)
		}
	}
}
