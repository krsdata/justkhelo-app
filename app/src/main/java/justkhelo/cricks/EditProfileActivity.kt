package justkhelo.cricks

import android.app.Activity
import android.app.DatePickerDialog
import android.app.DatePickerDialog.OnDateSetListener
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
import com.bumptech.glide.Glide
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.gson.JsonObject
import justkhelo.cricks.databinding.ActivityEditProfileBinding
import justkhelo.cricks.models.ResponseModel
import justkhelo.cricks.models.UserInfo
import justkhelo.cricks.models.UsersPostDBResponse
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
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.text.DecimalFormat
import java.util.*

class EditProfileActivity : AppCompatActivity() {

    private var mBinding: ActivityEditProfileBinding? = null
    private var photoUrl: String = ""
    private lateinit var userInfo: UserInfo
    private lateinit var customeProgressDialog: CustomeProgressDialog
    private lateinit var mContext: Context
    private var mImageFile: File? = null

    companion object {
        private var TAG: String = EditProfileActivity::class.java.simpleName
        private const val GALLERY_IMAGE_REQ_CODE = 102
        private const val CAMERA_IMAGE_REQ_CODE = 103
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = DataBindingUtil.setContentView(
            this,
            R.layout.activity_edit_profile
        )
        mContext = this

        userInfo = (application as NinjaApplication).userInformations
        customeProgressDialog = CustomeProgressDialog(mContext)

        mBinding!!.toolbar.title = "Update Profile"
        mBinding!!.toolbar.setTitleTextColor(resources.getColor(R.color.white))
        mBinding!!.toolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp)
        setSupportActionBar(mBinding!!.toolbar)
        mBinding!!.toolbar.setNavigationOnClickListener(View.OnClickListener {
            finish()
        })

        Glide.with(this)
            .load(userInfo.profileImage)
            .placeholder(R.drawable.ic_profile)
            .into(mBinding!!.profileImage)

        updateUserOtherInfo()

        mBinding!!.profileImage.setOnClickListener {
            if (!TextUtils.isEmpty(photoUrl)) {
                val intent =
                    Intent(this@EditProfileActivity, FullScreenImageViewActivity::class.java)
                intent.putExtra(FullScreenImageViewActivity.KEY_IMAGE_URL, photoUrl)
                startActivity(intent)
            } else {
                selectImage()
            }
        }

        mBinding!!.imageEdit.setOnClickListener {
            selectImage()
        }

