package me.flotion.services

import me.flotion.model.NotionUser
import me.flotion.model.UnderstandingLimits
import org.springframework.stereotype.Component

@Component
class UserSettingsService {
    fun setUnderstandingLimits(yellow: Int?, green: Int?, user: NotionUser): UnderstandingLimits? {
        val newLimits = UnderstandingLimits(
            yellow ?: (user.limits.yellowLimit),
            green ?: (user.limits.yellowLimit)
        )

        val areLimitsValid = newLimits.isValid()

        if(areLimitsValid) {
            newLimits.saveToUser(user)
            return newLimits
        }

        return null
    }
}