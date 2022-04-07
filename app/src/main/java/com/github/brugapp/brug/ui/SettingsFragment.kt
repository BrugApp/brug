package com.github.brugapp.brug.ui

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.viewModels
import com.github.brugapp.brug.R
import com.github.brugapp.brug.view_model.SettingsViewModel

class SettingsFragment(
    private val registry: ActivityResultRegistry
) : Fragment() {

    private val viewModel: SettingsViewModel by viewModels()

    private fun resize(image: Drawable?): Drawable? {
        if(image == null) return null
        val b = (image as BitmapDrawable).bitmap
        val bitmapResized = Bitmap.createScaledBitmap(b, 80, 80, false)
        return BitmapDrawable(resources, bitmapResized)
    }

    private val getContent = registerForActivityResult(ActivityResultContracts.GetContent(),registry) { uri: Uri? ->
        if(uri != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.P){
            val inputStream = activity?.contentResolver?.openInputStream(uri)
            val drawable = Drawable.createFromStream(inputStream, uri.toString())
            activity?.let {
                Log.d("temp", "I'm begin in $drawable")
                viewModel.setProfilePic(resize(drawable))
            }
        }
        val myIntent = Intent(activity, SettingsActivity::class.java)
        startActivity(myIntent)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.settings_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val button = view.findViewById<Button>(R.id.loadButton)

        button.setOnClickListener {
            getContent.launch("image/*")
        }


        view.findViewById<Button>(R.id.sign_out_button).setOnClickListener {
            signOut()
        }
    }

    private fun signOut() {
        val myIntent = Intent(activity, SignInActivity::class.java).apply {
            putExtra(EXTRA_SIGN_OUT, true)
        }
        startActivity(myIntent)
    }
}