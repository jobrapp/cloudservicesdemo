package com.jobrapp.cloudservices.services

/**
 * Send this error in on Error
 */
class CloudServiceException(message: String?, error: Throwable? = null) :
        Exception(message, error) {
}