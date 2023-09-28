package pw.mihou.nexus.core.exceptions

import java.lang.RuntimeException

class NotInheritableException(clazz: Class<*>):
    RuntimeException("${clazz.name} is not an inheritable class, ensure that there is at least one empty constructor, or no constructors at all.")