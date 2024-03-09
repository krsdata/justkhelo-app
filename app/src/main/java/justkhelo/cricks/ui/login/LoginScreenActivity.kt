package justkhelo.cricks.ui.login

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import justkhelo.cricks.*
import justkhelo.cricks.databinding.ActivityLoginBinding
import justkhelo.cricks.models.ResponseModel
import justkhelo.cricks.models.UserInfo
import justkhelo.cricks.network.IApiMethod
import justkhelo.cricks.network.RetrofitClient
import justkhelo.cricks.ui.BaseActivity
import justkhelo.cricks.utils.BindingUtils
import justkhelo.cricks.utils.HardwareInfoManager
import justkhelo.cricks.utils.MyPreferences
import justkhelo.cricks.utils.MyUtils
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class LoginScreenActivity : BaseActivity(), Callback<ResponseModel> {

    private var firebaseProvider: String = ""
    var photoUrl: String = ""
    private var isActivityRequiredResult: Boolean? = false
    private val rC_SIGN_IN: Int = 1
    lateinit var mGoogleSignInClient: GoogleSignInClient
    lateinit var mGoogleSignInOptions: GoogleSignInOptions
    private lateinit var firebaseAuth: FirebaseAuth
    var name = ""
    var emailId = ""
    private var idToken = ""
    var binding: ActivityLoginBinding? = null

    companion object {
        var AUTH_TYPE_GMAIL = "googleAuth"
        var TAG: String = LoginScreenActivity::class.java.simpleName
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_login)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)
        firebaseAuth = FirebaseAuth.getInstance()
        window.statusBarColor = resources.getColor(R.color.gradient_start)

        if (intent.hasExtra(RegisterScreenActivity.ISACTIVITYRESULT)) {
            isActivityRequiredResult =
                intent.getBooleanExtra(RegisterScreenActivity.ISACTIVITYRESULT, false)
        }

        val animation: Animation =
            AnimationUtils.loadAnimation(this, R.anim.grow_linear_animation)
        animation.duration = 500
        binding!!.loginPanel.animation = animation
        binding!!.loginPanel.animate()
        animation.start()

        configureGoogleSignIn()
        processStep1()
        initClicks()
    }

    private fun initClicks() {

        binding!!.signInButton.setOnClickListener {
            signIn()
        }

        binding!!.signInWithPhoneButton.setOnClickListener {
            val intent = Intent(this@LoginScreenActivity, PhoneNumberActivity::class.java)
            startActivity(intent)
        }

        binding!!.termsCondition.setOnClickListener {
            val intent = Intent(this@LoginScreenActivity, WebActivity::class.java)
            intent.putExtra(WebActivity.KEY_TITLE, BindingUtils.WEB_TITLE_TERMS_CONDITION)
            intent.putExtra(WebActivity.KEY_URL, BindingUtils.WEBVIEW_TNC)
            startActivity(intent)
        }

        binding!!.privacyPolicy.setOnClickListener {
            val intent = Intent(this@LoginScreenActivity, WebActivity::class.java)
            intent.putExtra(WebActivity.KEY_TITLE, BindingUtils.WEB_TITLE_PRIVACY_POLICY)
            intent.putExtra(WebActivity.KEY_URL, BindingUtils.WEBVIEW_PRIVACY)
            startActivity(intent)
        }
        binding!!.txtTrouble.setOnClickListener{
            try {
                val text = ""
                val intent = Intent(Intent.ACTION_VIEW)
                intent.data =
                    Uri.parse("http://api.whatsapp.com/send?phone=${BindingUtils.PHONE_NUMBER}&text=$text")
                startActivity(intent)
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun configureGoogleSignIn() {
        mGoogleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        mGoogleSignInClient = GoogleSignIn.getClient(this, mGoogleSignInOptions)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == rC_SIGN_IN) {
            val task: Task<GoogleSignInAccount> = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account!!)
            } catch (e: ApiException) {
                Log.e(TAG, "ApiException message =========> ${e.message}")
                Log.e(TAG, "ApiException statusCode =========> ${e.statusCode}")
                Toast.makeText(this, "Google sign in failed:(", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onBitmapSelected(bitmap: Bitmap) {

    }

    override fun onUploadedImageUrl(url: String) {
        photoUrl = url
    }

    private fun firebaseAuthWithGoogle(acct: GoogleSignInAccount) {

        Log.e(TAG, "email =========> " + acct.email)
        Log.e(TAG, "displayName =========> " + acct.displayName)

        customeProgressDialog.show()
        val credential = GoogleAuthProvider.getCredential(acct.idToken, null)
        firebaseAuth.signInWithCredential(credential).addOnCompleteListener { it ->
            customeProgressDialog.dismiss()
            if (it.isSuccessful) {
                val user = FirebaseAuth.getInstance().currentUser
                if (user != null) {
                    firebaseProvider = credential.provider
                    name = user.displayName.toString()
                    emailId = if (user.email != null) {
                        user.email.toString()
                    } else {
                        acct.email!!
                    }
                    photoUrl = firebaseAuth.currentUser!!.photoUrl.toString()

                    user.getIdToken(true)
                        .addOnCompleteListener {
                            if (it.isSuccessful) {
                                Log.e(TAG, "idToken ==========> $idToken")
                                idToken = it.result!!.token.toString()
                                login(emailId, AUTH_TYPE_GMAIL)

                                mGoogleSignInClient.signOut()
                            } else {
                               Log.e(
                                    TAG,
                                    "idToken exception ==========> ${it.exception!!.message}"
                                )
                                Toast.makeText(this, "Google sign in failed:(", Toast.LENGTH_LONG)
                                    .show()
                            }
                        }
                }
            } else {

                Log.e(TAG, "firebaseAuth message ==========> ${it.exception!!.message}")

                Toast.makeText(this, "Google sign in failed:(", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun signIn() {
        val signInIntent: Intent = mGoogleSignInClient.signInIntent
        startActivityForResult(signInIntent, rC_SIGN_IN)
    }

    fun login(email: String, authType: String) {
        if (!MyUtils.isConnectedWithInternet(this@LoginScreenActivity)) {
            MyUtils.showToast(this@LoginScreenActivity, "No Internet connection found")
            return
        }
        customeProgressDialog.show()

        val jsonRequest = JsonObject()
        jsonRequest.addProperty("name", "name")
        jsonRequest.addProperty("email", email)
        jsonRequest.addProperty("device_id", notificationToken)
        jsonRequest.addProperty("user_type", authType)
        jsonRequest.addProperty("provider_id", firebaseAuth.uid)
        jsonRequest.addProperty("id_token", idToken)
        jsonRequest.addProperty("isRegistration",false)
         val gson = Gson()
        val jsonString: String =
            gson.toJson(HardwareInfoManager(this).collectData(MyPreferences.getDeviceToken(this)!!))
        val deviceDetails: JsonObject = JsonParser().parse(jsonString).asJsonObject
        jsonRequest.add("deviceDetails", deviceDetails)

        RetrofitClient(this).client.create(IApiMethod::class.java).customerLogin(jsonRequest)
            .enqueue(this)
    }

    override fun onResponse(call: Call<ResponseModel>?, response: Response<ResponseModel>?) {
        if (!isFinishing) {
            customeProgressDialog.dismiss()

            val responseBody = response!!.body()
            if (responseBody != null) {
                if (responseBody.status) {
                    val infoModels = responseBody.infomodel
                    if (infoModels != null) {
                        if (TextUtils.isEmpty(responseBody.infomodel!!.profileImage)) {
                            //MyPreferences.setProfilePicture(this, photoUrl)
                            responseBody.infomodel!!.profileImage = photoUrl
                        }
                        MyPreferences.setOtpAuthRequired(this, responseBody.isOTPRequired)
                        MyPreferences.setToken(this, responseBody.token)

                        MyPreferences.setUserID(this, "" + responseBody.infomodel!!.userId)
                        (applicationContext as NinjaApplication).saveUserInformations(
                            responseBody.infomodel
                        )

                        MyPreferences.setOtpAuthRequired(this, responseBody.isOTPRequired)
                        MyPreferences.setToken(this, responseBody.token)
                        MyPreferences.setUserID(this, "" + responseBody.infomodel!!.userId)
                        MyPreferences.setPaytmMid(this, responseBody.paytmMid)
                        MyPreferences.setPaytmCallback(this, responseBody.callbackurrl)
                        MyPreferences.setGooglePayId(this, responseBody.gpayid)
                        MyPreferences.setRazorPayId(this, responseBody.razorPay)

                        if (responseBody.baseUrl != null && responseBody.baseUrl != "") {
                            MyPreferences.setBaseUrl(this, responseBody.baseUrl)
                        }
                        MyPreferences.setSystemToken(this, responseBody.systemToken)

                        if (TextUtils.isEmpty(infoModels.mobileNumber) ||
                            TextUtils.isEmpty(infoModels.userEmail) ||
                            TextUtils.isEmpty(infoModels.fullName)
                        ) {
                            registerUsers()
                        } else
                            if (infoModels.isOtpVerified) {
                                MyPreferences.setLoginStatus(this@LoginScreenActivity, true)
                                val intent =
                                    Intent(this@LoginScreenActivity, MainActivity::class.java)
                                setResult(Activity.RESULT_OK)
                                startActivity(intent)
                                finish()
                            } else {
                                sendOTP(firebaseAuth.uid)
                            }
                    } else {
                        MyUtils.showToast(
                            this@LoginScreenActivity,
                            "Something went wrong, please contact admin"
                        )
                    }
                } else {
                    when (responseBody.statusCode) {
                        BindingUtils.REUEST_STATUS_CODE_FRAUD -> {
                            showDeadLineAlert(responseBody.message)
                        }
                        401 -> {
                            showDeadLineAlert(responseBody.message)
                        }
                        else -> {
                            var infoModel = UserInfo()
                            if (responseBody.infomodel != null) {
                                infoModel = responseBody.infomodel!!
                            }
                            if(infoModel.userEmail.isEmpty()) {
                                infoModel.userEmail = emailId
                            }
                            if(infoModel.fullName.isEmpty()) {
                                infoModel.fullName = name
                            }
                            (applicationContext as NinjaApplication).saveUserInformations(
                                infoModel
                            )
                            registerUsers()
                        }
                    }
                }
            } else {
                MyUtils.showToast(this@LoginScreenActivity, "Invalid Email or Password")
            }
        }
    }

    private fun registerUsers() {
        val intent = Intent(this@LoginScreenActivity, RegisterScreenActivity::class.java)
        intent.putExtra(OtpVerifyActivity.EXTRA_KEY_PROVIDER_ID, firebaseAuth.uid)
        intent.putExtra(OtpVerifyActivity.EXTRA_KEY_ID_TOKEN, idToken)
        startActivity(intent)
    }

    private fun sendOTP(uid: String?) {
        val intent = Intent(this, OtpVerifyActivity::class.java)
        intent.putExtra(OtpVerifyActivity.EXTRA_KEY_EDIT_MOBILE_NUMBER, true)
        intent.putExtra(OtpVerifyActivity.EXTRA_KEY_PROVIDER_ID, uid)
        startActivityForResult(intent, RegisterScreenActivity.REQUESTCODE_LOGIN)
    }

    private fun processStep1() {

       /* binding!!.txtTnc.setOnClickListener {
            val intent = Intent(this@LoginScreenActivity, WebActivity::class.java)
            intent.putExtra(WebActivity.KEY_TITLE, BindingUtils.WEB_TITLE_TERMS_CONDITION)
            intent.putExtra(WebActivity.KEY_URL, BindingUtils.WEBVIEW_TNC)
            val options =
                ActivityOptions.makeSceneTransitionAnimation(this@LoginScreenActivity)
            startActivity(intent, options.toBundle())
        }

        binding!!.txtPrivacy.setOnClickListener {
            val intent = Intent(this@LoginScreenActivity, WebActivity::class.java)
            intent.putExtra(WebActivity.KEY_TITLE, BindingUtils.WEB_TITLE_PRIVACY_POLICY)
            intent.putExtra(WebActivity.KEY_URL, BindingUtils.WEBVIEW_PRIVACY)
            val options =
                ActivityOptions.makeSceneTransitionAnimation(this@LoginScreenActivity)
            startActivity(intent, options.toBundle())
        }*/
    }

    override fun onFailure(call: Call<ResponseModel>?, t: Throwable?) {

    }
}