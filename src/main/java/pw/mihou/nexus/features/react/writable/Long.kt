package pw.mihou.nexus.features.react.writable

import pw.mihou.nexus.features.react.React

operator fun React.Writable<Long>.plus(number: Long): React.Writable<Long> {
    this.update { it + number }
    return this
}

operator fun React.Writable<Long>.plusAssign(number: Long) {
    this.plus(number)
}

operator fun React.Writable<Long>.minus(number: Long): React.Writable<Long> {
    this.update { it - number }
    return this
}

operator fun React.Writable<Long>.minusAssign(number: Long) {
    this.minus(number)
}

operator fun React.Writable<Long>.times(number: Long): React.Writable<Long> {
    this.update { it * number }
    return this
}


operator fun React.Writable<Long>.timesAssign(number: Long) {
    this.times(number)
}

operator fun React.Writable<Long>.div(number: Long): React.Writable<Long> {
    this.update { it / number }
    return this
}

operator fun React.Writable<Long>.divAssign(number: Long) {
    this.div(number)
}


operator fun React.Writable<Long>.rem(number: Long): React.Writable<Long> {
    this.update { it % number }
    return this
}

operator fun React.Writable<Long>.remAssign(number: Long) {
    this.rem(number)
}

operator fun React.Writable<Long>.compareTo(number: Long): Int {
    return this.get().compareTo(number)
}

operator fun React.Writable<Long>.dec(): React.Writable<Long> {
    return minus(1)
}

operator fun React.Writable<Long>.inc(): React.Writable<Long> {
    return plus(1)
}

operator fun React.Writable<Long>.unaryPlus(): React.Writable<Long> {
    this.update { +it }
    return this
}

operator fun React.Writable<Long>.unaryMinus(): React.Writable<Long> {
    this.update { -it }
    return this
}