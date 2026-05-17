package com.wayfarer.app.ui.home

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.wayfarer.app.R
import com.wayfarer.app.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHost = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHost.navController

        binding.bottomNavigation.setupWithNavController(navController)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.chatFragment, R.id.editProfileFragment, R.id.helpSupportFragment -> {
                    binding.fabChat.hide()
                    binding.bottomNavigation.visibility = android.view.View.GONE
                }
                else -> {
                    binding.fabChat.show()
                    binding.bottomNavigation.visibility = android.view.View.VISIBLE
                }
            }
        }

        binding.fabChat.setOnClickListener {
            navController.navigate(R.id.chatFragment)
        }
    }
}
