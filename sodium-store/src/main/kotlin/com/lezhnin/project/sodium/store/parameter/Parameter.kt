package com.lezhnin.project.sodium.store.parameter

interface Parameter<V> {
    val key: String
    val value: V
}

interface ChangeSource<T> {
    fun consumer(consumer: (T) -> Unit)
}

class ReloadableParameter<T, V>(
    override val key: String,
    changeSource: ChangeSource<T>,
    initialValue: T,
    transform: (T) -> V
) : Parameter<V> {

    override val value: V
        get() = reloadbleValue

    @Volatile
    private var reloadbleValue = transform(initialValue)

    init {
        changeSource.consumer {
            reloadbleValue = transform(it)
        }
    }
}