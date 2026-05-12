package com.wayfarer.app.ui.profile

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.wayfarer.app.R
import com.wayfarer.app.WayFarerApp
import com.wayfarer.app.databinding.FragmentProfileBinding
import com.wayfarer.app.ui.auth.AuthActivity

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val session = (requireActivity().application as WayFarerApp).sessionManager
        val user = session.getUser()
        val isLoggedIn = session.isLoggedIn()

        binding.tvUserName.text = if (isLoggedIn) user?.name else "Guest Traveller"
        binding.tvUserEmail.text = if (isLoggedIn) user?.email else "Sign in to manage your profile"

        val initials = if (isLoggedIn) {
            user?.name?.split(" ")
                ?.mapNotNull { it.firstOrNull()?.toString() }
                ?.take(2)?.joinToString("") ?: "WF"
        } else {
            "GT"
        }
        binding.tvInitials.text = initials.uppercase()

        binding.btnBookings.setOnClickListener {
            val bottomNav = requireActivity().findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(R.id.bottom_navigation)
            bottomNav?.selectedItemId = R.id.bookingsFragment
        }

        binding.btnSettings.setOnClickListener {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            val uri = Uri.fromParts("package", requireContext().packageName, null)
            intent.data = uri
            startActivity(intent)
        }

        binding.switchDarkMode.isChecked = session.isDarkMode()
        binding.switchDarkMode.setOnCheckedChangeListener { _, isChecked ->
            session.saveDarkMode(isChecked)
            if (isChecked) {
                androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES)
            } else {
                androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO)
            }
        }
        
        binding.btnEditProfile.setOnClickListener {
            android.widget.Toast.makeText(requireContext(), "Edit Profile coming soon", android.widget.Toast.LENGTH_SHORT).show()
        }
        
        binding.btnHelp.setOnClickListener {
            android.widget.Toast.makeText(requireContext(), "Help & Support coming soon", android.widget.Toast.LENGTH_SHORT).show()
        }

        binding.btnNotifications.setOnClickListener {
            binding.switchNotifications.isChecked = !binding.switchNotifications.isChecked
        }
        
        binding.switchNotifications.setOnCheckedChangeListener { _, isChecked ->
            val status = if (isChecked) "enabled" else "disabled"
            android.widget.Toast.makeText(requireContext(), "Notifications $status", android.widget.Toast.LENGTH_SHORT).show()
        }

        if (isLoggedIn) {
            binding.btnLogout.text = "Sign Out"
            binding.btnLogout.setOnClickListener {
                session.clearSession()
                startActivity(Intent(requireContext(), AuthActivity::class.java))
                requireActivity().finish()
            }
        } else {
            binding.btnLogout.text = "Sign In"
            binding.btnLogout.setOnClickListener {
                startActivity(Intent(requireContext(), AuthActivity::class.java))
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
