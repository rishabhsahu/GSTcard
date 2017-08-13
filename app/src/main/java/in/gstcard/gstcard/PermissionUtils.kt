package `in`.gstcard.gstcard

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.widget.Toast

/**
 * Created by kshivang on 20/07/17.

 */

class PermissionUtils : PermissionListener{
    companion object {
        val READ_AND_RECEIVE_SMS_PERMISSION_CODE = 710
        val READ_CONTACT_PERMISSION_CODE = 720
        val WRITE_EXTERNAL_STORAGE_PEMISSION_CODE = 730

        fun newInstance(activity: Activity, listener: PermissionListener): PermissionUtils {
            val permission = PermissionUtils()
            permission.activity = activity
            permission.listener = listener
            return permission
        }
    }

    private var activity: Activity? = null
    private var listener: PermissionListener? = null

    fun onRequestPermissionsRequest(requestCode: Int?, permissions: Array<String>,
                                    grantResults: IntArray){
        if (requestCode != null && permissions.size == grantResults.size) {
            permissions.indices
                    .filter { grantResults[it] == PackageManager.PERMISSION_DENIED }
                    .forEach {
                        onPermissionDeny(requestCode)
                        return
                    }
            onPermissionGrant(requestCode)
        }
    }

    private fun requirePermissionRequest(vararg permissions: String): Boolean {
        return permissions.any { ContextCompat.checkSelfPermission(activity!!, it) != PackageManager.PERMISSION_GRANTED }
    }

    private fun showRequestPermissionRationale(vararg permissions: String): Boolean {
        return permissions.any { ActivityCompat.shouldShowRequestPermissionRationale(activity!!, it) }
    }

    override fun onPermissionGrant(permissionCode: Int) : Boolean{
        if (!(listener?.onPermissionGrant(permissionCode) ?: false)) {
            when (permissionCode) {
                READ_AND_RECEIVE_SMS_PERMISSION_CODE -> {
                }
            }
        }
        return true
    }

    override fun onPermissionDeny(permissionCode: Int) : Boolean{
        if (!(listener?.onPermissionDeny(permissionCode) ?: true)) {
            when (permissionCode) {
                READ_AND_RECEIVE_SMS_PERMISSION_CODE -> {
                    // Recursive call to invoke sms permission if not granted
                    getSmsReadAndReceivePermission()
                }
                READ_CONTACT_PERMISSION_CODE -> {
                    // Recursive call to invoke contact permission if not granted
                    getReadContactPermission()
                }
                WRITE_EXTERNAL_STORAGE_PEMISSION_CODE -> {
                    getWritePermission()
                }
            }
        }
        return true
    }

    /**
     * Use this method to invoke any no of permissions with a message
     */
    fun getPermission(message: String, requestCode: Int,
                      vararg permissions: String){
        if (requirePermissionRequest(*permissions)) {
            if (showRequestPermissionRationale(*permissions)) {
                AlertDialog.Builder(activity!!)
                        .setTitle("Grant Permission")
                        .setMessage(message)
                        .setPositiveButton("Grant") { _, _ ->
                            ActivityCompat.requestPermissions(activity!!,
                                    permissions, requestCode)
                        }
                        .create().show()
            } else {
                ActivityCompat.requestPermissions(activity!!, permissions, requestCode)
            }
        } else {
            onPermissionGrant(requestCode)
        }
        return
    }

    fun toastMessage(message: String) {
        Toast.makeText(activity, message, Toast.LENGTH_SHORT).show()
    }

    /**
     * Use this method to read and receive SMS permission
     */
    fun getSmsReadAndReceivePermission(message: String = activity?.
            getString(R.string.get_sms_read_and_receive_permission) ?: "Need Permission") {
        getPermission(message, READ_AND_RECEIVE_SMS_PERMISSION_CODE,
                Manifest.permission.READ_SMS, Manifest.permission.RECEIVE_SMS)
    }

    /**
     * Use this method to read and receive SMS permission
     */
    fun getReadContactPermission(message: String = activity
            ?.getString(R.string.default_read_contact_permission) ?: "Need Permission") {
        getPermission(message, READ_CONTACT_PERMISSION_CODE,
                Manifest.permission.READ_CONTACTS)
    }

    fun getWritePermission(message: String = "Need Permission") {
        getPermission(message, WRITE_EXTERNAL_STORAGE_PEMISSION_CODE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
    }

}

interface PermissionListener {
    fun onPermissionGrant(permissionCode: Int) : Boolean
    fun onPermissionDeny(permissionCode: Int) : Boolean
}
