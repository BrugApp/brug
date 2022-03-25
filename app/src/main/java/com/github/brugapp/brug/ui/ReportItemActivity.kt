package com.github.brugapp.brug.ui

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.github.brugapp.brug.R
import com.github.brugapp.brug.messaging.MyFCMMessagingService

class ReportItemActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_report_item)

        findViewById<Button>(R.id.report_item_button).setOnClickListener {
            displayReportNotification()
        }
    }

    private fun displayReportNotification() {
        MyFCMMessagingService.sendNotification(this, "Item found",
            "One of your Items was found !")
    }
}