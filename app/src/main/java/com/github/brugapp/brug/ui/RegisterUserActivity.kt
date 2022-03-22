package com.github.brugapp.brug.ui

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.github.brugapp.brug.R
import com.github.brugapp.brug.view_model.RegisterUserViewModel


class RegisterUserActivity : AppCompatActivity(), View.OnClickListener {

    private val viewModel: RegisterUserViewModel by viewModels()
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register_user)
        val regUser = findViewById<Button>(R.id.registerbutton)
        regUser.setOnClickListener(this)
        progressBar = findViewById(R.id.progressBar)
    }

    override fun onClick(v: View?) { //we clicked the register button
        viewModel.storeUserInput(
            findViewById(R.id.firstname),
            findViewById(R.id.lastName),
            findViewById(R.id.PasswordReg),
            findViewById(R.id.emailAddressReg)
        )
        if (!viewModel.anyEmpty(findViewById(R.id.firstname),
                findViewById(R.id.lastName),
                findViewById(R.id.PasswordReg),
                findViewById(R.id.emailAddressReg))) {
            onClickHelper()
        }
    }

    private fun onClickHelper() {
        progressBar.visibility = View.VISIBLE
        //make an authentication account with email password tuple
        viewModel.createAuthAccount(this@RegisterUserActivity, progressBar)
    }
}