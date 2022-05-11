package com.github.brugapp.brug

import android.app.Activity
import android.content.Intent
import android.nfc.NfcAdapter
import android.nfc.NfcAdapter.CreateNdefMessageCallback
import android.os.Bundle
import android.view.View
import androidx.test.core.app.launchActivity
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import com.github.brugapp.brug.ui.NFCScannerActivity
import com.google.protobuf.NullValue
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.hamcrest.core.IsNot
import org.hamcrest.core.IsNull
import org.junit.Test
import org.mockito.ArgumentMatchers
import org.mockito.Mockito
import org.mockito.Mockito.*
import org.mockito.internal.matchers.Null


class NFCScannerActivityTest {
    @Test
     fun nfcScannerActivityMockTest() {
        val mockActivity: NFCScannerActivity = mock(NFCScannerActivity::class.java)
        assertThat(mockActivity.adapter, `is`(IsNull.nullValue()))}
    @Test
    fun writeModeOffTest(){
        val mockActivity: NFCScannerActivity = mock(NFCScannerActivity::class.java)
        mockActivity.writeModeOff()
        assertThat(mockActivity.writeMode,`is`(true))
    }

    @Test
    fun writeModeOnTest(){
        val mockActivity: NFCScannerActivity = mock(NFCScannerActivity::class.java)
        mockActivity.writeModeOn()
        assertThat(mockActivity.writeMode,`is`(false))
    }

    @Test
    fun findViewsTest(){
        val mockActivity: NFCScannerActivity = mock(NFCScannerActivity::class.java)
        assertThat(mockActivity.findViews(), `is`(false))
    }

    @Test
    fun onNewIntentTest(){
        val mockActivity: NFCScannerActivity = mock(NFCScannerActivity::class.java)
        val intent = Intent()
        mockActivity.onNewIntent(intent)
        assertThat(mockActivity.intent,`is`(IsNull.nullValue()))
    }

    @Test
    fun onNewActionTagIntentTest(){
        val mockActivity: NFCScannerActivity = mock(NFCScannerActivity::class.java)
        val intent = Intent()
        intent.action = NfcAdapter.ACTION_TAG_DISCOVERED
        mockActivity.onNewIntent(intent)
        assertThat(mockActivity.tag,`is`(IsNull.nullValue()))
    }

    @Test
    fun onPauseThenResumeTest(){
        val mockActivity: NFCScannerActivity = mock(NFCScannerActivity::class.java)
        mockActivity.onPause()
        mockActivity.onResume()
        assertThat(mockActivity.writeMode,`is`(false))
    }

    @Test
    fun onCreateTest(){
        val mockActivity: NFCScannerActivity = mock(NFCScannerActivity::class.java)
        val bundle = Bundle()
        mockActivity.onCreate(bundle)
        assertThat(mockActivity.editMessage,`is`(IsNull.nullValue()))
    }

    /*
    @Test
    fun nonNullNfcAdapterTest() {
        var mockActivity: NFCScannerActivity = mock(NFCScannerActivity::class.java)
        val adapter = NfcAdapter.getDefaultAdapter(mockActivity)
        mockActivity.adapter = adapter
        assertThat(mockActivity.adapter, isNotNull())
    }
     */
}