package me.flotion.services

import me.flotion.config.NotionSingleton
import me.flotion.exceptions.PersonalWorkspaceOnlyException
import me.flotion.model.NotionUser
import me.flotion.model.NotionUserFactory
import org.jraf.klibnotion.model.exceptions.NotionClientRequestException
import org.jraf.klibnotion.model.user.Person
import org.springframework.stereotype.Component
import kotlin.random.Random

@Component
class AuthenticationService {
    /**
     * Produces a URL the frontend can redirect to for authorisation
     */
    fun generateAuthenticationLink(): String = NotionSingleton
        .client.oAuth
        .getUserPromptUri(
            oAuthCredentials = NotionSingleton.oAuthCredentials,
            uniqueState = Random.nextLong().toString()
        )

    /**
     * Takes an authorisation code from Notion and returns the NotionUser for the workspace
     * @throws PersonalWorkspaceOnlyException if the registered workspace has more than one user in it
     * @throws NotionClientRequestException if code has expired
     */
    suspend fun authoriseUser(code: String): NotionUser {
        val token = NotionSingleton.client.oAuth.getAccessToken(
                oAuthCredentials = NotionSingleton.oAuthCredentials,
                code = code
            ).accessToken

        val userClient = NotionSingleton.userClient(token)

        val workspaceUsers = userClient.users.getUserList().results.filter { it as? Person != null }

        if (!NotionUserFactory.userExists(token) && workspaceUsers.size != 1) {
            throw PersonalWorkspaceOnlyException()
        } else {
            return NotionUserFactory.fromPerson(token, workspaceUsers[0] as Person)
        }
    }
}