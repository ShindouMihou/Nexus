package pw.mihou.nexus.features.command.interceptors.annotations

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD, AnnotationTarget.CLASS)
annotation class Name(val value: String)
