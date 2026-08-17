# Reglas mínimas — ajustar por el integrador según necesidad real de shrink/obfuscate.
-keep class com.centinela.app.admin.AdminReceiver { *; }
-keep class com.sistemapersonal.data.entity.** { *; }
-keepattributes *Annotation*
-dontwarn org.slf4j.**
