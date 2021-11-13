package me.flotion.mutation

import com.expediagroup.graphql.generator.annotations.GraphQLDescription
import com.expediagroup.graphql.server.operations.Mutation
import me.flotion.config.*
import me.flotion.context.NotionContext
import me.flotion.model.UnderstandingLimits
import me.flotion.responses.ResponseObjects
import me.flotion.services.UserSettingsService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class LimitAlterMutation @Autowired constructor(private val userService: UserSettingsService) : Mutation {

	@GraphQLDescription("alters the current user's card colour limits")
	suspend fun alterLimits(yellow: Int?, green: Int?, context: NotionContext): ResponseObjects.AlterLimitResponse {
		if(context.user == null) return ResponseObjects.AlterLimitResponse(401, ResponseMessages.NOT_LOGGED_IN.message)

		return try {
			val newLimits = userService.setUnderstandingLimits(yellow, green, context.user)

			if(newLimits != null) {
				ResponseObjects.AlterLimitResponse(limits = newLimits)
			} else {
				ResponseObjects.AlterLimitResponse(400, ResponseMessages.INVALID_LIMITS.message, context.user.limits)
			}
		} catch(e: Exception) {
			ResponseObjects.AlterLimitResponse(500, ResponseMessages.SERVER_ERROR.message)
		}
	}
}
