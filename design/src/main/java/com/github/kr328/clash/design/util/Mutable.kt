package com.github.kr328.clash.design.util

import kotlin.reflect.*

private class MutableProperty0<T>(
    override val name: String,
    private val getFunc: () -> T,
    private val setFunc: (T) -> Unit,
) : KMutableProperty0<T> {
    override val annotations: List<Annotation>
        get() = emptyList()
    override val isAbstract: Boolean
        get() = false
    override val isConst: Boolean
        get() = false
    override val isFinal: Boolean
        get() = true
    override val isOpen: Boolean
        get() = false
    override val isLateinit: Boolean
        get() = false
    override val isSuspend: Boolean
        get() = false
    override val parameters: List<KParameter>
        get() = emptyList()
    override val returnType: KType
        get() = throw UnsupportedOperationException()
    override val typeParameters: List<KTypeParameter>
        get() = emptyList()
    override val visibility: KVisibility
        get() = KVisibility.PUBLIC

    override fun call(vararg args: Any?): T {
        return get()
    }

    override fun callBy(args: Map<KParameter, Any?>): T {
        return get()
    }

    override fun get(): T {
        return getFunc()
    }

    override fun set(value: T) {
        setFunc(value)
    }

    override fun invoke(): T {
        return get()
    }

    override val getter: KProperty0.Getter<T>
        get() = throw UnsupportedOperationException()
    override val setter: KMutableProperty0.Setter<T>
        get() = throw UnsupportedOperationException()

    override fun getDelegate(): Any? {
        return null
    }
}

fun <T> KMutableProperty0<T?>.asMutable(defaultValue: T): KMutableProperty0<T> {
    return MutableProperty0(name, { get() ?: defaultValue }, { set(it) })
}

fun <T> asMutable(name: String = "", get: () -> T, set: (T) -> Unit): KMutableProperty0<T> {
    return MutableProperty0(name, get, set)
}
