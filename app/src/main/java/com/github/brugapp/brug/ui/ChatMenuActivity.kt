package com.github.brugapp.brug.ui

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.LinearLayout
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.liveData
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.brugapp.brug.DUMMY_TEXT
import com.github.brugapp.brug.R
import com.github.brugapp.brug.data.ConvRepo
import com.github.brugapp.brug.ui.components.BottomNavBar
import com.github.brugapp.brug.ui.components.CustomTopBar
import com.github.brugapp.brug.view_model.ChatMenuViewModel
import com.github.brugapp.brug.view_model.ConversationListAdapter
import com.github.brugapp.brug.view_model.ListCallback
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers

const val CHAT_SEARCH_HINT: String = "Search for a conversation…"
const val CHAT_CHECK_TEXT: String = "The conversation has been marked as resolved."

const val CHAT_INTENT_KEY = "conversation"


class ChatMenuActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_menu)

        initChatList()
        BottomNavBar().initBottomBar(this)
    }

    // Initializing the top-bar
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        customTopBar.inflateTopBar(menuInflater, menu, CHAT_SEARCH_HINT)
        return super.onCreateOptionsMenu(menu)
    }

    // For the settings icon on top bar
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        customTopBar.defineTopBarActions(window.decorView, DUMMY_TEXT, item, this)
        return super.onOptionsItemSelected(item)
    }

    // ONLY GETS THE LIST OF CONVERSATIONS RELATED TO THE USER, NOT THE FULL USER PROFILE !
    private fun initChatList() = liveData(Dispatchers.IO){
        emit(ConvRepo.getUserConvFromUID(Firebase.auth.currentUser!!.uid))
    }.observe(this) { list ->
        val conversations = if(list.isNullOrEmpty()) mutableListOf() else list.toMutableList()

        val listView = findViewById<RecyclerView>(R.id.chat_listview)
        val listViewAdapter = ConversationListAdapter(conversations) { clickedConv ->
            val intent = Intent(this, ChatActivity::class.java)
            intent.putExtra(CHAT_INTENT_KEY, clickedConv)
            startActivity(intent)
        }

        listView.layoutManager = LinearLayoutManager(this)
        val dragPair = Pair(
            0, ItemTouchHelper.LEFT.or(ItemTouchHelper.RIGHT)
        )

        val swipePair = Pair(
            ContextCompat.getDrawable(this, R.drawable.ic_baseline_check_circle_outline_24)!!,
            ContextCompat.getColor(this, R.color.chat_list_resolve_BG))

        val listAdapterPair = Pair(
            conversations,
            listViewAdapter
        )

        val listCallback = ListCallback(CHAT_CHECK_TEXT, dragPair, swipePair, listAdapterPair){ delConv ->
            liveData(Dispatchers.IO){
                emit(
                    ConvRepo.deleteConversationFromID(delConv.convId, Firebase.auth.currentUser!!.uid))
            }.observe(this){ response ->
                if(response.onSuccess){
                    val position = conversations.indexOf(delConv)
                    conversations.removeAt(position)
                    listViewAdapter.notifyItemRemoved(position)

                    Snackbar.make(listView,
                        "Conversation has successfully been marked as resolved.",
                        Snackbar.LENGTH_LONG).setAction("Undo") {
                        liveData(Dispatchers.IO){
                            emit(
                                ConvRepo.addNewConversation(Firebase.auth.currentUser!!.uid, delConv.userFields.uid, delConv.lostItemName)
                            )}.observe(this){
                            if(!response.onSuccess){
                                Snackbar.make(listView,
                                    "ERROR: Unable to re-add requested conversation to database",
                                    Snackbar.LENGTH_LONG).show()
                            }
                        }

                        conversations.add(position, delConv)
                        listViewAdapter.notifyItemInserted(position)
                    }.show()
                } else {
                    Snackbar.make(listView,
                        "ERROR: Unable to delete requested item from database",
                        Snackbar.LENGTH_LONG).show()
                }
            }
        }
        ItemTouchHelper(listCallback).attachToRecyclerView(listView)
        listView.adapter = listViewAdapter
        listView.addItemDecoration(DividerItemDecoration(this, LinearLayout.VERTICAL))
    }

    companion object {
        private val customTopBar = CustomTopBar()
    }

}
