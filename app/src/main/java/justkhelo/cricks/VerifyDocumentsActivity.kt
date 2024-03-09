package justkhelo.cricks

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import justkhelo.cricks.databinding.ActivityVerifyDocumentBinding
import justkhelo.cricks.models.ResponseModel
import justkhelo.cricks.models.UserInfo
import justkhelo.cricks.network.IApiMethod
import justkhelo.cricks.network.WebServiceClient
import justkhelo.cricks.ui.BaseActivity
import justkhelo.cricks.utils.CustomeProgressDialog
import justkhelo.cricks.utils.MyPreferences
import justkhelo.cricks.utils.MyUtils
import justkhelo.cricks.utils.setLocalImage
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.util.*

class VerifyDocumentsActivity : AppCompatActivity() {

    private lateinit var userInfo: UserInfo
    private lateinit var customeProgressDialog: CustomeProgressDialog
    private lateinit var mContext: Context
    private var mPanImageFile: File? = null
    private var mPassbookImageFile: File? = null

    private var mBinding: ActivityVerifyDocumentBinding? = null
    private var isMobileNumberVerified = true
    private var isEmailVeirfied = true

    var pancardDocumentUrl: String = ""
    var bankPassbookUrl: String = ""

    companion object {
        private var TAG: String = VerifyDocumentsActivity::class.java.simpleName
        var REQUESTCODE_VERIFY_DOC = 1008
        private const val PASSBOOK_IMAGE_REQ_CODE = 102
        private const val PAN_IMAGE_REQ_CODE = 103
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = DataBindingUtil.setContentView(
            this,
            R.layout.activity_verify_document
        )

        mContext = this

        userInfo = (application as NinjaApplication).userInformations
        customeProgressDialog = CustomeProgressDialog(mContext)

        mBinding!!.toolbar.title = "Verify Documents"
        mBinding!!.toolbar.setTitleTextColor(resources.getColor(R.color.white))
        mBinding!!.toolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp)
        setSupportActionBar(mBinding!!.toolbar)
        mBinding!!.toolbar.setNavigationOnClickListener(View.OnClickListener {
            finish()
        })

        customeProgressDialog = CustomeProgressDialog(this)

        initCommunication()

        mBinding!!.imgPancard.setOnClickListener {
            selectImage()
        }

        mBinding!!.imgBankPassbook.setOnClickListener {
            selectImagePassbook()
        }

