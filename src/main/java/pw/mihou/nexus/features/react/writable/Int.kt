package pw.mihou.nexus.features.react.writable

import pw.mihou.nexus.features.react.React

operator fun React.Writable<Int>.plus(number: Int): React.Writable<Int> {
    this.getAndUpdate { it + number }
    return this
}

operator fun React.Writable<Int>.plusAssign(number: Int) {
    this.plus(number)
}

operator fun React.Writable<Int>.minus(number: Int): React.Writable<Int> {
    this.getAndUpdate { it - number }
    return this
}

operator fun React.Writable<Int>.minusAssign(number: Int) {
    this.minus(number)
}

operator fun React.Writable<Int>.times(number: Int): React.Writable<Int> {
    this.getAndUpdate { it * number }
    return this
}


operator fun React.Writable<Int>.timesAssign(number: Int) {
    this.times(number)
}

operator fun React.Writable<Int>.div(number: Int): React.Writable<Int> {
    this.getAndUpdate { it / number }
    return this
}

operator fun React.Writable<Int>.divAssign(number: Int) {
    this.div(number)
}


operator fun React.Writable<Int>.rem(number: Int): React.Writable<Int> {
    this.getAndUpdate { it % number }
    return this
}

operator fun React.Writable<Int>.remAssign(number: Int) {
    this.rem(number)
}

operator fun React.Writable<Int>.compareTo(number: Int): Int {
    return this.get().compareTo(number)
}

operator fun React.Writable<Int>.dec(): React.Writable<Int> {
    return minus(1)
}

operator fun React.Writable<Int>.inc(): React.Writable<Int> {
    return plus(1)
}

operator fun React.Writable<Int>.unaryPlus(): React.Writable<Int> {
    this.getAndUpdate { +it }
    return this
}

operator fun React.Writable<Int>.unaryMinus(): React.Writable<Int> {
    this.getAndUpdate { -it }
    return this
}