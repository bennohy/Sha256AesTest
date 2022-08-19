package com.evermore.sha256aestest

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.ui.AppBarConfiguration
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

        binding.sha256.setOnClickListener {
            var rawdata = binding.rawData.text.toString()
            if (rawdata.isNotEmpty()) {
                val hashValue = AESUtil.toSha256(rawdata)
                binding.hashValue.text = hashValue
            }
        }

        binding.clear.setOnClickListener {
            binding.rawData.setText("")
            binding.encValue.text = ""
            binding.hashValue.text = ""
            binding.decValue.text = ""
            binding.ivspec.text = ""
            binding.key.text = ""
        }
    }
}