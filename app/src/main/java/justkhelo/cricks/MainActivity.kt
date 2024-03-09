package justkhelo.cricks

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.etebarian.meowbottomnavigation.MeowBottomNavigation
import com.google.gson.JsonObject
import justkhelo.cricks.databinding.ActivityMainBinding
import justkhelo.cricks.models.UsersPostDBResponse
import justkhelo.cricks.network.IApiMethod
import justkhelo.cricks.network.RetrofitClient
import justkhelo.cricks.network.WebServiceClient
import justkhelo.cricks.ui.BaseActivity
import justkhelo.cricks.ui.dashboard.FixtureCricketFragment
import justkhelo.cricks.ui.dashboard.MoreOptionsFragment
import justkhelo.cricks.ui.dashboard.MyAccountFragment
import justkhelo.cricks.ui.dashboard.MyMatchesFragment
import justkhelo.cricks.ui.predictWin.PredictWinFragment
import justkhelo.cricks.utils.MyPreferences
import justkhelo.cricks.utils.MyUtils
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : BaseActivity(),
    UpdatePayment/*, BottomNavigationView.OnNavigationItemSelectedListener */ {

    var fragment: Fragment? = null
    private var mBinding: ActivityMainBinding? = null
    private lateinit var mContext: Context


    companion object {
        var menuArrayList = ArrayList<JSONObject>()
        var showScore: Boolean = false
        var CHECK_WALLET_ONCE: Boolean? = false
        var updatedApkUrl: String = ""
        var releaseNote: String = ""
        var CHECK_APK_UPDATE_API: Boolean = false
        var CHECK_FORCE_UPDATE: Boolean = true

        const val ID_HOME = 1
        const val ID_DASHBOARD = 2
        const val ID_PREDICT_WIN = 3
        const val ID_MY_ACCOUNT = 4
        const val ID_NOTIFICATIONS = 5
        const val temp_leaderboard = 6
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = DataBindingUtil.setContentView(
            this,
            R.layout.activity_main
        )
        initBottomNavigation()
        mContext = this
        userInfo = (application as NinjaApplication).userInformations
        setSupportActionBar(mBinding!!.toolbar)

        mBinding!!.txtWalletAmount.setOnClickListener {
            val intent = Intent(mContext, MyBalanceActivity::class.java)
            startActivityForResult(intent, MyBalanceActivity.REQUEST_CODE_ADD_MONEY)
        }

        mBinding!!.imgWalletAmount.setOnClickListener {
            val intent = Intent(mContext, MyBalanceActivity::class.java)
            startActivityForResult(intent, MyBalanceActivity.REQUEST_CODE_ADD_MONEY)
        }
        mBinding!!.notificationId.setOnClickListener {
            val intent = Intent(mContext, NotificationListActivity::class.java)
            startActivityForResult(intent, MyBalanceActivity.REQUEST_CODE_ADD_MONEY)
        }

        getWalletBalances()

        Glide.with(this).load(userInfo!!.profileImage)
            .placeholder(R.drawable.player_blue).into(mBinding!!.profileImage)

        mBinding!!.profileImage.setOnClickListener {
            val intent = Intent(mContext, EditProfileActivity::class.java)
            intent.putExtra(FullScreenImageViewActivity.KEY_IMAGE_URL, userInfo!!.profileImage)
            startActivity(intent)
        }

        // mBinding!!.navigation.setOnNavigationItemSelectedListener(this)

        fragment = FixtureCricketFragment()
        loadFragment()
    }

    private fun initBottomNavigation() {
        mBinding?.navigation?.apply {

            add(
                MeowBottomNavigation.Model(
                    ID_HOME,
                    R.drawable.ic_outline_home_2
                )
            )

            add(
                MeowBottomNavigation.Model(
                    ID_PREDICT_WIN,
                    R.drawable.predictive_chart
                )
            )
            add(
                MeowBottomNavigation.Model(
                    ID_DASHBOARD,
                    R.drawable.ic_matches
                )
            )
            add(
                MeowBottomNavigation.Model(
                    ID_MY_ACCOUNT,
                    R.drawable.ic_outline_account
                )
            )
            add(
                MeowBottomNavigation.Model(
                    ID_NOTIFICATIONS,
                    R.drawable.ic_outline_category_2
                )
            )
/*
            add(
                MeowBottomNavigation.Model(
                    temp_leaderboard,
                    R.drawable.king
                )
            )
*/

            //setCount(ID_NOTIFICATION, "115")

            setOnShowListener {
                val name = when (it.id) {
                    ID_HOME -> {
                        fragment = FixtureCricketFragment()
                        loadFragment()
                    }
                    ID_DASHBOARD -> {
                        fragment = MyMatchesFragment()
                        loadFragment()
                    }
                    ID_PREDICT_WIN -> {
                        fragment = PredictWinFragment()
                        loadFragment()
                    }
                    ID_MY_ACCOUNT -> {
                        fragment = MyAccountFragment()
                        loadFragment()
                    }
                    ID_NOTIFICATIONS -> {
                        fragment = MoreOptionsFragment()
                        loadFragment()
                    }
/*
                    temp_leaderboard -> {
                        startActivity(Intent(this@MainActivity,ContestLeaderBoardActivity::class.java))
                    }
*/
                    else -> ""
                }

                //xxtvSelected.text = getString(R.string.main_page_selected, name)
            }

            setOnClickMenuListener {
                val name = when (it.id) {
                    ID_HOME -> "HOME"
                    ID_DASHBOARD -> "EXPLORE"
                    ID_PREDICT_WIN -> "MESSAGE"
                    ID_NOTIFICATIONS -> "NOTIFICATION"
                    ID_MY_ACCOUNT -> "ACCOUNT"
                    else -> ""
                }
            }

            setOnReselectListener {
                //  Toast.makeText(this@MainActivity, "item ${it.id} is reselected.", Toast.LENGTH_LONG).show()
            }

            show(ID_HOME)

        }

    }

    override fun onResume() {
        super.onResume()
        updateCheckApk()
        userInfo = (application as NinjaApplication).userInformations
        if (userInfo != null) {
            Glide.with(this)
                .load(userInfo!!.profileImage)
                .placeholder(R.drawable.player_blue)
                .into(mBinding!!.profileImage)
        }
    }

    fun viewUpcomingMatches() {
        //   mBinding!!.navigation.selectedItemId = R.id.navigation_home
    }

    fun viewAllMatches() {
        //    mBinding!!.navigation.selectedItemId = R.id.navigation_home
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == MyBalanceActivity.REQUEST_CODE_ADD_MONEY) {
            getWalletBalances()
        }
    }

    override fun onBitmapSelected(bitmap: Bitmap) {
    }

    override fun onUploadedImageUrl(url: String) {

    }

    override fun onStart() {
        super.onStart()
        if (CHECK_APK_UPDATE_API) {
            CHECK_APK_UPDATE_API = false
            /*val fm = supportFragmentManager
            val pioneersFragment =
                UpdateAppDialogFragment(updatedApkUrl, releaseNote)
            pioneersFragment.isCancelable = false
            pioneersFragment.show(fm, "updateapp_tag")*/

            val intent = Intent(this@MainActivity, UpdateApplicationActivity::class.java)
            intent.putExtra(UpdateApplicationActivity.REQUEST_CODE_APK_UPDATE, updatedApkUrl)
            intent.putExtra(UpdateApplicationActivity.REQUEST_RELEASE_NOTE, releaseNote)
            startActivity(intent)
        }
    }

    private fun getWalletBalances() {
        customeProgressDialog.show()
        val jsonRequest = JsonObject()
        jsonRequest.addProperty("user_id", MyPreferences.getUserID(this)!!)
        jsonRequest.addProperty("system_token", MyPreferences.getSystemToken(this)!!)

        WebServiceClient(this).client.create(IApiMethod::class.java).getWallet(jsonRequest)
            .enqueue(object : Callback<UsersPostDBResponse?> {
                override fun onFailure(call: Call<UsersPostDBResponse?>?, t: Throwable?) {
                    CHECK_APK_UPDATE_API = false
                }

                override fun onResponse(
                    call: Call<UsersPostDBResponse?>?,
                    response: Response<UsersPostDBResponse?>?
                ) {
                    CHECK_APK_UPDATE_API = false
                    val res = response!!.body()
                    if (res != null) {
                        if (res.status) {
                            val responseModel = res.walletObjects
                            if (responseModel != null) {
                                MyPreferences.setRazorPayId(this@MainActivity, res.razorPay)
                                MyPreferences.setShowPaytm(this@MainActivity, res.paytm_show)
                                MyPreferences.setShowGpay(this@MainActivity, res.gpay_show)
                                MyPreferences.setShowRazorPay(this@MainActivity, res.rozarpay_show)

                                MyPreferences.setShowPaytmWithdraw(
                                    this@MainActivity,
                                    res.paytm_withdrawal
                                )
                                MyPreferences.setShowBankWithdraw(
                                    this@MainActivity,
                                    res.bank_withdrawal
                                )
                                MyPreferences.setShowUPIWithdraw(
                                    this@MainActivity,
                                    res.upi_withdrawal
                                )

                                MyPreferences.setMinWithdrawal(
                                    this@MainActivity,
                                    res.minWithdrawal
                                )

                                MyPreferences.setPaytmWithdrawBtn(
                                    this@MainActivity,
                                    res.paytm_withdrawal_btn
                                )
                                mBinding!!.txtWalletAmount.text =
                                    "₹${responseModel.depositAmount + responseModel.prizeAmount + responseModel.bonusAmount}"
                                Log.d("walletAmount", responseModel.walletAmount.toString())
                                (application as NinjaApplication).saveWalletInformation(
                                    responseModel
                                )
                            }
                        } else {
                            if (res.code == 1001) {
                                MyUtils.showMessage(this@MainActivity, res.message)
                                MyUtils.logoutApp(this@MainActivity)
                            } else {
                                MyUtils.showMessage(this@MainActivity, res.message)
                            }
                        }
                    }
                    customeProgressDialog.dismiss()
                }
            })
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            return true
        }
        return super.onOptionsItemSelected(item)
    }

