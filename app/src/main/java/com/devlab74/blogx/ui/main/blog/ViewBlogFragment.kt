package com.devlab74.blogx.ui.main.blog

import android.os.Bundle
import android.view.*
import androidx.navigation.fragment.findNavController
import com.devlab74.blogx.R
import com.devlab74.blogx.databinding.FragmentViewBlogBinding

class ViewBlogFragment : BaseBlogFragment() {
    private var _binding: FragmentViewBlogBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentViewBlogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        // TODO: Check if user is author of blog post
        val isAuthorOfBlogPost = true
        if (isAuthorOfBlogPost) {
            inflater.inflate(R.menu.edit_view_menu, menu)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val isAuthorOfBlogPost = true
        if (isAuthorOfBlogPost) {
            if (item.itemId == R.id.edit) {
                navUpdateBlogFragment()
                return true
            }
        }

        return super.onOptionsItemSelected(item)
    }

    fun navUpdateBlogFragment() {
        findNavController().navigate(R.id.action_viewBlogFragment_to_updateBlogFragment)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}