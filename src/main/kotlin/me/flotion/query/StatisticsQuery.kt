package me.flotion.query

import com.expediagroup.graphql.generator.annotations.GraphQLDescription
import com.expediagroup.graphql.server.operations.Query
import me.flotion.config.MODULE_SELECT_KEY
import me.flotion.config.NotionSingleton
import me.flotion.config.ResponseMessages
import me.flotion.config.UNDERSTANDING_SELECT_KEY
import me.flotion.context.NotionContext
import me.flotion.model.FlashcardFactory
import me.flotion.model.NotionUser
import me.flotion.model.Understanding
import org.jraf.klibnotion.model.database.query.DatabaseQuery
import org.jraf.klibnotion.model.database.query.filter.DatabaseQueryPredicate
import org.jraf.klibnotion.model.database.query.filter.DatabaseQueryPropertyFilter
import org.jraf.klibnotion.model.page.Page
import org.jraf.klibnotion.model.pagination.Pagination
import org.jraf.klibnotion.model.pagination.ResultPage
import org.jraf.klibnotion.model.property.sort.PropertySort
import org.springframework.stereotype.Component

@Component
class StatisticsQuery : Query {
	class StatData(val name: String, val module: String, val amount: Int)
	class StatsResponse(
		val response: Int = 200, val message: String = ResponseMessages.SUCCESS.message,
		var modules: List<String>? = null,
		var overall: StatData? = null,
		var moduleRed: List<StatData>? = null,
		var moduleYellow: List<StatData>? = null,
		var moduleGreen: List<StatData>? = null,
		var overallRed: Int? = null, var overallYellow: Int? = null, var overallGreen: Int? = null
	)

	private suspend fun buildCardQuery(
		user: NotionUser,
		filters: Array<DatabaseQueryPropertyFilter.MultiSelect>,
		pagination: Pagination? = null
	): ResultPage<Page> = NotionSingleton.userClient(user.accessToken).databases.queryDatabase(
		user.databaseID, DatabaseQuery().all(
			*filters
		), pagination = pagination ?: Pagination()
	)

	@GraphQLDescription("gets statistics on user's flashcards")
	suspend fun getStats(hiddenModules: List<String>, context: NotionContext): StatsResponse {
		if (context.user == null) return StatsResponse(401, ResponseMessages.NOT_LOGGED_IN.message)

		val modules = context.user.getAllModules()

		val redModuleMapping = HashMap<String, Int>()
		val yellowModuleMapping = HashMap<String, Int>()
		val greenModuleMapping = HashMap<String, Int>()

		val filters = hiddenModules.map {
			DatabaseQueryPropertyFilter.MultiSelect(
				MODULE_SELECT_KEY,
				DatabaseQueryPredicate.MultiSelect.DoesNotContain(it)
			)
		}.toTypedArray()

		var cardQueryResponse = buildCardQuery(context.user, filters)
		val allCards = ArrayList<Page>()

		while (cardQueryResponse.nextPagination != null) {
			allCards += cardQueryResponse.results
			cardQueryResponse = buildCardQuery(context.user, filters, cardQueryResponse.nextPagination)
		}

		var overallCardCount = 0

		for (page in allCards) {
			val card = FlashcardFactory.buildCardWithoutContents(page, context).cardDetails
			val moduleMapping = when(card.understanding) {
				Understanding.RED -> redModuleMapping
				Understanding.YELLOW -> yellowModuleMapping
				Understanding.GREEN -> greenModuleMapping
			}

			card.modules.forEach { moduleMapping[it] = moduleMapping.getOrDefault(it, 0) + 1 }
			overallCardCount++
		}

		val totalReds = redModuleMapping.values.sum()
		val totalYellows = yellowModuleMapping.values.sum()
		val totalGreens = greenModuleMapping.values.sum()

		val CARD_LABEL = "No. of cards"

		return StatsResponse(
			modules = modules,
			moduleRed = modules.map { StatData(CARD_LABEL, it, redModuleMapping[it] ?: 0) },
			moduleYellow = modules.map { StatData(CARD_LABEL, it, yellowModuleMapping[it] ?: 0) },
			moduleGreen = modules.map { StatData(CARD_LABEL, it, greenModuleMapping[it] ?: 0) },
			overall = StatData(CARD_LABEL, "Overall", overallCardCount),
			overallRed = totalReds,
			overallYellow = totalYellows,
			overallGreen = totalGreens
		)
	}
}
