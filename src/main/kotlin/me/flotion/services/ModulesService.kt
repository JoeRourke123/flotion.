package me.flotion.services

import me.flotion.model.ExcludedModules
import me.flotion.model.NotionUser
import org.springframework.stereotype.Component

@Component
class ModulesService {
    fun setExcludedModules(modules: List<String>, user: NotionUser): List<String> {
        val newModules = ExcludedModules(ArrayList(modules))
        newModules.saveToDB(user.accessToken)

        return newModules
    }
}