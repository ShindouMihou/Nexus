package pw.mihou.nexus.features.react.writable

import pw.mihou.nexus.features.react.React

operator fun React.Writable<Double>.plus(number: Double): React.Writable<Double> {
    this.update { it + number }
    return this
}

operator fun React.Writable<Double>.plusAssign(number: Double) {
    this.plus(number)
}

operator fun React.Writable<Double>.minus(number: Double): React.Writable<Double> {
    this.update { it - number }
    return this
}

operator fun React.Writable<Double>.minusAssign(number: Double) {
    this.minus(number)
}

operator fun React.Writable<Double>.times(number: Double): React.Writable<Double> {
    this.update { it * number }
    return this
}


operator fun React.Writable<Double>.timesAssign(number: Double) {
    this.times(number)
}

operator fun React.Writable<Double>.div(number: Double): React.Writable<Double> {
    this.update { it / number }
    return this
}

operator fun React.Writable<Double>.divAssign(number: Double) {
    this.div(number)
}


operator fun React.Writable<Double>.rem(number: Double): React.Writable<Double> {
    this.update { it % number }
    return this
}

operator fun React.Writable<Double>.remAssign(number: Double) {
    this.rem(number)
}

operator fun React.Writable<Double>.compareTo(number: Double): Int {
    return this.get().compareTo(number)
}

operator fun React.Writable<Double>.dec(): React.Writable<Double> {
    return minus(1.0)
}

operator fun React.Writable<Double>.inc(): React.Writable<Double> {
    return plus(1.0)
}

operator fun React.Writable<Double>.unaryPlus(): React.Writable<Double> {
    this.update { +it }
    return this
}

operator fun React.Writable<Double>.unaryMinus(): React.Writable<Double> {
    this.update { -it }
    return this
}