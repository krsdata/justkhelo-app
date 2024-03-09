package justkhelo.cricks.ui

import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import justkhelo.cricks.*
import justkhelo.cricks.databinding.FragmentJoinContestConfirmationBinding
import justkhelo.cricks.models.*
import justkhelo.cricks.network.IApiMethod
import justkhelo.cricks.network.WebServiceClient
import justkhelo.cricks.utils.BindingUtils
import justkhelo.cricks.utils.CustomeProgressDialog
import justkhelo.cricks.utils.MyPreferences
import justkhelo.cricks.utils.MyUtils
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.math.abs

class JoinContestActivity : AppCompatActivity() {

    private lateinit var userInfo: UserInfo
    private lateinit var customeProgressDialog: CustomeProgressDialog
    private var walletAmount: Double = 0.0
    private var bonusAmount: Double = 0.0
    private var extraCashAmount: Double = 0.0
    var createdTeamIdList: ArrayList<Int>? = null
    private var mBinding: FragmentJoinContestConfirmationBinding? = null
    private var mContext: Context? = null
    var myTeamArrayList: ArrayList<MyTeamModels> = ArrayList<MyTeamModels>()
    var matchObject: UpcomingMatchesModel? = null
    var contestModel: ContestModelLists? = null

    companion object {
        val TAG: String = JoinContestActivity::class.java.simpleName
        var DISCOUNT_ON_BONUS: Int = 0
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = DataBindingUtil.setContentView(
            this,
            R.layout.fragment_join_contest_confirmation
        )
        mContext = this

        matchObject =
            intent.getSerializableExtra(CreateTeamActivity.SERIALIZABLE_MATCH_KEY) as UpcomingMatchesModel?
        contestModel =
            intent.getSerializableExtra(CreateTeamActivity.SERIALIZABLE_CONTEST_KEY) as ContestModelLists?
        myTeamArrayList =
            intent.getSerializableExtra(CreateTeamActivity.SERIALIZABLE_SELECTED_TEAMS) as ArrayList<MyTeamModels>

        customeProgressDialog = CustomeProgressDialog(mContext)
        mBinding!!.imgClose.setOnClickListener(View.OnClickListener {
            setResult(RESULT_OK)
            finish()
        })
        initWalletInfo()
    }

    override fun onBackPressed() {
        setResult(RESULT_OK)
        finish()
    }

    private fun initWalletInfo() {
        val walletInfo = (applicationContext as NinjaApplication).walletInfo
        userInfo = (applicationContext as NinjaApplication).userInformations
        JoinContestDialogFragment.DISCOUNT_ON_BONUS = contestModel!!.usableBonus.toInt()
        walletAmount = walletInfo.walletAmount
        bonusAmount = walletInfo.bonusAmount
        extraCashAmount = walletInfo.extraCash
        createdTeamIdList = ArrayList<Int>()
        var totalEntryFees = 0.0
        var discountFromBonusAmount = 0.0
        var totalPayable = 0.0
        if (contestModel!!.isBonusContest) {
            mBinding!!.walletTotalAmount.text = String.format("Bonus Amount =₹%.2f", bonusAmount)
        } else {
            mBinding!!.walletTotalAmount.text =
                String.format("Amount Added + Bonus =₹%.2f", walletAmount + bonusAmount)
        }

        for (x in 0 until myTeamArrayList.size) {
            val objects = myTeamArrayList[x]
            if (objects.isSelected!!) {
                createdTeamIdList!!.add(objects.teamId!!.teamId)

                totalEntryFees += contestModel!!.entryFees.toInt()
            }
        }

        var actualPayable: Double = 0.0
        if (contestModel!!.isBonusContest) {
            actualPayable = totalEntryFees
            mBinding!!.entryFees.text = "0"
            mBinding!!.usableCashbonus.text = String.format("₹%.2f", actualPayable)
        } else {

            discountFromBonusAmount =
                ((totalEntryFees * JoinContestDialogFragment.DISCOUNT_ON_BONUS)) / 100

            if (bonusAmount >= discountFromBonusAmount) {
                totalPayable = totalEntryFees - discountFromBonusAmount
            } else {
                discountFromBonusAmount = 0.0
                totalPayable = totalEntryFees - discountFromBonusAmount
            }
            actualPayable = totalPayable
            mBinding!!.entryFees.text = String.format("₹%.2f", totalEntryFees)
            mBinding!!.usableCashbonus.text = String.format("₹%.2f", discountFromBonusAmount)

            Log.e(TAG, "actualPayable ======> $actualPayable")
        }

        if (contestModel!!.extra_cash_usable == "1") {
            mBinding!!.extraLayout.visibility = View.VISIBLE
            if (extraCashAmount >= totalEntryFees) {
                mBinding!!.usableExtraCash.text = String.format("₹%.2f", totalEntryFees)
                actualPayable = 0.0
            } else if(totalEntryFees >= extraCashAmount) {
                mBinding!!.usableExtraCash.text = String.format("₹%.2f", extraCashAmount)
                actualPayable -= extraCashAmount
            }
        } else {
            mBinding!!.extraLayout.visibility = View.GONE
        }

        Log.e(TAG, "actualPayable ======> $actualPayable")

        mBinding!!.usableTopay.text = String.format("₹%.2f", abs(actualPayable))
        if (actualPayable > walletAmount) {
            mBinding!!.joinContest.text = "Pay Now"
            mBinding!!.joinContest.setBackgroundResource(R.drawable.default_flat_button_sportsfight)
        }
        mBinding!!.joinContest.setOnClickListener {

            if (actualPayable > walletAmount && !contestModel!!.isBonusContest) {
                val intent = Intent(mContext, AddMoneyActivity::class.java)
                intent.putExtra(AddMoneyActivity.ADD_EXTRA_AMOUNT, Math.abs(actualPayable))
                startActivityForResult(intent, MyBalanceActivity.REQUEST_CODE_ADD_MONEY)
                finish()
            } else {
                placeOrders(totalEntryFees, actualPayable, discountFromBonusAmount)
            }
        }

        mBinding!!.termsCondition.setOnClickListener(View.OnClickListener {
            val intent = Intent(mContext, WebActivity::class.java)
            intent.putExtra(WebActivity.KEY_TITLE, BindingUtils.WEB_TITLE_TERMS_CONDITION)
            intent.putExtra(WebActivity.KEY_URL, BindingUtils.WEBVIEW_TNC)
            val options =
                ActivityOptions.makeSceneTransitionAnimation(this@JoinContestActivity)
            startActivity(intent, options.toBundle())
        })
    }

