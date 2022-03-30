package com.example.mobilepay.ui.mainPage

import android.Manifest
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupActionBarWithNavController
import com.example.mobilepay.R
import com.example.mobilepay.databinding.ActivityMainPageBinding

class MainPageActivity: AppCompatActivity() {


    private lateinit var binding:ActivityMainPageBinding
    private lateinit var navController: NavController
    private var selectedFramentId:Int = -1


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

        binding.bottomNavigation.setOnItemSelectedListener { item ->

            Log.d("Mainss","change ?")
           val resId =  when(item.itemId) {
                R.id.home -> R.id.homeFragment
                R.id.personalDeal -> R.id.personalDealFrament
                R.id.businessDeal -> R.id.businessDealFragment
               else -> {-1}
           }
                navController.navigate(resId)
                selectedFramentId = resId
                true
        }

        binding.bottomNavigation.setOnItemReselectedListener {
            Log.d("Mainss","not change?")
        }


    }

    override fun onSupportNavigateUp(): Boolean {
        return super.onSupportNavigateUp() || navController.navigateUp()
    }






}