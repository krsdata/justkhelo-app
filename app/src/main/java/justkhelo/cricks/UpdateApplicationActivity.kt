package justkhelo.cricks

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import androidx.databinding.DataBindingUtil
import justkhelo.cricks.databinding.ActivityUpdateApplicationBinding
import justkhelo.cricks.ui.BaseActivity
import justkhelo.cricks.utils.CustomeProgressDialog
import justkhelo.cricks.utils.DownloadController


class UpdateApplicationActivity : BaseActivity() {

    private var mBinding: ActivityUpdateApplicationBinding? = null
    lateinit var downloadController: DownloadController

    companion object {
        val TAG: String = UpdateApplicationActivity::class.java.simpleName
        val REQUEST_CODE_APK_UPDATE: String = "apkupdateurl"
        val REQUEST_RELEASE_NOTE: String = "release_note"
        const val PERMISSION_REQUEST_STORAGE = 0
        const val REQUEST_READ_CONTACTS = 0
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = DataBindingUtil.setContentView(
            this,
            R.layout.activity_update_application
        )
        customeProgressDialog = CustomeProgressDialog(this)

        val apkUrl = intent.getStringExtra(REQUEST_CODE_APK_UPDATE)
        val releaseNote = intent.getStringExtra(REQUEST_RELEASE_NOTE)
        Log.e(TAG, "releaseNotes ======> $releaseNote")
        if (!TextUtils.isEmpty(releaseNote)) {
            mBinding!!.releaseNote.text = releaseNote
        }
        downloadController = DownloadController(this, apkUrl!!, customeProgressDialog)

        mBinding!!.toolbar.title = this@UpdateApplicationActivity.getString(R.string.label_update)
        mBinding!!.toolbar.setTitleTextColor(resources.getColor(R.color.white))
        mBinding!!.toolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp)
        setSupportActionBar(mBinding!!.toolbar)
        mBinding!!.toolbar.setNavigationOnClickListener {
            finish()
        }

        mBinding!!.addCash.setOnClickListener {
            checkStoragePermission()
        }
    }

    override fun onBitmapSelected(bitmap: Bitmap) {
    }

    override fun onUploadedImageUrl(url: String) {
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == PERMISSION_REQUEST_STORAGE) {
            // Request for camera permission.
            if (grantResults.size == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // start downloading
                downloadController.enqueueDownload()
            }
        }
    }

    private fun checkStoragePermission() {
        // Check if the storage permission has been granted
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                downloadController.enqueueDownload()
            } else {
                requestStoragePermission()
            }
        }
    }

    private fun requestStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                requestPermissions(
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    PERMISSION_REQUEST_STORAGE
                )
            } else {
                requestPermissions(
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    PERMISSION_REQUEST_STORAGE
                )
            }
        }
    }


    override fun onBackPressed() {
        //super.onBackPressed()
        val intent = Intent(Intent.ACTION_MAIN)
        intent.addCategory(Intent.CATEGORY_HOME)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
    }
}