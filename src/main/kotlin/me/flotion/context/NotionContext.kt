package me.flotion.context

import com.expediagroup.graphql.server.spring.execution.SpringGraphQLContext
import me.flotion.model.NotionUser
import org.springframework.web.reactive.function.server.ServerRequest

class NotionContext(
	request: ServerRequest,
	val user: NotionUser
) : SpringGraphQLContext(request)
