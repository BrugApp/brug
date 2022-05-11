package com.github.brugapp.brug

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.brugapp.brug.view_model.NFCScanViewModel
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.IsNot
import org.hamcrest.core.IsNull
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NFCScanViewModelTest {
    private val viewModel = NFCScanViewModel()

    @Test
    fun createRecordTest(){
        val text = "hello"
        assertThat(viewModel.createRecord(text), IsNot(IsNull.nullValue()))
    }
    
    @Test
    fun setupTagTest(){
        val tagDetected = IntentFilter(ACTION_TAG_DISCOVERED)
        tagDetected.addCategory(Intent.CATEGORY_DEFAULT)
        assert(viewModel.setupTagTest()==tagDetected)
     }

     
}
/*- - - - - UI tests only work on physical devices: can't test emulator without NFC! - - - - - -
@RunWith(AndroidJUnit4::class)
class NFCScannerActivityTest {

    private val myIntent = Intent(ApplicationProvider.getApplicationContext(), NFCScannerActivity::class.java)

    @Test
    fun correctTitleShown(){
        ActivityScenario.launch<NFCScannerActivity>(myIntent).use {
            onView(withId(R.id.nfcScanTitle)).check(matches(withText("NFC Scanner")))
        }
    }

    @get:Rule
    var nfcScannerActivityRule = ActivityScenarioRule(NFCScannerActivity::class.java)

    @Test
    fun correctHintText(){
        onView(withId(R.id.edit_message)).check(matches((withHint("type something"))))
    }

    @Test
    fun correctButton(){
        onView(withId(R.id.buttonReportItem)).check(matches(withText("activate tag")))
    }
//- - - - - - - - - - - - - - - - -these tests work on physical devices - - - - - - - - - - - - - -
*/
