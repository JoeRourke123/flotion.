package me.flotion.context

import com.expediagroup.graphql.server.spring.execution.SpringGraphQLContext
import me.flotion.model.NotionUser
import org.springframework.http.HttpCookie
import org.springframework.http.server.ServerHttpRequest
import org.springframework.http.server.ServerHttpResponse
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse

data class NotionContext(val req: ServerRequest, val user: NotionUser?) : SpringGraphQLContext(req)
