package com.devlab74.blogx.ui.main.create_blog

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.*
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.RequestManager
import com.devlab74.blogx.R
import com.devlab74.blogx.databinding.FragmentCreateBlogBinding
import com.devlab74.blogx.di.main.MainScope
import com.devlab74.blogx.ui.*
import com.devlab74.blogx.ui.main.create_blog.state.CREATE_BLOG_VIEW_STATE_BUNDLE_KEY
import com.devlab74.blogx.ui.main.create_blog.state.CreateBlogStateEvent
import com.devlab74.blogx.ui.main.create_blog.state.CreateBlogViewState
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
class CreateBlogFragment
@Inject
constructor(
    viewModelFactory: ViewModelProvider.Factory,
    private val requestManager: RequestManager
): BaseCreateBlogFragment(viewModelFactory) {
    private var _binding: FragmentCreateBlogBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Restore state after process death
        savedInstanceState?.let { inState ->
            (inState[CREATE_BLOG_VIEW_STATE_BUNDLE_KEY] as CreateBlogViewState?)?.let { viewState ->
                viewModel.setViewState(viewState)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentCreateBlogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)

        binding.blogImage.setOnClickListener {
            if (uiCommunicationListener.isStoragePermissionGranted()) {
                pickFromGallery()
            }
        }
        binding.updateTextview.setOnClickListener {
            if (uiCommunicationListener.isStoragePermissionGranted()) {
                pickFromGallery()
            }
        }

        subscribeObservers()
    }

    // !IMPORTANT!
    // Must save ViewState b/c in event of process death the LiveData in ViewModel will be lost
    override fun onSaveInstanceState(outState: Bundle) {
        outState.putParcelable(
            CREATE_BLOG_VIEW_STATE_BUNDLE_KEY,
            viewModel.viewState.value
        )

        super.onSaveInstanceState(outState)
    }

    fun subscribeObservers(){
        viewModel.viewState.observe(viewLifecycleOwner, Observer { viewState ->
            viewState.blogFields.let{ newBlogFields ->
                setBlogProperties(
                    newBlogFields.newBlogTitle,
                    newBlogFields.newBlogBody,
                    newBlogFields.newImageUri
                )
            }
        })

        viewModel.numActiveJobs.observe(viewLifecycleOwner, Observer { jobCounter ->
            uiCommunicationListener.displayProgressBar(viewModel.areAnyJobsActive())
        })

        viewModel.stateMessage.observe(viewLifecycleOwner, Observer { stateMessage ->

            stateMessage?.let {
                if (it.response.message == handleErrors(4011, activity?.application!!)) {
                    viewModel.clearNewBlogFields()
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

    private fun setBlogProperties(
        title: String? = "",
        body: String? = "",
        image: Uri?
    ) {
        if(image != null){
            requestManager
                .load(image)
                .into(binding.blogImage)
        }
        else{
            requestManager
                .load(R.drawable.default_image)
                .into(binding.blogImage)
        }

        binding.blogTitle.setText(title)
        binding.blogBody.setText(body)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when(requestCode) {
                GALLERY_REQUEST_CODE -> {
                    data?.data?.let { uri ->
                        launchImageCrop(uri)
                    }?: showErrorDialog(getString(R.string.error_something_wrong_with_image))
                }
                CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE -> {
                    Timber.d("CROP: CROP_IMAGE_ACTIVITY_REQUEST_CODE")
                    val result = CropImage.getActivityResult(data)
                    val resultUri = result.uri
                    Timber.d("CROP: CROP_IMAGE_ACTIVITY_REQUEST_CODE: uri: $resultUri")
                    viewModel.setNewBlogFields(
                        title = null,
                        body = null,
                        uri = resultUri
                    )
                }
                CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE -> {
                    showErrorDialog(getString(R.string.error_something_wrong_with_image))
                }
            }
        }
    }

    private fun launchImageCrop(uri: Uri?) {
        context?.let {
            CropImage.activity(uri)
                .setGuidelines(CropImageView.Guidelines.ON)
                .start(it, this)
        }
    }

    private fun publishNewBlog(){
        var multipartBody: MultipartBody.Part? = null
        viewModel.viewState.value?.blogFields?.newImageUri?.let{ imageUri ->
            imageUri.path?.let{filePath ->
                val imageFile = File(filePath)
                Timber.d("CreateBlogFragment, imageFile: file: $imageFile")
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

        multipartBody?.let {
            viewModel.setStateEvent(
                CreateBlogStateEvent.CreateNewBlogEvent(
                    binding.blogTitle.text.toString(),
                    binding.blogBody.text.toString(),
                    it
                )
            )
            uiCommunicationListener.hideSoftKeyboard()
        }?: showErrorDialog(getString(R.string.error_must_select_image))

    }

    fun showErrorDialog(errorMessage: String){
        uiCommunicationListener.onResponseReceived(
            response = Response(
                message = errorMessage,
                uiComponentType = UIComponentType.Dialog(),
                messageType = MessageType.Error()
            ),
            stateMessageCallback = object: StateMessageCallback{
                override fun removeMessageFromStack() {
                    viewModel.clearStateMessage()
                }
            }
        )
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.publish_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.publish) {
            val callback: AreYouSureCallback = object : AreYouSureCallback {
                override fun proceed() {
                    publishNewBlog()
                }

                override fun cancel() {
                    // Ignore
                }
            }
            uiCommunicationListener.onResponseReceived(
                response = Response(
                    message = getString(R.string.are_you_sure_publish),
                    uiComponentType = UIComponentType.AreYouSureDialog(callback),
                    messageType = MessageType.Info()
                ),
                stateMessageCallback = object: StateMessageCallback{
                    override fun removeMessageFromStack() {
                        viewModel.clearStateMessage()
                    }
                }
            )
            return true
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onPause() {
        super.onPause()
        viewModel.setNewBlogFields(
            binding.blogTitle.text.toString(),
            binding.blogBody.text.toString(),
            null
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}