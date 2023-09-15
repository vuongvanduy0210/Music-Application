package com.vuongvanduy.music.ui.settings.account

import android.annotation.SuppressLint
import android.content.IntentSender
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.vuongvanduy.music.R
import com.vuongvanduy.music.base.dialogs.ProgressDialog
import com.vuongvanduy.music.base.fragment.BaseFragment
import com.vuongvanduy.music.common.TITLE_ACCOUNT
import com.vuongvanduy.music.common.showDialog
import com.vuongvanduy.music.databinding.FragmentSignInBinding


class SignInFragment : BaseFragment() {

    private lateinit var binding: FragmentSignInBinding

    private lateinit var oneTapClient: SignInClient
    private lateinit var signUpRequest: BeginSignInRequest

    private val activityResultLauncherGoogle =
        registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
            if (result.resultCode == AppCompatActivity.RESULT_OK) {
                try {
                    val credential = oneTapClient.getSignInCredentialFromIntent(result.data)
                    val idToken = credential.googleIdToken
                    when {
                        idToken != null -> {
                            val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
                            Firebase.auth.signInWithCredential(firebaseCredential)
                                .addOnCompleteListener(mainActivity) { task ->
                                    if (task.isSuccessful) {
                                        // Sign in success, update UI with the signed-in user's information
                                        Log.e("SignInFragment", "SignInSuccess")
                                        findNavController().popBackStack(
                                            R.id.accountFragment,
                                            false
                                        )
                                    } else {
                                        // If sign in fails, display a message to the user.
                                        Log.e("SignInFragment", "SignIn Fail")
                                    }
                                }
                        }

                        else -> {
                            // Shouldn't happen.
                            Log.e("SignInFragment", "No ID token!")
                        }
                    }
                } catch (e: ApiException) {
                    when (e.statusCode) {
                        CommonStatusCodes.CANCELED -> {
                            Log.e("SignInFragment", "One-tap dialog was closed.")
                        }
                    }
                }
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSignInBinding.inflate(inflater, container, false)
        init()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        drawUI()

        initListener()

        setSignInWithGoogle()
    }

    private fun drawUI() {
        Glide.with(mainActivity).load(R.drawable.ic_google).into(binding.btSignInGoogle)
        Glide.with(mainActivity).load(R.drawable.ic_github).into(binding.btSignInGithub)
    }

    private fun setSignInWithGoogle() {
        oneTapClient = Identity.getSignInClient(mainActivity)
        signUpRequest = BeginSignInRequest.builder()
            .setGoogleIdTokenRequestOptions(
                BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                    .setSupported(true)
                    // Your server's client ID, not your Android client ID.
                    .setServerClientId(getString(R.string.your_web_client_id))
                    // Show all accounts on the device.
                    .setFilterByAuthorizedAccounts(false)
                    .build()
            )
            .build()
    }

    private fun initListener() {

        binding.btSignIn.setOnClickListener {
            onClickSignIn()
        }

        binding.layoutSignUp.setOnClickListener {
            goToSignUp()
        }

        binding.layoutForgotPassword.setOnClickListener {
            goToForgotPassword()
        }

        binding.btSignInGoogle.setOnClickListener {
            onClickSignInWithGoogle()
        }

        binding.btSignInGithub.setOnClickListener {
            goToGithubAuthFragment()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun onClickSignIn() {

        val progressDialog = ProgressDialog(mainActivity, "Signing in...")

        binding.tvError.apply {
            text = ""
            visibility = View.GONE
        }

        val auth = FirebaseAuth.getInstance()

        val email = binding.edtEmail.text.trim().toString()
        val password = binding.edtPassword.text.trim().toString()

        ValidationUtils.checkValidSignInInput(email, password)?.let {
            binding.apply {
                tvError.text = it
                tvError.visibility = View.VISIBLE
                if (it.contains("email", true)) {
                    edtEmail.setText("")
                } else {
                    edtPassword.setText("")
                }
            }
            return
        }

        progressDialog.show()
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                progressDialog.dismiss()
                if (task.isSuccessful) {
                    // Sign in success
                    val user = FirebaseAuth.getInstance().currentUser
                    if (user != null) {
                        if (!user.isEmailVerified) {
                            val message = "Your account is not verified. " +
                                    "Please check your email (${user.email}) to continue."
                            showDialog(mainActivity, layoutInflater, message)
                            FirebaseAuth.getInstance().signOut()
                        } else {
                            findNavController().popBackStack(R.id.accountFragment, false)
                        }
                    }
                }
            }
            .addOnFailureListener { exception ->
                if (exception is FirebaseAuthInvalidUserException) {
                    binding.tvError.apply {
                        text = "Email does not exist"
                        visibility = View.VISIBLE
                    }
                } else if (exception is FirebaseAuthInvalidCredentialsException) {
                    binding.tvError.apply {
                        text = "Password is incorrect"
                        visibility = View.VISIBLE
                    }
                    binding.edtPassword.setText("")
                }
            }
    }

    private fun goToSignUp() {
        if (isFragmentInBackStack(R.id.signUpFragment)) {
            findNavController().popBackStack(R.id.signUpFragment, false)
        } else {
            val action = SignInFragmentDirections.actionSignInFragmentToSignUpFragment()
            findNavController().navigate(action)
        }
    }

    private fun goToForgotPassword() {
        if (isFragmentInBackStack(R.id.forgotPasswordFragment)) {
            findNavController().popBackStack(R.id.forgotPasswordFragment, false)
        } else {
            val action = SignInFragmentDirections.actionSignInFragmentToForgotPasswordFragment()
            findNavController().navigate(action)
        }
    }

    private fun onClickSignInWithGoogle() {
        val progressDialog = ProgressDialog(mainActivity, "Loading...")
        progressDialog.show()
        oneTapClient.beginSignIn(signUpRequest)
            .addOnSuccessListener(mainActivity) { result ->
                try {
                    progressDialog.dismiss()
                    val intentSenderRequest =
                        IntentSenderRequest.Builder(result.pendingIntent.intentSender).build()
                    activityResultLauncherGoogle.launch(intentSenderRequest)
                } catch (e: IntentSender.SendIntentException) {
                    Log.e("Duy", "Couldn't start One Tap UI: ${e.localizedMessage}")
                }
            }
    }

    private fun goToGithubAuthFragment() {
        val action = SignInFragmentDirections.actionSignInFragmentToGithubAuthFragment()
        findNavController().navigate(action)
    }

    override fun onResume() {
        super.onResume()
        mainActivity.binding.toolBarTitle.text = TITLE_ACCOUNT
    }
}