        mBinding!!.dateOfBirth.setOnClickListener(View.OnClickListener {
            val c = Calendar.getInstance()
            val mYear = c[Calendar.YEAR]
            val mMonth = c[Calendar.MONTH]
            val mDay = c[Calendar.DAY_OF_MONTH]

            val datePickerDialog = DatePickerDialog(
                this,
                OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
                    val a = monthOfYear + 1
                    val formatter = DecimalFormat("00")
                    val month = formatter.format(a.toLong())

                    val formatter2 = DecimalFormat("00")
                    val date = formatter2.format(dayOfMonth.toLong())

                    mBinding!!.dateOfBirth.setText(
                        String.format(
                            Locale.ENGLISH,
                            "%s-%s-%d",
                            date,
                            month,
                            year
                        )
                    )
                }, mYear, mMonth, mDay
            )
            datePickerDialog.datePicker.maxDate = System.currentTimeMillis()- 568080000000
            datePickerDialog.show()
        })

        mBinding!!.btnUpdateProfile.setOnClickListener {
            updateProfile()
        }
        initProfile()
        getProfile()
    }

    private fun updateUserOtherInfo() {
        if (!TextUtils.isEmpty(userInfo.teamName)) {
            mBinding!!.editTeamName.setText(userInfo.teamName)
            mBinding!!.editTeamName.setSelection(userInfo.teamName.length)
        }

        if (!TextUtils.isEmpty(userInfo.dateOfBirth)) {
            mBinding!!.dateOfBirth.setText(userInfo.dateOfBirth)
        }

        if (!TextUtils.isEmpty(userInfo.city)) {
            mBinding!!.editCity.setText(userInfo.city)
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
            .saveDir(
                File(
                    cacheDir,
                    "Ninja11"
                )
            ) // External file path
            .start(CAMERA_IMAGE_REQ_CODE)
    }

    private fun getImageGallery() {
        ImagePicker.with(this)
            .galleryOnly() // User can only select image from Gallery
            .crop() // Crop Image(User can choose Aspect Ratio)
            .compress(2048) // Image size will be less than 2048 KB
            .saveDir(
                File(
                    cacheDir,
                    "Ninja11"
                )
            ) // External file path
            .galleryMimeTypes(  //Exclude gif images
                mimeTypes = arrayOf(
                    "image/png",
                    "image/jpg",
                    "image/jpeg"
                )
            )
            .maxResultSize(1080, 1920) // Image resolution will be less than 1080 x 1920
            .start(GALLERY_IMAGE_REQ_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            Log.e(TAG, "Path:${ImagePicker.getFilePath(data)}")
            val file = ImagePicker.getFile(data)!! // File object will not be null for RESULT_OK
            when (requestCode) {
                GALLERY_IMAGE_REQ_CODE -> {
                    mImageFile = file
                    mBinding!!.profileImage.setLocalImage(file, true)
                    uploadImageToServer(file)
                }
                CAMERA_IMAGE_REQ_CODE -> {
                    mImageFile = file
                    mBinding!!.profileImage.setLocalImage(file, true)
                    uploadImageToServer(file)
                }
            }
        } else if (resultCode == ImagePicker.RESULT_ERROR) {
            MyUtils.showToast(this, ImagePicker.getError(data))
        } else {
            MyUtils.showToast(this, "Task Cancelled")
        }
    }

    private fun initProfile() {
        photoUrl = userInfo.profileImage
        mBinding!!.editTeamName.setText(userInfo.teamName)
        mBinding!!.updateProfileName.setText(userInfo.fullName)
        mBinding!!.updateEmail.setText(userInfo.userEmail)
        mBinding!!.updateEditMobile.setText(userInfo.mobileNumber)

        if (userInfo.gender.equals("male")) {
            mBinding!!.genderMale.isChecked = true
            mBinding!!.genderFemale.isChecked = false
        } else {
            mBinding!!.genderMale.isChecked = false
            mBinding!!.genderFemale.isChecked = true
        }

        if (userInfo.profileImage.isNotEmpty())
            Glide.with(this).load(userInfo.profileImage).placeholder(R.drawable.player_blue)
                .into(mBinding!!.profileImage)
    }

    private fun updateProfile() {
        if (!MyUtils.isConnectedWithInternet(this)) {
            MyUtils.showToast(this, "No Internet connection found")
            return
        }
        val editTeamName = mBinding!!.editTeamName.text.toString()
        val editName = mBinding!!.updateProfileName.text.toString()
        val mobileNumber = mBinding!!.updateEditMobile.text.toString()
        val emailAddress = mBinding!!.updateEmail.text.toString()
        val cityName = mBinding!!.editCity.text.toString()
        var gender = "male"
        if (!mBinding!!.genderMale.isChecked) {
            gender = "female"
        }
        val dateOfBirth = mBinding!!.dateOfBirth.text.toString()

        if (TextUtils.isEmpty(editName)) {
            MyUtils.showToast(this@EditProfileActivity, "Please enter your real name")
            return
        } else if (TextUtils.isEmpty(mobileNumber)) {
            MyUtils.showToast(this@EditProfileActivity, "Please enter valid mobile number")
            return
        } else if (mobileNumber.length < 10) {
            MyUtils.showToast(this@EditProfileActivity, "Please enter valid mobile number")
            return
        } else if (TextUtils.isEmpty(emailAddress) || !MyUtils.isEmailValid(emailAddress)) {
            MyUtils.showToast(this@EditProfileActivity, "Please enter valid email address")
            return
        } else if (TextUtils.isEmpty(cityName)) {
            MyUtils.showToast(this@EditProfileActivity, "Please enter city Name")
            return
        } else if (TextUtils.isEmpty(dateOfBirth)) {
            MyUtils.showToast(this@EditProfileActivity, "Please enter your Date of Birth")
            return
        }

        mBinding!!.progressBar.visibility = View.VISIBLE

        val jsonRequest = JsonObject()
        jsonRequest.addProperty("user_id", MyPreferences.getUserID(this)!!)
        jsonRequest.addProperty("system_token", MyPreferences.getSystemToken(this)!!)
        jsonRequest.addProperty("image_url", photoUrl)
        jsonRequest.addProperty("team_name", mBinding!!.editTeamName.text.toString())
        jsonRequest.addProperty("name", editName)
        jsonRequest.addProperty("email", emailAddress)
        jsonRequest.addProperty("mobile_number", mobileNumber)
        jsonRequest.addProperty("city", cityName)
        jsonRequest.addProperty("gender", gender)
        jsonRequest.addProperty("dateOfBirth", dateOfBirth)

        WebServiceClient(this).client.create(IApiMethod::class.java).updateProfile(jsonRequest)
            .enqueue(object : Callback<UsersPostDBResponse?> {
                override fun onFailure(call: Call<UsersPostDBResponse?>?, t: Throwable?) {
                    mBinding!!.progressBar.visibility = View.GONE
                }

                override fun onResponse(
                    call: Call<UsersPostDBResponse?>?,
                    response: Response<UsersPostDBResponse?>?
                ) {
                    mBinding!!.progressBar.visibility = View.GONE
                    val res = response!!.body()
                    if (res != null) {
                        if (res.status) {
                            userInfo.profileImage = photoUrl
                            userInfo.teamName = editTeamName
                            userInfo.fullName = editName
                            userInfo.city = cityName
                            userInfo.gender = gender
                            userInfo.dateOfBirth = dateOfBirth

                            (application as NinjaApplication).saveUserInformations(userInfo)
                            MyUtils.showMessage(mContext, "Profile updated successfully")
                        } else {
                            if (res.code == 1001) {
                                MyUtils.showMessage(this@EditProfileActivity, res.message)
                                MyUtils.logoutApp(this@EditProfileActivity)
                            } else {
                                MyUtils.showMessage(this@EditProfileActivity, res.message)
                            }
                        }
                    }
                }
            })
    }

    private fun getProfile() {
        if (!MyUtils.isConnectedWithInternet(this)) {
            MyUtils.showToast(this, "No Internet connection found")
            return
        }
        customeProgressDialog.show()

        val jsonRequest = JsonObject()
        jsonRequest.addProperty("user_id", MyPreferences.getUserID(this)!!)
        jsonRequest.addProperty("system_token", MyPreferences.getSystemToken(this)!!)

        WebServiceClient(this).client.create(IApiMethod::class.java).getProfile(jsonRequest)
            .enqueue(object : Callback<ResponseModel?> {
                override fun onFailure(call: Call<ResponseModel?>?, t: Throwable?) {
                    customeProgressDialog.dismiss()
                }

                override fun onResponse(
                    call: Call<ResponseModel?>?,
                    response: Response<ResponseModel?>?
                ) {
                    customeProgressDialog.dismiss()
                    val res = response!!.body()
                    if (res != null) {
                        if (res.status) {
                            val infoModels = res.infomodel
                            if (infoModels != null) {
                                (application as NinjaApplication).saveUserInformations(infoModels)
                                userInfo = (application as NinjaApplication).userInformations
                                initProfile()
                                updateUserOtherInfo()
                            } else {
                                MyUtils.showToast(
                                    this@EditProfileActivity,
                                    "Something went wrong, please contact admin"
                                )
                            }
                        } else {
                            if (res.statusCode == 1001) {
                                MyUtils.showMessage(this@EditProfileActivity, res.message)
                                MyUtils.logoutApp(this@EditProfileActivity)
                            } else {
                                MyUtils.showMessage(this@EditProfileActivity, res.message)
                            }
                        }
                    }
                }
            })
    }

    private fun uploadImageToServer(file: File) {

        var multipartImage: MultipartBody.Part? = null
        val requestPanImage: RequestBody = file
            .asRequestBody("multipart/jpg".toMediaTypeOrNull())
        multipartImage = MultipartBody.Part.createFormData("image_bytes", file.name, requestPanImage)

        val userId: RequestBody = createPartFromString(MyPreferences.getUserID(mContext)!!)
        val documentType: RequestBody = createPartFromString(BaseActivity.DOCUMENTS_TYPE_PROFILES)
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
                    MyUtils.showToast(this@EditProfileActivity, t!!.localizedMessage!!)
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
                                photoUrl = res.image_url
                                userInfo.profileImage = res.image_url
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