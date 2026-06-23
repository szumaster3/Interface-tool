package com.alex

import com.alex.store.Store
import java.io.IOException

object Cache {
    @JvmStatic
    var store: Store? = null
        private set

    @Throws(IOException::class)
    fun init(path: String?) {
        store = Store(path)
    }
}
