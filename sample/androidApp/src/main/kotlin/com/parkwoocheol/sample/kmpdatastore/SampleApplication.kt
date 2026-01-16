package com.parkwoocheol.sample.kmpdatastore

import android.app.Application
import com.parkwoocheol.kmpdatastore.platform.KmpDataStoreContext

class SampleApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        KmpDataStoreContext.init(this)
    }
}
