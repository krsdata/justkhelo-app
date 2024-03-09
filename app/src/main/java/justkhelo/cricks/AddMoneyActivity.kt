package  justkhelo.cricks

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.opengl.Visibility
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import com.google.android.gms.wallet.*
import com.google.gson.JsonObject
import com.paytm.pgsdk.Log
import com.paytm.pgsdk.PaytmOrder
import com.paytm.pgsdk.PaytmPaymentTransactionCallback
import com.paytm.pgsdk.TransactionManager
import com.razorpay.Checkout
import com.razorpay.PaymentResultListener
import justkhelo.cricks.databinding.ActivityAddMoneyBinding
import justkhelo.cricks.models.UsersPostDBResponse
import justkhelo.cricks.network.IApiMethod
import justkhelo.cricks.network.WebServiceClient
import justkhelo.cricks.ui.BaseActivity
import justkhelo.cricks.utils.BindingUtils
import justkhelo.cricks.utils.BindingUtils.Companion.GOOGLE_TEZ_PACKAGE_NAME
import justkhelo.cricks.utils.CustomeProgressDialog
import justkhelo.cricks.utils.MyPreferences
import justkhelo.cricks.utils.MyUtils
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

class AddMoneyActivity : BaseActivity(), PaymentResultListener {

    private var mBinding: ActivityAddMoneyBinding? = null
    var paymentMode = ""
    var transactionId = ""
    var orderId = ""
    private val TAG: String? = AddMoneyActivity::class.java.simpleName
    private var mContext: Context? = null
    private var ActivityRequestCode = 2
    private var paytmOrderId = ""
    private var isValidCoupon: Boolean = false
    private var appliedCouponCode: String = ""
    private var matchId: String? = ""
    private var answer_id: String? = ""
    private var questionId: String? = ""



    companion object {
        val ADD_EXTRA_AMOUNT: String = "add_extra_amount"
        val PAYEMENT_TYPE_PAYTM: String = "paytm"
        val PAYEMENT_TYPE_GPAY: String = "gpay"
        val PAYEMENT_TYPE_RAZORPAY: String = "razorpay"
        private const val TEZ_REQUEST_CODE = 10013
        private const val UPI_REQUEST_CODE = 10014
        private const val PAYTM_REQUEST_CODE = 10015
    }

    override fun onBitmapSelected(bitmap: Bitmap) {
    }

    override fun onUploadedImageUrl(url: String) {
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = DataBindingUtil.setContentView(
            this,
            R.layout.activity_add_money
        )
        mContext = this

        mBinding!!.toolbar.title = "Add Cash"
        mBinding!!.toolbar.setTitleTextColor(resources.getColor(R.color.white))
        mBinding!!.toolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp)
        setSupportActionBar(mBinding!!.toolbar)
            mBinding!!.toolbar.setNavigationOnClickListener {
            finish()
        }

        if (intent.hasExtra(ADD_EXTRA_AMOUNT)) {
            if (intent.getIntExtra(ADD_EXTRA_AMOUNT,0) != 0) {
                val additionalAmount = intent.getIntExtra(ADD_EXTRA_AMOUNT, 0)
                mBinding!!.editAmounts.setText(String.format("%s", additionalAmount))
            } else {
                val additionalAmount = intent.getDoubleExtra(ADD_EXTRA_AMOUNT, 0.0)
                mBinding!!.editAmounts.setText(String.format("%s", additionalAmount))
            }

        }
        if (intent.hasExtra("match_id")) {
            matchId = intent.getStringExtra("match_id")
            answer_id = intent.getStringExtra("answer_id")
            questionId = intent.getStringExtra("question_id")
        }


        customeProgressDialog = CustomeProgressDialog(this)
        initWalletInfo()

        if (!MyUtils.isConnectedWithInternet(this)) {
            MyUtils.showToast(this, "No Internet connection found")
            return
        }

