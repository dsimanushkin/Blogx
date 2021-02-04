package com.devlab74.blogx.ui.main.create_blog

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.*
import androidx.lifecycle.Observer
import com.devlab74.blogx.R
import com.devlab74.blogx.databinding.FragmentCreateBlogBinding
import com.devlab74.blogx.ui.*
import com.devlab74.blogx.ui.main.create_blog.state.CreateBlogStateEvent
import com.devlab74.blogx.util.Constants.Companion.GALLERY_REQUEST_CODE
import com.devlab74.blogx.util.ErrorHandling.Companion.handleErrors
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import timber.log.Timber
import java.io.File

class CreateBlogFragment: BaseCreateBlogFragment() {
    private var _binding: FragmentCreateBlogBinding? = null
    private val binding get() = _binding!!

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
            if (stateChangeListener.isStoragePermissionGranted()) {
                pickFromGallery()
            }
        }
        binding.updateTextview.setOnClickListener {
            if (stateChangeListener.isStoragePermissionGranted()) {
                pickFromGallery()
            }
        }

        subscribeObservers()
    }

    private fun subscribeObservers() {
        viewModel.dataState.observe(viewLifecycleOwner, Observer { dataState ->
            stateChangeListener.onDataStateChange(dataState)
            dataState.data?.let { data ->
                data.response?.let { event ->
                    event.peekContent().let { response ->
                        response.message?.let { message ->
                            if (message == getString(R.string.blog_created_successfully)) {
                                viewModel.clearNewBlogFields()
                            }
                        }
                    }
                }
            }
        })

        viewModel.viewState.observe(viewLifecycleOwner, Observer { viewState ->
            viewState.blogFields.let { newBlogFields ->
                setBlogProperties(
                    newBlogFields.newBlogTitle,
                    newBlogFields.newBlogBody,
                    newBlogFields.newImageUri
                )
            }
        })
    }

    private fun publishNewBlog() {
        var multipartBody: MultipartBody.Part? = null
        viewModel.getNewImageUri()?.let { imageUri ->
            imageUri.path?.let { filePath ->
                val imageFile = File(filePath)
                Timber.d("CreateBlogFragment: imageFile: $imageFile")
                val requestBody = RequestBody.create(
                    MediaType.parse("image/jpg"),
                    imageFile
                )
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

            stateChangeListener.hideSoftKeyboard()
        }?: showErrorDialog(getString(R.string.error_must_select_image))
    }

    private fun setBlogProperties(
        title: String?,
        body: String?,
        image: Uri?
    ) {
        image?.let {
            requestManager
                .load(image)
                .into(binding.blogImage)
        }?: setDefaultImage()

        binding.blogTitle.setText(title)
        binding.blogBody.setText(body)
    }

    private fun setDefaultImage() {
        requestManager
            .load(R.drawable.default_image)
            .into(binding.blogImage)
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

    private fun showErrorDialog(errorMessage: String) {
        stateChangeListener.onDataStateChange(
            DataState(
                Event(
                    StateError(
                        Response(
                            errorMessage,
                            ResponseType.Dialog()
                        )
                    )
                ),
                Loading(false),
                Data(Event.dataEvent(null), null)
            )
        )
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
            uiCommunicationListener.onUIMessageReceived(
                UIMessage(
                    getString(R.string.are_you_sure_publish),
                    UIMessageType.AreYouSureDialog(callback)
                )
            )
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