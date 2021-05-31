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
		var overallRed: Int? = null, var overallYellow: Int? = null, var overallGreen: Int? = null,
		var moduleCount: Int? = null
	)

	/**
	 * Builds the query for fetching all cards from database, except those excluded in the filter list
	 */
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

		val hiddenSet = setOf(*hiddenModules.toTypedArray())

		val modules = context.user.getAllModules().filter { it !in hiddenSet }

		val redModuleMapping = HashMap<String, Int>()
		val yellowModuleMapping = HashMap<String, Int>()
		val greenModuleMapping = HashMap<String, Int>()

		// Build the filters to exclude from the stats
		val filters = hiddenModules.map {
			DatabaseQueryPropertyFilter.MultiSelect(
				MODULE_SELECT_KEY,
				DatabaseQueryPredicate.MultiSelect.DoesNotContain(it)
			)
		}.toTypedArray()

		// Gets all the cards in the user's flashcard database
		var cardQueryResponse = buildCardQuery(context.user, filters)
		val allCards = ArrayList<Page>()
		allCards.addAll(cardQueryResponse.results)

		// Continues through the different pages also
		while (cardQueryResponse.nextPagination != null) {
			cardQueryResponse = buildCardQuery(context.user, filters, cardQueryResponse.nextPagination)
			allCards.addAll(cardQueryResponse.results)
		}

		// Keeps count of all the cards - so unnecessary iterations aren't required.
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

		// Get the total number of different understanding levels from the mappings
		val totalReds = redModuleMapping.values.sum()
		val totalYellows = yellowModuleMapping.values.sum()
		val totalGreens = greenModuleMapping.values.sum()

		val CARD_LABEL = "No. of cards"

		// Build the response data using the included modules and mappings
		return StatsResponse(
			modules = modules,
			moduleRed = modules.map { StatData(CARD_LABEL, it, redModuleMapping[it] ?: 0) },
			moduleYellow = modules.map { StatData(CARD_LABEL, it, yellowModuleMapping[it] ?: 0) },
			moduleGreen = modules.map { StatData(CARD_LABEL, it, greenModuleMapping[it] ?: 0) },
			overall = StatData(CARD_LABEL, "Overall", overallCardCount),
			overallRed = totalReds,
			overallYellow = totalYellows,
			overallGreen = totalGreens,
			moduleCount = modules.size
		)
	}
}
