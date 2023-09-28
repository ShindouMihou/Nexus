package pw.mihou.nexus.features.command.interceptors.annotations

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD)
annotation class Name(val value: String)
