# Keep main entry point
-keep class com.l2loot.MainKt { *; }

# Keep Compose runtime
-keep class androidx.compose.** { *; }
-keep class org.jetbrains.compose.** { *; }

# Keep Koin
-keep class org.koin.** { *; }
-dontwarn org.koin.**

# Keep Kotlin metadata
-keep class kotlin.Metadata { *; }
-keepattributes RuntimeVisibleAnnotations

# Keep coroutines
-keepnames class kotlinx.coroutines.** { *; }
-dontwarn kotlinx.coroutines.**

# Keep SQLDelight
-keep class app.cash.sqldelight.** { *; }
-keep class com.l2loot.data.** { *; }

# Keep serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Keep data classes
-keepclassmembers class * {
    *** copy(...);
    *** component1();
    *** component2();
    *** component3();
    *** component4();
    *** component5();
}

# Keep ViewModels
-keep class * extends androidx.lifecycle.ViewModel { *; }

# Suppress warnings
-dontwarn org.slf4j.**
-dontwarn javax.annotation.**
