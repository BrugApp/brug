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
import com.github.brugapp.brug.R
import com.github.brugapp.brug.model.Conversation
import com.github.brugapp.brug.view_model.ChatViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices

class ChatActivity : AppCompatActivity() {

    private val viewModel: ChatViewModel by viewModels()
    private val RECORDING_REQUEST_CODE = 3000 // Do we have to keep this?
    private val STORAGE_REQUEST_CODE = 2000 // Do we have to keep this?
    private val TAKE_PICTURE_REQUEST_CODE = 10
    private val SELECT_PICTURE_REQUEST_CODE = 1

    private lateinit var buttonSendTextMessage: ImageButton
    private lateinit var recordButton: RecordButton
    private lateinit var messageLayout: LinearLayout
    private lateinit var audioRecMessage: TextView
    private lateinit var buttonSendAudio: ImageButton
    private lateinit var deleteAudio: ImageButton
    private lateinit var textMessage: EditText

    private lateinit var conversation: Conversation

    @SuppressLint("CutPasteId") // Needed as we read values from EditText fields
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        val model: ChatViewModel by viewModels()

        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        buttonSendTextMessage = findViewById(R.id.buttonSendMessage)

        initMessageList(model)
        initSendMessageButton(model)
        initSendLocationButton(model, locationManager, fusedLocationClient)
        initSendImageCameraButton(model)
        initSendImageButton(model)

        messageLayout = findViewById(R.id.messageLayout)
        audioRecMessage = findViewById(R.id.audioRecording)
        buttonSendAudio = findViewById(R.id.buttonSendAudio)

        recordButton = findViewById(R.id.recordButton)
        model.setListenForRecord(recordButton, false)
        initRecordButton(model)

        deleteAudio = findViewById(R.id.deleteAudio)
        initDeleteAudioButton(model)

        textMessage = findViewById(R.id.editMessage)
        initTextInputField()

        initSendAudioButton(model)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        return super.onCreateOptionsMenu(menu)
    }

    private fun initMessageList(model: ChatViewModel) {
        conversation = intent.getSerializableExtra(CHAT_INTENT_KEY) as Conversation

        val messageList = findViewById<RecyclerView>(R.id.messagesList)
        model.initViewModel(conversation.messages)
        messageList.layoutManager = LinearLayoutManager(this)
        messageList.adapter = model.getAdapter()

        scrollToBottom((model.getAdapter().itemCount) - 1)

        inflateActionBar(
            conversation.userFields.getFullName(), conversation.lostItemName
        )
    }

    private fun inflateActionBar(username: String, itemLost: String) {
        supportActionBar!!.title = username
        supportActionBar!!.subtitle = "Related to the item \"$itemLost\""
    }

    private fun initSendMessageButton(model: ChatViewModel) {
        buttonSendTextMessage.setOnClickListener {
            val content: String = findViewById<TextView>(R.id.editMessage).text.toString()
            // Clear the message field
            this.findViewById<TextView>(R.id.editMessage).text = ""
            model.sendMessage(content, conversation.convId, this)
        }
    }

    private fun initSendImageCameraButton(model: ChatViewModel) {
        val buttonSendMessage = findViewById<ImageButton>(R.id.buttonSendImagePerCamera)
        buttonSendMessage.setOnClickListener {
            viewModel.takeCameraImage(this)
        }
    }

    private fun initSendImageButton(model: ChatViewModel) {
        val buttonSendMessage = findViewById<ImageButton>(R.id.buttonSendImage)
        buttonSendMessage.setOnClickListener {
            viewModel.selectGalleryImage(this)
        }
    }

    private fun initSendLocationButton(
        model: ChatViewModel,
        locationManager: LocationManager,
        fusedLocationClient: FusedLocationProviderClient
    ) {
        val buttonSendLocalisation = findViewById<ImageButton>(R.id.buttonSendLocalisation)
        buttonSendLocalisation.setOnClickListener {
            model.requestLocation(this, fusedLocationClient, locationManager)
        }
    }

    //TODO: Implement this (@Hamza)
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
            model.sendAudio()

            buttonSendAudio.visibility = View.GONE
            deleteAudio.visibility = View.GONE
            audioRecMessage.visibility = View.GONE
            messageLayout.visibility = View.VISIBLE
            recordButton.visibility = View.VISIBLE
            model.setListenForRecord(recordButton, false)

            model.sendMessage(
                "Audio sent, will be able to listen soon ...!",
                conversation.convId,
                this
            )
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

        if (requestCode == TAKE_PICTURE_REQUEST_CODE && resultCode == RESULT_OK) {
            Toast.makeText(this, "Image taken", Toast.LENGTH_SHORT).show()
        } else if (requestCode == SELECT_PICTURE_REQUEST_CODE && resultCode == RESULT_OK) {
            Toast.makeText(this, "Image selected", Toast.LENGTH_SHORT).show()
        }

        val imageUri = data?.extras?.getString("imageUri")
        if (imageUri != null) {
            // this will be the case for the gallery image
            // camera images returns null as extras
            println("=== URI set ===")
            viewModel.setImageUri(Uri.parse(imageUri))
        }
        viewModel.uploadImage(this)
    }
}