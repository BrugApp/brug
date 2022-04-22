package com.github.brugapp.brug

import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import androidx.test.core.app.ApplicationProvider
import com.github.brugapp.brug.model.MyUser
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.IsEqual
import org.hamcrest.core.IsNot
import org.hamcrest.core.IsNull
import org.junit.Test

class MyUserTest {
    @Test
    fun initUserWithoutIconCorrectlyInitializesUser() {
        val userID = "DUMMYID"
        val firstName = "Rayan"
        val lastName = "Kikou"
        val userIcon: Drawable? = null

        val user = MyUser(userID, firstName, lastName, userIcon)
        assertThat(user.uid, IsEqual(userID))
        assertThat(user.firstName, IsEqual(firstName))
        assertThat(user.lastName, IsEqual(lastName))
        assertThat(user.getUserIcon(), IsNull.nullValue())
    }

    @Test
    fun initUserWithIconCorrectlyInitializesUser() {
        val userID = "DUMMYID"
        val firstName = "Rayan"
        val lastName = "Kikou"
        val userIcon: Drawable? = ContextCompat.getDrawable(ApplicationProvider.getApplicationContext(), R.mipmap.ic_launcher)

        val user = MyUser(userID, firstName, lastName, userIcon)
        assertThat(user.uid, IsEqual(userID))
        assertThat(user.firstName, IsEqual(firstName))
        assertThat(user.lastName, IsEqual(lastName))
        assertThat(user.getUserIcon(), IsNot(IsNull.nullValue()))
        assertThat(user.getUserIcon().toString(), IsEqual(userIcon.toString()))
    }

    @Test
    fun comparingTwoIdenticalUsersReturnsEquality() {
        val user1 = MyUser("DUMMYID", "Rayan", "Kikou", null)
        val user2 = MyUser("DUMMYID", "Rayan", "Kikou", null)
        assertThat(user1, IsEqual(user2))
    }

    @Test
    fun comparingTwoAlmostIdenticalUsersReturnsFalse() {
        val user1 = MyUser("DUMMYID", "Rayan", "Kikou", null)
        val user2 = MyUser("DUMMYID2", "Rayan", "Kikou", null)
        assertThat(user1, IsNot(IsEqual(user2)))
    }
}