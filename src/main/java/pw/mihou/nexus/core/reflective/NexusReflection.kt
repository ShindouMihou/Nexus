package pw.mihou.nexus.core.reflective

import pw.mihou.nexus.Nexus
import pw.mihou.nexus.core.assignment.NexusUuidAssigner
import pw.mihou.nexus.core.reflective.annotations.*
import pw.mihou.nexus.features.command.annotation.IdentifiableAs
import java.lang.reflect.Field

object NexusReflection {

    /**
     * Accumulates all the declared fields of the `from` parameter, this is useful for cases such as
     * interceptor repositories.
     *
     * @param from the origin object where all the fields will be accumulated from.
     * @param accumulator the accumulator to use.
     */
    fun accumulate(from: Any, accumulator: (field: Field) -> Unit) {
        val instance = from::class.java
        for (field in instance.declaredFields) {
            field.isAccessible = true
            accumulator(field)
        }
    }

    /**
     * Mirrors the fields from origin (`from`) to a new instance of the `to` class.
     * This requires the `to` class to have an empty constructor (no parameters), otherwise this will not work.
     *
     * @param from the origin object where all the fields will be copied.
     * @param to the new instance class where all the new fields will be pushed.
     * @return a new instance with all the fields copied.
     */
    fun copy(from: Any, to: Class<*>): Any {
        val instance = to.getDeclaredConstructor().newInstance()
        val fields =  NexusReflectionFields(from, instance)

        if (to.isAnnotationPresent(MustImplement::class.java)) {
            val extension = to.getAnnotation(MustImplement::class.java).clazz.java
            if (!extension.isAssignableFrom(from::class.java)) {
                throw IllegalStateException("${from::class.java.name} must implement the following class: ${extension.name}")
            }
        }

        var uuid: String? = null
        val uuidFields = fields.referencesWithAnnotation(Uuid::class.java)
        if (uuidFields.isNotEmpty()) {
            if (uuidFields.size == 1) {
                uuid = fields.stringify(uuidFields[0].name)
            } else {
                for (field in uuidFields) {
                    if (uuid == null) {
                        uuid = fields.stringify(field.name)
                    } else {
                        uuid += ":" + fields.stringify(field.name)
                    }
                }
            }
        }

        if (from::class.java.isAnnotationPresent(IdentifiableAs::class.java)) {
            uuid = from::class.java.getAnnotation(IdentifiableAs::class.java).key
        }

        for (field in instance::class.java.declaredFields) {
            field.isAccessible = true

            if (field.isAnnotationPresent(InjectReferenceClass::class.java)) {
                field.set(instance, from)
            } else if(field.isAnnotationPresent(InjectUUID::class.java)) {
                if (uuid == null) {
                    uuid = NexusUuidAssigner.request()
                }

                field.set(instance, uuid)
            } else if(field.isAnnotationPresent(Stronghold::class.java)) {
                field.set(instance, fields.shared)
            } else {
                val value: Any = fields[field.name] ?: continue
                val clazz = fromPrimitiveToNonPrimitive(field.type)
                if (clazz.isAssignableFrom(value::class.java) || value::class.java == clazz) {
                    field.set(instance, value)
                }
            }
        }

        return instance
    }

    private fun fromPrimitiveToNonPrimitive(clazz: Class<*>): Class<*> {
        return when (clazz) {
            Boolean::class.java, Boolean::class.javaPrimitiveType -> Boolean::class.java
            Int::class.java, Int::class.javaPrimitiveType -> Int::class.java
            Long::class.java, Long::class.javaPrimitiveType -> Long::class.java
            Char::class.java, Char::class.javaPrimitiveType -> Char::class.java
            String::class.java -> String::class.java
            Double::class.java, Double::class.javaPrimitiveType -> Double::class.java
            else -> clazz
        }
    }

}