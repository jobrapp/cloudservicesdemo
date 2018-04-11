package com.jobrapp.cloudservices.services

/**
 *
 */
sealed class AuthState {
    class Init : AuthState()
    class Cancelled : AuthState()
    data class Authenticated(val state: Any?) : AuthState()
    data class Error(val error: Any?) : AuthState()
}