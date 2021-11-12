package me.flotion.services

import me.flotion.config.MODULE_SELECT_KEY
import me.flotion.config.NotionSingleton
import me.flotion.model.FlashcardFactory
import me.flotion.model.NotionUser
import me.flotion.model.Understanding
import me.flotion.query.StatisticsQuery
import org.jraf.klibnotion.model.database.query.DatabaseQuery
import org.jraf.klibnotion.model.page.Page
import org.jraf.klibnotion.model.pagination.Pagination
import org.jraf.klibnotion.model.pagination.ResultPage
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class StatisticsService @Autowired constructor(private val modulesService: ModulesService) {
    fun getModuleUnderstandingCounts(allCards: List<Page>, user: NotionUser):
            Triple<Map<String, Int>, Map<String, Int>, Map<String, Int>> {
        val redModuleMapping = mutableMapOf<String, Int>()
        val yellowModuleMapping = mutableMapOf<String, Int>()
        val greenModuleMapping = mutableMapOf<String, Int>()

        for (page in allCards) {
            val card = FlashcardFactory.buildCardWithoutContents(page, user).cardDetails
            val moduleMapping = when(card.understanding) {
                Understanding.RED -> redModuleMapping
                Understanding.YELLOW -> yellowModuleMapping
                Understanding.GREEN -> greenModuleMapping
            }

            card.modules.forEach { moduleMapping[it] = moduleMapping.getOrDefault(it, 0) + 1 }
        }

        return Triple(redModuleMapping, yellowModuleMapping, greenModuleMapping)
    }

    suspend fun getFilteredStatsCards(hiddenModules: List<String>, user: NotionUser): List<Page> {
        // Build the filters to exclude from the stats
        val filters = modulesService.buildModuleFilter(hiddenModules, false)

        // Gets all the cards in the user's flashcard database
        var cardQueryResponse = buildCardQuery(user, filters)
        val allCards = ArrayList<Page>()
        allCards.addAll(cardQueryResponse.results)

        // Continues through the different pages also
        while (cardQueryResponse.nextPagination != null) {
            cardQueryResponse = buildCardQuery(user, filters, cardQueryResponse.nextPagination)
            allCards.addAll(cardQueryResponse.results)
        }

        return allCards
    }


    /**
     * Builds the query for fetching all cards from database, except those excluded in the filter list
     */
    private suspend fun buildCardQuery(
        user: NotionUser,
        filters: DatabaseQuery,
        pagination: Pagination? = null
    ): ResultPage<Page> = NotionSingleton.userClient(user.accessToken).databases.queryDatabase(
        user.databaseID, filters, pagination = pagination ?: Pagination()
    )

    fun buildResponseObject(
        redModuleMapping: Map<String, Int>,
        yellowModuleMapping: Map<String, Int>,
        greenModuleMapping: Map<String, Int>,
        modules: List<String>
    ): StatisticsQuery.StatsResponse {
        val totalReds = redModuleMapping.values.sum()
        val totalYellows = yellowModuleMapping.values.sum()
        val totalGreens = greenModuleMapping.values.sum()
        val overallCardCount = totalReds + totalYellows + totalGreens

        val cardLabel = "No. of cards"

        // Build the response data using the included modules and mappings
        return StatisticsQuery.StatsResponse(
            modules = modules,
            moduleRed = modules.map { StatisticsQuery.StatData(cardLabel, it, redModuleMapping[it] ?: 0) },
            moduleYellow = modules.map { StatisticsQuery.StatData(cardLabel, it, yellowModuleMapping[it] ?: 0) },
            moduleGreen = modules.map { StatisticsQuery.StatData(cardLabel, it, greenModuleMapping[it] ?: 0) },
            overall = StatisticsQuery.StatData(cardLabel, "Overall", overallCardCount),
            overallRed = totalReds,
            overallYellow = totalYellows,
            overallGreen = totalGreens,
            moduleCount = modules.size
        )
    }
}