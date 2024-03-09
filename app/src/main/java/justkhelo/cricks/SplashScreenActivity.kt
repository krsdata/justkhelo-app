package justkhelo.cricks

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import com.bumptech.glide.Glide
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.installations.FirebaseInstallations
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import com.google.gson.JsonObject
import io.branch.referral.Branch
import justkhelo.cricks.databinding.ActivitySplashBinding
import justkhelo.cricks.network.IApiMethod
import justkhelo.cricks.network.RetrofitClient
import justkhelo.cricks.ui.BaseActivity
import justkhelo.cricks.ui.login.LoginScreenActivity
import justkhelo.cricks.utils.HardwareInfoManager
import justkhelo.cricks.utils.MyPreferences
import justkhelo.cricks.utils.MyUtils
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class SplashScreenActivity : BaseActivity() {

    private lateinit var mContext: Context
    private var mBinding: ActivitySplashBinding? = null
    private var mDelayHandler: Handler? = null
    private val SPLASH_DELAYED: Long = 2500
    private var TAG: String = SplashScreenActivity::class.java.simpleName

    private val mRunnable: Runnable = Runnable {
        if (!isFinishing) {
            checkUserLoggedIn()
        }
    }

    private fun checkUserLoggedIn() {
        if (!MyPreferences.getLoginStatus(mContext)!!) {
            loginRequired()
        } else {
            val intent = Intent(
                applicationContext,
                MainActivity::class.java
            )
            startActivity(intent)
            finish()
        }
    }

    private fun loginRequired() {
        val intent = Intent(
            applicationContext,
            LoginScreenActivity::class.java
        )
        startActivity(intent)
        finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mContext = this
        mBinding = DataBindingUtil.setContentView(
            this,
            R.layout.activity_splash
        )

        Log.d("deviceId",MyUtils.getDeviceID(this).toString())
        updateCheckApk()
        sendDeviceInfo()

        MainActivity.CHECK_APK_UPDATE_API = false
        MainActivity.CHECK_WALLET_ONCE = false
        updateFireBase()

            Glide.with(mContext)
                .load(R.drawable.splash_screen)
                .placeholder(R.drawable.splash_screen)
                .into(mBinding!!.splashView)

        mDelayHandler = Handler()
        mDelayHandler!!.postDelayed(mRunnable, SPLASH_DELAYED)

        val branch = Branch.getAutoInstance(mContext)
        branch.setRetryCount(5)

        branch.initSession({ referringParams, error ->
            if (error != null) {
                //Log.e("onCreate", error.getMessage());
            } else if (referringParams != null) {
                //Log.e("onCreate", referringParams.toString());
                if (referringParams.has("refer_code")) {
                    MyPreferences.setTempReferCode(
                        mContext,
                        referringParams.optString("refer_code")
                    )
                }
            }
        }, this.intent.data, this)
    }

    override fun onBitmapSelected(bitmap: Bitmap) {

    }

    override fun onUploadedImageUrl(url: String) {

    }

    private fun updateCheckApk() {
        val jsonRequest = JsonObject()
        jsonRequest.addProperty("user_id", MyPreferences.getUserID(this)!!)
        jsonRequest.addProperty("system_token", MyPreferences.getSystemToken(this)!!)
        jsonRequest.addProperty("version_code", NinjaApplication.appVersion)

        RetrofitClient(mContext).client.create(IApiMethod::class.java).apkUpdate(jsonRequest)
            .enqueue(object : Callback<JsonObject?> {
                override fun onFailure(call: Call<JsonObject?>?, t: Throwable?) {
                    MainActivity.CHECK_APK_UPDATE_API = false
                }

                override fun onResponse(
                    call: Call<JsonObject?>?,
                    response: Response<JsonObject?>?
                ) {
                    if (!isFinishing) {
                        if (response!!.body() != null) {
                            val res = JSONObject(response.body().toString())
                            MyPreferences.setSplashScreen(
                                this@SplashScreenActivity,
                                res.getString("splashScreen")
                            )
                            if (res.getBoolean("status")) {
                                MainActivity.CHECK_APK_UPDATE_API = true
                                MainActivity.CHECK_FORCE_UPDATE = res.getBoolean("force_update")
                                MainActivity.updatedApkUrl = res.getString("url")
                                MainActivity.releaseNote = res.getString("release_note")
                                if (res.getString("base_url") != null && res.getString("base_url") != "") {
                                    MyPreferences.setBaseUrl(mContext, res.getString("base_url"))
                                }
                            }
                        }
                    }
                }
            })
    }

    private fun sendDeviceInfo() {
        if (!MyPreferences.getIsUserLaunchedApp(this)) {
            Log.d("sendDeviceInfo", "Sending device info to server for first launch")
            FirebaseInstallations.getInstance().id
                .addOnSuccessListener { id ->
                    Log.d(TAG, "Firebase Installation unique ID is: $id")
                    val jsonRequest = JsonObject()
                    jsonRequest.addProperty("device_id", id)
                    jsonRequest.addProperty("device_ip", MyUtils.getIPAddress(true))
                    val deviceJson = JsonObject()
                    deviceJson.addProperty("device_name", HardwareInfoManager.deviceName)
                    deviceJson.addProperty("app_version", NinjaApplication.appVersionName)
                    jsonRequest.add("other", deviceJson)

                    RetrofitClient(mContext).client.create(IApiMethod::class.java).updateDeviceInfo(jsonRequest)
                        .enqueue(object : Callback<JsonObject?> {
                            override fun onFailure(call: Call<JsonObject?>?, t: Throwable?) {
                                Log.d("sendDeviceInfo", "failed to upload device info:"+t?.message);
                            }

                            override fun onResponse(
                                call: Call<JsonObject?>?,
                                response: Response<JsonObject?>?
                            ) {
                                Log.d("sendDeviceInfo", "Successfully upload device info")
                                MyPreferences.setUserLaunchedApp(this@SplashScreenActivity, true)
                            }
                        })
                }


        } else {
            Log.d("sendDeviceInfo", "User has already used App.");
        }
    }

    override fun onStart() {
        super.onStart()
        val intent = this.intent
        intent.putExtra("branch_force_new_session", true)
        Branch.getInstance()
            .initSession({ branchUniversalObject, linkProperties, error ->

            }, intent.data, this)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        Branch.getInstance()
            .reInitSession(
                this
            ) { referringParams, error ->
                if (error != null) {
                    //Log.e("onCreate", error.getMessage());
                } else if (referringParams != null) {
                    //Log.e("onCreate", referringParams.toString());
                    if (referringParams.has("refer_code")) {
                        MyPreferences.setTempReferCode(
                            mContext,
                            referringParams.optString("refer_code")
                        )
                    }
                }
            }
    }
}