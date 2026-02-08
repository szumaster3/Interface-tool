package rs2.cache

import com.alex.store.Store
import properties.PropertyValues
import java.io.IOException

object Cache {
    var store: Store? = null

    @Throws(IOException::class)
    fun init() {
        store = Store(PropertyValues.cache_path)
    }
}
