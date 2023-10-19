package pw.mihou.nexus.features.react.writable

import pw.mihou.nexus.features.react.React

operator fun React.Writable<Float>.plus(number: Float): React.Writable<Float> {
    this.getAndUpdate { it + number }
    return this
}

operator fun React.Writable<Float>.plusAssign(number: Float) {
    this.plus(number)
}

operator fun React.Writable<Float>.minus(number: Float): React.Writable<Float> {
    this.getAndUpdate { it - number }
    return this
}

operator fun React.Writable<Float>.minusAssign(number: Float) {
    this.minus(number)
}

operator fun React.Writable<Float>.times(number: Float): React.Writable<Float> {
    this.getAndUpdate { it * number }
    return this
}


operator fun React.Writable<Float>.timesAssign(number: Float) {
    this.times(number)
}

operator fun React.Writable<Float>.div(number: Float): React.Writable<Float> {
    this.getAndUpdate { it / number }
    return this
}

operator fun React.Writable<Float>.divAssign(number: Float) {
    this.div(number)
}


operator fun React.Writable<Float>.rem(number: Float): React.Writable<Float> {
    this.getAndUpdate { it % number }
    return this
}

operator fun React.Writable<Float>.remAssign(number: Float) {
    this.rem(number)
}

operator fun React.Writable<Float>.compareTo(number: Float): Int {
    return this.get().compareTo(number)
}

operator fun React.Writable<Float>.dec(): React.Writable<Float> {
    return minus(1.0f)
}

operator fun React.Writable<Float>.inc(): React.Writable<Float> {
    return plus(1.0f)
}

operator fun React.Writable<Float>.unaryPlus(): React.Writable<Float> {
    this.getAndUpdate { +it }
    return this
}

operator fun React.Writable<Float>.unaryMinus(): React.Writable<Float> {
    this.getAndUpdate { -it }
    return this
}