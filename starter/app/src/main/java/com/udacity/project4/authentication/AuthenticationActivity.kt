package com.udacity.project4.authentication

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult
import com.google.firebase.auth.FirebaseAuth
import com.udacity.project4.R
import com.udacity.project4.databinding.ActivityAuthenticationBinding

/**
 * This class should be the starting point of the app, It asks the users to sign in / register, and redirects the
 * signed in users to the RemindersActivity.
 */
private const val TAG = "AuthenticationActivity"

class AuthenticationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAuthenticationBinding
    private val signInLauncher = registerForActivityResult(FirebaseAuthUIActivityResultContract()){
            result ->
        onSignInResult(result)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAuthenticationBinding.inflate(layoutInflater)
        setContentView(binding.root)

//         TODO: Implement the create account and sign in using FirebaseUI, use sign in using email and sign in using Google
        binding.loginButton.setOnClickListener { launchSignInFlow() }
//          TODO: If the user was authenticated, send him to RemindersActivity

//          TODO: a bonus is to customize the sign in flow to look nice using :
        //https://github.com/firebase/FirebaseUI-Android/blob/master/auth/README.md#custom-layout

    }

    private fun launchSignInFlow() {

        val providers = arrayListOf(
            AuthUI.IdpConfig.EmailBuilder().build(),
            AuthUI.IdpConfig.GoogleBuilder().build())

        signInLauncher.launch(AuthUI.getInstance().createSignInIntentBuilder().setTheme(R.style.LoginTheme).setAvailableProviders(providers).build())
    }

    private fun onSignInResult(result: FirebaseAuthUIAuthenticationResult) {
        val response = result.idpResponse
        if (result.resultCode == RESULT_OK) {
            val user = FirebaseAuth.getInstance().currentUser
            Log.d(TAG, "Successfully signed in user " + "${user?.displayName}!")
        } else {
            if (response == null){
                Log.d(TAG,"User pressed back")
            } else {
                Log.d(TAG, "Sign in not successful ${response.error?.errorCode}")
            }
        }
    }
}
