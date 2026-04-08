# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in the SDK tools.

# Keep Room entities
-keep class com.smartsplit.app.data.model.** { *; }

# Keep Firebase Auth classes
-keep class com.google.firebase.** { *; }
