package com.github.brugapp.brug

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

const val DUMMY_TEXT: String = "Actual behavior coming soon…"
const val USER_INTENT_KEY = "userkey"
const val ITEM_INTENT_KEY = "itemkey"
const val LOCATION_REQUEST_CODE = 1
const val TAKE_PICTURE_REQUEST_CODE = 1
const val SELECT_PICTURE_REQUEST_CODE = 10
const val RECORDING_REQUEST_CODE = 3000
const val STORAGE_REQUEST_CODE = 2000

@HiltAndroidApp
class BrugApp : Application()