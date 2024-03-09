package justkhelo.cricks

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.provider.FontRequest
import androidx.emoji.text.EmojiCompat
import androidx.emoji.text.FontRequestEmojiCompatConfig
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.multidex.MultiDex
import androidx.multidex.MultiDexApplication
import com.google.firebase.FirebaseApp
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import io.branch.referral.Branch
import justkhelo.cricks.models.MatchesModels
import justkhelo.cricks.models.TransactionModel
import justkhelo.cricks.models.UserInfo
import justkhelo.cricks.models.WalletInfo
import justkhelo.cricks.network.IApiMethod
import justkhelo.cricks.network.WebServiceClient
import justkhelo.cricks.models.UsersPostDBResponse
import justkhelo.cricks.utils.BindingUtils
import justkhelo.cricks.utils.MyPreferences
import justkhelo.cricks.utils.MyPreferences.KEY_TRANSACTION_HISTORY
import justkhelo.cricks.utils.MyPreferences.KEY_UPCOMING_MATCHES
import justkhelo.cricks.utils.MyUtils
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class NinjaApplication : MultiDexApplication() {

    companion object{
        var appVersion: Int? = null
        var appVersionName: String? = null
    }
    private var mGetWallet: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            getWalletBalances()
        }
    }

    override fun onCreate() {
        super.onCreate()
        MultiDex.install(this)
        try {
            val pInfo = packageManager.getPackageInfo(packageName, 0)
            appVersion = pInfo.versionCode
            appVersionName = pInfo.versionName
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
            e.message?.let { Log.d("chechversion", it) }
        }

        val fontRequest = FontRequest(
            "com.google.android.gms.fonts",
            "com.google.android.gms",
            "Noto Color Emoji Compat",
            R.array.com_google_android_gms_fonts_certs
        )
        val config = FontRequestEmojiCompatConfig(this, fontRequest)
            .setReplaceAll(true)
            .setEmojiSpanIndicatorEnabled(true)

        EmojiCompat.init(config)

        FirebaseApp.initializeApp(this)
        Branch.getAutoInstance(this)
        FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(true)

        val filter1 = IntentFilter(BindingUtils.EXTRA_DATA_GET_WALLET)
        LocalBroadcastManager.getInstance(this).registerReceiver(mGetWallet, filter1)
    }

    fun saveUserInformations(value: UserInfo?) {
        if (value != null) {
            val gson = Gson()
            val data = gson.toJson(value)
            MyPreferences.saveUserInformations(applicationContext, data)
        }
    }

    val userInformations: UserInfo
        get() {
            var mStoreListModels: UserInfo? = null
            val type = object : TypeToken<UserInfo>() {

            }.type
            val gson = Gson()
            val gsonObject = MyPreferences.getUserInformations(this)
            if (gsonObject != null) {
                mStoreListModels = gson.fromJson<UserInfo>(gsonObject, type)
            }
            return mStoreListModels ?: UserInfo()
        }

    fun saveWalletInformation(value: WalletInfo?) {
        if (value != null) {
            val gson = Gson()
            val data = gson.toJson(value)
            MyPreferences.saveWalletInformation(applicationContext, data)
        }
    }

    val walletInfo: WalletInfo
        get() {
            var mStoreListModels: WalletInfo? = null
            val type = object : TypeToken<WalletInfo>() {

            }.type
            val gson = Gson()
            val gsonObject = MyPreferences.getWalletInformation(this)
            if (gsonObject != null) {
                mStoreListModels = gson.fromJson<WalletInfo>(gsonObject, type)
            }
            return mStoreListModels ?: WalletInfo()
        }

    fun saveUpcomingMatches(value: ArrayList<MatchesModels>?) {
        if (value != null) {
            val gson = Gson()
            val data = gson.toJson(value)
            MyPreferences.setSFApiCaches(applicationContext, KEY_UPCOMING_MATCHES, data)
        }
    }

    val getUpcomingMatches: ArrayList<MatchesModels>
        get() {
            var mStoreListModels: ArrayList<MatchesModels>? = null
            val type = object : TypeToken<ArrayList<MatchesModels>>() {

            }.type
            val gson = Gson()
            val gsonObject = MyPreferences.getSFApiCaches(this, KEY_UPCOMING_MATCHES)
            if (gsonObject != null) {
                mStoreListModels = gson.fromJson<ArrayList<MatchesModels>>(gsonObject, type)
            }
            return mStoreListModels ?: ArrayList()
        }

    fun saveTransactionHistory(value: ArrayList<TransactionModel>?) {
        if (value != null) {
            val gson = Gson()
            val data = gson.toJson(value)
            MyPreferences.setSFApiCaches(applicationContext, data, KEY_TRANSACTION_HISTORY)
        }
    }

    val getTransactionHistory: ArrayList<TransactionModel>
        get() {
            var mStoreListModels: ArrayList<TransactionModel>? = null
            val type = object : TypeToken<ArrayList<TransactionModel>>() {

            }.type
            val gson = Gson()
            val gsonObject = MyPreferences.getSFApiCaches(this, KEY_TRANSACTION_HISTORY)
            if (gsonObject != null) {
                mStoreListModels = gson.fromJson<ArrayList<TransactionModel>>(gsonObject, type)
            }
            return mStoreListModels ?: ArrayList()
        }

    private fun getWalletBalances() {
        if (!MyUtils.isConnectedWithInternet(this)) {
            return
        }

        val jsonRequest = JsonObject()
        jsonRequest.addProperty("user_id", MyPreferences.getUserID(this)!!)
        jsonRequest.addProperty("system_token", MyPreferences.getSystemToken(this)!!)

        WebServiceClient(this).client.create(IApiMethod::class.java).getWallet(jsonRequest)
            .enqueue(object : Callback<UsersPostDBResponse?> {
                override fun onFailure(call: Call<UsersPostDBResponse?>?, t: Throwable?) {

                }

                override fun onResponse(
                    call: Call<UsersPostDBResponse?>?,
                    response: Response<UsersPostDBResponse?>?
                ) {
                    val res = response!!.body()
                    if (res != null && res.status) {
                        val responseModel = res.walletObjects
                        if (responseModel != null) {

                            MyPreferences.setRazorPayId(this@NinjaApplication, res.razorPay)
                            MyPreferences.setShowPaytm(this@NinjaApplication, res.paytm_show)
                            MyPreferences.setShowGpay(this@NinjaApplication, res.gpay_show)
                            MyPreferences.setShowRazorPay(this@NinjaApplication, res.rozarpay_show)

                            MyPreferences.setShowPaytmWithdraw(
                                this@NinjaApplication,
                                res.paytm_withdrawal
                            )
                            MyPreferences.setShowBankWithdraw(
                                this@NinjaApplication,
                                res.bank_withdrawal
                            )
                            MyPreferences.setShowUPIWithdraw(
                                this@NinjaApplication,
                                res.upi_withdrawal
                            )

                            MyPreferences.setPaytmWithdrawBtn(
                                this@NinjaApplication,
                                res.paytm_withdrawal_btn
                            )

                            MyPreferences.setMinWithdrawal(this@NinjaApplication, res.minWithdrawal)

                            saveWalletInformation(responseModel)
                        }
                    }
                }
            })
    }
}