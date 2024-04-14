package de.hbch.traewelling.shared

import androidx.lifecycle.ViewModel
import de.hbch.traewelling.api.TraewellingApi
import de.hbch.traewelling.api.models.user.User

class BottomSearchViewModel : ViewModel() {
    suspend fun searchUsers(query: String): List<User> {
        return try {
            TraewellingApi.userService.searchUsers(query).data
        } catch (_: Exception) {
            listOf()
        }
    }
}
