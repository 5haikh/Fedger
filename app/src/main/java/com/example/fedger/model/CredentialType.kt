package com.example.fedger.model

/**
 * Defines the types of credentials that can be stored in the password manager
 */
enum class CredentialType(val displayName: String) {
    USERNAME_PASSWORD("Username & Password"),
    PASSWORD_ONLY("Password"),
    PIN("PIN"),
    CUSTOM("Custom");

    companion object {
        /**
         * Find a credential type from its display name
         */
        fun fromDisplayName(displayName: String): CredentialType {
            return values().find { it.displayName == displayName } ?: CUSTOM
        }
    }
} 