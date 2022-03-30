package com.example.mobilepay.ui.mainPage

import android.Manifest
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupActionBarWithNavController
import com.example.mobilepay.databinding.ActivityMainPageBinding

class MainPageActivity: AppCompatActivity() {


    private lateinit var binding:ActivityMainPageBinding
     private lateinit var navController: NavController


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        ActivityCompat.requestPermissions(this,
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE),
            0);


        binding = ActivityMainPageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.hide()

        val navHostFragment = supportFragmentManager
            .findFragmentById(binding.fragmentContainerView.id) as NavHostFragment

        navController = navHostFragment.navController
        setupActionBarWithNavController(navController)
    }

    override fun onSupportNavigateUp(): Boolean {
        return super.onSupportNavigateUp() || navController.navigateUp()
    }






}