package com.github.brugapp.brug.ui

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
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
import androidx.lifecycle.liveData
import com.github.brugapp.brug.R
import com.github.brugapp.brug.data.UserRepository
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers

class ProfileSettingsFragment(
    private val registry: ActivityResultRegistry
) : Fragment() {

    private fun resize(image: Drawable?): Drawable? {
        if(image == null) return null
        val b = (image as BitmapDrawable).bitmap
        val bitmapResized = Bitmap.createScaledBitmap(b, 200, 200, false)
        return BitmapDrawable(resources, bitmapResized)
    }

    private val getContent = registerForActivityResult(ActivityResultContracts.GetContent(),registry) { uri: Uri? ->
        if(uri != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.P){
            val inputStream = activity?.contentResolver?.openInputStream(uri)
            val drawable = Drawable.createFromStream(inputStream, uri.toString())
            activity?.let {
                liveData(Dispatchers.IO){
                    emit(UserRepository.updateUserIcon(Firebase.auth.currentUser!!.uid, drawable))
                }.observe(this){
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
        liveData(Dispatchers.IO){
            emit(UserRepository.getMinimalUserFromUID(Firebase.auth.currentUser!!.uid))
        }.observe(viewLifecycleOwner){ user ->
            view.findViewById<ProgressBar>(R.id.loadingUserProfile).visibility = View.GONE

            if(user == null){
                Snackbar.make(view, "ERROR: User cannot be retrieved !", Snackbar.LENGTH_LONG)
                    .show()
//                startActivity(Intent(this, SettingsActivity::class.java))

            } else {
                val profilePic = view.findViewById<ImageView>(R.id.imgProfile)
                val profilePicDrawable = Drawable.createFromPath(user.getUserIconPath())

                if(profilePicDrawable != null){
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
    }

}