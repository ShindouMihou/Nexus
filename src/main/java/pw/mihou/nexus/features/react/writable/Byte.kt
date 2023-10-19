package pw.mihou.nexus.features.react.writable

import pw.mihou.nexus.features.react.React

operator fun React.Writable<Byte>.plus(number: Byte): React.Writable<Byte> {
    this.getAndUpdate { (it + number).toByte() }
    return this
}

operator fun React.Writable<Byte>.plusAssign(number: Byte) {
    this.plus(number)
}

operator fun React.Writable<Byte>.minus(number: Byte): React.Writable<Byte> {
    this.getAndUpdate { (it - number).toByte() }
    return this
}

operator fun React.Writable<Byte>.minusAssign(number: Byte) {
    this.minus(number)
}

operator fun React.Writable<Byte>.times(number: Byte): React.Writable<Byte> {
    this.getAndUpdate { (it * number).toByte() }
    return this
}


operator fun React.Writable<Byte>.timesAssign(number: Byte) {
    this.times(number)
}

operator fun React.Writable<Byte>.div(number: Byte): React.Writable<Byte> {
    this.getAndUpdate { (it / number).toByte() }
    return this
}

operator fun React.Writable<Byte>.divAssign(number: Byte) {
    this.div(number)
}


operator fun React.Writable<Byte>.rem(number: Byte): React.Writable<Byte> {
    this.getAndUpdate { (it % number).toByte() }
    return this
}

operator fun React.Writable<Byte>.remAssign(number: Byte) {
    this.rem(number)
}

operator fun React.Writable<Byte>.compareTo(number: Byte): Int {
    return this.get().compareTo(number)
}

operator fun React.Writable<Byte>.dec(): React.Writable<Byte> {
    return minus(1.toByte())
}

operator fun React.Writable<Byte>.inc(): React.Writable<Byte> {
    return plus(1.toByte())
}

operator fun React.Writable<Byte>.unaryPlus(): React.Writable<Byte> {
    this.getAndUpdate { (+it).toByte() }
    return this
}

operator fun React.Writable<Byte>.unaryMinus(): React.Writable<Byte> {
    this.getAndUpdate { (-it).toByte() }
    return this
}