        initDocumentSubmit()
    }

    private fun initCommunication() {
        if (isMobileNumberVerified) {
            mBinding!!.linearMobileBorder.setBackgroundResource(R.drawable.btn_selector_verified)
            mBinding!!.verifyMobileMessage.text = "Your mobile number verified"
            mBinding!!.verifyMobileNumber.text = userInfo.mobileNumber
            mBinding!!.verifyMobileNumber.setTextColor(resources.getColor(R.color.green))

        } else {
            mBinding!!.linearMobileBorder.setBackgroundResource(R.drawable.btn_selector_not_verified)
            mBinding!!.verifyMobileMessage.text = "Your mobile number not verified"
            mBinding!!.verifyMobileNumber.text = userInfo.mobileNumber
            mBinding!!.verifyMobileNumber.setTextColor(resources.getColor(R.color.red))
        }

        if (isEmailVeirfied) {
            mBinding!!.linearEmailBorder.setBackgroundResource(R.drawable.btn_selector_verified)
            mBinding!!.verifyEmailMessage.text = "Your Email Address verified"
            mBinding!!.verifyEmailAddress.text = userInfo.userEmail
            mBinding!!.verifyEmailAddress.setTextColor(resources.getColor(R.color.green))
        } else {
            mBinding!!.linearEmailBorder.setBackgroundResource(R.drawable.btn_selector_not_verified)
            mBinding!!.verifyEmailMessage.text = "Your Email Address not verified"
            mBinding!!.verifyEmailAddress.text = userInfo.userEmail
            mBinding!!.verifyEmailAddress.setTextColor(resources.getColor(R.color.red))
        }
    }

    private fun selectImage() {
        val options: Array<CharSequence> =
            arrayOf<CharSequence>("Take Photo", "Choose from Gallery", "Cancel")

        val builder: AlertDialog.Builder = AlertDialog.Builder(mContext)
        builder.setTitle("Add Photo")
        builder.setItems(options) { dialog, items ->
            if (options[items] == "Take Photo") {
                getImageCamera()
            } else if (options[items] == "Choose from Gallery") {
                getImageGallery()
            } else if (options[items] == "Cancel") {
                dialog!!.dismiss()
            }
        }
        builder.show()
    }

    private fun getImageCamera() {
        ImagePicker.with(this)
            .cameraOnly() // User can only capture image from Camera
            .crop() // Crop Image(User can choose Aspect Ratio)
            .compress(2048) // Image size will be less than 1024 KB
            .saveDir(File(cacheDir, "Ninja11")) // External file path
            .start(PAN_IMAGE_REQ_CODE)
    }

    private fun getImageGallery() {
        ImagePicker.with(this)
            .galleryOnly() // User can only select image from Gallery
            .crop() // Crop Image(User can choose Aspect Ratio)
            .compress(2048) // Image size will be less than 2048 KB
            .saveDir(File(cacheDir, "Ninja11")) // External file path
            .galleryMimeTypes(  //Exclude gif images
                mimeTypes = arrayOf(
                    "image/png",
                    "image/jpg",
                    "image/jpeg"
                )
            )
            .maxResultSize(1080, 1920) // Image resolution will be less than 1080 x 1920
            .start(PAN_IMAGE_REQ_CODE)
    }

    private fun selectImagePassbook() {
        val options: Array<CharSequence> =
            arrayOf<CharSequence>("Take Photo", "Choose from Gallery", "Cancel")

        val builder: AlertDialog.Builder = AlertDialog.Builder(mContext)
        builder.setTitle("Add Photo")
        builder.setItems(options) { dialog, items ->
            if (options[items] == "Take Photo") {
                getImageCameraPassbook()
            } else if (options[items] == "Choose from Gallery") {
                getImageGalleryPassbook()
            } else if (options[items] == "Cancel") {
                dialog!!.dismiss()
            }
        }
        builder.show()
    }

    private fun getImageCameraPassbook() {
        ImagePicker.with(this)
            .cameraOnly() // User can only capture image from Camera
            .crop() // Crop Image(User can choose Aspect Ratio)
            .compress(2048) // Image size will be less than 1024 KB
            .saveDir(File(cacheDir, "Ninja11")) // External file path
            .start(PASSBOOK_IMAGE_REQ_CODE)
    }

    private fun getImageGalleryPassbook() {
        ImagePicker.with(this)
            .galleryOnly() // User can only select image from Gallery
            .crop() // Crop Image(User can choose Aspect Ratio)
            .compress(2048) // Image size will be less than 2048 KB
            .saveDir(File(cacheDir, "Ninja11")) // External file path
            .galleryMimeTypes(  //Exclude gif images
                mimeTypes = arrayOf(
                    "image/png",
                    "image/jpg",
                    "image/jpeg"
                )
            )
            .maxResultSize(1080, 1920) // Image resolution will be less than 1080 x 1920
            .start(PASSBOOK_IMAGE_REQ_CODE)
    }

    private fun initDocumentSubmit() {
        mBinding!!.btnSubmitVerification.setOnClickListener(View.OnClickListener {
            val panCardName = mBinding!!.editPancardName.text.toString()
            val panCardNumber = mBinding!!.editPancardNumber.text.toString()
            val panCardConfirmNumber = mBinding!!.editPancardConfirmNumber.text.toString()
            val paytmNumber = mBinding!!.editPaytmNumber.text.toString()

            val bankName = mBinding!!.editBankName.text.toString()
            val accountHolderName = mBinding!!.editAccountHolderName.text.toString()
            val accountNumber = mBinding!!.editAccountNumber.text.toString()
            val ifscCode = mBinding!!.editAccountIfscCode.text.toString()
            val accountType = mBinding!!.editAccoutType.text.toString()
            val UPI_Id = mBinding!!.editUpiId.text.toString()


            if (TextUtils.isEmpty(panCardName)) {
                MyUtils.showToast(this@VerifyDocumentsActivity, "Please enter name as on Pan card")
                return@OnClickListener
            } else if (TextUtils.isEmpty(panCardNumber)) {
                MyUtils.showToast(this@VerifyDocumentsActivity, "Please enter Pan card number")
                return@OnClickListener
            } else if (TextUtils.isEmpty(panCardConfirmNumber)) {
                MyUtils.showToast(this@VerifyDocumentsActivity, "Please confirm Pan card number")
                return@OnClickListener
            } else if (!panCardNumber.equals(panCardConfirmNumber)) {
                MyUtils.showToast(
                    this@VerifyDocumentsActivity,
                    "Both Pan card number does not matched"
                )
                return@OnClickListener
            } else if (TextUtils.isEmpty(pancardDocumentUrl)) {
                MyUtils.showToast(
                    this@VerifyDocumentsActivity,
                    "Please upload picture of pan card"
                )
                return@OnClickListener
            } else if (TextUtils.isEmpty(bankName)) {

                MyUtils.showToast(this@VerifyDocumentsActivity, "Please enter your Bank name")
                return@OnClickListener
            } else if (TextUtils.isEmpty(accountHolderName)) {
                MyUtils.showToast(this@VerifyDocumentsActivity, "Please your name on Bank card")
                return@OnClickListener
            } else if (TextUtils.isEmpty(accountNumber)) {
                MyUtils.showToast(this@VerifyDocumentsActivity, "Please enter Bank account number")
                return@OnClickListener
            } else if (TextUtils.isEmpty(ifscCode)) {
                MyUtils.showToast(this@VerifyDocumentsActivity, "Please enter IFSC code")
                return@OnClickListener
            } else if (TextUtils.isEmpty(accountType)) {
                MyUtils.showToast(this@VerifyDocumentsActivity, "Please enter account type")
                return@OnClickListener
            } else if (TextUtils.isEmpty(bankPassbookUrl)) {
                MyUtils.showToast(
                    this@VerifyDocumentsActivity,
                    "Please upload picture of passbook or cheque"
                )
                return@OnClickListener
            } else if (TextUtils.isEmpty(paytmNumber)) {
                MyUtils.showToast(this@VerifyDocumentsActivity, "Please enter your Paytm number")
                return@OnClickListener
            } else if (TextUtils.isEmpty(UPI_Id)) {
                MyUtils.showToast(this@VerifyDocumentsActivity, "Please enter your UPI Id")
            }

            val models = JSONObject()
            models.put("user_id", userInfo.userId)
            models.put("panCardName", panCardName)
            models.put("panCardNumber", panCardConfirmNumber)
            models.put("pancardDocumentUrl", pancardDocumentUrl)

            models.put("bankName", bankName)
            models.put("accountHolderName", accountHolderName)
            models.put("accountNumber", accountNumber)
            models.put("ifscCode", ifscCode)
            models.put("accountType", accountType)
            models.put("bankPassbookUrl", bankPassbookUrl)

            models.put("paytmNumber", paytmNumber)
            models.put("upi_id", UPI_Id)
            models.put("documentType", BaseActivity.DOCUMENT_TYPE_PANCARD)
            models.put("system_token", MyPreferences.getSystemToken(this)!!)

            if (MyUtils.isConnectedWithInternet(this@VerifyDocumentsActivity)) {
                submitDocuments(models)
            } else {
                MyUtils.showToast(
                    this@VerifyDocumentsActivity,
                    "Please check your internet connections"
                )
            }
        })
    }

    private fun submitDocuments(models: JSONObject) {
        customeProgressDialog.show()

        val jsonObject: JsonObject = JsonParser().parse(models.toString()) as JsonObject

        if (MyUtils.isConnectedWithInternet(this@VerifyDocumentsActivity)) {
            WebServiceClient(this@VerifyDocumentsActivity).client.create(IApiMethod::class.java)
                .saveAllDocuments(jsonObject)
                .enqueue(object : Callback<ResponseModel?> {
                    override fun onFailure(call: Call<ResponseModel?>?, t: Throwable?) {
                        customeProgressDialog.dismiss()
                        MyUtils.showToast(this@VerifyDocumentsActivity, t!!.localizedMessage!!)
                    }

                    override fun onResponse(
                        call: Call<ResponseModel?>?,
                        response: Response<ResponseModel?>?
                    ) {
                        customeProgressDialog.dismiss()
                        val res = response!!.body()
                        if (res != null) {
                            if (res.status) {
                                Toast.makeText(
                                    this@VerifyDocumentsActivity,
                                    res.message,
                                    Toast.LENGTH_LONG
                                ).show()
                                setResult(RESULT_OK)
                                finish()
                            } else {
                                if (res.statusCode == 1001) {
                                    MyUtils.showMessage(this@VerifyDocumentsActivity, res.message)
                                    MyUtils.logoutApp(this@VerifyDocumentsActivity)
                                } else {
                                    MyUtils.showToast(this@VerifyDocumentsActivity, res.message)
                                }
                            }
                        }
                    }
                })
        } else {
            MyUtils.showToast(
                this@VerifyDocumentsActivity,
                "Please check your internet connections"
            )
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            Log.e(TAG, "Path:${ImagePicker.getFilePath(data)}")
            val file = ImagePicker.getFile(data)!! // File object will not be null for RESULT_OK
            when (requestCode) {
                PAN_IMAGE_REQ_CODE -> {
                    mPanImageFile = file
                    mBinding!!.imgPancard.setLocalImage(file, false)
                    uploadImageToServer(file, BaseActivity.DOCUMENT_TYPE_PANCARD)
                }
                PASSBOOK_IMAGE_REQ_CODE -> {
                    mPassbookImageFile = file
                    mBinding!!.imgBankPassbook.setLocalImage(file, false)
                    uploadImageToServer(file, BaseActivity.DOCUMENT_TYPE_BANK_PASSBOOK)
                }
            }
        } else if (resultCode == ImagePicker.RESULT_ERROR) {
            MyUtils.showToast(this, ImagePicker.getError(data))
        } else {
            MyUtils.showToast(this, "Task Cancelled")
        }
    }

    private fun uploadImageToServer(file: File, docType: String) {

        var multipartImage: MultipartBody.Part? = null
        val requestPanImage: RequestBody = file
            .asRequestBody("multipart/jpg".toMediaTypeOrNull())
        multipartImage =
            MultipartBody.Part.createFormData("image_bytes", file.name, requestPanImage)

        val userId: RequestBody = createPartFromString(MyPreferences.getUserID(mContext)!!)
        val documentType: RequestBody = createPartFromString(docType)
        val systemToken: RequestBody =
            createPartFromString(MyPreferences.getSystemToken(mContext)!!)

        val map: HashMap<String, RequestBody> = HashMap<String, RequestBody>()
        map["user_id"] = userId
        map["documents_type"] = documentType
        map["system_token"] = systemToken

        customeProgressDialog.show()
        WebServiceClient(mContext).client.create(IApiMethod::class.java)
            .saveDocumentImage(map, multipartImage)
            .enqueue(object : Callback<ResponseModel?> {
                override fun onFailure(call: Call<ResponseModel?>?, t: Throwable?) {
                    customeProgressDialog.dismiss()
                    MyUtils.showToast(this@VerifyDocumentsActivity, t!!.localizedMessage!!)
                }

                override fun onResponse(
                    call: Call<ResponseModel?>?,
                    response: Response<ResponseModel?>?
                ) {
                    if (!isFinishing) {
                        customeProgressDialog.dismiss()
                        val res = response!!.body()
                        if (res != null) {
                            if (res.status) {
                                if (docType == BaseActivity.DOCUMENT_TYPE_PANCARD) {
                                    pancardDocumentUrl = res.image_url
                                } else {
                                    bankPassbookUrl = res.image_url
                                }
                                MyUtils.showMessage(mContext, res.message)
                            } else {
                                MyUtils.showMessage(mContext, res.message)
                            }
                        }
                    }
                }
            })
    }

    private fun createPartFromString(param: String): RequestBody {
        return param.toRequestBody("text/plain".toMediaTypeOrNull())
    }
}