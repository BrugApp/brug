package com.github.brugapp.brug

import com.github.brugapp.brug.model.User
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.IsEqual
import org.hamcrest.core.IsNot
import org.hamcrest.core.IsNull
import org.junit.Test

class UserTest {
    @Test
    fun initUserWithoutIconCorrectlyInitializesUser() {
        val userID = "DUMMYID"
        val firstName = "Rayan"
        val lastName = "Kikou"
        val userIconPath: String? = null

        val user = User(userID, firstName, lastName, userIconPath, mutableListOf())
        assertThat(user.uid, IsEqual(userID))
        assertThat(user.firstName, IsEqual(firstName))
        assertThat(user.lastName, IsEqual(lastName))
        assertThat(user.getUserIconPath(), IsNull.nullValue())
    }

    @Test
    fun comparingTwoIdenticalUsersReturnsEquality() {
        val user1 = User("DUMMYID", "Rayan", "Kikou", null, mutableListOf())
        val user2 = User("DUMMYID", "Rayan", "Kikou", null, mutableListOf())
        assertThat(user1, IsEqual(user2))
    }

    @Test
    fun comparingTwoAlmostIdenticalUsersReturnsFalse() {
        val user1 = User("DUMMYID", "Rayan", "Kikou", null, mutableListOf())
        val user2 = User("DUMMYID2", "Rayan", "Kikou", null, mutableListOf())
        assertThat(user1, IsNot(IsEqual(user2)))
    }
}