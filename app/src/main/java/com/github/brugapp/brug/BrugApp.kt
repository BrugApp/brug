package com.github.brugapp.brug

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

const val DUMMY_TEXT: String = "Actual behavior coming soon…"

@HiltAndroidApp
class BrugApp : Application()