package pw.mihou.nexus.core.managers.indexes.exceptions

class IndexIdentifierConflictException(name: String):
    RuntimeException("An index-identifier conflict was identified between commands (or context menus) with the name $name. We do not " +
            "recommend having commands (or context menus) with the same name that have the same unique identifier, please change one of the commands' (or context menus') identifier " +
            "by using the @IdentifiableAs annotation. (https://github.com/ShindouMihou/Nexus/wiki/Slash-Command-Indexing)")