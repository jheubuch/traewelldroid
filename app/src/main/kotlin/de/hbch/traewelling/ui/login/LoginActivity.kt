package de.hbch.traewelling.ui.login

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.MutableLiveData
import com.jcloquell.androidsecurestorage.SecureStorage
import de.hbch.traewelling.BuildConfig
import de.hbch.traewelling.R
import de.hbch.traewelling.api.TraewellingApi
import de.hbch.traewelling.api.getGson
import de.hbch.traewelling.api.models.webhook.WebhookCreateResponse
import de.hbch.traewelling.api.models.webhook.WebhookUserCreateRequest
import de.hbch.traewelling.shared.SharedValues
import de.hbch.traewelling.theme.MainTheme
import de.hbch.traewelling.ui.info.InfoActivity
import de.hbch.traewelling.ui.main.MainActivity
import net.openid.appauth.AuthorizationRequest
import net.openid.appauth.AuthorizationResponse
import net.openid.appauth.AuthorizationService
import net.openid.appauth.ResponseTypeValues
import java.security.MessageDigest
import java.security.SecureRandom

class LoginActivity : ComponentActivity() {

    private lateinit var secureStorage: SecureStorage
    private lateinit var authorizationLauncher: ActivityResultLauncher<Intent>
    private lateinit var authorizationService : AuthorizationService
    private lateinit var authIntent: Intent
    private var notificationsEnabled: Boolean = false
    private val isLoading = MutableLiveData(false)
    private val loginViewModel: LoginViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        secureStorage = SecureStorage(this)
        initAuthInitial()

        setContent {
            MainTheme {
                LoginScreen(
                    loginAction = {
                        notificationsEnabled = it
                        initiateOAuthPKCELogin()
                    },
                    informationAction = { showInfoActivity() },
                    loadingData = isLoading
                )
            }
        }
    }

    private fun initAuthInitial() {
        authorizationService = AuthorizationService(application)

        authorizationLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()){
                result ->
            run {
                if (result.resultCode == RESULT_OK) {
                    handleAuthorizationResponse(result.data!!)
                }
            }
        }
    }

    private fun initAuthRequest() {
        val secureRandom = SecureRandom()
        val bytes = ByteArray(64)
        secureRandom.nextBytes(bytes)

        val encoding = Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP
        val codeVerifier = Base64.encodeToString(bytes, encoding)

        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(codeVerifier.toByteArray())
        val codeChallenge = Base64.encodeToString(hash, encoding)

        val builder = AuthorizationRequest.Builder(
            SharedValues.AUTH_SERVICE_CONFIG,
            BuildConfig.OAUTH_CLIENT_ID,
            ResponseTypeValues.CODE,
            Uri.parse(BuildConfig.OAUTH_REDIRECT_URL)
        )
        builder
            .setCodeVerifier(
                codeVerifier,
                codeChallenge,
                "S256"
            )
            .setScopes(SharedValues.AUTH_SCOPES)
            .setPrompt("consent")
        if (notificationsEnabled) {
            builder.setAdditionalParameters(
                mapOf(
                    Pair("trwl_webhook_url", "${BuildConfig.WEBHOOK_URL}/webhook"),
                    Pair("trwl_webhook_events", "notification")
                )
            )
        }

        val request = builder.build()
        authIntent = authorizationService.getAuthorizationRequestIntent(request)
    }

    private fun initiateOAuthPKCELogin() {
        initAuthRequest()
        try {
            authorizationLauncher.launch(authIntent)
        } catch (exception: ActivityNotFoundException) {
            val alertDialog = AlertDialog.Builder(this).create()
            alertDialog.setTitle(getString(R.string.no_browser_title))
            alertDialog.setMessage(getString(R.string.no_browser_description))
            alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.ok)) { _, _ ->
                alertDialog.dismiss()
            }
            alertDialog.show()
        }
    }

    private fun handleAuthorizationResponse(intent: Intent) {
        isLoading.postValue(true)
        val authorizationResponse: AuthorizationResponse? = AuthorizationResponse.fromIntent(intent)

        if (authorizationResponse != null) {
            val tokenExchangeRequest = authorizationResponse.createTokenExchangeRequest()
            authorizationService.performTokenRequest(tokenExchangeRequest) { response, _ ->
                if (response?.accessToken != null) {
                    secureStorage.storeObject(SharedValues.SS_JWT, response.accessToken!!)
                    secureStorage.storeObject(SharedValues.SS_REFRESH_TOKEN, response.refreshToken ?: "")
                    secureStorage.storeObject(SharedValues.SS_NOTIFICATIONS_ENABLED, notificationsEnabled)
                    TraewellingApi.jwt = response.accessToken!!
                    if (notificationsEnabled) {
                        val webhookResponse = getGson().fromJson(
                            response.additionalParameters["webhook"],
                            WebhookCreateResponse::class.java
                        )
                        handleWebhookResponse(webhookResponse)
                    } else {
                        redirectToMainActivity()
                    }
                } else {
                    isLoading.postValue(false)
                }
            }
        }
    }

    private fun redirectToMainActivity() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    private fun handleWebhookResponse(webhook: WebhookCreateResponse) {
        val endpoint = secureStorage.getObject(SharedValues.SS_UP_ENDPOINT, String::class.java)
        if (endpoint != null) {
            val webhookUser = WebhookUserCreateRequest(
                webhook.id,
                webhook.secret,
                endpoint
            )
            loginViewModel.createWebhookUser(
                webhookUser,
                { id ->
                    secureStorage.storeObject(SharedValues.SS_WEBHOOK_USER_ID, id)
                    secureStorage.storeObject(SharedValues.SS_TRWL_WEBHOOK_ID, webhookUser.webhookId)
                    redirectToMainActivity()
                },
                {
                    redirectToMainActivity()
                }
            )
        }
    }

    private fun showInfoActivity() {
        startActivity(Intent(this, InfoActivity::class.java))
    }
}
