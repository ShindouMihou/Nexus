package pw.mihou.nexus.features.react.writable

import pw.mihou.nexus.features.react.React

operator fun React.Writable<UByte>.plus(number: UByte): React.Writable<UByte> {
    this.update { (it + number).toUByte() }
    return this
}

operator fun React.Writable<UByte>.plusAssign(number: UByte) {
    this.plus(number)
}

operator fun React.Writable<UByte>.minus(number: UByte): React.Writable<UByte> {
    this.update { (it - number).toUByte() }
    return this
}

operator fun React.Writable<UByte>.minusAssign(number: UByte) {
    this.minus(number)
}

operator fun React.Writable<UByte>.times(number: UByte): React.Writable<UByte> {
    this.update { (it * number).toUByte() }
    return this
}


operator fun React.Writable<UByte>.timesAssign(number: UByte) {
    this.times(number)
}

operator fun React.Writable<UByte>.div(number: UByte): React.Writable<UByte> {
    this.update { (it / number).toUByte() }
    return this
}

operator fun React.Writable<UByte>.divAssign(number: UByte) {
    this.div(number)
}


operator fun React.Writable<UByte>.rem(number: UByte): React.Writable<UByte> {
    this.update { (it % number).toUByte() }
    return this
}

operator fun React.Writable<UByte>.remAssign(number: UByte) {
    this.rem(number)
}

operator fun React.Writable<UByte>.compareTo(number: UByte): Int {
    return this.get().compareTo(number)
}

operator fun React.Writable<UByte>.dec(): React.Writable<UByte> {
    return minus(1.toUByte())
}

operator fun React.Writable<UByte>.inc(): React.Writable<UByte> {
    return plus(1.toUByte())
}