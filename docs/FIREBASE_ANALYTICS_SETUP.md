# Firebase Google Analytics Setup Guide

This guide will help you configure Firebase Google Analytics for the L2Loot application.

## Overview

The application uses Firebase Google Analytics 4 (GA4) Measurement Protocol to track user events. Events are sent when:
- User opens the application for the first time (always tracked)
- User opens the application on subsequent launches (only if `track_events` is enabled in settings)

Each user is identified by a unique GUID that is generated on first launch and stored in the UserSettings database.

## Setup Steps

### 1. Create a Firebase Project

1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Click "Add project" or select an existing project
3. Follow the setup wizard to create your project

### 2. Add a Data Stream

1. In your Firebase project, go to **Settings** (gear icon) > **Project settings**
2. Navigate to the **Data Streams** section
3. Click **Add stream** and select **Web** (yes, web - even for desktop apps)
4. Enter a name for your stream (e.g., "L2Loot Desktop")
5. Optionally enable Enhanced measurement
6. Click **Create stream**

### 3. Get Your Measurement ID and API Secret

#### Get Measurement ID:
1. After creating the stream, you'll see a **Measurement ID** (format: `G-XXXXXXXXXX`)
2. Copy this ID

#### Get API Secret:
1. In the same Data Stream page, scroll down to **Measurement Protocol API secrets**
2. Click **Create** to generate a new API secret
3. Give it a nickname (e.g., "L2Loot Desktop API")
4. Click **Create**
5. Copy the generated secret value

### 4. Configure the Application

Open the file: `shared/src/jvmMain/kotlin/com/l2loot/data/analytics/AnalyticsService.kt`

Replace the placeholder values on lines 39-40:

```kotlin
class AnalyticsServiceImpl(
    private val measurementId: String = "G-XXXXXXXXXX", // Replace with your Measurement ID
    private val apiSecret: String = "YOUR_API_SECRET_HERE" // Replace with your API Secret
) : AnalyticsService {
```

**Example:**
```kotlin
class AnalyticsServiceImpl(
    private val measurementId: String = "G-ABC1234567",
    private val apiSecret: String = "sK7xJ2mP9qR4nT8vY3wZ"
) : AnalyticsService {
```

### Security Note

**Is it safe to store credentials this way?**

- **Measurement ID** (`G-XXXXXXXXXX`): ✅ Safe - This is a public identifier meant to be visible
- **API Secret**: ⚠️ Partially exposed - Can be extracted from decompiled JAR, but risk is limited:
  - Only allows **sending** events (write-only access)
  - Cannot read your analytics data
  - Cannot access other Firebase services
  - Worst case: Someone could send fake events to your analytics

For a desktop application, this approach is acceptable. The API secret is less sensitive than database credentials or auth tokens.

### 5. Verify Event Tracking

1. Build and run your application
2. In Firebase Console, go to **Analytics** > **Events**
3. Wait a few minutes (events can take 5-30 minutes to appear)
4. Look for the `app_open` event in the list

## User Settings

The application respects user privacy with the following settings stored in the `user_settings` table:

### `track_events` (Boolean, default: true)
- Controls whether analytics events are sent after the first app open
- First app open is ALWAYS tracked to establish the user
- Users can disable this in the app settings

### `user_guid` (String)
- Unique identifier generated on first app launch
- Used to identify the same user across sessions
- Stored persistently in the local database

## Event Details

### `app_open` Event

This event is tracked when the user opens the application.

**Parameters:**
- `first_open` (boolean): `true` if this is the first time the app is opened, `false` otherwise
- `engagement_time_msec` (number): Engagement time in milliseconds (set to 1)

**Custom Dimensions:**
- `client_id`: The user's unique GUID
- `user_id`: The user's unique GUID (same as client_id)

## Privacy Considerations

- No personal information is collected
- Only a randomly generated GUID is used to identify users
- Users can disable event tracking after first launch
- All data is stored locally in the user's device
- Analytics can be completely disabled by setting `track_events` to `false` in the database

## Troubleshooting

### Events not appearing in Firebase Console

1. **Wait**: Events can take up to 30 minutes to appear
2. **Check credentials**: Verify your Measurement ID and API Secret are correct
3. **Check console logs**: Look for messages starting with "Analytics" in the application console
4. **Test network**: Ensure your application can reach `https://www.google-analytics.com`

### Error messages

- **"User GUID not set"**: The GUID initialization may have failed. Check the database
- **"Failed to send analytics event"**: Network error or invalid credentials
- **"Failed to track app open"**: General error in the tracking logic

## Disabling Analytics

To completely disable analytics:

1. **In code**: Set both values to empty strings in `AnalyticsServiceImpl`:
   ```kotlin
   private val measurementId: String = ""
   private val apiSecret: String = ""
   ```

2. **In database**: Set `track_events` to `0` in the `user_settings` table

## Additional Resources

- [GA4 Measurement Protocol Documentation](https://developers.google.com/analytics/devguides/collection/protocol/ga4)
- [Firebase Analytics Overview](https://firebase.google.com/docs/analytics)
- [Privacy Best Practices](https://firebase.google.com/support/privacy)
