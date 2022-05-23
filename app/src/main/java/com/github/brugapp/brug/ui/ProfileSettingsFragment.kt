package com.github.brugapp.brug.ui

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.github.brugapp.brug.R
import com.github.brugapp.brug.data.BrugDataCache
import com.github.brugapp.brug.data.UserRepository
import com.github.brugapp.brug.model.User
import com.github.brugapp.brug.view_model.ProfileSettingsViewModel
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ProfileSettingsFragment(
    private val registry: ActivityResultRegistry,
    private val firebaseAuth: FirebaseAuth,
    private val firebaseStorage: FirebaseStorage,
    private val firestore: FirebaseFirestore
) : Fragment() {

    private val viewModel: ProfileSettingsViewModel by viewModels()

    private val getContent =
        registerForActivityResult(ActivityResultContracts.GetContent(), registry) { uri: Uri? ->
            if (uri != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val inputStream = activity?.contentResolver?.openInputStream(uri)
                val drawable = Drawable.createFromStream(inputStream, uri.toString())
                activity?.let {
                    liveData(Dispatchers.IO) {
                        emit(
                            UserRepository.updateUserIcon(
                                firebaseAuth.currentUser!!.uid,
                                drawable,
                                firebaseAuth,
                                firebaseStorage,
                                firestore
                            )
                        )
                    }.observe(this) {
                        val myIntent = Intent(activity, ProfilePictureSetActivity::class.java)
                        startActivity(myIntent)
                    }
                }
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.profile_page_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewModel.viewModelScope.launch {
            val user = UserRepository.getUserFromUID(
                firebaseAuth.currentUser!!.uid,
                firestore,
                firebaseAuth,
                firebaseStorage
            )
            if (user != null) {
                BrugDataCache.setUserInCache(user)
            }
        }

        BrugDataCache.getCachedUser().observe(viewLifecycleOwner){ user ->
            view.findViewById<ProgressBar>(R.id.loadingUserProfile).visibility = View.GONE
            modifyPicture(user, view)
        }
    }

    private fun modifyPicture(user: User?, view: View) {
        if (user == null) {
            Snackbar.make(view, "ERROR: User cannot be retrieved !", Snackbar.LENGTH_LONG)
                .show()
        } else {
            val profilePic = view.findViewById<ImageView>(R.id.imgProfile)
            val profilePicDrawable = Drawable.createFromPath(user.getUserIconPath())

            if (profilePicDrawable != null) {
                Log.d("ProfileSettingsFragment", "Drawable is not null")
                profilePic.setImageDrawable(resize(profilePicDrawable))
            } else {
                profilePic.setImageResource(R.mipmap.ic_launcher_round)
            }

            val username = view.findViewById<TextView>(R.id.username)
            username.text = user.getFullName()

            val button = view.findViewById<Button>(R.id.loadButton)
            button.setOnClickListener {
                getContent.launch("image/*")
            }
        }
    }

    private fun resize(image: Drawable?): Drawable? {
        Log.d("ProfilePageFragment", "resize")
        if (image == null) return null
        val b = (image as BitmapDrawable).bitmap
        val bitmapResized = Bitmap.createScaledBitmap(b, 200, 200, false)
        return BitmapDrawable(resources, bitmapResized)
    }

}