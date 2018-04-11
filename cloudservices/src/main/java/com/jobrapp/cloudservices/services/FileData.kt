package com.jobrapp.cloudservices.services

/**
 * File Data holder types.
 * Currently there are 2 types. A generic FileData and a specific Gmail message
 */
sealed class FileDataType
data class FileData(val id: String, val name : String, val isFolder : Boolean = false,
                    val path : String? = null,
                    val rev : String? = null,
                    val data : Any? = null) : FileDataType()
data class GmailMessage(var id: String, val from: String, val subject: String, val date : String) : FileDataType()