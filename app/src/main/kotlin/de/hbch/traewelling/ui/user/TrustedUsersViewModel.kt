package de.hbch.traewelling.ui.user

import androidx.lifecycle.ViewModel
import de.hbch.traewelling.api.TraewellingApi
import de.hbch.traewelling.api.models.user.CreateTrustedUser
import de.hbch.traewelling.api.models.user.TrustedUser
import java.time.ZonedDateTime

class TrustedUsersViewModel: ViewModel() {

    suspend fun getTrustedUsers(): List<TrustedUser>? {
        return try {
            val response = TraewellingApi.userService.getTrustedUsers()
            response.body()?.data
        } catch (_: Exception) {
            null
        }
    }

    suspend fun addTrustedUser(userId: Int, expiresAt: ZonedDateTime?): Boolean {
        return try {
            val response = TraewellingApi.userService.trustUser(CreateTrustedUser(userId, expiresAt))
            response.isSuccessful
        } catch (_: Exception) {
            false
        }
    }

    suspend fun removeTrustedUser(userId: Int): Boolean {
        return try {
            val response = TraewellingApi.userService.removeTrustedUser(userId)
            response.isSuccessful
        } catch (_: Exception) {
            false
        }
    }
}