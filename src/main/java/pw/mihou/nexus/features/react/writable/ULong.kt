package pw.mihou.nexus.features.react.writable

import pw.mihou.nexus.features.react.React

operator fun React.Writable<ULong>.plus(number: ULong): React.Writable<ULong> {
    this.update { it + number }
    return this
}

operator fun React.Writable<ULong>.plusAssign(number: ULong) {
    this.plus(number)
}

operator fun React.Writable<ULong>.minus(number: ULong): React.Writable<ULong> {
    this.update { it - number }
    return this
}

operator fun React.Writable<ULong>.minusAssign(number: ULong) {
    this.minus(number)
}

operator fun React.Writable<ULong>.times(number: ULong): React.Writable<ULong> {
    this.update { it * number }
    return this
}


operator fun React.Writable<ULong>.timesAssign(number: ULong) {
    this.times(number)
}

operator fun React.Writable<ULong>.div(number: ULong): React.Writable<ULong> {
    this.update { it / number }
    return this
}

operator fun React.Writable<ULong>.divAssign(number: ULong) {
    this.div(number)
}


operator fun React.Writable<ULong>.rem(number: ULong): React.Writable<ULong> {
    this.update { it % number }
    return this
}

operator fun React.Writable<ULong>.remAssign(number: ULong) {
    this.rem(number)
}

operator fun React.Writable<ULong>.compareTo(number: ULong): Int {
    return this.get().compareTo(number)
}

operator fun React.Writable<ULong>.dec(): React.Writable<ULong> {
    return minus(1.toULong())
}

operator fun React.Writable<ULong>.inc(): React.Writable<ULong> {
    return plus(1.toULong())
}