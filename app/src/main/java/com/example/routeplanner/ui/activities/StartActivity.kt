package com.example.routeplanner.ui.activities

import android.Manifest
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AccelerateInterpolator
import android.widget.*
import androidx.core.animation.doOnEnd
import com.example.routeplanner.R
import com.example.routeplanner.database.DBHelper
import com.example.routeplanner.database.JsonWebLoader

class StartActivity : AppCompatActivity() {
    private lateinit var db: DBHelper

    private lateinit var apiKEyLabel: TextView
    private lateinit var apiKeyEdit: EditText
    private lateinit var startButton: Button
    private lateinit var loadingImage: ImageView

    private var permissionsGranted: Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start)

        db = DBHelper(this.applicationContext)

        apiKEyLabel = findViewById<TextView>(R.id.api_key_label)
        apiKeyEdit = findViewById<EditText>(R.id.api_key_edit)
        startButton = findViewById<Button>(R.id.start_button)
        loadingImage = findViewById<ImageView>(R.id.loading_image)

        checkPermissions()

        setupGUI()
    }

    private fun checkPermissions() {
        val permissionsToRequest = mutableListOf<String>()
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.ACCESS_FINE_LOCATION)
        }
        if (checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.ACCESS_COARSE_LOCATION)
        }
        if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }

        if (permissionsToRequest.size > 0) {
            permissionsGranted = false
            requestPermissions(permissionsToRequest.toTypedArray(),
                permissionRequestCode
            )
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == permissionRequestCode) {
            if (grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                permissionsGranted = true
            } else {
                stopThisActivity()
            }
        }
    }

    private fun setupGUI() {
        apiKeyEdit.setText(db.getSetting(DBHelper.SETT_APIKEY_NAME))
        setStartButtonListener()
    }

    private fun setStartButtonListener() {
        startButton.setOnClickListener {
            val key: String = apiKeyEdit.text.toString()

            if (permissionsGranted) validateApiKey(key)
            else stopThisActivity()
        }
    }

    private fun validateApiKey(apiKey: String) {
        apiKEyLabel.visibility = View.GONE
        apiKeyEdit.visibility = View.GONE
        startButton.visibility = View.GONE
        loadingImage.visibility = View.VISIBLE

        val loadingRotateAnimator: ObjectAnimator = ObjectAnimator
                .ofFloat(loadingImage, "rotation", 0F, 360F)
                .setDuration(1500)
        loadingRotateAnimator.interpolator = AccelerateInterpolator()
        val loadingRotateAnimator2: ObjectAnimator = ObjectAnimator
                .ofFloat(loadingImage, "rotation", 0F, 360F)
                .setDuration(1500)
        loadingRotateAnimator2.interpolator = AccelerateDecelerateInterpolator()
        val loadingAnimation = AnimatorSet()
        loadingAnimation.play(loadingRotateAnimator)
                .before(loadingRotateAnimator2)
        loadingAnimation.doOnEnd {
            startMainActivity()
        }

        Thread {
            kotlin.run {
                if (JsonWebLoader.validateKey(apiKey)) {
                    runOnUiThread {
                        loadingAnimation.start()
                    }
                    db.setSetting(DBHelper.SETT_APIKEY_NAME, apiKey)
                } else {
                    runOnUiThread {
                        stopThisActivity()
                    }
                }
            }
        }.start()

    }

    private fun startMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun stopThisActivity() {
//        val intent = Intent(this, StartActivity::class.java)
//        startActivity(intent)
        Toast.makeText(this, "Unable to start", Toast.LENGTH_SHORT).show()
        finish()
    }

    companion object {
        private const val permissionRequestCode = 1
    }
}