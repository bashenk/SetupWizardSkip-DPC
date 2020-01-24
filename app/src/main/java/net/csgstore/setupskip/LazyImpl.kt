package net.csgstore.setupskip

import java.util.*
import kotlin.reflect.KProperty

class LazyW<This,Return>(val initializer:This.()->Return) {
    private val values = WeakHashMap<This,Return>()

    @Suppress("UNCHECKED_CAST")
    operator fun getValue(thisRef:Any,property:KProperty<*>):Return = synchronized(values)
    {
        thisRef as This
        return values.getOrPut(thisRef) {thisRef.initializer()}
    }
}
public inline fun <reified This, Return> wLazy(noinline initializer: This.() -> Return): LazyW<This, Return> = LazyW(initializer)

//class LazyW<This, Return>(val initializer: This.() -> Return) {
//    private val values = WeakHashMap<This, Return>()
//
//    @Suppress("UNCHECKED_CAST")
//    operator fun getValue(thisRef: Any, property: KProperty<*>): Return = synchronized(values) {
//        thisRef as This
//        return values.getOrPut(thisRef) { thisRef.initializer() }
//    }
//}

/**
 * Represents a value with lazy initialization.
 *
 * To create an instance of [Lazy] use the [mLazy] function.
 */
public interface Lazy<This, out Return> {

    public val argument: This

    /**
     * Gets the lazily initialized value of the current Lazy instance.
     * Once the value was initialized it must not change during the rest of lifetime of this Lazy instance.
     */
    public val value: Return

    /**
     * Returns `true` if a value for this Lazy instance has been already initialized, and `false` otherwise.
     * Once this function has returned `true` it stays `true` for the rest of lifetime of this Lazy instance.
     */
    public fun isInitialized(): Boolean
}

/**
 * Creates a new instance of the [Lazy] that is already initialized with the specified [value].
 */
public inline fun <reified This, Return> mLazyOf(value: Return): Lazy<This, Return> = InitializedLazyImpl(This::class.objectInstance!!, value)

/**
 * An extension to delegate a read-only property of type [Return] to an instance of [Lazy].
 *
 * This extension allows to use instances of Lazy for property delegation:
 * `val property: String by lazy { initializer }`
 */
@Suppress("NOTHING_TO_INLINE")
public inline operator fun <This, Return> Lazy<This, Return>.getValue(thisRef: Any?, property: KProperty<*>): Return = value
@Suppress("NOTHING_TO_INLINE")
public inline fun <This, Return> Lazy<This, Return>.getArgument(thisRef: Any?, property: KProperty<*>): This = argument

/**
 * Specifies how a [Lazy] instance synchronizes initialization among multiple threads.
 */
public enum class LazyThreadSafetyMode {

    /**
     * Locks are used to ensure that only a single thread can initialize the [Lazy] instance.
     */
    SYNCHRONIZED,

    /**
     * Initializer function can be called several times on concurrent access to uninitialized [Lazy] instance value,
     * but only the first returned value will be used as the value of [Lazy] instance.
     */
    PUBLICATION,

    /**
     * No locks are used to synchronize an access to the [Lazy] instance value; if the instance is accessed from multiple threads, its behavior is undefined.
     *
     * This mode should not be used unless the [Lazy] instance is guaranteed never to be initialized from more than one thread.
     */
    NONE,
}

public inline fun <reified This, Return> mLazy(noinline initializer: This.() -> Return): Lazy<This, Return> = SynchronizedLazyImpl(This::class.objectInstance!!, initializer)

internal object UNINITIALIZED_VALUE
class InitializedLazyImpl<This, out Return>(override val argument: This, override val value: Return) : Lazy<This, Return> {
    override fun isInitialized(): Boolean = true
    override fun toString(): String = value.toString()
}

class SynchronizedLazyImpl<This, out Return>(override val argument: This, initializer: (This) -> Return, lock: Any? = null
) : Lazy<This, Return> {
    private var initializer: ((This) -> Return)? = initializer
    @Volatile private var _value: Any? = UNINITIALIZED_VALUE
    // final field is required to enable safe publication of constructed instance
    private val lock = lock ?: this

    override val value: Return
        get() {
            val _v1 = _value
            if (_v1 !== UNINITIALIZED_VALUE) {
                @Suppress("UNCHECKED_CAST") return _v1 as Return
            }

            return synchronized(lock) {
                val _v2 = _value
                if (_v2 !== UNINITIALIZED_VALUE) {
                    @Suppress("UNCHECKED_CAST") (_v2 as Return)
                } else {
                    val typedValue = initializer!!(argument)
                    _value = typedValue
                    initializer = null
                    typedValue
                }
            }
        }

    override fun isInitialized(): Boolean = _value !== UNINITIALIZED_VALUE

    override fun toString(): String = if (isInitialized()) value.toString() else "Lazy value not initialized yet."

    private fun writeReplace(): Any = InitializedLazyImpl<This, Return>(argument, value)
}


private class SafePublicationLazyImpl<This, out Return>(override val argument: This, initializer: ((This) -> Return)) : Lazy<This, Return> {
    @Volatile private var initializer: ((This) -> Return)? = initializer
    @Volatile private var _value: Any? = UNINITIALIZED_VALUE
    // this final field is required to enable safe publication of constructed instance
    private val final: Any = UNINITIALIZED_VALUE

    override val value: Return
        get() {
            val _v1 = _value
            if (_v1 !== UNINITIALIZED_VALUE) {
                @Suppress("UNCHECKED_CAST") return _v1 as Return
            }

            val initializerValue = initializer

            // if we see null in initializer here, it means that the value is already set by another thread
            if (initializerValue != null) {
                val _v2 = initializerValue(argument)
                if (valueUpdater.compareAndSet(this, UNINITIALIZED_VALUE, _v2)) {
                    initializer = null
                    return _v2
                }
            }
            @Suppress("UNCHECKED_CAST") return _value as Return
        }

    override fun isInitialized(): Boolean = _value !== UNINITIALIZED_VALUE

    override fun toString(): String = if (isInitialized()) value.toString() else "Lazy value not initialized yet."

    private fun writeReplace(): Any = InitializedLazyImpl<This, Return>(argument, value)

    companion object {
        private val valueUpdater =
            java.util.concurrent.atomic.AtomicReferenceFieldUpdater.newUpdater(SafePublicationLazyImpl::class.java,
                Any::class.java, "_value")
    }
}

internal class UnsafeLazyImpl<This, out Return>(initializer: (This) -> Return) : Lazy<This, Return> {
    private var initializer: ((This) -> Return)? = initializer

    private var _value: Any? = UNINITIALIZED_VALUE

    override val argument: This
        get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.

    override val value: Return
        get() {
            if (_value === UNINITIALIZED_VALUE) {
                _value = initializer!!(argument)
                initializer = null
            }
            @Suppress("UNCHECKED_CAST") return _value as Return
        }

    override fun isInitialized(): Boolean = _value !== UNINITIALIZED_VALUE

    override fun toString(): String = if (isInitialized()) value.toString() else "Lazy value not initialized yet."
    private fun writeReplace(): Any = InitializedLazyImpl<This, Return>(argument, value)

}
