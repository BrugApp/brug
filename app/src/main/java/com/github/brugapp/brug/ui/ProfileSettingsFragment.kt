package com.github.brugapp.brug.ui

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.viewModels
import com.github.brugapp.brug.R
import com.github.brugapp.brug.view_model.SettingsViewModel

class ProfileSettingsFragment(
    private val registry: ActivityResultRegistry
) : Fragment() {

    private val viewModel: SettingsViewModel by viewModels()

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
                viewModel.setProfilePic(resize(drawable))
            }
        }
        val myIntent = Intent(activity, ProfilePictureSetActivity::class.java)
        startActivity(myIntent)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.profile_page_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val profilePic = view.findViewById<ImageView>(R.id.imgProfile)
        val profilePicDrawable = viewModel.getProfilePic()

        if(profilePicDrawable != null){
            profilePic.setImageDrawable(profilePicDrawable)
        }else{
            profilePic.setImageResource(R.drawable.ic_person_outline_black_24dp)
        }

        val username = view.findViewById<TextView>(R.id.username)
        username.text = viewModel.getUsername()

        val button = view.findViewById<Button>(R.id.loadButton)
        button.setOnClickListener {
            getContent.launch("image/*")
        }


    }

}