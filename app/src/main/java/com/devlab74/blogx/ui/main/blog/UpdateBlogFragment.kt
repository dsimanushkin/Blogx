package com.devlab74.blogx.ui.main.blog

import android.net.Uri
import android.os.Bundle
import android.view.*
import androidx.lifecycle.Observer
import com.devlab74.blogx.R
import com.devlab74.blogx.databinding.FragmentUpdateBlogBinding
import com.devlab74.blogx.ui.main.blog.state.BlogStateEvent
import okhttp3.MultipartBody

class UpdateBlogFragment : BaseBlogFragment() {
    private var _binding: FragmentUpdateBlogBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentUpdateBlogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setHasOptionsMenu(true)
        subscribeObservers()
    }

    private fun subscribeObservers() {
        viewModel.dataState.observe(viewLifecycleOwner, Observer { dataState ->
            stateChangeListener.onDataStateChange(dataState)
            dataState.data?.let { data ->
                data.data?.getContentIfNotHandled()?.let { viewState ->
                    // If this is not null, the blogpost was updated
                    viewState.viewBlogFields.blogPost?.let { blogPost ->
                        // TODO: onBlogPostUpdateSuccess
                    }
                }
            }
        })

        viewModel.viewState.observe(viewLifecycleOwner, Observer { viewState ->
            viewState.updatedBlogFields.let { updatedBlogFields ->
                setBlogProperties(
                    updatedBlogFields.updateBlogTitle,
                    updatedBlogFields.updateBlogBody,
                    updatedBlogFields.updateImageUri
                )
            }
        })
    }

    private fun setBlogProperties(
        updatedBlogTitle: String?,
        updatedBlogBody: String?,
        updatedImageUri: Uri?
    ) {
        requestManager
            .load(updatedImageUri)
            .into(binding.blogImage)
        binding.blogTitle.setText(updatedBlogTitle)
        binding.blogBody.setText(updatedBlogBody)
    }

    private fun saveChanges() {
        var multipartBody: MultipartBody.Part? = null
        viewModel.setStateEvent(
            BlogStateEvent.UpdatedBlogPostEvent(
                binding.blogTitle.text.toString(),
                binding.blogBody.text.toString(),
                multipartBody
            )
        )
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.update_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.save) {
            saveChanges()
            return true
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}