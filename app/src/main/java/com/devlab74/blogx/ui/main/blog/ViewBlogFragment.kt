package com.devlab74.blogx.ui.main.blog

import android.os.Bundle
import android.view.*
import androidx.core.net.toUri
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.RequestManager
import com.devlab74.blogx.R
import com.devlab74.blogx.databinding.FragmentViewBlogBinding
import com.devlab74.blogx.di.main.MainScope
import com.devlab74.blogx.models.BlogPost
import com.devlab74.blogx.ui.AreYouSureCallback
import com.devlab74.blogx.ui.main.blog.state.BLOG_VIEW_STATE_BUNDLE_KEY
import com.devlab74.blogx.ui.main.blog.state.BlogStateEvent
import com.devlab74.blogx.ui.main.blog.state.BlogViewState
import com.devlab74.blogx.ui.main.blog.viewmodels.*
import com.devlab74.blogx.util.*
import com.devlab74.blogx.util.ErrorHandling.Companion.handleErrors
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import timber.log.Timber
import java.lang.Exception
import javax.inject.Inject

@FlowPreview
@ExperimentalCoroutinesApi
@MainScope
class ViewBlogFragment
@Inject
constructor(
    viewModelFactory: ViewModelProvider.Factory,
    private val requestManager: RequestManager
): BaseBlogFragment(viewModelFactory) {
    private var _binding: FragmentViewBlogBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Restore state after process death
        savedInstanceState?.let { inState ->
            (inState[BLOG_VIEW_STATE_BUNDLE_KEY] as BlogViewState?)?.let { viewState ->
                viewModel.setViewState(viewState)
            }
        }
    }

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
        subscribeObservers()
        checkIsAuthorOfBlogPost()
        uiCommunicationListener.expandAppBar()

        binding.deleteButton.setOnClickListener {
            confirmDeleteRequest()
        }
    }

    // !IMPORTANT!
    // Must save ViewState b/c in event of process death the LiveData in ViewModel will be lost
    override fun onSaveInstanceState(outState: Bundle) {
        val viewState = viewModel.viewState.value
        viewState?.blogFields?.blogList = ArrayList()
        outState.putParcelable(
            BLOG_VIEW_STATE_BUNDLE_KEY,
            viewState
        )

        super.onSaveInstanceState(outState)
    }

    private fun confirmDeleteRequest() {
        val callback: AreYouSureCallback = object : AreYouSureCallback {
            override fun proceed() {
                deleteBlogPost()
            }

            override fun cancel() {
                // Ignore
            }
        }
        uiCommunicationListener.onResponseReceived(
            response = Response(
                message = getString(R.string.are_you_sure_delete),
                uiComponentType = UIComponentType.AreYouSureDialog(callback),
                messageType = MessageType.Info
            ),
            stateMessageCallback = object: StateMessageCallback {
                override fun removeMessageFromStack() {
                    viewModel.clearStateMessage()
                }
            }
        )
    }

    fun subscribeObservers(){
        viewModel.viewState.observe(viewLifecycleOwner, Observer { viewState ->
            viewState.viewBlogFields.blogPost?.let{ blogPost ->
                setBlogProperties(blogPost)
            }

            if(viewState.viewBlogFields.isAuthorOfBlogPost == true){
                adaptViewToAuthorMode()
            }
        })

        viewModel.numActiveJobs.observe(viewLifecycleOwner, Observer {
            uiCommunicationListener.displayProgressBar(viewModel.areAnyJobsActive())
        })

        viewModel.stateMessage.observe(viewLifecycleOwner, Observer { stateMessage ->
            Timber.d("View Blog Fragment: stateMessage: $stateMessage")
            if(stateMessage?.response?.message == handleErrors(4003, activity?.application!!)){
                viewModel.removeDeletedBlogPost()
                findNavController().popBackStack()
            }

            stateMessage?.let {
                uiCommunicationListener.onResponseReceived(
                    response = it.response,
                    stateMessageCallback = object: StateMessageCallback {
                        override fun removeMessageFromStack() {
                            viewModel.clearStateMessage()
                        }
                    }
                )
            }
        })
    }

    private fun deleteBlogPost() {
        viewModel.setStateEvent(
            BlogStateEvent.DeleteBlogPostEvent
        )
    }

    private fun adaptViewToAuthorMode() {
        activity?.invalidateOptionsMenu()
        binding.deleteButton.visibility = View.VISIBLE
    }

    private fun checkIsAuthorOfBlogPost() {
        viewModel.setIsAuthorOfBlogPost(false) // reset
        viewModel.setStateEvent(BlogStateEvent.CheckAuthorOfBlogPost)
    }

    private fun setBlogProperties(blogPost: BlogPost) {
        requestManager
            .load(blogPost.image)
            .into(binding.blogImage)
        binding.blogTitle.text = blogPost.title
        binding.blogAuthor.text = blogPost.username
        binding.blogUpdateDate.text = DateUtils.convertLongToStringDate(blogPost.dateUpdated)
        binding.blogBody.text = blogPost.body
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        if (viewModel.isAuthorOfBlogPost()) {
            inflater.inflate(R.menu.edit_view_menu, menu)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (viewModel.isAuthorOfBlogPost()) {
            if (item.itemId == R.id.edit) {
                navUpdateBlogFragment()
                return true
            }
        }

        return super.onOptionsItemSelected(item)
    }

    private fun navUpdateBlogFragment() {
        try {
            // prep for next fragment
            viewModel.setUpdatedTitle(viewModel.getBlogPost().title)
            viewModel.setUpdatedBody(viewModel.getBlogPost().body)
            viewModel.setUpdatedUri(viewModel.getBlogPost().image.toUri())
            findNavController().navigate(R.id.action_viewBlogFragment_to_updateBlogFragment)
        } catch (e: Exception) {
            Timber.e("Exception: ${e.message}")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}