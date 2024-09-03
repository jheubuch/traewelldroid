package de.hbch.traewelling.ui.followers

import android.util.Log
import androidx.lifecycle.ViewModel
import de.hbch.traewelling.api.TraewellingApi
import de.hbch.traewelling.api.models.user.User

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
            val response = TraewellingApi.userService.removeFollower(userId)
            response.isSuccessful
        } catch (ex: Exception) {
            Log.e("Error", ex.message ?: "")
            false
        }
    }

    suspend fun getFollowings(page: Int = 0): List<User> {
        val users = try {
            val response = TraewellingApi.userService.getFollowings(page)
            response.body()?.data ?: listOf()
        } catch (_: Exception) {
            listOf()
        }

        return users
    }

    suspend fun unfollowUser(userId: Int): Boolean {
        return try {
            val response = TraewellingApi.userService.removeFollowing(userId)
            response.isSuccessful
        } catch (ex: Exception) {
            Log.e("Error", ex.message ?: "")
            false
        }
    }

    suspend fun getFollowRequests(page: Int = 0): List<User> {
        return try {
            val response = TraewellingApi.userService.getFollowRequests(page)
            response.body()?.data ?: listOf()
        } catch (_: Exception) {
            listOf()
        }
    }

    suspend fun acceptFollowRequest(userId: Int): Boolean {
        return try {
            val response = TraewellingApi.userService.acceptFollowRequest(userId)
            response.isSuccessful
        } catch (ex: Exception) {
            Log.e("Error", ex.message ?: "")
            false
        }
    }

    suspend fun declineFollowRequest(userId: Int): Boolean {
        return try {
            val response = TraewellingApi.userService.declineFollowRequest(userId)
            response.isSuccessful
        } catch (ex: Exception) {
            Log.e("Error", ex.message ?: "")
            false
        }
    }
}
