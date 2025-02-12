package com.adr.opsc7312_poe

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.lifecycle.lifecycleScope
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.launch
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.security.MessageDigest
import java.util.UUID
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import android.view.View



class LoginPage : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login_page)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Setting up for the google sign-in
        val btnGoogleLogin = findViewById<Button>(R.id.btnGoogleLogin)
        btnGoogleLogin.setOnClickListener {
            val credentialManager = CredentialManager.create(this)
            val nonce = UUID.randomUUID().toString()
            val bytes = nonce.toByteArray()
            val md = MessageDigest.getInstance("SHA-256")
            val digest = md.digest(bytes)
            val hashNonce = digest.fold("") { str, it -> str + "%02x".format(it) }
            val googleIdOption: GetGoogleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(BuildConfig.CLIENT_ID_KEY)
                .setNonce(hashNonce)
                .build()

            val request: GetCredentialRequest = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            var email = ""
            lifecycleScope.launch {
                try {
                    // Fetching the credential
                    val result = credentialManager.getCredential(this@LoginPage, request)
                    val credential = result.credential

                    val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                    val googleIdToken = googleIdTokenCredential.idToken

                    // Verifying the token
                    val payload = verifyGoogleIdToken(googleIdToken)
                    // Extracting the email
                    payload?.let {
                        email = it.email
                        Log.d("TokenTest", "Email: $email")
                        LoginAndRegister(email)
                    } ?: run {
                        Log.e("TokenTest", "Invalid ID token.")
                    }

                } catch (e: Exception) {
                    Toast.makeText(this@LoginPage, "Google authentication canceled.", Toast.LENGTH_SHORT).show()
                    Log.e("TokenTest", "Error: ${e.message}", e)
                }
            }
        }

        // Setting up the fingerprint sign-in
        val btnFingerprintLogin = findViewById<Button>(R.id.btn_fingerprint_signin)
        btnFingerprintLogin.setOnClickListener {
            showBiometricPrompt()
        }

        // Checking if the devices support biometric authentication
        checkBiometricSupport(btnFingerprintLogin)
    }

    private fun showBiometricPrompt() {
        val biometricPrompt = BiometricPrompt(this, ContextCompat.getMainExecutor(this),
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    navigateToApp()
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    Toast.makeText(this@LoginPage, "Authentication error: $errString", Toast.LENGTH_SHORT).show()
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    Toast.makeText(this@LoginPage, "Authentication failed", Toast.LENGTH_SHORT).show()
                }
            })

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Sign in with Fingerprint")
            .setSubtitle("Use your fingerprint to access the app")
            .setNegativeButtonText("Cancel")
            .build()

        biometricPrompt.authenticate(promptInfo)
    }

    private fun checkBiometricSupport(fingerprintButton: Button) {
        val biometricManager = BiometricManager.from(this)
        if (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG) == BiometricManager.BIOMETRIC_SUCCESS) {
            fingerprintButton.visibility = View.VISIBLE
        } else {
            fingerprintButton.visibility = View.GONE
        }
    }

    // Google SSO user registration and login
    private fun LoginAndRegister(email: String) {
        lifecycleScope.launch {
            try {
                Log.d("CurrentEmail: ", email)
                val service = ServiceUser()
                service.CreateUser(email)
                val userID = service.validateUser(email) ?: 0
                if (!email.isNullOrEmpty()) {
                    saveUserIDToPreferences(userID)
                    val intent = Intent(this@LoginPage, MainActivity::class.java)
                    startActivity(intent)
                }
            } catch (e: Exception) {
                Log.e("LoginAndRegister", "Error: ${e.message}", e)
            }
        }
    }

    private suspend fun verifyGoogleIdToken(idToken: String?): GoogleIdToken.Payload? = withContext(Dispatchers.IO) {
        try {
            val transport = NetHttpTransport()
            val jsonFactory = GsonFactory.getDefaultInstance()
            val verifier = GoogleIdTokenVerifier.Builder(transport, jsonFactory)
                .setAudience(listOf(BuildConfig.CLIENT_ID_KEY))
                .build()

            // Verifying the ID token
            val verifiedIdToken: GoogleIdToken? = verifier.verify(idToken)
            verifiedIdToken?.payload
        } catch (e: Exception) {
            Log.e("TokenTest", "Token verification failed: ${e.message}")
            null
        }
    }

    private fun saveUserIDToPreferences(userID: Int) {
        val sharedPref = getSharedPreferences("user_prefs", MODE_PRIVATE)
        val editor = sharedPref.edit()
        editor.putInt("userid", userID)
        editor.apply()
    }

    private fun navigateToApp() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}