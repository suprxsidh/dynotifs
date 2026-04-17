# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.kts.

# Keep Hilt generated code
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep class * extends dagger.hilt.android.internal.managers.ComponentSupplier { *; }

# Keep Room entities
-keep class com.suprasidh.dynotifs.data.database.** { *; }

# Keep DataStore
-keep class androidx.datastore.** { *; }

# Keep Notification classes
-keep class android.app.** { *; }