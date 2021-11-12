package me.flotion.query

import com.expediagroup.graphql.generator.annotations.GraphQLDescription
import com.expediagroup.graphql.server.operations.Query
import me.flotion.config.ResponseMessages
import me.flotion.context.NotionContext
import me.flotion.model.NotionUser
import me.flotion.responses.ResponseObjects
import org.springframework.stereotype.Component

@Component
class ModulesQuery : Query {

    private suspend fun getModulesAndColours(
        modules: List<String>,
        context: NotionContext
    ): ResponseObjects.ModulesResponse {
        val modulesProperty = context.user?.getModuleProperty() ?: return ResponseObjects.ModulesResponse(
            400,
            ResponseMessages.MALFORMED_CARD.message
        )

        val excludedSet = setOf(*modules.toTypedArray())

        val colours =
            modulesProperty.options.asSequence().filter { it.name in excludedSet }.map { it.color.name }.toList()

        return ResponseObjects.ModulesResponse(modules = modules, colours = colours)
    }

    private suspend fun buildModulesResponse(
        context: NotionContext,
        modulesFetcher: suspend NotionUser.() -> List<String>
    ) = try {
        if (context.user == null) ResponseObjects.ModulesResponse(
            401,
            ResponseMessages.NOT_LOGGED_IN.message
        ) else getModulesAndColours(modulesFetcher(context.user), context)
    } catch (e: Exception) {
        ResponseObjects.ModulesResponse(500, ResponseMessages.SERVER_ERROR.message)
    }

    @GraphQLDescription("retrieves the modules a user has chosen to not show in the application")
    suspend fun getExcludedModules(context: NotionContext): ResponseObjects.ModulesResponse =
        buildModulesResponse(context, NotionUser::getExcludedModules)


    @GraphQLDescription("gets all the user's modules - except those which are excluded")
    suspend fun getModules(context: NotionContext): ResponseObjects.ModulesResponse =
        buildModulesResponse(context, NotionUser::getModules)

    @GraphQLDescription("gets all the user's modules - even those which are excluded")
    suspend fun allModules(context: NotionContext): ResponseObjects.ModulesResponse =
        buildModulesResponse(context, NotionUser::getAllModules)
}