/*
    override fun onNavigationItemSelected(menuItem: MenuItem): Boolean {
        when (menuItem.itemId) {
            R.id.navigation_home -> {
                fragment = FixtureCricketFragment()
                loadFragment()
                return true
            }
            R.id.navigation_dashboard -> {
                fragment = MyMatchesFragment()
                loadFragment()
                return true
            }
            R.id.navigation_myaccount -> {
                fragment = MyAccountFragment()
                loadFragment()
                return true
            }
            R.id.navigation_notifications -> {
                fragment = MoreOptionsFragment()
                loadFragment()
                return true
            }
        }
        return false
    }
*/

    private fun loadFragment() {
        if (fragment != null) {
            val fragmentManager = supportFragmentManager
            val fragmentTransaction = fragmentManager.beginTransaction()
            fragmentTransaction.replace(R.id.container_body, fragment!!)
            fragmentTransaction.commit()
        }
    }

    private fun updateCheckApk() {
        val jsonRequest = JsonObject()
        jsonRequest.addProperty("user_id", MyPreferences.getUserID(this)!!)
        jsonRequest.addProperty("system_token", MyPreferences.getSystemToken(this)!!)
        jsonRequest.addProperty("version_code", NinjaApplication.appVersion)

        RetrofitClient(mContext).client.create(IApiMethod::class.java).apkUpdate(jsonRequest)
            .enqueue(object : Callback<JsonObject?> {
                override fun onFailure(call: Call<JsonObject?>?, t: Throwable?) {
                    CHECK_APK_UPDATE_API = false
                }

                override fun onResponse(
                    call: Call<JsonObject?>?,
                    response: Response<JsonObject?>?
                ) {
                    if (!isFinishing) {
                        if (response!!.body() != null) {
                            val res = JSONObject(response.body().toString())
                            showScore = res.getBoolean("show_scoreboard")
                            menuArrayList.clear()

                            for (i in 0 until res.getJSONArray("menu").length()) {
                                menuArrayList.add(res.getJSONArray("menu").getJSONObject(i))
                            }

                            MyPreferences.setSplashScreen(
                                mContext,
                                res.getString("splashScreen")
                            )
                            if (res.getBoolean("status")) {
                                CHECK_APK_UPDATE_API = true
                                CHECK_FORCE_UPDATE = res.getBoolean("force_update")
                                updatedApkUrl = res.getString("url")
                                releaseNote = res.getString("release_note")
                                if (res.getString("base_url") != null && res.getString("base_url") != "") {
                                    MyPreferences.setBaseUrl(mContext, res.getString("base_url"))
                                }

                                if (CHECK_APK_UPDATE_API) {
                                    CHECK_APK_UPDATE_API = false
                                    /*val fm = supportFragmentManager
                                    val pioneersFragment =
                                        UpdateAppDialogFragment(updatedApkUrl, releaseNote)
                                    pioneersFragment.isCancelable = false
                                    pioneersFragment.show(fm, "updateapp_tag")*/

                                    if (!isActivityRunning(UpdateApplicationActivity::class.java)) {

                                        val intent = Intent(
                                            this@MainActivity,
                                            UpdateApplicationActivity::class.java
                                        )
                                        intent.putExtra(
                                            UpdateApplicationActivity.REQUEST_CODE_APK_UPDATE,
                                            updatedApkUrl
                                        )
                                        intent.putExtra(
                                            UpdateApplicationActivity.REQUEST_RELEASE_NOTE,
                                            releaseNote
                                        )
                                        startActivity(intent)
                                    }
                                }
                            }
                        }
                    }
                }
            })
    }

    private fun isActivityRunning(aClass: Class<*>): Boolean {
        return try {
            val am = mContext.getSystemService(ACTIVITY_SERVICE) as ActivityManager
            val taskInfo = am.getRunningTasks(1)
            val componentInfo = taskInfo[0].topActivity
            componentInfo!!.className == aClass.name
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    override fun onRestart() {

        getWalletBalances()
        super.onRestart()
    }

    override fun onUpdatePayment() {
        getWalletBalances()
    }

}

interface UpdatePayment {
    fun onUpdatePayment()
}