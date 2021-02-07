package com.devlab74.blogx.ui.main.blog

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.*
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.RequestManager
import com.devlab74.blogx.R
import com.devlab74.blogx.databinding.FragmentUpdateBlogBinding
import com.devlab74.blogx.di.main.MainScope
import com.devlab74.blogx.ui.main.blog.state.BLOG_VIEW_STATE_BUNDLE_KEY
import com.devlab74.blogx.ui.main.blog.state.BlogStateEvent
import com.devlab74.blogx.ui.main.blog.state.BlogViewState
import com.devlab74.blogx.ui.main.blog.viewmodels.*
import com.devlab74.blogx.util.*
import com.devlab74.blogx.util.Constants.Companion.GALLERY_REQUEST_CODE
import com.devlab74.blogx.util.ErrorHandling.Companion.handleErrors
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import timber.log.Timber
import java.io.File
import javax.inject.Inject

@FlowPreview
@ExperimentalCoroutinesApi
@MainScope
class UpdateBlogFragment
@Inject
constructor(
    viewModelFactory: ViewModelProvider.Factory,
    private val requestManager: RequestManager
): BaseBlogFragment(viewModelFactory) {
    private var _binding: FragmentUpdateBlogBinding? = null
    private val binding get() = _binding!!

    private var isImageUpdated: Boolean = false

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
        _binding = FragmentUpdateBlogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setHasOptionsMenu(true)
        subscribeObservers()

        binding.imageContainer.setOnClickListener {
            if (uiCommunicationListener.isStoragePermissionGranted()) {
                pickFromGallery()
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

    fun subscribeObservers(){
        viewModel.viewState.observe(viewLifecycleOwner, Observer { viewState ->
            viewState.updatedBlogFields.let{ updatedBlogFields ->
                setBlogProperties(
                    updatedBlogFields.updatedBlogTitle,
                    updatedBlogFields.updatedBlogBody,
                    updatedBlogFields.updatedImageUri
                )
            }
        })

        viewModel.numActiveJobs.observe(viewLifecycleOwner, Observer {
            uiCommunicationListener.displayProgressBar(viewModel.areAnyJobsActive())
        })

        viewModel.stateMessage.observe(viewLifecycleOwner, Observer { stateMessage ->
            stateMessage?.let {
                Timber.d("Update Blog Fragment: stateMessage: $stateMessage")
                if(stateMessage.response.message == handleErrors(4010, activity?.application!!)){
                    viewModel.updateListItem()
                }

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

    private fun pickFromGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.type = "image/*"
        val mimeType = arrayOf("image/jpeg", "image/png", "image/jpg")
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeType)
        intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        startActivityForResult(intent, GALLERY_REQUEST_CODE)
    }

    private fun launchImageCrop(uri: Uri?) {
        context?.let {
            CropImage.activity(uri)
                .setGuidelines(CropImageView.Guidelines.ON)
                .start(it, this)
        }
    }

    private fun showImageSelectionError(){
        uiCommunicationListener.onResponseReceived(
            response = Response(
                message = getString(R.string.something_wrong_with_image),
                uiComponentType = UIComponentType.Dialog,
                messageType = MessageType.Error
            ),
            stateMessageCallback = object: StateMessageCallback{
                override fun removeMessageFromStack() {
                    viewModel.clearStateMessage()
                }
            }
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                GALLERY_REQUEST_CODE -> {
                    data?.data?.let { uri ->
                        activity?.let{
                            launchImageCrop(uri)
                        }
                    }?: showImageSelectionError()
                }

                CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE -> {
                    val result = CropImage.getActivityResult(data)
                    val resultUri = result.uri
                    viewModel.setUpdatedUri(resultUri)
                    isImageUpdated = true
                }

                CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE -> {
                    showImageSelectionError()
                }
            }
        }
    }

    private fun setBlogProperties(title: String?, body: String?, image: Uri?){
        image?.let {
            requestManager
                .load(it)
                .into(binding.blogImage)
        }
        binding.blogTitle.setText(title)
        binding.blogBody.setText(body)
    }

    private fun saveChanges() {
        var multipartBody: MultipartBody.Part? = null

        if (isImageUpdated) {
            viewModel.getUpdatedBlogUri()?.let { imageUri ->
                imageUri.path?.let { filePath ->
                    val imageFile = File(filePath)
                    Timber.d("UpdateBlogFragment: imageFile: $imageFile")
                    if(imageFile.exists()){
                        val requestBody =
                            RequestBody.create(
                                MediaType.parse("image/jpg"),
                                imageFile
                            )
                        // name = field name in serializer
                        // filename = name of the image file
                        // requestBody = file with file type information
                        multipartBody = MultipartBody.Part.createFormData(
                            "image",
                            imageFile.name,
                            requestBody
                        )
                    }
                }
            }
        }

        viewModel.setStateEvent(
            BlogStateEvent.UpdatedBlogPostEvent(
                binding.blogTitle.text.toString(),
                binding.blogBody.text.toString(),
                multipartBody
            )
        )
        uiCommunicationListener.hideSoftKeyboard()
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

    override fun onPause() {
        super.onPause()
        viewModel.setUpdatedTitle(binding.blogTitle.text.toString())
        viewModel.setUpdatedBody(binding.blogBody.text.toString())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}