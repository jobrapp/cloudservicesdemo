package com.jobrapp.cloudservices.services

/**
 * Used to keep track of the current authorization state
 */
sealed class AuthState {
    class Init : AuthState()
    class Cancelled : AuthState()
    data class Authenticated(val state: Any?) : AuthState()
    data class Error(val error: Any?) : AuthState()
}