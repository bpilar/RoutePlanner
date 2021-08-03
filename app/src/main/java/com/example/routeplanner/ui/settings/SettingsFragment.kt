package com.example.routeplanner.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.Fragment
import com.example.routeplanner.R
import com.example.routeplanner.database.DBHelper

class SettingsFragment : Fragment() {
    private lateinit var db: DBHelper
    private lateinit var root: View
    private lateinit var timeDistAccEdit: EditText
    private lateinit var speedEdit: EditText
    private lateinit var apiKeyEdit: EditText
    private lateinit var saveButton: Button

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        root = inflater.inflate(R.layout.fragment_settings, container, false)
        db = DBHelper(requireContext())
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        timeDistAccEdit = root.findViewById<EditText>(R.id.time_dist_acc_edit)
        speedEdit = root.findViewById<EditText>(R.id.speed_edit)
        apiKeyEdit = root.findViewById<EditText>(R.id.api_key_edit)
        saveButton = root.findViewById<Button>(R.id.save_button)

        restoreSettings()

        saveButton.setOnClickListener { saveSettings() }
    }

    private fun saveSettings() {
        val timeDistAcc = timeDistAccEdit.text.toString().toInt()
        val speed = speedEdit.text.toString().toDouble()
        val apiKey = apiKeyEdit.text.toString()
        db.setSetting(DBHelper.SETT_TIMEDISTACC_NAME, timeDistAcc.toString())
        db.setSetting(DBHelper.SETT_SPEED_NAME, speed.toString())
        db.setSetting(DBHelper.SETT_APIKEY_NAME, apiKey)
    }

    private fun restoreSettings() {
        val timeDistAcc = db.getSetting(DBHelper.SETT_TIMEDISTACC_NAME)
        val speed = db.getSetting(DBHelper.SETT_SPEED_NAME)
        val apiKey = db.getSetting(DBHelper.SETT_APIKEY_NAME)
        timeDistAccEdit.setText(timeDistAcc)
        speedEdit.setText(speed)
        apiKeyEdit.setText(apiKey)
    }
}