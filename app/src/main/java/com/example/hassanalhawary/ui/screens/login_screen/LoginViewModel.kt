package com.example.hassanal_hawary.ui.screens.login_screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hassanalhawary.domain.use_cases.LoginWithGoogleUseCase
import com.example.hassanalhawary.ui.screens.login_screen.LoginResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel
    @Inject constructor(
        private val loginWithGoogleUseCase: LoginWithGoogleUseCase
    ): ViewModel()
{
    private val _state = MutableStateFlow(LoginState())

    val state = _state.asStateFlow()

    fun loginWithGoogle() {

         viewModelScope.launch {
            val loginResult = loginWithGoogleUseCase()
            onSignInResult(loginResult)
        }
    }


    fun onSignInResult(loginResult: LoginResult) {
        _state.update {
            it.copy(
                isSignInSuccessful = loginResult.data != null,
                errorMessage = loginResult.errorMessage
            )
        }
    }
    fun resetState() {
        _state.update {
            LoginState()
        }
    }

    fun userClickSignInBtn() {
        if (_state.value.enteredEmail.isBlank()) {
            _state.update {
                it.copy(
                    enterValidEmailMsg =
                    "Please enter a vaild email"
                )
            }
        } else if (_state.value.enteredPassword.isBlank()) {
            _state.update {
                it.copy(
                    enterValidPassowrdMsg =
                    "Please enter a valid password"
                )
            }
        } else {
            // show progress bar indicator
            _state.update {
                it.copy(
                    showSignInProgressBar = true
                )
            }
        }
    }

    fun emailChanged(email: String) {
        viewModelScope.launch {
            _state.update {
                it.copy(enteredEmail = email)
            }
        }

    }
    fun passwordChanged(password: String) {

        viewModelScope.launch {
            _state.update {
                it.copy(enteredPassword = password)
            }
        }
    }

    fun showProgressBar() {
        viewModelScope.launch {
            _state.update {
                it.copy(
                    showSignInProgressBar = true
                )
            }
        }

    }
    fun hideProgressBar() {
        viewModelScope.launch {
            _state.update {
                it.copy(
                    showSignInProgressBar = false
                )
            }
        }

    }

/*

    fun signInWithEmailAndPassword(email: String, password: String) {
        // validate the email and password
        if (email.isEmpty()) {
            _state.update {
                it.copy(
                    enterValidEmailMsg = "enter valid email!",
                    showSignInProgressBar = false

                )
            }
        } else if (password.isEmpty()) {
            _state.update {
                it.copy(
                    enterValidPassowrdMsg = "enter valid password!",
                    showSignInProgressBar = false
                )
            }
        } else {

            */
/*
            here we are going to create a new
            user with email and password yaah!
            I'm really excited about that *_*
             *//*


            _state.update {
                it.copy(
                    showSignInProgressBar = true
                )
            }
            auth.signInWithEmailAndPassword(
                email,
                password
            ).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser

                    if (user != null) {
                        Log.d("login: ", "login: login the user successfully" +
                                "user email: ${user.email} \n" +
                                "user id: ${user.uid}")
                    }
                    _state.update {
                        it.copy(
                            navigateTo = "main_screen"
                        )
                    }
                }
            }.addOnFailureListener{
                Log.d("login", "login: failed with msg: ${it.message}")
            }


        }
    }
*/



}