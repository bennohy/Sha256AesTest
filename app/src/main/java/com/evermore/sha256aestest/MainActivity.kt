package com.evermore.sha256aestest

import android.os.Bundle
import android.util.Log
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import android.view.Menu
import android.view.MenuItem
import com.evermore.sha256aestest.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding

    private val TAG = MainActivity::class.java.simpleName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.samekey.isEnabled = AESUtil.getSavedSecretKeyAsString(this) != "no_key"

        binding.encrypt.setOnClickListener {
            var rawdata = binding.rawData.text.toString()
            var ivString = binding.ivparam.text.toString().run {
                this.ifEmpty {
                    null
                }
            }
            Log.d(TAG, "ivString > $ivString")
            if (rawdata.isNotEmpty()) {
                val encValue = AESUtil.encrypt(this, rawdata, binding.samekey.isChecked, ivString)
                binding.encValue.text = encValue
                binding.key.text = AESUtil.getSavedSecretKeyAsString(this)
                binding.ivspec.text = AESUtil.getSavedInitializationVectorAsString(this)
            }
        }
        binding.decrypt.setOnClickListener {
            var encData = binding.encValue.text.toString()
            if (encData.isNotEmpty()) {
                val decValue = AESUtil.decrypt(this, encData)
                binding.decValue.text = decValue
            }
        }

        binding.clear.setOnClickListener {
            binding.rawData.setText("")
            binding.encValue.text = ""
            binding.decValue.text = ""
            binding.ivspec.text = ""
            binding.key.text = ""
        }
    }
}