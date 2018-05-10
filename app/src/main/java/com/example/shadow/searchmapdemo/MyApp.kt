package com.example.shadow.searchmapdemo

import android.app.Application
import com.blankj.utilcode.util.Utils

class MyApp :Application() {
    override fun onCreate() {
        super.onCreate()
        Utils.init(this)
    }
}