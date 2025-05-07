package com.example.notes

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class AuthViewModel: ViewModel() {
    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> =_uiState.asStateFlow()
    fun setEmail(email: String) {
        _uiState.update {content->
            content.copy(email = email)
        }
    }
    fun setPassword(password: String) {
        _uiState.update { content ->
            content.copy(password = password)
        }
    }
    fun setConfirmPassword(confirmPassword: String) {
        _uiState.update { content ->
            content.copy(confirmPassword = confirmPassword)
        }
    }

    fun checkConfirmPassword(){
        if(uiState.value.confirmPassword != uiState.value.password) {
            _uiState.update { content ->
                content.copy(confirmPassword = "",isConfirmPasswordValid = true)
            }
        }else{
            _uiState.update { content ->
                content.copy(isConfirmPasswordValid = false)
            }
            signup(_uiState.value.email,_uiState.value.password)
        }
    }
    fun togglePasswordVisibility() {
        _uiState.update { content ->
            content.copy(passwordVisible = !content.passwordVisible)
        }
    }

    private val auth: FirebaseAuth= FirebaseAuth.getInstance()
    private val _authState= MutableLiveData<AuthState>()
    val authState: LiveData<AuthState> = _authState

    init{
        checkAuthStatus()
    }
    fun checkAuthStatus(){
        if(auth.currentUser==null)
            _authState.value=AuthState.Unauthenticated
        else
            _authState.value=AuthState.Authenticated
    }

    fun login(email: String,password: String){
        if(email.isEmpty() || password.isEmpty()){
            _authState.value=AuthState.Error("Email and password cannot be empty")
            return
        }
        _authState.value=AuthState.Loading
        auth.signInWithEmailAndPassword(email,password)
            .addOnCompleteListener { task ->
                if(task.isSuccessful){
                    _authState.value=AuthState.Authenticated
                }else{
                    _authState.value=AuthState.Error(task.exception?.message?:"Login failed")
                }
            }
    }

    private fun signup(email: String,password: String){
        if(email.isEmpty() || password.isEmpty()){
            _authState.value=AuthState.Error("Email and password cannot be empty")
            return
        }
        _authState.value=AuthState.Loading
        auth.createUserWithEmailAndPassword(email,password)
            .addOnCompleteListener { task ->
                if(task.isSuccessful){
                    _authState.value=AuthState.Authenticated
                }else{
                    _authState.value=AuthState.Error(task.exception?.message?:"SignUp failed")
                }
            }
    }
    fun getEmail(): String{
        return auth.currentUser?.email?:""
    }
    fun signOut(){
        auth.signOut()
        _authState.value= AuthState.Unauthenticated
    }
    fun forgotPassword(){
        _authState.value=AuthState.Forgot
    }
    fun passwordReset(email: String){
        if(email.isEmpty()){
            _authState.value=AuthState.Error("Email cannot be empty")
            Log.e("PasswordReset", "Failure")
            return
        }
        _authState.value=AuthState.Loading
        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if(task.isSuccessful){
                    _authState.value=AuthState.Unauthenticated
                }else{
                    _authState.value=AuthState.Error(task.exception?.message?:"Password Change Failed")
                }
            }
    }
    fun getCurrentUser(): String? {
        return auth.currentUser?.uid
    }

}

sealed class AuthState{
    object Authenticated: AuthState()
    object Unauthenticated: AuthState()
    object Forgot: AuthState()
    object Loading: AuthState()
    data class Error(val message: String): AuthState()
}