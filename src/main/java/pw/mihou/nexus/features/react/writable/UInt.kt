package pw.mihou.nexus.features.react.writable

import pw.mihou.nexus.features.react.React

operator fun React.Writable<UInt>.plus(number: UInt): React.Writable<UInt> {
    this.getAndUpdate { it + number }
    return this
}

operator fun React.Writable<UInt>.plusAssign(number: UInt) {
    this.plus(number)
}

operator fun React.Writable<UInt>.minus(number: UInt): React.Writable<UInt> {
    this.getAndUpdate { it - number }
    return this
}

operator fun React.Writable<UInt>.minusAssign(number: UInt) {
    this.minus(number)
}

operator fun React.Writable<UInt>.times(number: UInt): React.Writable<UInt> {
    this.getAndUpdate { it * number }
    return this
}


operator fun React.Writable<UInt>.timesAssign(number: UInt) {
    this.times(number)
}

operator fun React.Writable<UInt>.div(number: UInt): React.Writable<UInt> {
    this.getAndUpdate { it / number }
    return this
}

operator fun React.Writable<UInt>.divAssign(number: UInt) {
    this.div(number)
}


operator fun React.Writable<UInt>.rem(number: UInt): React.Writable<UInt> {
    this.getAndUpdate { it % number }
    return this
}

operator fun React.Writable<UInt>.remAssign(number: UInt) {
    this.rem(number)
}

operator fun React.Writable<UInt>.compareTo(number: UInt): Int {
    return this.get().compareTo(number)
}

operator fun React.Writable<UInt>.dec(): React.Writable<UInt> {
    return minus(1.toUInt())
}

operator fun React.Writable<UInt>.inc(): React.Writable<UInt> {
    return plus(1.toUInt())
}