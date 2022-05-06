package com.github.brugapp.brug.ui

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Menu
import android.view.View
import android.widget.*
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.devlomi.record_view.RecordButton
import com.github.brugapp.brug.PIC_ATTACHMENT_INTENT_KEY
import com.github.brugapp.brug.R
import com.github.brugapp.brug.SELECT_PICTURE_REQUEST_CODE
import com.github.brugapp.brug.TAKE_PICTURE_REQUEST_CODE
import com.github.brugapp.brug.model.ChatMessagesListAdapter
import com.github.brugapp.brug.model.Conversation
import com.github.brugapp.brug.model.message_types.TextMessage
import com.github.brugapp.brug.model.services.DateService
import com.github.brugapp.brug.view_model.ChatViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import java.time.LocalDateTime


class ChatActivity : AppCompatActivity() {

    private val viewModel: ChatViewModel by viewModels()
    private lateinit var convID: String

    private lateinit var buttonSendTextMessage: ImageButton
    private lateinit var recordButton: RecordButton
    private lateinit var messageLayout: LinearLayout
    private lateinit var audioRecMessage: TextView
    private lateinit var buttonSendAudio: ImageButton
    private lateinit var deleteAudio: ImageButton
    private lateinit var textMessage: EditText

    @SuppressLint("CutPasteId") // Needed as we read values from EditText fields
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        val conversation = intent.getSerializableExtra(CHAT_INTENT_KEY) as Conversation
        convID = conversation.convId

        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        buttonSendTextMessage = findViewById(R.id.buttonSendMessage)

        initMessageList(conversation)
        initSendTextMessageButton()
        initSendLocationButton(locationManager, fusedLocationClient)
        initSendImageCameraButton()
        initSendImageButton()

        messageLayout = findViewById(R.id.messageLayout)
        audioRecMessage = findViewById(R.id.audioRecording)
        buttonSendAudio = findViewById(R.id.buttonSendAudio)

        recordButton = findViewById(R.id.recordButton)
        viewModel.setListenForRecord(recordButton, false)
        initRecordButton(viewModel)

        deleteAudio = findViewById(R.id.deleteAudio)
        initDeleteAudioButton(viewModel)

        textMessage = findViewById(R.id.editMessage)
        initTextInputField()

