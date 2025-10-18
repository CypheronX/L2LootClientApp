package com.l2loot.domain.firebase

/**
 * Service for handling Firebase Anonymous Authentication.
 * This allows the app to authenticate with Firebase to read protected data.
 */
interface FirebaseAuthService {
    /**
     * Get a valid ID token for Firebase requests.
     * Automatically handles token refresh if expired.
     */
    suspend fun getIdToken(): String?
    
    /**
     * Sign in anonymously to Firebase.
     */
    suspend fun signInAnonymously(): Boolean
}

