package pw.mihou.nexus.features.react.writable

import pw.mihou.nexus.features.react.React

operator fun <List, T> React.Writable<List>.plus(element: T): React.Writable<List> where List : MutableCollection<T> {
    this.getAndUpdate {
        it += element
        it
    }
    return this
}

operator fun <List, T> React.Writable<List>.plusAssign(element: T) where List : MutableCollection<T> {
    this.plus(element)
}

operator fun <List, T> React.Writable<List>.minus(element: T): React.Writable<List> where List : MutableCollection<T> {
    this.getAndUpdate {
        it.remove(element)
        it
    }
    return this
}

operator fun <List, T> React.Writable<List>.minusAssign(element: T) where List : MutableCollection<T> {
    this.plus(element)
}

operator fun <List, T> React.Writable<List>.contains(element: T): Boolean where List : Collection<T> {
    return this.get().contains(element)
}