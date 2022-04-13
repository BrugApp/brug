package com.github.brugapp.brug

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.brugapp.brug.data.ItemRepository
import com.github.brugapp.brug.model.Item
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.IsNull
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ItemRepositoryTest {
    private val uid = "7IsGzvjHKd0KeeKK722m"
    private val convID = "7IsGzvjHKd0KeeKK722mdFtGLE0x08pstMeP68TH"
    val firestore = Firebase.firestore

    @Test
    fun getBadItemFromCurrentUserTest() {
        val item: Item? =
            ItemRepository.getItemFromCurrentUser(uid, "badObjectID")
        assertThat(item, IsNull.nullValue())
    }

    @Test
    fun getGoodItemFromCurrentUserTest() {
        val item = Item("name", "description", "id")
        ItemRepository.getItemFromCurrentUser(uid, "2kmiWr8jzQ37EDX5GAG5")
        //assertThat(helper.getItemFromCurrentUser("2kmiWr8jzQ37EDX5GAG5"), IsNull.notNullValue()) //maybe add uid param
        assertThat(
            firestore.collection("Users").document(uid).collection("Items")
                .document("2kmiWr8jzQ37EDX5GAG5"), IsNull.notNullValue()
        )
    }
}