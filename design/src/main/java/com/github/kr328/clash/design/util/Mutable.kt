package com.github.kr328.clash.design.util

import kotlin.reflect.KMutableProperty0

fun <T> KMutableProperty0<T?>.asMutable(defaultValue: T): KMutableProperty0<T> {
    val property = this

    return object : KMutableProperty0<T> by property {
        override fun get(): T {
            return property.get() ?: defaultValue
        }

        override fun set(value: T) {
            property.set(value)
        }
    }
}
