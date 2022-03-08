package com.devlab74.blogx.ui.main.blog

import android.app.SearchManager
import android.content.Context.SEARCH_SERVICE
import android.os.Bundle
import android.view.*
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.customview.getCustomView
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.bumptech.glide.request.RequestOptions
import com.devlab74.blogx.R
import com.devlab74.blogx.databinding.FragmentBlogBinding
import com.devlab74.blogx.databinding.LayoutBlogFilterBinding
import com.devlab74.blogx.di.main.MainScope
import com.devlab74.blogx.models.BlogPost
import com.devlab74.blogx.persistence.BlogQueryUtils.Companion.BLOG_FILTER_DATE_UPDATED
import com.devlab74.blogx.persistence.BlogQueryUtils.Companion.BLOG_FILTER_USERNAME
import com.devlab74.blogx.persistence.BlogQueryUtils.Companion.BLOG_ORDER_ASC
import com.devlab74.blogx.ui.main.blog.state.BLOG_VIEW_STATE_BUNDLE_KEY
import com.devlab74.blogx.ui.main.blog.state.BlogViewState
import com.devlab74.blogx.ui.main.blog.viewmodels.*
import com.devlab74.blogx.util.StateMessageCallback
import com.devlab74.blogx.util.TopSpacingItemDecoration
import kotlinx.android.synthetic.main.layout_blog_filter.view.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import timber.log.Timber
import javax.inject.Inject

/**
 * BlogFragment Class
 */

