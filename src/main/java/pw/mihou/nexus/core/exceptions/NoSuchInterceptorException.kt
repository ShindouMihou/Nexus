package pw.mihou.nexus.core.exceptions

class NoSuchMiddlewareException(middleware: String):
    RuntimeException("No middleware with the identifier of $middleware can be found, please validate that the name is correct and " +
            "the middleware does exist. Initializing the command before the middlewares are added can also cause this exception.")

class NoSuchAfterwareException(afterware: String):
    RuntimeException("No afterware with the identifier of $afterware can be found, please validate that the name is correct and " +
            "the afterware does exist. Initializing the command before the afterwares are added can also cause this exception.")