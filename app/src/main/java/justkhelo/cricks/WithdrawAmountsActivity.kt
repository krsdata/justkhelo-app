package justkhelo.cricks

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.text.Html
import android.view.View
import android.widget.RadioButton
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import com.andrognito.flashbar.Flashbar
import com.google.gson.JsonObject
import justkhelo.cricks.databinding.ActivityWithdrawAmountBinding
import justkhelo.cricks.models.UsersPostDBResponse
import justkhelo.cricks.models.WalletInfo
import justkhelo.cricks.network.IApiMethod
import justkhelo.cricks.network.WebServiceClient
import justkhelo.cricks.ui.BaseActivity
import justkhelo.cricks.utils.CustomeProgressDialog
import justkhelo.cricks.utils.MyPreferences
import justkhelo.cricks.utils.MyUtils
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

class WithdrawAmountsActivity : BaseActivity() {

    private var walletInfo: WalletInfo? = null
    private var mBinding: ActivityWithdrawAmountBinding? = null
    private var pageType: String = ""
    private var mContext: Context? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        customeProgressDialog = CustomeProgressDialog(this)
        userInfo = (application as NinjaApplication).userInformations
        walletInfo = (application as NinjaApplication).walletInfo
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_withdraw_amount)

        mContext = this

        mBinding!!.toolbar.title = "Withdraw Money"
        mBinding!!.toolbar.setTitleTextColor(resources.getColor(R.color.white))
        mBinding!!.toolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp)
        setSupportActionBar(mBinding!!.toolbar)
        mBinding!!.toolbar.setNavigationOnClickListener {
            finish()
        }

        mBinding!!.winningAmount.text = String.format("₹%s", walletInfo!!.prizeAmount)

        if (MyPreferences.getShowPaytmWithdraw(mContext!!)) {
            mBinding!!.paytmBtn.visibility = View.VISIBLE
        } else {
            mBinding!!.paytmBtn.visibility = View.GONE
        }

        if (MyPreferences.getShowBankWithdraw(mContext!!)) {
            mBinding!!.bankBtn.visibility = View.VISIBLE
        } else {
            mBinding!!.bankBtn.visibility = View.GONE
        }

        if (MyPreferences.getShowUPIWithdraw(mContext!!)) {
            mBinding!!.upiBtn.visibility = View.VISIBLE
        } else {
            mBinding!!.upiBtn.visibility = View.GONE
        }

        mBinding!!.editWithdrawalAmount.hint =
            String.format("₹%s", MyPreferences.getMinWithdrawal(mContext!!))

        mBinding!!.radioGroup.setOnCheckedChangeListener { group, checkedId ->
            val rb = group.findViewById<RadioButton>(checkedId)
            if (rb != null) {
                if (rb.text.toString().equals("paytm", true)) {
                    pageType = "paytm"
                } else if (rb.text.toString().equals("Bank Transfer", true)) {
                    pageType = "bank account"
                } else {
                    pageType = "UPI"
                }
            }
        }

        mBinding!!.submitBtnWithdrawal.setOnClickListener {
            val amount = mBinding!!.editWithdrawalAmount.text.toString().trim()
            if (amount.isEmpty()) {
                MyUtils.showMessage(mContext!!, "Withdraw amount cannot be empty")
            } else if (pageType.isEmpty()) {
                MyUtils.showMessage(mContext!!, "Please select Withdraw type")
            } else {

                val minWithdraw = MyPreferences.getMinWithdrawal(mContext!!)

                if (pageType.equals("paytm", true)) {
                    if (amount.toInt() < minWithdraw) {
                        MyUtils.showMessage(
                            mContext!!,
                            "You can not withdraw amount less than ₹$minWithdraw"
                        )
                    } else if (amount.toInt() <= 1000) {
                        if (mBinding!!.paytmEditText.visibility == View.VISIBLE) {
                            if (mBinding!!.paytmEditText.text.toString().length < 0) {
                                MyUtils.showMessage(
                                    mContext!!,
                                    "Please add your Paytm number"
                                )
                            } else {
                                showWithdrawalAlert(amount.toInt(), pageType)
                            }
                        } else {
                            showWithdrawalAlert(amount.toInt(), pageType)
                        }
                    } else {
                        MyUtils.showMessage(
                            mContext!!,
                            "Please try Bank or UPI withdraw."
                        )
                    }
                } else if (pageType.equals("bank account", true)) {
                    when {
                        amount.toInt() < 1001 -> {
                            MyUtils.showMessage(
                                mContext!!,
                                "Please try Paytm or UPI"
                            )
                        }
                        amount.toInt() <= 10000 -> {
                            showWithdrawalAlert(amount.toInt(), pageType)
                        }
                        else -> {
                            MyUtils.showMessage(
                                mContext!!,
                                "You can not withdraw amount more then ₹10000"
                            )
                        }
                    }
                } else {
                    if (amount.toInt() < minWithdraw) {
                        MyUtils.showMessage(
                            mContext!!,
                            "You can not withdraw amount less than ₹$minWithdraw"
                        )
                    } else if (amount.toInt() <= 10000) {
                        if (mBinding!!.upiEditText.visibility == View.VISIBLE) {
                            if (mBinding!!.upiEditText.text.toString().length < 0) {
                                MyUtils.showMessage(
                                    mContext!!,
                                    "Please add your UPI id"
                                )
                            } else {
                                showWithdrawalAlert(amount.toInt(), pageType)
                            }
                        } else {
                            showWithdrawalAlert(amount.toInt(), pageType)
                        }
                    } else {
                        MyUtils.showMessage(
                            mContext!!,
                            "You can not withdraw amount more then ₹10000"
                        )
                    }
                }
            }
        }

        mBinding!!.contactUs.setOnClickListener {
            val intent = Intent(this@WithdrawAmountsActivity, SupportActivity::class.java)
            startActivity(intent)
        }

        getMessage()

        mBinding!!.upiText.visibility = View.GONE
        mBinding!!.upiEditText.visibility = View.GONE
    }

    private fun showWithdrawalAlert(amount: Int, type: String) {
        val builder = AlertDialog.Builder(this)
        //set title for alert dialog
        builder.setTitle("Confirmation")
        //set message for alert dialog
        builder.setMessage(
            String.format(
                "your amount ₹%d will be transfer in respective account.",
                amount
            )
        )
        //performing positive action
        builder.setPositiveButton("Proceed") { dialogInterface, which ->
            dialogInterface.dismiss()
            withdrawalRequest(amount, type)
        }
        builder.setNegativeButton("Cancel") { dialogInterface, which ->
            dialogInterface.dismiss()
        }
        // Create the AlertDialog
        val alertDialog: AlertDialog = builder.create()
        // Set other dialog properties
        alertDialog.setCancelable(false)
        alertDialog.setCanceledOnTouchOutside(false)
        alertDialog.show()
    }

    private fun withdrawalRequest(amount: Int, type: String) {
        if (!MyUtils.isConnectedWithInternet(this)) {
            MyUtils.showToast(this, "No Internet connection found")
            return
        }
        customeProgressDialog.show()

        val jsonRequest = JsonObject()
        jsonRequest.addProperty("user_id", MyPreferences.getUserID(this)!!)
        jsonRequest.addProperty("system_token", MyPreferences.getSystemToken(this)!!)
        jsonRequest.addProperty(
            "withdraw_amount",
            MyUtils.encodeBase64(amount.toString()).toString()
        )
        jsonRequest.addProperty("payment_taken_in", MyUtils.encodeBase64(type).toString())
        if (type == "UPI") {
            jsonRequest.addProperty("upi_id", mBinding!!.upiEditText.text.toString())
        }
        if (type == "paytm") {
            jsonRequest.addProperty("paytm_number", mBinding!!.paytmEditText.text.toString())
        }
        WebServiceClient(this).client.create(IApiMethod::class.java).withdrawAmountNew(jsonRequest)
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
                            successAlert(res.message, true)
                        } else {
                            if (res.code == 1001) {
                                MyUtils.showMessage(mContext!!, res.message)
                                MyUtils.logoutApp(this@WithdrawAmountsActivity)
                            } else if (res.code == 405) {
                                mBinding!!.upiText.visibility = View.VISIBLE
                                mBinding!!.upiEditText.visibility = View.VISIBLE
                                errorAlert(res.message)
                            } else if (res.code == 406) {
                                mBinding!!.paytmText.visibility = View.VISIBLE
                                mBinding!!.paytmEditText.visibility = View.VISIBLE
                                errorAlert(res.message)
                            } else {
                                MyUtils.showMessage(mContext!!, res.message)
                            }
                        }
                    } else {
                        errorAlert("Please try again! Something went wrong")
                    }
                }
            })
    }

    private fun successAlert(message: String, isClose: Boolean) {
        val flashbar = Flashbar.Builder(this@WithdrawAmountsActivity)
            .gravity(Flashbar.Gravity.TOP)
            //.title(getString(R.string.app_name))
            .message(message)
            .backgroundDrawable(R.color.green)
            .build()
        flashbar.show()
        Handler().postDelayed(Runnable {
            if (isClose) {
                setResult(Activity.RESULT_OK)
                finish()
            }

        }, 2000L)
    }

    private fun errorAlert(message: String) {
        val flashBar = Flashbar.Builder(this@WithdrawAmountsActivity)
            .gravity(Flashbar.Gravity.TOP)
            //.title(getString(R.string.app_name))
            .message(message)
            .backgroundDrawable(R.color.green)
            .build()
        flashBar.show()
        Handler().postDelayed(Runnable { flashBar.dismiss() }, 2000L)
    }

    override fun onBitmapSelected(bitmap: Bitmap) {

    }

    override fun onUploadedImageUrl(url: String) {

    }

    private fun getMessage() {
        if (!MyUtils.isConnectedWithInternet(this)) {
            return
        }

        val jsonRequest = JsonObject()
        jsonRequest.addProperty("user_id", MyPreferences.getUserID(this)!!)
        jsonRequest.addProperty("system_token", MyPreferences.getSystemToken(this)!!)
        jsonRequest.addProperty("version_code", NinjaApplication.appVersion)

        WebServiceClient(mContext!!).client.create(IApiMethod::class.java).getMessages(jsonRequest)
            .enqueue(object : Callback<JsonObject?> {
                override fun onFailure(call: Call<JsonObject?>?, t: Throwable?) {
                    // customeProgressDialog.dismiss()
                }

                override fun onResponse(
                    call: Call<JsonObject?>?,
                    response: Response<JsonObject?>?
                ) {

                    val resObje = response!!.body().toString()
                    val jsonObject = JSONObject(resObje)
                    if (jsonObject.optBoolean("status")) {
                        val array = jsonObject.getJSONArray("data")
                        val data = array.getJSONObject(1)
                        if (data.optInt("message_status") == 0) {
                            mBinding!!.withdrawMessage.visibility = View.GONE
                        } else {
                            if (data.getString("message_type") == "HTML") {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                    mBinding!!.withdrawMessage.text =
                                        Html.fromHtml(
                                            data.getString("message"), Html.FROM_HTML_MODE_COMPACT
                                        )
                                } else {
                                    mBinding!!.withdrawMessage.text = Html.fromHtml(
                                        data.getString("message")
                                    )
                                }
                            } else {
                                mBinding!!.withdrawMessage.text = data.getString("message")
                            }
                            mBinding!!.withdrawMessage.visibility = View.VISIBLE
                        }

                        val withdrawData = array.getJSONObject(2)

                        if (withdrawData.optInt("message_status") == 0) {
                            mBinding!!.walletCard.visibility = View.GONE
                            mBinding!!.viewAmount.visibility = View.GONE
                            mBinding!!.showAlert.visibility = View.VISIBLE
                            mBinding!!.alertMessage.text = withdrawData.getString("message")
                        } else {
                            mBinding!!.walletCard.visibility = View.VISIBLE
                            mBinding!!.viewAmount.visibility = View.VISIBLE
                            mBinding!!.showAlert.visibility = View.GONE
                        }
                    }
                }
            })
    }
}