    private fun placeOrders(
        totalEntryFees: Double,
        totalPayable: Double,
        discountFromBonusAmount: Double
    ) {
        if (!MyUtils.isConnectedWithInternet(this@JoinContestActivity)) {
            MyUtils.showToast(this@JoinContestActivity, "No Internet connection found")
            return
        }
        customeProgressDialog.show()

        val jsonRequest = JsonObject()
        jsonRequest.addProperty("user_id", MyPreferences.getUserID(this)!!)
        jsonRequest.addProperty("system_token", MyPreferences.getSystemToken(this)!!)
        jsonRequest.addProperty("match_id", matchObject!!.matchId)
        jsonRequest.addProperty("contest_id", contestModel!!.id)
        jsonRequest.addProperty("entryFees", totalEntryFees.toString())
        jsonRequest.addProperty("totalPaidAmount", totalPayable.toString())
        jsonRequest.addProperty("discountOnBonusAmount", discountFromBonusAmount.toString())

        val gson = Gson()
        val jsonString: String = gson.toJson(createdTeamIdList)
        val createdTeamIds: JsonArray = JsonParser().parse(jsonString).asJsonArray

        jsonRequest.add("created_team_id", createdTeamIds)

        WebServiceClient(mContext!!).client.create(IApiMethod::class.java)
            .joinContest(jsonRequest)
            .enqueue(object : Callback<UsersPostDBResponse?> {
                override fun onFailure(call: Call<UsersPostDBResponse?>?, t: Throwable?) {
                    customeProgressDialog.dismiss()
                }

                override fun onResponse(
                    call: Call<UsersPostDBResponse?>?,
                    response: Response<UsersPostDBResponse?>?
                ) {
                    customeProgressDialog.dismiss()
                    val res = response!!.body()
                    if (res != null) {
                        if (res.status) {
                            if (res.sessionExpired) {
                                logoutApp("Session Expired Please login again!!", false)
                            } else {
                                MyUtils.showMessage(mContext!!, res.message)
                                val intent1 = Intent(BindingUtils.EXTRA_DATA_GET_WALLET)
                                LocalBroadcastManager.getInstance(mContext!!).sendBroadcast(intent1)
                                setResult(RESULT_OK)
                                finish()
                            }
                        } else {
                            if (res.code == 1001) {
                                MyUtils.showMessage(this@JoinContestActivity, res.message)
                                MyUtils.logoutApp(this@JoinContestActivity)
                            } else {
                                MyUtils.showMessage(mContext!!, res.message)
                            }
                        }
                    }
                }
            })
    }

    fun logoutApp(message: String, boolean: Boolean) {
        if (!MyUtils.isConnectedWithInternet(this@JoinContestActivity)) {
            MyUtils.showToast(this@JoinContestActivity, "No Internet connection found")
            return
        }
        genericAlertDialog(message, boolean)
    }

    private fun genericAlertDialog(message: String, boolean: Boolean) {
        val builder = AlertDialog.Builder(mContext!!)
        //set title for alert dialog
        // builder.setTitle("Warning")
        //set message for alert dialog

        builder.setMessage(message)
        builder.setIcon(android.R.drawable.ic_dialog_alert)

        //performing positive action
        if (boolean) {
            builder.setNegativeButton("Cancel", null)
        }
        builder.setPositiveButton("OK") { dialogInterface, which ->

            customeProgressDialog.show()
            /*val request = RequestModel()
            request.user_id = MyPreferences.getUserID(mContext!!)!!
            request.token = MyPreferences.getToken(mContext!!)!!*/

            val jsonRequest = JsonObject()
            jsonRequest.addProperty("user_id", MyPreferences.getUserID(this)!!)
            jsonRequest.addProperty("system_token", MyPreferences.getSystemToken(this)!!)

            WebServiceClient(mContext!!).client.create(IApiMethod::class.java).logout(jsonRequest)
                .enqueue(object : Callback<UsersPostDBResponse?> {
                    override fun onFailure(call: Call<UsersPostDBResponse?>?, t: Throwable?) {
                        customeProgressDialog.dismiss()
                    }

                    override fun onResponse(
                        call: Call<UsersPostDBResponse?>?,
                        response: Response<UsersPostDBResponse?>?
                    ) {

                        customeProgressDialog.dismiss()
                        MyPreferences.clear(mContext!!)
                        val intent = Intent(
                            mContext!!,
                            SplashScreenActivity::class.java
                        )
                        intent.flags =
                            Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                        finish()
                    }

                })
        }
        // Create the AlertDialog
        val alertDialog: AlertDialog = builder.create()
        // Set other dialog properties
        alertDialog.setCancelable(false)
        alertDialog.setCanceledOnTouchOutside(false)
        alertDialog.show()
    }
}