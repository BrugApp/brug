package com.github.brugapp.brug.ui

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ProgressBar
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.liveData
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.brugapp.brug.ITEM_INTENT_KEY
import com.github.brugapp.brug.R
import com.github.brugapp.brug.USER_ID_INTENT_KEY
import com.github.brugapp.brug.data.ItemsRepository
import com.github.brugapp.brug.data.UserRepository
import com.github.brugapp.brug.ui.components.BottomNavBar
import com.github.brugapp.brug.ui.components.CustomTopBar
import com.github.brugapp.brug.view_model.ItemsListAdapter
import com.github.brugapp.brug.view_model.ItemsMenuViewModel
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject

private const val ITEMS_SEARCH_HINT: String = "Search items here…"
const val ITEMS_MOVE_TEXT: String = "Item has been moved."
const val ITEMS_DELETE_TEXT: String = "Item has been deleted."

@AndroidEntryPoint
class ItemsMenuActivity : AppCompatActivity() {

    private val viewModel: ItemsMenuViewModel by viewModels()

    @Inject
    lateinit var firestore: FirebaseFirestore

    @Inject
    lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_items_menu)

        initItemsList()
        initFloatingAddButton()
        BottomNavBar().initBottomBar(this)
    }




    // For the searchbar when pressing on the top bar's search icon
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        customTopBar.inflateTopBar(menuInflater, menu, ITEMS_SEARCH_HINT)
        return super.onCreateOptionsMenu(menu)
    }

    // For the settings icon on top bar
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        customTopBar.defineTopBarActions(item, this)
        return super.onOptionsItemSelected(item)
    }


    // ONLY GETS THE LIST OF ITEMS RELATED TO THE USER, NOT THE FULL USER PROFILE !
    private fun initItemsList() = liveData(Dispatchers.IO){
        emit(ItemsRepository.getUserItemsFromUID(firebaseAuth.currentUser!!.uid,firestore))
    }.observe(this) { itemsList ->
        findViewById<ProgressBar>(R.id.loadingItems).visibility = View.GONE
        val list = if(itemsList.isNullOrEmpty()) mutableListOf() else itemsList.toMutableList()

        val listView = findViewById<RecyclerView>(R.id.items_listview)
        val itemsListAdapter = ItemsListAdapter(list)
        { clickedItem ->
            val intent = Intent(this, ItemInformationActivity::class.java)
            intent.putExtra(ITEM_INTENT_KEY, clickedItem)
            startActivity(intent)
        }

        listView.layoutManager = LinearLayoutManager(this)

        val dragPair = Pair(
            ItemTouchHelper.UP.or(ItemTouchHelper.DOWN),
            ItemTouchHelper.LEFT.or(ItemTouchHelper.RIGHT)
        )

        val swipePair = Pair(
            ContextCompat.getDrawable(this, R.drawable.ic_baseline_delete_24) !!,
            ContextCompat.getColor(this, R.color.list_item_del_BG))

        val listAdapterPair = Pair(
            list,
            itemsListAdapter
        )

        val listCallback = viewModel.setCallback(
            this,
            dragPair,
            swipePair,
            listAdapterPair,
            firebaseAuth,
            firestore)
        ItemTouchHelper(listCallback).attachToRecyclerView(listView)
        listView.adapter = itemsListAdapter
    }

    private fun initFloatingAddButton(){
        val addButton = findViewById<FloatingActionButton>(R.id.add_new_item_button)

        addButton.setOnClickListener{
            val myIntent = Intent(this, AddItemActivity::class.java)
            startActivity(myIntent)
        }
    }

    companion object {
        private val customTopBar = CustomTopBar()
    }
}