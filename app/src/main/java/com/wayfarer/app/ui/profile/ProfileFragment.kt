package com.wayfarer.app.ui.profile

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
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
        val firebaseUser = FirebaseAuth.getInstance().currentUser
        val isLoggedIn = firebaseUser != null

        binding.tvUserName.text = if (isLoggedIn) firebaseUser?.displayName?.ifEmpty { "Traveller" } else "Guest Traveller"
        binding.tvUserEmail.text = if (isLoggedIn) firebaseUser?.email else "Sign in to manage your profile"

        val initials = if (isLoggedIn) {
            (firebaseUser?.displayName ?: "")
                .split(" ")
                .mapNotNull { it.firstOrNull()?.toString() }
                .take(2).joinToString("").ifEmpty { "WF" }
        } else "GT"
        binding.tvInitials.text = initials.uppercase()

        binding.btnBookings.setOnClickListener {
            val bottomNav = requireActivity().findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(R.id.bottom_navigation)
            bottomNav?.selectedItemId = R.id.bookingsFragment
        }

        binding.btnSettings.setOnClickListener {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            intent.data = Uri.fromParts("package", requireContext().packageName, null)
            startActivity(intent)
        }

        val isDark = session.isDarkMode()
        binding.switchDarkMode.isChecked = isDark
        binding.tvDarkModeLabel.text = if (isDark) "Light Mode" else "Dark Mode"

        binding.switchDarkMode.setOnCheckedChangeListener { _, isChecked ->
            binding.tvDarkModeLabel.text = if (isChecked) "Light Mode" else "Dark Mode"
            session.saveDarkMode(isChecked)
            AppCompatDelegate.setDefaultNightMode(
                if (isChecked) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
            )
            requireActivity().recreate()
        }

        binding.btnEditProfile.setOnClickListener {
            findNavController().navigate(R.id.action_profile_to_editProfile)
        }

        binding.btnHelp.setOnClickListener {
            findNavController().navigate(R.id.action_profile_to_helpSupport)
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
                FirebaseAuth.getInstance().signOut()
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
