package com.example.mobilepay.ui.mainPage

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupActionBarWithNavController
import com.example.mobilepay.MainActivity
import com.example.mobilepay.MainApplication
import com.example.mobilepay.R
import com.example.mobilepay.Util
import com.example.mobilepay.databinding.ActivityMainPageBinding
import kotlinx.coroutines.*


class MainPageActivity : AppCompatActivity() {


    private lateinit var binding: ActivityMainPageBinding
    private lateinit var navController: NavController
    private var selectedFramentId: Int = -1
    private var updateJob: Job? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        INSTANCE = this

        ActivityCompat.requestPermissions(this,
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE),
            0)

        binding = ActivityMainPageBinding.inflate(layoutInflater)
        setContentView(binding.root)


        supportActionBar?.hide()

        val navHostFragment = supportFragmentManager
            .findFragmentById(binding.fragmentContainerView.id) as NavHostFragment

        navController = navHostFragment.navController
        setupActionBarWithNavController(navController)

        binding.bottomNavigation.setOnItemSelectedListener { item ->


            val resId = when (item.itemId) {
                R.id.home -> R.id.homeFragment
                R.id.personalDeal -> R.id.personalDealFrament
                R.id.businessDeal -> R.id.businessDealFragment
                else -> {
                    -1
                }
            }
            navController.navigate(resId)
            selectedFramentId = resId
            true
        }

        binding.bottomNavigation.setOnItemReselectedListener {}

        binding.userFloatingBtn.setOnClickListener {
            navController.navigate(R.id.settingFragment)
        }


    }

    override fun onResume() {
        super.onResume()
        if (updateJob == null) {
            updateJob = lifecycleScope.launch(Dispatchers.Default) {
                while (true) {
                    withContext(Dispatchers.IO) {

                        Log.d("Mainss", "do it")

                        val isOkay = Util.tryUpdateSelfInfo(this@MainPageActivity)

                        //if not okay, delete the old token, inform user to login and switch to login page
                        if (!isOkay) {

                            MainApplication.db().kvDao().deleteAll()

                            withContext(Dispatchers.Main) {
                                Toast.makeText(this@MainPageActivity,
                                    "Token has expired, Please login again!", Toast.LENGTH_SHORT)
                                    .show()

                                delay(1000L)

                                MainActivity.toLoginPage(this@MainPageActivity)
                            }
                        }
                    }
                    //set delay for another 30 seconds to update info periodically
                    delay(30 * 1000L)
                }
            }
        }
    }


    override fun onSupportNavigateUp(): Boolean {
        return super.onSupportNavigateUp() || navController.navigateUp()
    }


    fun setFullScreenVisibility(viewVisibility: Int) {
        binding.userFloatingBtn.visibility = viewVisibility
        binding.bottomNavigation.visibility = viewVisibility
    }

    companion object {
        private var INSTANCE: MainPageActivity? = null
        fun getInstance(): MainPageActivity? {
            return INSTANCE
        }
    }
}