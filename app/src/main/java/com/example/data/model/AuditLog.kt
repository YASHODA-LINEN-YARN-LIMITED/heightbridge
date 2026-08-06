package com.example.data.model

data class AuditLogEntry(
    val id: String,
    val timestamp: String,
    val userRole: String,
    val actionType: String,
    val gatePass: String,
    val details: String,
    val isSecureSigned: Boolean = true
)