        initSendAudioButton(viewModel)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        return super.onCreateOptionsMenu(menu)
    }

    private fun initMessageList(conversation: Conversation) {
        val messageList = findViewById<RecyclerView>(R.id.messagesList)
        viewModel.initViewModel(conversation.messages, this)
        messageList.layoutManager = LinearLayoutManager(this)

        val adapter = viewModel.getAdapter()
        messageList.adapter = adapter

        adapter.setOnItemClickListener(object : ChatMessagesListAdapter.onItemClickListener {
            override fun onItemClick(position: Int) {
                if (adapter.getItemViewType(position) == ChatMessagesListAdapter.MessageType.TYPE_LOCATION_RIGHT.ordinal ||
                    adapter.getItemViewType(position) == ChatMessagesListAdapter.MessageType.TYPE_LOCATION_LEFT.ordinal
                )
                    Toast.makeText(this@ChatActivity, "Map pressed", Toast.LENGTH_SHORT).show()
            }
        })

        scrollToBottom((adapter.itemCount) - 1)

        inflateActionBar(
            conversation.userFields.getFullName(), conversation.lostItemName
        )
    }

    private fun inflateActionBar(username: String, itemLost: String) {
        supportActionBar!!.title = username
        supportActionBar!!.subtitle = "Related to the item \"$itemLost\""
    }

    private fun initSendTextMessageButton() {
        val buttonSendTextMsg = findViewById<ImageButton>(R.id.buttonSendMessage)
        buttonSendTextMsg.setOnClickListener {
            // Get elements from UI
            val content: String = findViewById<TextView>(R.id.editMessage).text.toString()
            val newMessage = TextMessage(
                "Me",
                DateService.fromLocalDateTime(LocalDateTime.now()),
                content
            )
            viewModel.sendMessage(newMessage, convID, this)

            // Clear the message field
            this.findViewById<TextView>(R.id.editMessage).text = ""

        }
    }

    private fun initSendImageCameraButton() {
        // SEND IMAGE CAMERA BUTTON
        val buttonSendMessage = findViewById<ImageButton>(R.id.buttonSendImagePerCamera)
        buttonSendMessage.setOnClickListener {
            viewModel.takeCameraImage(this)
        }
    }

    private fun initSendImageButton() {
        // SEND IMAGE BUTTON (from gallery)
        val buttonSendMessage = findViewById<ImageButton>(R.id.buttonSendImage)
        buttonSendMessage.setOnClickListener {
            viewModel.selectGalleryImage(this)

        }
    }

    private fun initSendLocationButton(
        locationManager: LocationManager,
        fusedLocationClient: FusedLocationProviderClient
    ) {
        val buttonSendLocationMsg = findViewById<ImageButton>(R.id.buttonSendLocalisation)
        buttonSendLocationMsg.setOnClickListener {
            viewModel.requestLocation(
                convID,
                this,
                fusedLocationClient,
                locationManager
            )
        }
    }

    /* Function to test if device has a microphone,
    private fun hasMicrophone(): Boolean {
        val pmanager = this.packageManager
        return pmanager.hasSystemFeature(
            PackageManager.FEATURE_MICROPHONE)
    }*/


    /*override fun onRequestPermissionsResult( // This should go to the bottom with the same function
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode == RECORDING_REQUEST_CODE){
            recordButton.isEnabled = true
        }
    }*/

    private fun initRecordButton(model: ChatViewModel) {
        recordButton.setOnClickListener {

            model.setListenForRecord(recordButton, true)

            if (model.isAudioPermissionOk(this) && model.isExtStorageOk(this)) {

                model.setupRecording()

                messageLayout.visibility = View.GONE
                recordButton.visibility = View.GONE
                buttonSendAudio.visibility = View.VISIBLE
                deleteAudio.visibility = View.VISIBLE
                audioRecMessage.visibility = View.VISIBLE

            } else if (model.isAudioPermissionOk(this)) {
                model.requestExtStorage(this)
            } else if (model.isExtStorageOk(this)) {
                model.requestRecording(this)
            } else {
                model.requestRecording(this)
                model.requestExtStorage(this)
            }
        }
    }

    private fun initDeleteAudioButton(model: ChatViewModel) {
        deleteAudio.setOnClickListener {
            model.deleteAudio()

            buttonSendAudio.visibility = View.GONE
            deleteAudio.visibility = View.GONE
            audioRecMessage.visibility = View.GONE
            messageLayout.visibility = View.VISIBLE
            recordButton.visibility = View.VISIBLE
            model.setListenForRecord(recordButton, false)
        }
    }

    private fun initSendAudioButton(model: ChatViewModel) {
        buttonSendAudio.setOnClickListener {
            model.stopAudio()

            buttonSendAudio.visibility = View.GONE
            deleteAudio.visibility = View.GONE
            audioRecMessage.visibility = View.GONE
            messageLayout.visibility = View.VISIBLE
            recordButton.visibility = View.VISIBLE
            model.setListenForRecord(recordButton, false)

            model.sendAudio()
        }
    }

    private fun initTextInputField() {
        textMessage.addTextChangedListener(object : TextWatcher {

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

                if (s.toString().trim().isEmpty()) {
                    buttonSendTextMessage.visibility = View.GONE
                    recordButton.visibility = View.VISIBLE
                } else {
                    recordButton.visibility = View.GONE
                    buttonSendTextMessage.visibility = View.VISIBLE
                }
            }

            override fun afterTextChanged(s: Editable?) {
            }
        })
    }

    fun scrollToBottom(position: Int) {
        val rv = findViewById<View>(R.id.messagesList) as RecyclerView
        rv.smoothScrollToPosition(position)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            if (requestCode == SELECT_PICTURE_REQUEST_CODE) {
                val imageUri =
                    data!!.data ?: Uri.parse(data.extras?.getString(PIC_ATTACHMENT_INTENT_KEY))
                viewModel.setImageUri(imageUri)
                viewModel.sendPicMessage(this, convID)
            } else if (requestCode == TAKE_PICTURE_REQUEST_CODE) {
                // for the tests (use of stubs that store uri as extra)
                val uriString = data?.extras?.getString(PIC_ATTACHMENT_INTENT_KEY)
                if (uriString != null) {
                    viewModel.setImageUri(Uri.parse(uriString))
                }
                viewModel.sendPicMessage(this, convID)
            }
        }
    }
}