package pw.mihou.nexus.features.command.annotation

import pw.mihou.nexus.Nexus

/**
 * An annotation that tells [Nexus] that the command should use the given key as its unique identifier.
 *
 * This tends to be used when a command's name is common among other commands, which therefore causes an index-identifier conflict,
 * and needs to be resolved by using this annotation to change the identifier of the command.
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
annotation class IdentifiableAs(val key: String)