@FlowPreview
@ExperimentalCoroutinesApi
@MainScope
class BlogFragment
@Inject
constructor(
    viewModelFactory: ViewModelProvider.Factory
): BaseBlogFragment(viewModelFactory),
    BlogListAdapter.Interaction,
    SwipeRefreshLayout.OnRefreshListener
{
    private var _binding: FragmentBlogBinding? = null
    private val binding get() = _binding!!

    private lateinit var recyclerAdapter: BlogListAdapter

    private lateinit var searchView: SearchView

    private var requestManager: RequestManager? = null // can leak memory, must be nullable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Restore state after process death
        savedInstanceState?.let { inState ->
            (inState[BLOG_VIEW_STATE_BUNDLE_KEY] as BlogViewState?)?.let { viewState ->
                viewModel.setViewState(viewState)
            }
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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentBlogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as AppCompatActivity).supportActionBar?.setDisplayShowTitleEnabled(false)
        setHasOptionsMenu(true)
        binding.swipeRefresh.setOnRefreshListener(this)

        setupGlide()
        initRecyclerView()
        subscribeObservers()
    }

    private fun subscribeObservers(){
        viewModel.viewState.observe(viewLifecycleOwner, Observer{ viewState ->
            if(viewState != null){
                recyclerAdapter.apply {
                    viewState.blogFields.blogList?.let {
                        preloadGlideImages(
                            requestManager = requestManager as RequestManager,
                            list = it
                        )
                    }

                    viewState.blogFields.blogList?.let {
                        submitList(
                            blogList = it,
                            isQueryExhausted = viewState.blogFields.isQueryExhausted?: true
                        )
                    }
                }

            }
        })

        viewModel.numActiveJobs.observe(viewLifecycleOwner, Observer {
            uiCommunicationListener.displayProgressBar(viewModel.areAnyJobsActive())
        })

        viewModel.stateMessage.observe(viewLifecycleOwner, Observer { stateMessage ->
            stateMessage?.let {
                if(stateMessage.response.message!!.contains("Invalid page.")){
                    viewModel.setQueryExhausted(true)
                    viewModel.clearStateMessage()
                }else{
                    uiCommunicationListener.onResponseReceived(
                        response = it.response,
                        stateMessageCallback = object: StateMessageCallback {
                            override fun removeMessageFromStack() {
                                viewModel.clearStateMessage()
                            }
                        }
                    )
                }
            }
        })
    }

    private fun initSearchView(menu: Menu) {
        activity?.apply {
            val searchManager: SearchManager = getSystemService(SEARCH_SERVICE) as SearchManager
            searchView = menu.findItem(R.id.action_search).actionView as SearchView
            searchView.setSearchableInfo(searchManager.getSearchableInfo(componentName))
            searchView.maxWidth = Integer.MAX_VALUE
            searchView.setIconifiedByDefault(true)
            searchView.isSubmitButtonEnabled = true
        }

        // Case 1: Enter on keyboard
        val searchPlate = searchView.findViewById(R.id.search_src_text) as EditText
        searchPlate.setOnEditorActionListener { v, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_UNSPECIFIED || actionId == EditorInfo.IME_ACTION_SEARCH) {
                val searchQuery = v.text.toString()
                viewModel.setQuery(searchQuery).let {
                    onBlogSearchOrFilter()
                }
            }
            true
        }

        // Case 2: Search button clicked in toolbar
        (searchView.findViewById(R.id.search_go_btn) as View).setOnClickListener {
            val searchQuery = searchPlate.text.toString()
            onBlogSearchOrFilter()
        }
    }

    private fun onBlogSearchOrFilter() {
        viewModel.loadFirstPage().let {
            resetUI()
        }
    }

    private fun resetUI() {
        binding.blogPostRecyclerview.smoothScrollToPosition(0)
        uiCommunicationListener.hideSoftKeyboard()
        binding.focusableView.requestFocus()
    }

    private fun initRecyclerView() {
        binding.blogPostRecyclerview.apply {
            layoutManager = LinearLayoutManager(this@BlogFragment.context)
            val topSpacingItemDecoration = TopSpacingItemDecoration(30)
            removeItemDecoration(topSpacingItemDecoration) // Does nothing if not applied already
            addItemDecoration(topSpacingItemDecoration)

            recyclerAdapter = BlogListAdapter(
                requestManager = requestManager as RequestManager,
                interaction = this@BlogFragment
            )

            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    super.onScrollStateChanged(recyclerView, newState)
                    val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                    val lastPosition = layoutManager.findLastVisibleItemPosition()
                    if (lastPosition == recyclerAdapter.itemCount.minus(1)) {
                        Timber.d("BlogFragment: attempting to load next page")
                        viewModel.nextPage()
                    }
                }
            })
            adapter = recyclerAdapter
        }
    }

    private fun setupGlide(){
        val requestOptions = RequestOptions
            .placeholderOf(R.drawable.default_image)
            .error(R.drawable.default_image)

        activity?.let {
            requestManager = Glide.with(it)
                .applyDefaultRequestOptions(requestOptions)
        }
    }

    private fun showFilterOptions() {
        activity?.let {
            val dialog = MaterialDialog(it)
                .noAutoDismiss()
                .customView(R.layout.layout_blog_filter)

            val dialogBinding = LayoutBlogFilterBinding.bind(dialog.getCustomView())

            val filter = viewModel.getFilter()
            val order = viewModel.getOrder()

            if (filter == BLOG_FILTER_DATE_UPDATED) {
                dialogBinding.filterGroup.check(dialogBinding.filterDate.id)
            } else {
                dialogBinding.filterGroup.check(dialogBinding.filterAuthor.id)
            }

            if (order == BLOG_ORDER_ASC) {
                dialogBinding.orderGroup.check(dialogBinding.filterAsc.id)
            } else {
                dialogBinding.orderGroup.check(dialogBinding.filterDesc.id)
            }

            dialogBinding.positiveButton.setOnClickListener {

                val selectedFilter = if (dialogBinding.filterGroup.checkedRadioButtonId == dialogBinding.filterGroup.filter_author.id) {
                    dialogBinding.filterGroup.filter_author
                } else {
                    dialogBinding.filterGroup.filter_date
                }

                val selectedOrder = if (dialogBinding.orderGroup.checkedRadioButtonId == dialogBinding.orderGroup.filter_asc.id) {
                    dialogBinding.orderGroup.filter_asc
                } else {
                    dialogBinding.orderGroup.filter_desc
                }

                var filter = BLOG_FILTER_DATE_UPDATED
                if (selectedFilter.text.toString() == getString(R.string.filter_author)) {
                    filter = BLOG_FILTER_USERNAME
                }

                var order = ""
                if (selectedOrder.text.toString() == getString(R.string.filter_desc)) {
                    order = "-"
                }

                viewModel.saveFilterOptions(filter, order).let {
                    viewModel.setBlogFilter(filter)
                    viewModel.setBlogOrder(order)
                }

                onBlogSearchOrFilter()

                dialog.dismiss()
            }

            dialogBinding.negativeButton.setOnClickListener {
                Timber.d("FilterDialog: cancelling filter")
                dialog.dismiss()
            }
            dialog.show()
        }
    }

    override fun onRefresh() {
        onBlogSearchOrFilter()
        binding.swipeRefresh.isRefreshing = false
    }

    override fun restoreListPosition() {
        viewModel.viewState.value?.blogFields?.layoutManagerState?.let { lmState ->
            binding.blogPostRecyclerview.layoutManager?.onRestoreInstanceState(lmState)
        }
    }

    override fun onItemSelected(position: Int, item: BlogPost) {
        viewModel.setBlogPost(item)
        findNavController().navigate(R.id.action_blogFragment_to_viewBlogFragment)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.search_menu, menu)
        initSearchView(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_filter_settings) {
            showFilterOptions()
            return true
        }

        return super.onOptionsItemSelected(item)
    }

    private fun saveLayoutManagerState() {
        binding.blogPostRecyclerview.layoutManager?.onSaveInstanceState()?.let { lmState ->
            viewModel.setLayoutManagerState(lmState)
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.refreshFromCache()
    }

    override fun onPause() {
        super.onPause()
        saveLayoutManagerState()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.blogPostRecyclerview.adapter = null // Clear references (Can leak memory)
        _binding = null
    }
}