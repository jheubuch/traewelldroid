package de.hbch.traewelling.ui.followers

import android.util.Log
import androidx.lifecycle.ViewModel
import de.hbch.traewelling.api.TraewellingApi
import de.hbch.traewelling.api.models.user.User
import de.hbch.traewelling.api.models.user.UserId

class ManageFollowersViewModel: ViewModel() {
    suspend fun getFollowers(page: Int = 0): List<User> {
        val users = try {
            val response = TraewellingApi.userService.getFollowers(page)
            response.body()?.data ?: listOf()
        } catch (_: Exception) {
            listOf()
        }

        return users
    }

    suspend fun removeFollower(userId: Int): Boolean {
        return try {
            val response = TraewellingApi.userService.removeFollower(UserId(userId))
            response.isSuccessful
        } catch (ex: Exception) {
            Log.e("Error", ex.message ?: "")
            false
        }
    }
}