        mBinding!!.add100rs.setOnClickListener(View.OnClickListener {
            mBinding!!.editAmounts.setText("100")
            mBinding!!.add100rs.setBackgroundResource(R.drawable.default_rounded_button_sportsfight)
            mBinding!!.add100rs.setTextColor(resources.getColor(R.color.white))

            mBinding!!.add200rs.setBackgroundResource(R.drawable.button_selector_black)
            mBinding!!.add200rs.setTextColor(resources.getColor(R.color.black))

            mBinding!!.add300rs.setBackgroundResource(R.drawable.button_selector_black)
            mBinding!!.add300rs.setTextColor(resources.getColor(R.color.black))

            mBinding!!.add500rs.setBackgroundResource(R.drawable.button_selector_black)
            mBinding!!.add500rs.setTextColor(resources.getColor(R.color.black))
        })

        mBinding!!.add200rs.setOnClickListener(View.OnClickListener {
            mBinding!!.editAmounts.setText("200")
            mBinding!!.add100rs.setBackgroundResource(R.drawable.button_selector_black)
            mBinding!!.add100rs.setTextColor(resources.getColor(R.color.black))

            mBinding!!.add200rs.setBackgroundResource(R.drawable.default_rounded_button_sportsfight)
            mBinding!!.add200rs.setTextColor(resources.getColor(R.color.white))

            mBinding!!.add300rs.setBackgroundResource(R.drawable.button_selector_black)
            mBinding!!.add300rs.setTextColor(resources.getColor(R.color.black))

            mBinding!!.add500rs.setBackgroundResource(R.drawable.button_selector_black)
            mBinding!!.add500rs.setTextColor(resources.getColor(R.color.black))
        })

        mBinding!!.add300rs.setOnClickListener(View.OnClickListener {
            mBinding!!.editAmounts.setText("300")
            mBinding!!.add100rs.setBackgroundResource(R.drawable.button_selector_black)
            mBinding!!.add100rs.setTextColor(resources.getColor(R.color.black))

            mBinding!!.add200rs.setBackgroundResource(R.drawable.button_selector_black)
            mBinding!!.add200rs.setTextColor(resources.getColor(R.color.black))

            mBinding!!.add300rs.setBackgroundResource(R.drawable.default_rounded_button_sportsfight)
            mBinding!!.add300rs.setTextColor(resources.getColor(R.color.white))

            mBinding!!.add500rs.setBackgroundResource(R.drawable.button_selector_black)
            mBinding!!.add500rs.setTextColor(resources.getColor(R.color.black))
        })

        mBinding!!.add500rs.setOnClickListener(View.OnClickListener {
            mBinding!!.editAmounts.setText("500")
            mBinding!!.add100rs.setBackgroundResource(R.drawable.button_selector_black)
            mBinding!!.add100rs.setTextColor(resources.getColor(R.color.black))

            mBinding!!.add200rs.setBackgroundResource(R.drawable.button_selector_black)
            mBinding!!.add200rs.setTextColor(resources.getColor(R.color.black))

            mBinding!!.add300rs.setBackgroundResource(R.drawable.button_selector_black)
            mBinding!!.add300rs.setTextColor(resources.getColor(R.color.black))

            mBinding!!.add500rs.setBackgroundResource(R.drawable.default_rounded_button_sportsfight)
            mBinding!!.add500rs.setTextColor(resources.getColor(R.color.white))
        })

        mBinding!!.addCash.setOnClickListener {
            val amount = mBinding!!.editAmounts.text.toString()
            if (!TextUtils.isEmpty(amount)) {
                val amt = amount.toDouble()
                val minimumAmount = MyPreferences.getMinimumDeposit(this@AddMoneyActivity)
                if (amt >= minimumAmount!!) {
                    if (mBinding!!.usePaytmWallet.isChecked) {
                        startPaytmPayment(amt)
                    } else if (mBinding!!.useWalletGpay.isChecked) {
                        payUsingGooglePay(amt)
                    } else if (mBinding!!.useWalletPhonepay.isChecked) {
                        payUsingRazorPay(amt.toInt())
                    }
                } else {
                    MyUtils.showMessage(
                        this@AddMoneyActivity,
                        "Deposit amount cannot be less than ₹$minimumAmount"
                    )
                }
            } else {
                MyUtils.showMessage(this@AddMoneyActivity, "Please enter amount")
            }
        }

