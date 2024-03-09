package justkhelo.cricks

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.google.gson.JsonObject
import justkhelo.cricks.databinding.ActivityMyBallanceBinding
import justkhelo.cricks.models.AccountDocumentStatus
import justkhelo.cricks.models.UsersPostDBResponse
import justkhelo.cricks.models.WalletInfo
import justkhelo.cricks.network.IApiMethod
import justkhelo.cricks.network.WebServiceClient
import justkhelo.cricks.ui.login.RegisterScreenActivity
import justkhelo.cricks.utils.BindingUtils
import justkhelo.cricks.utils.MyPreferences
import justkhelo.cricks.utils.MyUtils
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class MyBalanceActivity : AppCompatActivity() {

    private lateinit var walletInfo: WalletInfo
    private var mBinding: ActivityMyBallanceBinding? = null
    private var mContext: Context? = null

    companion object {
        val REQUEST_CODE_ADD_MONEY: Int = 9001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_my_ballance)

        mContext = this

        walletInfo = (application as NinjaApplication).walletInfo
        mBinding!!.toolbar.title = "My Balance"
        mBinding!!.toolbar.setTitleTextColor(resources.getColor(R.color.white))
        mBinding!!.toolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp)
        setSupportActionBar(mBinding!!.toolbar)
        mBinding!!.toolbar.setNavigationOnClickListener {
            finish()
        }

        initWalletInfo()

        mBinding!!.addCash.setOnClickListener {
            if (MyPreferences.getLoginStatus(mContext!!)!!) {
                val intent = Intent(mContext!!, AddMoneyActivity::class.java)
                startActivityForResult(intent, REQUEST_CODE_ADD_MONEY)
            } else {
                val intent = Intent(mContext!!, RegisterScreenActivity::class.java)
                intent.putExtra(RegisterScreenActivity.ISACTIVITYRESULT, true)
                startActivityForResult(intent, RegisterScreenActivity.REQUESTCODE_LOGIN)
            }
        }

        mBinding!!.btnWithdraw.setOnClickListener {
            if (walletInfo.bankAccountVerified == BindingUtils.BANK_DOCUMENTS_STATUS_VERIFIED) {
                val value = walletInfo.walletAmount
                if (value >= MyPreferences.getMinWithdrawal(mContext!!)) {
                    val intent = Intent(mContext!!, WithdrawAmountsActivity::class.java)
                    startActivityForResult(intent, VerifyDocumentsActivity.REQUESTCODE_VERIFY_DOC)
                } else {
                    MyUtils.showToast(
                        this@MyBalanceActivity,
                        "Amount is less than ₹${MyPreferences.getMinWithdrawal(mContext!!)}"
                    )
                }
            } else {
                var message = "Please Verify your account"
                if (walletInfo.bankAccountVerified == BindingUtils.BANK_DOCUMENTS_STATUS_APPROVAL_PENDING) {
                    message = "Documents Approvals Pending"
                } else if (walletInfo.bankAccountVerified == BindingUtils.BANK_DOCUMENTS_STATUS_REJECTED) {
                    message = "Your Document Rejected Please contact admin"
                }
                MyUtils.showToast(this@MyBalanceActivity, message)
            }
        }

        mBinding!!.txtRecentTransaction.setOnClickListener {
            val intent = Intent(mContext!!, MyTransactionHistoryActivity::class.java)
            startActivityForResult(intent, REQUEST_CODE_ADD_MONEY)
        }

        mBinding!!.btnPaytmWithdraw.setOnClickListener {
            val intent = Intent(mContext!!, PaytmWithdrawActivity::class.java)
            startActivity(intent)
        }
    }

    private fun updateAccountVerification(accountStatus: AccountDocumentStatus?) {
        if (accountStatus != null) {
            /*if (accountStatus.documentsVerified == BindingUtils.BANK_DOCUMENTS_STATUS_REJECTED) {
                mBinding!!.verifyAccountMessage.visibility = View.GONE
                mBinding!!.verifyAccount.text = "REJECTED"
                mBinding!!.verifyAccount.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10.0f)
                mBinding!!.verifyAccount.setBackgroundResource(R.drawable.button_selector_red)
                mBinding!!.verifyAccount.setTextColor(Color.WHITE)
                mBinding!!.verifyAccount.setOnClickListener(View.OnClickListener {
                    gotoDocumentsListActivity()
                })
            } else*/
            if (accountStatus.documentsVerified == BindingUtils.BANK_DOCUMENTS_STATUS_VERIFIED) {
                mBinding!!.verifyAccountMessage.visibility = View.GONE
                mBinding!!.verifyAccount.text = "Account Verified"
                mBinding!!.verifyAccount.setBackgroundResource(R.drawable.button_selector_green)
                mBinding!!.verifyAccount.setTextColor(Color.WHITE)
                mBinding!!.verifyAccount.setOnClickListener {
                    gotoDocumentsListActivity()
                }

            } else if (accountStatus.documentsVerified == BindingUtils.BANK_DOCUMENTS_STATUS_APPROVAL_PENDING) {
                mBinding!!.verifyAccountMessage.visibility = View.GONE
                mBinding!!.verifyAccount.text = "Approval Pending"
                mBinding!!.verifyAccount.setBackgroundResource(R.drawable.button_selector_white)
                mBinding!!.verifyAccount.setTextColor(Color.BLACK)
                mBinding!!.verifyAccount.setOnClickListener {
                    //gotoDocumentsListActivity()
                }
            } else {
                mBinding!!.verifyAccountMessage.visibility = View.VISIBLE
                mBinding!!.verifyAccount.setOnClickListener {
                    val intent =
                        Intent(mContext!!, VerifyDocumentsActivity::class.java)
                    startActivityForResult(
                        intent,
                        VerifyDocumentsActivity.REQUESTCODE_VERIFY_DOC
                    )
                }
            }
        }
    }

    private fun gotoDocumentsListActivity() {
        val intent = Intent(this, DocumentsListActivity::class.java)
        startActivityForResult(intent, VerifyDocumentsActivity.REQUESTCODE_VERIFY_DOC)
    }

    override fun onResume() {
        super.onResume()
        getWalletBalances()
    }

    private fun initWalletInfo() {
        val walletInfo = (application as NinjaApplication).walletInfo
        val totalBalance =
            walletInfo.depositAmount + walletInfo.prizeAmount + walletInfo.bonusAmount
        mBinding!!.walletTotalAmount.text = String.format("₹%.2f", totalBalance)
        mBinding!!.amountAdded.text = String.format("₹%.2f", walletInfo.prizeAmount)
        mBinding!!.depositAmount.text = String.format("₹%.2f", walletInfo.depositAmount)
        mBinding!!.bonusAmount.text = String.format("₹%.2f", walletInfo.bonusAmount)
        mBinding!!.extraCashBonus.text = String.format("₹%.2f", walletInfo.extraCash)

        if (walletInfo != null) {
            val accountStatus = walletInfo.accountStatus
            updateAccountVerification(accountStatus)
        } else {
            mBinding!!.verifyAccountMessage.visibility = View.VISIBLE
            mBinding!!.verifyAccount.setOnClickListener {
                val intent = Intent(mContext!!, VerifyDocumentsActivity::class.java)
                startActivityForResult(intent, VerifyDocumentsActivity.REQUESTCODE_VERIFY_DOC)
            }
        }

        if (MyPreferences.getPaytmWithdrawBtn(mContext!!)) {
            mBinding!!.btnPaytmWithdraw.visibility = View.VISIBLE
        } else {
            mBinding!!.btnPaytmWithdraw.visibility = View.GONE
        }
    }

    private fun getWalletBalances() {
        if (!MyUtils.isConnectedWithInternet(this)) {
            MyUtils.showToast(this, "No Internet connection found")
            return
        }

        mBinding!!.progressBar.visibility = View.VISIBLE

        val jsonRequest = JsonObject()
        jsonRequest.addProperty("user_id", MyPreferences.getUserID(this)!!)
        jsonRequest.addProperty("system_token", MyPreferences.getSystemToken(this)!!)

        WebServiceClient(this).client.create(IApiMethod::class.java).getWallet(jsonRequest)
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
                        val responseModel = res.walletObjects
                        if (responseModel != null) {
                            if (res.status) {
                                MyPreferences.setRazorPayId(mContext!!, res.razorPay)
                                MyPreferences.setShowPaytm(mContext!!, res.paytm_show)
                                MyPreferences.setShowGpay(mContext!!, res.gpay_show)
                                MyPreferences.setShowRazorPay(mContext!!, res.rozarpay_show)

                                MyPreferences.setShowPaytmWithdraw(
                                    mContext!!,
                                    res.paytm_withdrawal
                                )
                                MyPreferences.setShowBankWithdraw(
                                    mContext!!,
                                    res.bank_withdrawal
                                )
                                MyPreferences.setShowUPIWithdraw(
                                    mContext!!,
                                    res.upi_withdrawal
                                )
                                MyPreferences.setMinWithdrawal(mContext!!, res.minWithdrawal)

                                MyPreferences.setPaytmWithdrawBtn(
                                    mContext!!,
                                    res.paytm_withdrawal_btn
                                )

                                (application as NinjaApplication).saveWalletInformation(
                                    responseModel
                                )
                                initWalletInfo()
                            } else {
                                if (res.code == 1001) {
                                    MyUtils.showMessage(this@MyBalanceActivity, res.message)
                                    MyUtils.logoutApp(this@MyBalanceActivity)
                                } else {
                                    MyUtils.showMessage(this@MyBalanceActivity, res.message)
                                }
                            }
                        }
                    }
                }
            })
    }
}