package com.devlab74.blogx.ui.main.account

import android.os.Bundle
import android.view.*
import androidx.navigation.fragment.findNavController
import com.devlab74.blogx.R
import com.devlab74.blogx.databinding.FragmentAccountBinding
import com.devlab74.blogx.session.SessionManager
import javax.inject.Inject

class AccountFragment: BaseAccountFragment() {
    private var _binding: FragmentAccountBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var sessionManager: SessionManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAccountBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setHasOptionsMenu(true)
        binding.changePassword.setOnClickListener {
            findNavController().navigate(R.id.action_accountFragment_to_changePasswordFragment)
        }
        binding.logoutButton.setOnClickListener {
            sessionManager.logout()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.edit_view_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.edit) {
            findNavController().navigate(R.id.action_accountFragment_to_updateAccountFragment)
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}