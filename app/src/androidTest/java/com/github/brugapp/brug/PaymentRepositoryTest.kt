package com.github.brugapp.brug

import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.brugapp.brug.data.PaymentRepository
import org.hamcrest.MatcherAssert
import org.hamcrest.core.IsNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PaymentRepositoryTest {

    @Test
    fun paymentRequestTest(){
        val item = PaymentRepository().paymentRequest()
        MatcherAssert.assertThat(item, IsNull.notNullValue())
    }


}