package com.github.brugapp.brug

import android.app.Application
import dagger.hilt.android.HiltAndroidApp


const val DUMMY_TEXT: String = "Actual behavior coming soon…"
const val PIC_ATTACHMENT_INTENT_KEY = "imageUri"
const val ITEM_INTENT_KEY = "itemkey"
const val USER_ID_INTENT_KEY = "userID"
const val LOCATION_REQUEST_CODE = 1
const val TAKE_PICTURE_REQUEST_CODE = 1
const val SELECT_PICTURE_REQUEST_CODE = 10
const val RECORDING_REQUEST_CODE = 3000
const val STORAGE_REQUEST_CODE = 2000
const val ITEMS_TEST_LIST_KEY = "ItemsTestList"
const val MESSAGE_TEST_LIST_KEY = "MessageTestList"
const val CONVERSATION_TEST_LIST_KEY = "ConvTestList"
const val EXTRA_ACTIVITY_NAME_KEY: String = "ActivityNameKey"
const val SUCCESS_TEXT = "Thank you ! The user will be notified."
const val ITEMMAPACTIVITY_NAMEKEY = "ACTIVITY"
const val SCANACTIVITY_NAMEKEY = "SIGNIN"


@HiltAndroidApp
/**
 * Hilt Android Application needed to use Hilt
 *
 */
class BrugApp : Application()