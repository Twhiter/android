package com.example.mobilepay

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupActionBarWithNavController
import com.example.mobilepay.databinding.ActivityMainBinding
import com.example.mobilepay.room.roomEntity.KV
import com.example.mobilepay.ui.mainPage.MainPageActivity
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect

class MainActivity : AppCompatActivity() {

    private lateinit var viewBinding:ActivityMainBinding
    private lateinit var navController: NavController





    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        ActivityCompat.requestPermissions(this,
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE),
            0);


        viewBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        supportActionBar?.hide()

        val navHostFragment = supportFragmentManager
            .findFragmentById(viewBinding.fragmentContainerView.id) as NavHostFragment

        navController = navHostFragment.navController
        setupActionBarWithNavController(navController)

        runBlocking {
            if (MainApplication.db().KVDao().get("token") != null)
                    toMainPage(this@MainActivity)
        }

    }

    override fun onSupportNavigateUp(): Boolean {
        return super.onSupportNavigateUp() || navController.navigateUp()
    }

    companion object {

        fun toMainPage(activity:Activity) {
            val intent = Intent(activity,MainPageActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            activity.finish()
            activity.startActivity(intent)
        }
    }


}