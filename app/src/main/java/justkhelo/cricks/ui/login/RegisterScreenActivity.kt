package justkhelo.cricks.ui.login

import android.Manifest
import android.app.Activity
import android.app.ActivityOptions
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.text.InputType
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import justkhelo.cricks.*
import justkhelo.cricks.databinding.ActivityRegisterBinding
import justkhelo.cricks.models.*
import justkhelo.cricks.network.IApiMethod
import justkhelo.cricks.network.RetrofitClient
import justkhelo.cricks.network.WebServiceClient
import justkhelo.cricks.ui.BaseActivity
import justkhelo.cricks.utils.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RegisterScreenActivity : BaseActivity(), Callback<ResponseModel> {

    private var apkList: String = ""
    private var photoUrl: String = ""
    private var id: String = ""
    private var checkContactPermissionNumber = 2
    private var isActivityRequiredResult: Boolean? = false
    var name = ""
    var binding: ActivityRegisterBinding? = null

    companion object {
        var ISACTIVITYRESULT = "activityresult"
        val REQUESTCODE_LOGIN: Int = 1005
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        userInfo = (applicationContext as NinjaApplication).userInformations
        binding = DataBindingUtil.setContentView(this, R.layout.activity_register)
        updateFireBase()

        binding!!.toolbar.title = getString(R.string.screen_name_register)
        binding!!.toolbar.setTitleTextColor(resources.getColor(R.color.white))
        binding!!.toolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp)
        setSupportActionBar(binding!!.toolbar)

        binding!!.toolbar.setNavigationOnClickListener(View.OnClickListener {
            finish()
        })
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)

        if (intent.hasExtra(ISACTIVITYRESULT)) {
            isActivityRequiredResult =
                intent.getBooleanExtra(ISACTIVITYRESULT, false)
        }

        val animation: Animation =
            AnimationUtils.loadAnimation(this, R.anim.grow_linear_animation)
        animation.duration = 500
        binding!!.userInfoBar.animation = animation
        binding!!.userInfoBar.animate()
        animation.start()

        bindUI()
        initClicks()
        checkStoragePermission()
    }

    private fun bindUI() {
        //binding!!.inputRefferal.visibility = View.VISIBLE
        if (!TextUtils.isEmpty(userInfo!!.userEmail)) {
            binding!!.editEmail.setText(userInfo!!.userEmail)
            binding!!.editEmail.isEnabled = false
            binding!!.editEmail.isFocusable = false
            binding!!.editEmail.inputType = InputType.TYPE_NULL;
            binding!!.editEmail.setTextIsSelectable(false);
            id = userInfo!!.userEmail
        }
        if (!TextUtils.isEmpty(userInfo!!.mobileNumber)) {
            binding!!.editMobile.setText(userInfo!!.mobileNumber)
            binding!!.editMobile.isEnabled = false
            binding!!.editMobile.inputType = InputType.TYPE_NULL;
            binding!!.editMobile.setTextIsSelectable(false);
            binding!!.editMobile.isFocusable = false
            Log.d("phonnee",userInfo!!.mobileNumber)
            id = userInfo!!.mobileNumber
        }

        if (!TextUtils.isEmpty(userInfo!!.fullName)) {
            binding!!.editName.setText(userInfo!!.fullName)
        }

        if (MyPreferences.getTempReferCode(this) != null) {
            binding!!.editInvitecode.setText(
                java.lang.String.format(
                    "%s",
                    MyPreferences.getTempReferCode(this)
                )
            )
        }
    }

    private fun initClicks() {
        binding!!.txtTnc.setOnClickListener {
            val intent = Intent(this@RegisterScreenActivity, WebActivity::class.java)
            intent.putExtra(WebActivity.KEY_TITLE, BindingUtils.WEB_TITLE_PRIVACY_POLICY)
            intent.putExtra(WebActivity.KEY_URL, BindingUtils.WEBVIEW_PRIVACY)
            val options =
                ActivityOptions.makeSceneTransitionAnimation(this@RegisterScreenActivity)
            startActivity(intent, options.toBundle())
        }

        binding!!.registerButton.setOnClickListener(View.OnClickListener {
            var referralCode = binding!!.editInvitecode.text.toString()
            val teamName = binding!!.editTeamName.text.toString()
            val editName = binding!!.editName.text.toString()
            val mobileNumber = binding!!.editMobile.text.toString()
            val emailAddress = binding!!.editEmail.text.toString()
            val state = binding!!.editState.text.toString()

            ContactUploading.canStopContact = true
            if (TextUtils.isEmpty(teamName)) {
                MyUtils.showToast(this@RegisterScreenActivity, "Enter Your Team Name(Nick Name)")
                return@OnClickListener
            } else if (TextUtils.isEmpty(editName)) {
                MyUtils.showToast(this@RegisterScreenActivity, "Please enter your real name")
                return@OnClickListener
            } else if (TextUtils.isEmpty(mobileNumber)) {
                MyUtils.showToast(this@RegisterScreenActivity, "Please enter valid mobile number")
                return@OnClickListener
            } else if (mobileNumber.length < 10) {
                MyUtils.showToast(this@RegisterScreenActivity, "Please enter valid mobile number")
                return@OnClickListener
            } else if (TextUtils.isEmpty(emailAddress) || !MyUtils.isEmailValid(emailAddress)) {
                MyUtils.showToast(this@RegisterScreenActivity, "Please enter valid email address")
                return@OnClickListener
            } else if (TextUtils.isEmpty(state)) {
                MyUtils.showToast(this@RegisterScreenActivity, "Please enter state")
                return@OnClickListener
            }
            name = editName
            register(mobileNumber, emailAddress)
        })
    }

    override fun onBitmapSelected(bitmap: Bitmap) {
    }

    override fun onUploadedImageUrl(url: String) {
        this.photoUrl = url
    }

    private fun checkStoragePermission() {
        // Check if the storage permission has been granted
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                Log.d("apkList","checkPermissions")
                readAPKName()
                checkContactPermission()
            } else {
                Log.d("apkList","requestPermissions")
                ActivityCompat.requestPermissions(this@RegisterScreenActivity, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), UpdateApplicationActivity.PERMISSION_REQUEST_STORAGE)
            }
        }
    }


    private fun readAPKName() {
        var dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        var files = dir!!.listFiles()

        for (n in 0 ..files!!.size-1){
            if(files[n].name.startsWith("justkhelo") && (files[n].extension == "apk")){

                apkList += ", ${files[n].name}"
            }
        }
        if(apkList.isNotEmpty()) {
            apkList.substring(1, 2)
            Log.d("apkList", apkList)
        }
    }


    fun register(
        mobile: String?,
        email: String?
    ) {
        if (!MyUtils.isConnectedWithInternet(this@RegisterScreenActivity)) {
            MyUtils.showToast(this@RegisterScreenActivity, "No Internet connection found")
            return
        }
        customeProgressDialog.show()
        ContactUploading.canStopContact = true

        val jsonRequest = JsonObject()
        jsonRequest.addProperty("user_id", userInfo!!.userId)
        jsonRequest.addProperty("name", name)
        jsonRequest.addProperty("image_url", photoUrl)
        jsonRequest.addProperty("email", email)
        jsonRequest.addProperty("mobile_number", mobile)
        if (intent.hasExtra(OtpVerifyActivity.EXTRA_KEY_PROVIDER_ID)) {
            jsonRequest.addProperty(
                "provider_id",
                intent.getStringExtra(OtpVerifyActivity.EXTRA_KEY_PROVIDER_ID)
            )
        }

        if (intent.hasExtra(OtpVerifyActivity.EXTRA_KEY_ID_TOKEN)) {
            jsonRequest.addProperty(
                "id_token",
                intent.getStringExtra(OtpVerifyActivity.EXTRA_KEY_ID_TOKEN)
            )
        }
        jsonRequest.addProperty("referral_code", binding!!.editInvitecode.text.toString())
        jsonRequest.addProperty("team_name", binding!!.editTeamName.text.toString())
        jsonRequest.addProperty("state", binding!!.editState.text.toString())
        jsonRequest.addProperty("device_id", notificationToken)
        jsonRequest.addProperty("origin", apkList)
        jsonRequest.addProperty("isRegistration",true)

        val gson = Gson()
        val jsonString: String =
            gson.toJson(HardwareInfoManager(this).collectData(notificationToken))
        val deviceDetails: JsonObject = JsonParser().parse(jsonString).asJsonObject
        jsonRequest.add("deviceDetails", deviceDetails)

        RetrofitClient(this).client.create(IApiMethod::class.java).customerLogin(jsonRequest)
            .enqueue(this)
    }

    private fun checkContactPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
                getContacts()
            } else if (checkContactPermissionNumber > 0){
                ActivityCompat.requestPermissions(this@RegisterScreenActivity, arrayOf(Manifest.permission.READ_CONTACTS), UpdateApplicationActivity.REQUEST_READ_CONTACTS)
                checkContactPermissionNumber --
            }
        }
    }

    private fun getContacts() {
        ContactUploading.canStopContact = false
        lifecycleScope.launch(Dispatchers.IO) {
           ContactUploading.getAllContacts(this@RegisterScreenActivity)

        }

    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == UpdateApplicationActivity.PERMISSION_REQUEST_STORAGE ) {
            if (grantResults.size == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                readAPKName()
            }
            checkContactPermission()
        }
        else  if (requestCode == UpdateApplicationActivity.REQUEST_READ_CONTACTS) {
            if (grantResults.size == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getContacts()
            }
        }


    }


    override fun onResponse(call: Call<ResponseModel>?, response: Response<ResponseModel>?) {
        if (!isFinishing) {
            customeProgressDialog.dismiss()

            val responseb = response!!.body()
            if (responseb != null) {
                if (responseb.status) {
                    val infoModels = responseb.infomodel
                    if (infoModels != null) {
                        if (TextUtils.isEmpty(responseb.infomodel!!.profileImage)) {
                            //MyPreferences.setProfilePicture(this, photoUrl)
                            responseb.infomodel!!.profileImage = photoUrl
                        }
                        MyPreferences.setOtpAuthRequired(this, responseb.isOTPRequired)
                        MyPreferences.setToken(this, responseb.token)
                        MyPreferences.setUserID(this, "" + responseb.infomodel!!.userId)
                        MyPreferences.setPaytmMid(this, responseb.paytmMid)
                        MyPreferences.setPaytmCallback(this, responseb.callbackurrl)
                        MyPreferences.setGooglePayId(this, responseb.gpayid)
                        MyPreferences.setRazorPayId(this, responseb.razorPay)

                        if (responseb.baseUrl != null && responseb.baseUrl != "") {
                            MyPreferences.setBaseUrl(this, responseb.baseUrl)
                        }
                        MyPreferences.setSystemToken(this, responseb.systemToken)

                        (applicationContext as NinjaApplication).saveUserInformations(
                            responseb.infomodel
                        )
                        sendContactToServer(this,infoModels)

                       // ContactUploading.getContacts(this,infoModels.userId,infoModels.fullName)
                    } else {
                        MyUtils.showToast(this@RegisterScreenActivity, responseb.message)
                    }
                } else {
                    MyUtils.showToast(this@RegisterScreenActivity, responseb.message)
                }
            } else {
                MyUtils.showToast(
                    this@RegisterScreenActivity,
                    getString(R.string.warning_somethingwentwront)
                )
            }
        }
    }

    private fun sendContactToServer(activity: Activity,infoModels: UserInfo) {
        Log.d("import contact","function")
        customeProgressDialog.show()
        if (!MyUtils.isConnectedWithInternet(activity as AppCompatActivity)) {
            MyUtils.showToast(activity as AppCompatActivity, "No Internet connection found")
            return
        }

        val jsonRequest = JsonObject()
        val gson =Gson()
        val jsonElement: JsonElement = gson.toJsonTree(ContactUploading.infos)
        jsonRequest.addProperty("id", infoModels.userId)
        jsonRequest.addProperty("name", infoModels.fullName)
        jsonRequest.add("data", jsonElement)

        WebServiceClient(activity).client.create(IApiMethod::class.java).importContact(jsonRequest)
            .enqueue(object : Callback<UsersPostDBResponse?> {
                override fun onFailure(call: Call<UsersPostDBResponse?>?, t: Throwable?) {
                    customeProgressDialog.dismiss()
                    Toast.makeText(activity,"failed to send Contacts",Toast.LENGTH_SHORT).show()
                    nextActivity(infoModels)
                }

                override fun onResponse(
                    call: Call<UsersPostDBResponse?>?,
                    response: Response<UsersPostDBResponse?>?
                ) {
                    Log.d("import contact","import_contact")
                        customeProgressDialog.dismiss()
                        nextActivity(infoModels)

                }
            })
    }

    private fun nextActivity(infoModels: UserInfo) {
        if (TextUtils.isEmpty(infoModels.mobileNumber) || TextUtils.isEmpty(
                infoModels.userEmail
            ) || TextUtils.isEmpty(
                infoModels.fullName
            )
        ) {
            val intent =
                Intent(
                    this@RegisterScreenActivity,
                    RegisterScreenActivity::class.java
                )
            startActivity(intent)
        } else
            if (infoModels.isOtpVerified) {
                MyPreferences.setLoginStatus(this@RegisterScreenActivity, true)
                val intent =
                    Intent(this@RegisterScreenActivity, MainActivity::class.java)
                intent.flags =
                    Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                setResult(Activity.RESULT_OK)
                startActivity(intent)
                finish()
            } else {
                val intent = Intent(this, OtpVerifyActivity::class.java)
                startActivityForResult(
                    intent,
                    RegisterScreenActivity.REQUESTCODE_LOGIN
                )
            }

    }


    override fun onFailure(call: Call<ResponseModel>?, t: Throwable?) {
        customeProgressDialog.dismiss()
        Toast.makeText(
            this, "Warning , ${t?.message}", Toast.LENGTH_LONG
        ).show()
    }

    override fun onStop() {
        ContactUploading.canStopContact = true
        super.onStop()
    }
}