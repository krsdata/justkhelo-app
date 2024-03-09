package justkhelo.cricks.ui.login

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.os.CountDownTimer
import android.text.TextUtils
import android.util.Log
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.FirebaseException
import com.google.firebase.auth.*
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import justkhelo.cricks.MainActivity
import justkhelo.cricks.NinjaApplication
import justkhelo.cricks.OtpVerifyActivity
import justkhelo.cricks.R
import justkhelo.cricks.databinding.ActivityPhoneLoginBinding
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
import java.util.concurrent.TimeUnit


class PhoneLoginActivity : BaseActivity(), Callback<ResponseModel> {
    private var canBackPress: Boolean = false
    private var isOtpVerified: Boolean = false
    private var uid: String? = null
    private var phoneNo: String? = null
    private lateinit var otpTimer: CountDownTimer
    private var firebaseProvider: String = ""
    var name = ""
    var verifiedEmailId = ""
    var verifiedPhoneNumber = ""
    private var photoUrl: String = ""
    private var idToken: String = ""
    private var binding: ActivityPhoneLoginBinding? = null
    private var auth: FirebaseAuth? = null
    private var callback: PhoneAuthProvider.OnVerificationStateChangedCallbacks? = null
    private var verificationId: String? = null
    private var canResend = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_phone_login)
        val bundle = intent.extras
        phoneNo = bundle?.getString("mobileNo")
        customeProgressDialog.show()
        init()
    }

    private fun init() {
        auth = FirebaseAuth.getInstance()
        binding!!.back.setOnClickListener {
            onBackPressed()
        }
        binding!!.btnSubmit.setOnClickListener {
            verifyCode(binding!!.otpView.text.toString())
        }
        binding!!.resendOtp.setOnClickListener {
            if (!canResend) {
                MyUtils.showToast(this, "Please wait to complete timer")
            } else {
                customeProgressDialog.show()
                sendOTP()
            }

        }
        initTimer()
        binding!!.otpView.setOtpCompletionListener {
            verifyCode(it)
        }
        initCallback()
        sendOTP()
    }

    private fun initCallback() {
        callback = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(p0: PhoneAuthCredential) {
                val otp = p0.smsCode
                if (otp != null) {
                    binding!!.otpView.setText(otp)
                    otpTimer.cancel()
                    binding!!.timerOtpDetect.text = ""
                    //          binding!!.progressBar.visibility = View.GONE
                }
            }

            override fun onVerificationFailed(p0: FirebaseException) {
                customeProgressDialog.dismiss()

                Toast.makeText(this@PhoneLoginActivity,p0.message,Toast.LENGTH_SHORT).show()
               /* val snackbar: Snackbar = Snackbar.make(binding!!.root,p0.message!!,Snackbar.LENGTH_SHORT)
                snackbar.view.setBackgroundColor(resources.getColor(R.color.green))
                snackbar.show()*/
                finish()
            }

            override fun onCodeSent(p0: String, p1: PhoneAuthProvider.ForceResendingToken) {
                super.onCodeSent(p0, p1)
                verificationId = p0
                canResend = false
                canBackPress = false
                customeProgressDialog.dismiss()
                otpTimer.start()
                Toast.makeText(
                    this@PhoneLoginActivity,
                    "6 digit OTP has been sent",
                    Toast.LENGTH_SHORT
                ).show()
            }

            override fun onCodeAutoRetrievalTimeOut(p0: String) {
                super.onCodeAutoRetrievalTimeOut(p0)
                canBackPress = true
            }
        }
    }


    private fun verifyCode(code: String) {
        if (binding!!.otpView.text != null && binding!!.otpView.text!!.isNotEmpty() && binding!!.otpView.text!!.length == 6) {
            if (verificationId != null) {
                val credential = PhoneAuthProvider.getCredential(verificationId!!, code)
                signInWithCredential(credential)
            } else {
                MyUtils.showToast(this, "Please try again")
            }
        } else {
            MyUtils.showToast(this, "OTP field can't be empty")
        }
    }

    private fun signInWithCredential(credential: PhoneAuthCredential) {
        customeProgressDialog.show()
        auth!!.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = FirebaseAuth.getInstance().currentUser
                    if (user != null) {
                        firebaseProvider = credential.provider
                        if (user.displayName != null) {
                            name = user.displayName.toString()
                        }
                        if (user.email != null) {
                            verifiedEmailId = user.email
                        }
                        if (auth!!.currentUser.photoUrl != null) {
                            photoUrl = auth!!.currentUser!!.photoUrl.toString()
                        }
                        if (user.phoneNumber != null){
                            verifiedPhoneNumber = user.phoneNumber.substring(3)
                        }

                        user.getIdToken(true)
                            .addOnCompleteListener {
                                if (it.isSuccessful) {
                                    Log.e(LoginScreenActivity.TAG, "idToken ==========> $idToken")
                                    idToken = it.result!!.token.toString()
                                    login(verifiedEmailId, "phoneAuth")
                                    uid = auth!!.uid
                                    isOtpVerified = true
                                    auth!!.signOut()
                                } else {
                                    /*Log.e(TAG, "idToken exception ==========> ${it.exception.toString()}")

                                    Log.e(TAG, "idToken exception ==========> ${it.exception!!.printStackTrace()}")*/

                                    Log.e(
                                        LoginScreenActivity.TAG,
                                        "idToken exception ==========> ${it.exception!!.message}"
                                    )
                                    customeProgressDialog.dismiss()
                                }
                            }
                    }
                } else {
                    Log.e(
                        LoginScreenActivity.TAG,
                        "firebaseAuth message ==========> ${task.exception.toString()}"
                    )
                    if (task.exception!!.message!!.contains("credential is invalid")) {
                        val snackbar = Snackbar.make(binding!!.root,"incorrect OTP, please enter correct OTP",Snackbar.LENGTH_SHORT)
                        snackbar.view.setBackgroundColor(resources.getColor(R.color.green))
                        snackbar.show()
                    }

                    customeProgressDialog.dismiss()
                }
            }
    }

    private fun sendOTP() {
        val options = PhoneAuthOptions.newBuilder(auth!!)
            .setPhoneNumber("+91$phoneNo")       // Phone number to verify
            .setTimeout(10L, TimeUnit.SECONDS) // Timeout and unit
            .setActivity(this)                 // Activity (for callback binding)
            .setCallbacks(callback!!)          // OnVerificationStateChangedCallbacks
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    private fun initTimer() {
        otpTimer = object : CountDownTimer(30000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                binding!!.timerOtpDetect.text =
                    "Time Remaining 0:${((millisUntilFinished / 1000) % 60)}"
            }

            override fun onFinish() {
                binding!!.timerOtpDetect.text = ""
                canResend = true

            }

        }

    }


    fun login(email: String, authType: String) {
        if (!MyUtils.isConnectedWithInternet(this@PhoneLoginActivity)) {
            MyUtils.showToast(this@PhoneLoginActivity, "No Internet connection found")
            return
        }
        customeProgressDialog.show()

        val jsonRequest = JsonObject()

        jsonRequest.addProperty("name", name)
        jsonRequest.addProperty("mobile_number", phoneNo)
        jsonRequest.addProperty("device_id", notificationToken)
        jsonRequest.addProperty("user_type", "login_phone")
        jsonRequest.addProperty("provider_id", auth?.uid)
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

    override fun onResponse(call: Call<ResponseModel>, response: Response<ResponseModel>) {
        if (!isFinishing) {
            customeProgressDialog.dismiss()

            val responseBody = response.body()
            if (responseBody != null) {
                if (responseBody.status) {
                    val infoModels = responseBody.infomodel
                    if (infoModels != null) {
                        if (TextUtils.isEmpty(responseBody.infomodel!!.profileImage)) {
                            //MyPreferences.setProfilePicture(this, photoUrl)
                            responseBody.infomodel!!.profileImage = photoUrl
                        }
                        MyPreferences.setMobile(this, phoneNo!!)
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

                        if (responseBody.baseUrl != "") {
                            MyPreferences.setBaseUrl(this, responseBody.baseUrl)
                        }
                        MyPreferences.setSystemToken(this, responseBody.systemToken)

                        if (TextUtils.isEmpty(infoModels.mobileNumber) ||
                            TextUtils.isEmpty(infoModels.userEmail) ||
                            TextUtils.isEmpty(infoModels.fullName)
                        ) {
                            registerUsers()
                        } else
                            if (isOtpVerified) {
                                MyPreferences.setLoginStatus(this@PhoneLoginActivity, true)
                                val intent =
                                    Intent(this@PhoneLoginActivity, MainActivity::class.java)
                                setResult(Activity.RESULT_OK)
                                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                startActivity(intent)
                                finish()
                            }
                    } else {
                        MyUtils.showToast(
                            this@PhoneLoginActivity,
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
                            if (infoModel.userEmail.isEmpty()) {
                                 infoModel.userEmail  = verifiedEmailId
                            }
                            if (infoModel.mobileNumber.isEmpty()) {
                                infoModel.mobileNumber = verifiedPhoneNumber
                                Log.d("mobileeee",verifiedPhoneNumber)
                            }
                            Log.d("mobileeee",infoModel.mobileNumber)

                            (applicationContext as NinjaApplication).saveUserInformations(
                                infoModel
                            )
                            registerUsers()
                        }
                    }
                }
            } else {
                MyUtils.showToast(this@PhoneLoginActivity, "Invalid Email or Password")
            }
        }
    }

    override fun onFailure(call: Call<ResponseModel>, t: Throwable) {
        Log.d("LoginResponse", t.message.toString())
    }

    private fun registerUsers() {
        val intent = Intent(this@PhoneLoginActivity, RegisterScreenActivity::class.java)
        intent.putExtra(OtpVerifyActivity.EXTRA_KEY_PROVIDER_ID, uid)
        intent.putExtra(OtpVerifyActivity.EXTRA_KEY_ID_TOKEN, idToken)
        startActivity(intent)
        finish()
    }

    override fun onBackPressed() {
        if (canBackPress) {
            super.onBackPressed()
        } else {
            Toast.makeText(this, "Please wait....", Toast.LENGTH_SHORT).show()
            return
        }
    }

    override fun onBitmapSelected(bitmap: Bitmap) {
        TODO("Not yet implemented")
    }

    override fun onUploadedImageUrl(url: String) {
        TODO("Not yet implemented")
    }
}