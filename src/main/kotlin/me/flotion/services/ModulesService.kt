package me.flotion.services

import me.flotion.config.MODULE_SELECT_KEY
import me.flotion.model.ExcludedModules
import me.flotion.model.NotionUser
import org.jraf.klibnotion.model.database.query.DatabaseQuery
import org.jraf.klibnotion.model.database.query.filter.DatabaseQueryPredicate
import org.jraf.klibnotion.model.database.query.filter.DatabaseQueryPropertyFilter
import org.springframework.stereotype.Component

@Component
class ModulesService {
    fun buildModuleFilter(modules: List<String>, doesContain: Boolean = true) = DatabaseQuery().any(
        *modules.map {
            DatabaseQueryPropertyFilter.MultiSelect(
                MODULE_SELECT_KEY,
                if(doesContain) {
                    DatabaseQueryPredicate.MultiSelect.Contains(it)
                } else {
                    DatabaseQueryPredicate.MultiSelect.DoesNotContain(it)
                }
            )
        }.toTypedArray()
    )

    fun setExcludedModules(modules: List<String>, user: NotionUser): List<String> {
        val newModules = ExcludedModules(ArrayList(modules))
        newModules.saveToDB(user.accessToken)

        return newModules
    }
}