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
            findNavController().navigate(R.id.bookingsFragment)
        }

        binding.btnSettings.setOnClickListener {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            val uri = Uri.fromParts("package", requireContext().packageName, null)
            intent.data = uri
            startActivity(intent)
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
