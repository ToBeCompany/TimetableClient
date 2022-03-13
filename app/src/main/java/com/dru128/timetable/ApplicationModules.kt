package com.dru128.timetable

import com.dru128.timetable.data.JsonDataManager
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val applicationModules = module {
    single { JsonDataManager() }
}