package com.example.playground.workmanager.ui

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.playground.R
import com.example.playground.databinding.FragmentSecondBinding
import com.example.playground.databinding.FragmentSelectBinding
import com.example.playground.util.clickAction
import timber.log.Timber
import java.util.*

class SelectFragment : Fragment() {

    private val REQUEST_CODE_IMAGE = 100
    private val REQUEST_CODE_PERMISSIONS = 101

    private val KEY_PERMISSIONS_REQUEST_COUNT = "KEY_PERMISSIONS_REQUEST_COUNT"
    private val MAX_NUMBER_REQUEST_PERMISSIONS = 2

    private val permissions = Arrays.asList(
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    )

    private var permissionRequestCount: Int = 0
    private lateinit var binding: FragmentSelectBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSelectBinding.inflate(inflater)

        savedInstanceState?.let {
            permissionRequestCount = it.getInt(KEY_PERMISSIONS_REQUEST_COUNT, 0)
        }

        // Make sure the app has correct permissions to run
        requestPermissionsIfNecessary()

        // Create request to get image from filesystem when button clicked
        binding.selectImage.clickAction {
            startActivityForResult(
                Intent(
                    Intent.ACTION_PICK,
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                ),
                REQUEST_CODE_IMAGE
            )
        }

        return binding.root
    }

    /**
     * Request permissions twice - if the user denies twice then show a toast about how to update
     * the permission for storage. Also disable the button if we don't have access to pictures on
     * the device.
     */
    private fun requestPermissionsIfNecessary() {
        if (!checkAllPermissions()) {
            if (permissionRequestCount < MAX_NUMBER_REQUEST_PERMISSIONS) {
                permissionRequestCount += 1
                ActivityCompat.requestPermissions(
                    requireActivity(),
                    permissions.toTypedArray(),
                    REQUEST_CODE_PERMISSIONS
                )
            } else {
                Toast.makeText(
                    requireContext(),
                    R.string.set_permissions_in_settings,
                    Toast.LENGTH_LONG
                ).show()
                binding.selectImage.isEnabled = false
            }
        }
    }
    /** Permission Checking  */
    private fun checkAllPermissions(): Boolean {
        var hasPermissions = true
        for (permission in permissions) {
            hasPermissions = hasPermissions and isPermissionGranted(permission)
        }
        return hasPermissions
    }

    private fun isPermissionGranted(permission: String) =
        ContextCompat.checkSelfPermission(requireActivity(), permission) ==
                PackageManager.PERMISSION_GRANTED

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            requestPermissionsIfNecessary() // no-op if permissions are granted already.
        }
    }

    private fun handleImageRequestResult(intent: Intent) {
        // If clipdata is available, we use it, otherwise we use data
        val imageUri: Uri? = intent.clipData?.let {
            it.getItemAt(0).uri
        } ?: intent.data

        if (imageUri == null) {
            Timber.e("Invalid input image Uri.")
            return
        }

        findNavController().navigate(
            SelectFragmentDirections.actionSelectFragmentToBlurFragment("$imageUri")
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                REQUEST_CODE_IMAGE -> data?.let { handleImageRequestResult(data) }
                else -> Timber.d("Unkown request code")
            }
        } else {
            Timber.e("Unexpected Result code $requestCode")
        }
    }
}