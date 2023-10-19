package pw.mihou.nexus.features.react.writable

import pw.mihou.nexus.features.react.React

operator fun React.Writable<Short>.plus(number: Short): React.Writable<Short> {
    this.getAndUpdate { (it + number).toShort() }
    return this
}

operator fun React.Writable<Short>.plusAssign(number: Short) {
    this.plus(number)
}

operator fun React.Writable<Short>.minus(number: Short): React.Writable<Short> {
    this.getAndUpdate { (it - number).toShort() }
    return this
}

operator fun React.Writable<Short>.minusAssign(number: Short) {
    this.minus(number)
}

operator fun React.Writable<Short>.times(number: Short): React.Writable<Short> {
    this.getAndUpdate { (it * number).toShort() }
    return this
}


operator fun React.Writable<Short>.timesAssign(number: Short) {
    this.times(number)
}

operator fun React.Writable<Short>.div(number: Short): React.Writable<Short> {
    this.getAndUpdate { (it / number).toShort() }
    return this
}

operator fun React.Writable<Short>.divAssign(number: Short) {
    this.div(number)
}


operator fun React.Writable<Short>.rem(number: Short): React.Writable<Short> {
    this.getAndUpdate { (it % number).toShort() }
    return this
}

operator fun React.Writable<Short>.remAssign(number: Short) {
    this.rem(number)
}

operator fun React.Writable<Short>.compareTo(number: Short): Int {
    return this.get().compareTo(number)
}

operator fun React.Writable<Short>.dec(): React.Writable<Short> {
    return minus(1)
}

operator fun React.Writable<Short>.inc(): React.Writable<Short> {
    return plus(1)
}

operator fun React.Writable<Short>.unaryPlus(): React.Writable<Short> {
    this.getAndUpdate { (+it).toShort() }
    return this
}

operator fun React.Writable<Short>.unaryMinus(): React.Writable<Short> {
    this.getAndUpdate { (-it).toShort() }
    return this
}