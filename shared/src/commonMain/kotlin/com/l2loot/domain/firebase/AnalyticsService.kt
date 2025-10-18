package com.l2loot.domain.firebase


interface AnalyticsService {
    /**
     * Track app opening event
     * @param isFirstOpen Whether this is the first time the app is opened
     */
    fun trackAppOpen(isFirstOpen: Boolean)

    /**
     * Track support link clicks
     * @param platform The support platform (patreon or kofi)
     * @param source Where the link was clicked (dialog or settings)
     */
    fun trackSupportLinkClick(platform: String, source: String)

    /**
     * Set the user GUID for analytics
     */
    fun setUserGuid(guid: String)

    /**
     * Get the current user GUID
     */
    fun getUserGuid(): String

    /**
     * Enable or disable event tracking
     */
    fun setTrackingEnabled(enabled: Boolean)
}