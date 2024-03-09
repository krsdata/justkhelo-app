package justkhelo.cricks.utils

import androidx.annotation.MainThread
import androidx.lifecycle.MutableLiveData
import java.util.concurrent.atomic.AtomicBoolean

class SingleLiveEvent<T> : MutableLiveData<T>() {

    private val mPending = AtomicBoolean(false)

    @MainThread
    override fun setValue(t: T?) {
        mPending.set(true)
        super.setValue(t)
    }

    @MainThread
    fun call() {
        value = null
    }

    companion object {

        private val TAG = "SingleLiveEvent"
    }
}