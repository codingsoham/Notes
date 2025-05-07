package com.example.notes

data class AuthUiState(
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val passwordVisible: Boolean = false,
    val isConfirmPasswordValid: Boolean = false,
)
