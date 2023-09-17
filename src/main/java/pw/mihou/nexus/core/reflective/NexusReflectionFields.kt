package pw.mihou.nexus.core.reflective

import pw.mihou.nexus.Nexus
import pw.mihou.nexus.core.exceptions.NotInheritableException
import pw.mihou.nexus.core.reflective.annotations.Required
import pw.mihou.nexus.core.reflective.annotations.Share
import pw.mihou.nexus.core.reflective.annotations.WithDefault
import pw.mihou.nexus.features.inheritance.Inherits
import java.lang.reflect.Constructor
import java.lang.reflect.Field
import java.lang.reflect.InvocationTargetException
import java.util.*

class NexusReflectionFields(private val from: Any, private val reference: Any) {

    private val fields = mutableMapOf<String, Any>()
    private val _shared = mutableMapOf<String, Any>()

    val shared: Map<String, Any> get() = Collections.unmodifiableMap(_shared)

    @Suppress("UNCHECKED_CAST")
    operator fun <R> get(key: String): R? {
        return fields[key.lowercase()]?.let { it as? R }
    }

    private val to = reference::class.java

    init {
        initDefaults()
        Nexus.configuration.global.inheritance?.let { parent -> load(parent::class.java) }

        if (from::class.java.isAnnotationPresent(Inherits::class.java)) {
            val parent = from::class.java.getAnnotation(Inherits::class.java).value.java
            load(instantiate(parent)::class.java)
        }

        load(from::class.java)
        ensureHasRequired()
    }

    fun referenceWithAnnotation(annotation: Class<out Annotation>): Field? {
        return to.declaredFields.find { it.isAnnotationPresent(annotation) }
    }

    /**
     * Loads all the declared fields of the `reference` class that has the [WithDefault] annotation, this
     * should be done first in order to have the fields be overridden when there is a value.
     */
    private fun initDefaults() {
        for (field in to.declaredFields) {
            if (!field.isAnnotationPresent(WithDefault::class.java)) continue
            field.isAccessible = true
            try {
                fields[field.name.lowercase()]  = field.get(reference)
            } catch (e: IllegalAccessException) {
                throw IllegalStateException("Unable to complete reflection due to IllegalAccessException. [class=${to.name},field=${field.name}]")
            }
        }
    }

    /**
     * Instantiates the `clazz`, if the class has a singleton instance, then it will use that instead. This requires
     * the class to have a constructor that has no parameters, otherwise it will fail.
     *
     * @param clazz the class to instantiate.
     * @return the instantiated class.
     */
    private fun instantiate(clazz: Class<*>): Any {
        try {
            val reference: Any
            var singleton: Field? =  null

            // Detecting singleton instances, whether by Kotlin, or self-declared by the authors.
            // This is important because we don't want to doubly-instantiate the instance.
            try  {
                singleton = clazz.getField("INSTANCE")
            } catch (_: NoSuchFieldError)  {
                for (field in clazz.declaredFields) {
                    if (field.name.equals("INSTANCE")
                        || field::class.java.name.equals(clazz.name)
                        || field::class.java == clazz) {
                        singleton = field
                    }
                }
            }

            if (singleton != null)  {
                reference = singleton.get(null)
            } else {
                val constructor: Constructor<*> = if (clazz.constructors.isNotEmpty()) {
                    clazz.constructors.firstOrNull { it.parameterCount == 0 } ?: throw NotInheritableException(clazz)
                } else {
                    clazz.getDeclaredConstructor()
                }

                constructor.isAccessible = true
                reference = constructor.newInstance()
            }

            return reference
        } catch (exception: Exception) {
            when(exception) {
                is InvocationTargetException, is InstantiationException, is IllegalAccessException, is NoSuchMethodException ->
                    throw IllegalStateException("Unable to instantiate class, likely no compatible constructor. [class=${clazz.name},error=${exception}]")
                else -> throw exception
            }
        }
    }

    /**
     * Pulls all the fields from the `clazz` to their respective fields depending on the annotation, for example, if there
     * is a [Share] annotation present, it will be recorded under [sharedFields] otherwise it will be under [fields].
     *
     * @param clazz the class to reference.
     */
    private fun load(clazz : Class<*>) {
        clazz.declaredFields.forEach {
            it.isAccessible = true
            try {
                val value = it.get(from) ?: return
                if (it.isAnnotationPresent(Share::class.java)) {
                    _shared[it.name.lowercase()] = value
                    return@forEach
                }

                fields[it.name.lowercase()]  = value
            } catch  (e: IllegalAccessException) {
                throw IllegalStateException("Unable to complete reflection due to IllegalAccessException. [class=${clazz.name},field=${it.name}]")
            }
        }
    }

    /**
     * Ensures that all required fields (ones with [Required] annotation) has a value, otherwise throws an exception.
     */
    private fun ensureHasRequired() {
        for (field in to.declaredFields) {
            if (!field.isAnnotationPresent(Required::class.java)) continue
            if (fields[field.name.lowercase()] == null) {
                throw IllegalStateException("${field.name} is a required field, therefore, needs to have a value in class ${from::class.java.name}.")
            }
        }
    }
}