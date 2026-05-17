package com.wayfarer.app.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.wayfarer.app.WayFarerApp
import com.wayfarer.app.data.models.User
import com.wayfarer.app.databinding.FragmentEditProfileBinding

class EditProfileFragment : Fragment() {

    private var _binding: FragmentEditProfileBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val session = (requireActivity().application as WayFarerApp).sessionManager
        val user = session.getUser()

        binding.etName.setText(user?.name)
        binding.etEmail.setText(user?.email)

        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.btnSave.setOnClickListener {
            val newName = binding.etName.text.toString().trim()
            if (newName.isEmpty()) {
                binding.tilName.error = "Name cannot be empty"
                return@setOnClickListener
            }

            if (user != null) {
                val updatedUser = user.copy(name = newName)
                session.saveUser(updatedUser)
                Toast.makeText(requireContext(), "Profile updated successfully", Toast.LENGTH_SHORT).show()
                findNavController().navigateUp()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
