package com.jobrapp.cloudservices.services

/**
 * Used to keep track of the current authorization state
 */
sealed class AuthState {
    class Init : AuthState()
    class Authenticating : AuthState()
    class Cancelled : AuthState()
    class Authenticated() : AuthState()
    data class Error(val error: Any?) : AuthState()
}