        mBinding!!.coupoCodeApply.setOnClickListener {
            checkCouponCode()
        }
    }

    private fun payUsingRazorPay(amount: Int) {
        customeProgressDialog.show()
        paymentMode = PAYEMENT_TYPE_RAZORPAY
        val amt = amount * 100
        val models = JsonObject()
        models.addProperty("amount", amt)
        models.addProperty("user_id", MyPreferences.getUserID(this)!!)
        models.addProperty("system_token", MyPreferences.getSystemToken(this)!!)

        WebServiceClient(this).client.create(IApiMethod::class.java).createRazorPayOrder(models)
            .enqueue(object : Callback<UsersPostDBResponse?> {
                override fun onFailure(call: Call<UsersPostDBResponse?>?, t: Throwable?) {
                    customeProgressDialog.dismiss()
                    showCommonAlert("" + t!!.message)
                }

                override fun onResponse(
                    call: Call<UsersPostDBResponse?>?,
                    response: Response<UsersPostDBResponse?>?
                ) {
                    customeProgressDialog.dismiss()
                    val res = response!!.body()
                    if (res != null) {
                        if (res.status) {
                            val co = Checkout()
                            co.setImage(R.mipmap.ic_launcher)
                            co.setKeyID(MyPreferences.getRazorPayId(this@AddMoneyActivity)!!)
                            Checkout.clearUserData(applicationContext)
                            try {
                                val options = JSONObject()
                                options.put(
                                    "key",
                                    MyPreferences.getRazorPayId(this@AddMoneyActivity)!!
                                )
                                options.put("name", getString(R.string.app_name))
                                options.put(
                                    "description",
                                    "Adding amount to play " + getString(R.string.app_name)
                                )
                                options.put("order_id", res.orderId) //order Id
                                options.put("theme.color", getString(R.string.razorpaythemecolor))
                                options.put("currency", "INR")
                                options.put("amount", amt.toString())  //1000 means 10rs
                                options.put("prefill.email", userInfo!!.userEmail)
                                options.put("prefill.contact", userInfo!!.mobileNumber)
                                co.open(this@AddMoneyActivity, options)
                            } catch (e: Exception) {
                                Toast.makeText(
                                    this@AddMoneyActivity,
                                    "Error in payment " ,
                                    Toast.LENGTH_LONG
                                ).show()
                                e.printStackTrace()
                            }
                        } else {
                            if (res.code == 1001) {
                                MyUtils.showMessage(this@AddMoneyActivity, res.message)
                                MyUtils.logoutApp(this@AddMoneyActivity)
                            } else {
                                MyUtils.showMessage(this@AddMoneyActivity, res.message)
                            }
                        }
                    }
                }
            })
    }

    private fun setGooglePayAvailable(available: Boolean) {
        if (available) {
            mBinding!!.useWalletGpay.visibility = View.VISIBLE
        } else {
            mBinding!!.useWalletGpay.visibility = View.GONE
        }
    }

    private fun payUsingGooglePay(amount: Double) {
        paymentMode = PAYEMENT_TYPE_GPAY

        if (isAppInstalled(GOOGLE_TEZ_PACKAGE_NAME)) {
            // showProgress();
            val upiId: String = MyPreferences.getGooglePayId(this@AddMoneyActivity)!!
            //Log.e(TAG, "upiId =======> $upiId")
            /*Uri.Builder()
                .scheme("upi")
                .authority("pay")
                .appendQueryParameter("pa", "your-merchant-vpa@xxx")
                .appendQueryParameter("pn", "your-merchant-name")
                .appendQueryParameter("mc", "your-merchant-code")
                .appendQueryParameter("tr", "your-transaction-ref-id")
                .appendQueryParameter("tn", "your-transaction-note")
                .appendQueryParameter("am", "your-order-amount")
                .appendQueryParameter("cu", "INR")
                .appendQueryParameter("url", "your-transaction-url")
                .build()*/
            val uri = Uri.Builder()
                .scheme("upi")
                .authority("pay")
                .appendQueryParameter("pa", upiId)
                .appendQueryParameter("pn", "Ninja 11 Service")
                .appendQueryParameter("tr", System.currentTimeMillis().toString())
                .appendQueryParameter("am", amount.toString())
                .appendQueryParameter("cu", "INR")
                .build()
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = uri
            intent.setPackage(GOOGLE_TEZ_PACKAGE_NAME)
            startActivityForResult(
                intent,
                TEZ_REQUEST_CODE
            )
        } else {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse(
                "https://play.google.com/store/apps/details?id=$GOOGLE_TEZ_PACKAGE_NAME"
            )
            intent.setPackage("com.android.vending")
            startActivity(intent)
        }
    }

    private fun isAppInstalled(packageName: String): Boolean {
        val pm: PackageManager = packageManager
        var installed = false
        installed = try {
            pm.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
        return installed
    }

    private fun initWalletInfo() {
        val walletInfo = (application as NinjaApplication).walletInfo

        MyPreferences.setGooglePayId(this, walletInfo.gPay)

        MyPreferences.setPaytmMid(this, walletInfo.paytmMid)
        MyPreferences.setPaytmCallback(this, walletInfo.callUrl)
        MyPreferences.setMinimumDeposit(this, walletInfo.minDeposit)

        val walletAmount = walletInfo.walletAmount
        mBinding!!.walletTotalAmount.text = String.format("₹%.2f", walletAmount)

        if (MyPreferences.getShowPaytm(mContext!!)) {
            mBinding!!.usePaytmWallet.visibility = View.VISIBLE
        } else {
            mBinding!!.usePaytmWallet.visibility = View.GONE
        }
        if (MyPreferences.getShowGpay(mContext!!)) {
            mBinding!!.useWalletGpay.visibility = View.VISIBLE
        } else {
            mBinding!!.useWalletGpay.visibility = View.GONE
        }
        if (MyPreferences.getShowRazorPay(mContext!!)) {
            mBinding!!.useWalletPhonepay.visibility = View.VISIBLE
        } else {
            mBinding!!.useWalletPhonepay.visibility = View.GONE
        }
    }

    private fun addWalletBalance() {
        if (!MyUtils.isConnectedWithInternet(this)) {
            MyUtils.showToast(this, "No Internet connection found")
            return
        }
        customeProgressDialog.show()

        val jsonRequest = JsonObject()
        jsonRequest.addProperty("user_id", MyPreferences.getUserID(this)!!)
        jsonRequest.addProperty("system_token", MyPreferences.getSystemToken(this)!!)
        jsonRequest.addProperty("deposit_amount", mBinding!!.editAmounts.text.toString())
        jsonRequest.addProperty("transaction_id", transactionId)
        jsonRequest.addProperty("order_id", orderId)
        jsonRequest.addProperty("payment_mode", paymentMode)
        jsonRequest.addProperty("payment_status", "success")
        if (mBinding!!.editCoupon.text.toString().isNotEmpty() && isValidCoupon) {
            jsonRequest.addProperty("coupon", appliedCouponCode)
        }

        WebServiceClient(this).client.create(IApiMethod::class.java).addMoney(jsonRequest)
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
                            val responseModel = res.walletObjects
                            if (responseModel != null) {
                                (application as NinjaApplication).saveWalletInformation(
                                    responseModel
                                )
                               // MyUtils.showMessage(this@AddMoneyActivity, res.message)
                                setResult(Activity.RESULT_OK)
                                finish()
                            }
                        } else {
                            if (res.code == 1001) {
                                MyUtils.showMessage(this@AddMoneyActivity, res.message)
                                MyUtils.logoutApp(this@AddMoneyActivity)
                            } else {
                                MyUtils.showMessage(this@AddMoneyActivity, res.message)
                            }
                            setResult(Activity.RESULT_CANCELED)
                            finish()
                        }
                    }
                }
            })
    }

    override fun onPaymentError(errorCode: Int, response: String?) {
        Toast.makeText(this, "Payment failed", Toast.LENGTH_LONG).show()
        try {
            Log.e(TAG, "Payment failed $errorCode \n $response")
        } catch (e: Exception) {
            Log.e(TAG, "Exception in onPaymentSuccess", e)
        }
    }

    override fun onPaymentSuccess(razorpayPaymentId: String?) {
        try {
            Toast.makeText(this, "Payment Successful ", Toast.LENGTH_LONG).show()
            transactionId = razorpayPaymentId!!
                addWalletBalance()

        } catch (e: Exception) {
            Log.e(TAG, "Exception in onPaymentSuccess", e)
        }
    }


    private fun startPaytmPayment(amt: Double) {
        if (MyUtils.isNetworkConnected(mContext!!)) {
            customeProgressDialog.show()
            try {
                val jsonRequest = JsonObject()
                jsonRequest.addProperty("user_id", MyPreferences.getUserID(this)!!)
                jsonRequest.addProperty("system_token", MyPreferences.getSystemToken(this)!!)
                jsonRequest.addProperty("deposit_amount", amt.toString())

                WebServiceClient(this).client.create(IApiMethod::class.java)
                    .initiateTransaction(jsonRequest)
                    .enqueue(object : Callback<JsonObject?> {
                        override fun onResponse(
                            call: Call<JsonObject?>,
                            response: Response<JsonObject?>
                        ) {
                            customeProgressDialog.dismiss()
                            if (response.body() != null) {
                                try {
                                    val jsonObject = JSONObject(response.body().toString())
                                    if (jsonObject.getBoolean("status")) {
                                        paytmOrderId =
                                            jsonObject.getJSONObject("data").getString("order_id")
                                        val mid = jsonObject.getJSONObject("data").getString("mid")
                                        val txnToken =
                                            jsonObject.getJSONObject("data").getString("txnToken")
                                        paytmNewPayment(paytmOrderId, mid, txnToken, amt.toString())
                                    } else {
                                        if (jsonObject.getInt("code") == 1001) {
                                            MyUtils.showMessage(
                                                mContext!!,
                                                jsonObject.getString("message")
                                            )
                                            MyUtils.logoutApp(this@AddMoneyActivity)
                                        } else {
                                            MyUtils.showMessage(
                                                mContext!!,
                                                jsonObject.getString("message")
                                            )
                                        }
                                    }
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                        }

                        override fun onFailure(call: Call<JsonObject?>, t: Throwable) {
                            customeProgressDialog.dismiss()
                            Log.e(TAG, "paytmDeposit t =======> ${t.localizedMessage}")
                        }
                    })
            } catch (e: Exception) {
                customeProgressDialog.dismiss()
                e.printStackTrace()
            }
        } else {
            MyUtils.showToast(
                this@AddMoneyActivity,
                resources.getString(R.string.internetconnection)
            )
        }
    }

    private fun paytmNewPayment(
        orderIdString: String,
        midString: String,
        txnTokenString: String,
        txnAmountString: String
    ) {
        customeProgressDialog.show()

        val callBackUrl: String = BindingUtils.PAYTM.callBackUrl + orderIdString
        Log.e(TAG, "callBackUrl =======> $callBackUrl")

        val paytmOrder = PaytmOrder(
            orderIdString,
            midString,
            txnTokenString,
            txnAmountString,
            callBackUrl
        )
        val transactionManager = TransactionManager(
            paytmOrder,
            object : PaytmPaymentTransactionCallback {
                override fun onTransactionResponse(inResponse: Bundle?) {
                    try {
                        if (inResponse != null) {
                            Log.e(TAG, "Response onTransactionResponse =====> $inResponse")
                            val jsonObject = JSONObject()
                            for (key in inResponse.keySet()) {
                                Log.e(
                                    TAG,
                                    "Response Key ========> $key  value ========> ${inResponse[key]}"
                                )
                                jsonObject.put(key, inResponse[key])
                            }
                            transactionId = inResponse["TXNID"].toString()
                                addWalletBalance()
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()

                    }
                }

                override fun networkNotAvailable() {
                    Log.e(TAG, mContext!!.resources.getString(R.string.internetconnection))
                }

                override fun onErrorProceed(inErrorMessage: String?) {
                    Log.e(TAG, "onErrorProceed  =======>  $inErrorMessage")
                }

                override fun clientAuthenticationFailed(inErrorMessage: String?) {
                    Log.e(TAG, "clientAuthenticationFailed  =======>  $inErrorMessage")
                }

                override fun someUIErrorOccurred(inErrorMessage: String?) {
                    Log.e(TAG, "someUIErrorOccurred  =======>  $inErrorMessage")
                }

                override fun onErrorLoadingWebPage(
                    iniErrorCode: Int,
                    inErrorMessage: String,
                    inFailingUrl: String
                ) {
                    Log.e(TAG, "someUIErrorOccurred  =======>  $inErrorMessage")
                }

                override fun onBackPressedCancelTransaction() {
                    Log.e(TAG, "onBackPressedCancelTransaction  =======>  ")
                    try {
                        val jsonObject = JSONObject()
                        jsonObject.put("STATUS", "USER_CANCELLED")
                        //updateOrderStatus(paytmOrderId, jsonObject)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                override fun onTransactionCancel(inErrorMessage: String, inResponse: Bundle) {
                    Log.e(TAG, "onTransactionCancel  =======>  $inErrorMessage")
                    try {
                        if (inResponse != null) {
                            Log.e(TAG, "onTransactionCancel  =======>  $inResponse")
                            val jsonObject = JSONObject()
                            for (key in inResponse.keySet()) {
                                jsonObject.put(key, inResponse[key])
                            }
                            //updateOrderStatus(paytmOrderId, jsonObject)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            })

        transactionManager.setShowPaymentUrl(BindingUtils.PAYTM.PaymentUrl)
        customeProgressDialog.dismiss()
        transactionManager.startTransaction(this, ActivityRequestCode)
        //transactionManager.startTransactionForONUS(this, ActivityRequestCode)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == TEZ_REQUEST_CODE) {
            if (data != null && data.extras != null) {
                if (data.extras!!.getString("Status").equals("SUCCESS", ignoreCase = true)) {
                    transactionId = data.extras!!.getString("txnId")!!

                        addWalletBalance()

                } else {
                    MyUtils.showToast(
                        this@AddMoneyActivity,
                        "Payment not completed, if any amount deducted, please contact us on our support system within 24hr with proof"
                    )
                }
            } else {
                MyUtils.showToast(this@AddMoneyActivity, "Payment not completed please check")
            }
        } else if (requestCode == ActivityRequestCode && data != null) {
//            MyUtils.showMessage(
//                mContext!!,
//                data.getStringExtra("nativeSdkForMerchantMessage") + data.getStringExtra("response")
//            )
            try {
                if (data.getStringExtra("response") != null) {
                    if (data.getStringExtra("response") != "") {
                        Log.e(TAG, "onActivityResult ======> " + data.getStringExtra("response"))
                        val inResponse = JSONObject(data.getStringExtra("response"))
                        Log.e(TAG, "response ======> $inResponse")
                        transactionId = inResponse.getString("TXNID")
                            addWalletBalance()

                    } else
                        try {
                            val jsonObject = JSONObject()
                            jsonObject.put("STATUS", "USER_CANCELLED")
                            //updateOrderStatus(paytmOrderId, jsonObject)
                        } catch (e: Exception) {
                            e.printStackTrace()

                        }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun addToWallet(transactionId: String, paytmOrderId: String, b: Boolean) {
        if (MyUtils.isConnectedWithInternet(this)) {
            customeProgressDialog.show()

            val jsonRequest = JsonObject()
            jsonRequest.addProperty("user_id", MyPreferences.getUserID(this)!!)
            jsonRequest.addProperty("system_token", MyPreferences.getSystemToken(this)!!)
            jsonRequest.addProperty("deposit_amount", mBinding!!.editAmounts.text.toString())
            jsonRequest.addProperty("transaction_id", transactionId)
            jsonRequest.addProperty("order_id", paytmOrderId)
            jsonRequest.addProperty("payment_mode", paymentMode)
            if (b) {
                jsonRequest.addProperty("payment_status", "success")
            } else {
                jsonRequest.addProperty("payment_status", "failed")
            }

            WebServiceClient(this).client.create(IApiMethod::class.java).addMoney(jsonRequest)
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
                                val responseModel = res.walletObjects
                                if (responseModel != null) {
                                    (application as NinjaApplication).saveWalletInformation(
                                        responseModel
                                    )
                                    MyUtils.showMessage(this@AddMoneyActivity, res.message)
                                    setResult(Activity.RESULT_OK)
                                    finish()
                                }
                            } else {
                                if (res.code == 1001) {
                                    MyUtils.showMessage(this@AddMoneyActivity, res.message)
                                    MyUtils.logoutApp(this@AddMoneyActivity)
                                } else {
                                    MyUtils.showMessage(this@AddMoneyActivity, res.message)
                                }
                            }
                        }
                    }
                })
        } else {
            MyUtils.showToast(
                this@AddMoneyActivity,
                mContext!!.resources.getString(R.string.internetconnection)
            )
        }
    }

    private fun checkCouponCode() {
        if (mBinding!!.editCoupon.text.toString().length < 0) {
            MyUtils.showMessage(mContext!!, "Please add coupon code")
        } else {
            if (MyUtils.isConnectedWithInternet(this)) {
                customeProgressDialog.show()

                val jsonRequest = JsonObject()
                jsonRequest.addProperty("user_id", MyPreferences.getUserID(this)!!)
                jsonRequest.addProperty("system_token", MyPreferences.getSystemToken(this)!!)
                jsonRequest.addProperty("code", mBinding!!.editCoupon.text.toString())

                WebServiceClient(this).client.create(IApiMethod::class.java)
                    .validateCoupon(jsonRequest)
                    .enqueue(object : Callback<JsonObject?> {
                        override fun onFailure(call: Call<JsonObject?>?, t: Throwable?) {
                            customeProgressDialog.dismiss()
                        }

                        override fun onResponse(
                            call: Call<JsonObject?>?,
                            response: Response<JsonObject?>?
                        ) {
                            customeProgressDialog.dismiss()

                            if (response!!.body() != null) {
                                val res = JSONObject(response.body().toString())
                                if (res.getBoolean("status")) {
                                    isValidCoupon = true
                                    appliedCouponCode = mBinding!!.editCoupon.text.toString()
                                    MyUtils.showToast(
                                        this@AddMoneyActivity,
                                        res.getString("message")
                                    )
                                } else {
                                    isValidCoupon = false
                                    appliedCouponCode = ""
                                    if (res.getInt("code") == 1001) {
                                        MyUtils.showMessage(
                                            this@AddMoneyActivity,
                                            res.getString("message")
                                        )
                                        MyUtils.logoutApp(this@AddMoneyActivity)
                                    } else {
                                        MyUtils.showToast(
                                            this@AddMoneyActivity,
                                            res.getString("message")
                                        )
                                    }
                                }
                            }
                        }
                    })
            } else {
                MyUtils.showToast(
                    this@AddMoneyActivity,
                    mContext!!.resources.getString(R.string.internetconnection)
                )
            }
        }
    }
}