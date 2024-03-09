package justkhelo.cricks.ui.myaccounts

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.bumptech.glide.Glide
import com.google.gson.JsonObject
import justkhelo.cricks.*
import justkhelo.cricks.databinding.FragmentMyAccountBalanceBinding
import justkhelo.cricks.models.UserInfo
import justkhelo.cricks.models.UsersPostDBResponse
import justkhelo.cricks.models.WalletInfo
import justkhelo.cricks.network.IApiMethod
import justkhelo.cricks.network.WebServiceClient
import justkhelo.cricks.ui.BaseFragment
import justkhelo.cricks.utils.BindingUtils
import justkhelo.cricks.utils.MyPreferences
import justkhelo.cricks.utils.MyUtils
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MyAccountBalanceFragment : BaseFragment() {

    private lateinit var walletInfo: WalletInfo
    private lateinit var userInfo: UserInfo
    private var mBinding: FragmentMyAccountBalanceBinding? = null

    companion object {
        val SERIALIZABLE_ACCOUNT_BAL: String = "playerslist"
        fun newInstance(bundle: Bundle): MyAccountBalanceFragment {
            val fragment = MyAccountBalanceFragment()
            fragment.arguments = bundle
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mBinding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_my_account_balance,
            container,
            false
        )
        return mBinding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mBinding!!.btnAddCash.setOnClickListener {
            val intent = Intent(requireActivity(), AddMoneyActivity::class.java)
            startActivity(intent)
        }

        mBinding!!.btnWithdraw.setOnClickListener {
            if (walletInfo.bankAccountVerified == BindingUtils.BANK_DOCUMENTS_STATUS_VERIFIED) {
                val amount = walletInfo.walletAmount
                if (amount >= MyPreferences.getMinWithdrawal(requireActivity())) {
                    val intent = Intent(requireActivity(), WithdrawAmountsActivity::class.java)
                    startActivityForResult(intent, VerifyDocumentsActivity.REQUESTCODE_VERIFY_DOC)
                } else {
                    MyUtils.showToast(
                        requireActivity() as AppCompatActivity,
                        "Amount is less than ₹${MyPreferences.getMinWithdrawal(requireActivity())}"
                    )
                }
            } else {
                var message = "Please Verify your account"
                if (walletInfo.bankAccountVerified == BindingUtils.BANK_DOCUMENTS_STATUS_APPROVAL_PENDING) {
                    message = "Documents Approvals Pending"
                } else if (walletInfo.bankAccountVerified == BindingUtils.BANK_DOCUMENTS_STATUS_REJECTED) {
                    message = "Your Document Rejected Please contact admin"
                }
                MyUtils.showToast(requireActivity() as AppCompatActivity, message)
            }
        }

        mBinding!!.refferalList.setOnClickListener {
            val intent = Intent(requireActivity(), InviteFriendsActivity::class.java)
            startActivity(intent)
        }
        initViews()
    }

    fun initViews() {
        walletInfo = (requireActivity().applicationContext as NinjaApplication).walletInfo
        if (walletInfo != null) {
            mBinding!!.progressBar.visibility = View.GONE
            initWalletViews(walletInfo)
            initProfile()
        }
    }

    private fun initProfile() {
        userInfo = (requireActivity().applicationContext as NinjaApplication).userInformations

        if (userInfo != null) {
            mBinding!!.profileName.text = userInfo.fullName
            mBinding!!.profileEmail.text = userInfo.userEmail
            Glide.with(requireActivity())
                .load(userInfo.profileImage)
                .placeholder(R.drawable.player_blue)
                .into(mBinding!!.profileImage)
        } else {
            mBinding!!.profileName.text = "GUEST"
        }

        mBinding!!.btnEditProfile.setOnClickListener {
            val intent = Intent(requireActivity(), EditProfileActivity::class.java)
            startActivity(intent)
        }

        if (walletInfo != null) {
            val accountStatus = walletInfo.accountStatus
            if (accountStatus != null) {
                if (walletInfo.bankAccountVerified == BindingUtils.BANK_DOCUMENTS_STATUS_REJECTED) {
                    mBinding!!.btnVerifyAccount.text = "REJECTED"
                    mBinding!!.btnVerifyAccount.setBackgroundResource(R.drawable.button_selector_red)
                    mBinding!!.btnVerifyAccount.setTextColor(Color.WHITE)
                    mBinding!!.btnVerifyAccount.setOnClickListener {
                        val intent =
                            Intent(requireActivity(), VerifyDocumentsActivity::class.java)
                        startActivityForResult(
                            intent,
                            VerifyDocumentsActivity.REQUESTCODE_VERIFY_DOC
                        )
                    }
                } else if (walletInfo.bankAccountVerified == BindingUtils.BANK_DOCUMENTS_STATUS_VERIFIED) {
                    mBinding!!.btnVerifyAccount.text = "Account Verified"
                    mBinding!!.btnVerifyAccount.setBackgroundResource(R.drawable.button_selector_green)
                    mBinding!!.btnVerifyAccount.setTextColor(Color.WHITE)
                    mBinding!!.btnVerifyAccount.setOnClickListener {
                        gotoDocumentsListActivity()
                    }
                } else if (walletInfo.bankAccountVerified == BindingUtils.BANK_DOCUMENTS_STATUS_APPROVAL_PENDING) {
                    mBinding!!.btnVerifyAccount.text = "Approval Pending"
                    mBinding!!.btnVerifyAccount.setBackgroundResource(R.drawable.button_selector_white)
                    mBinding!!.btnVerifyAccount.setOnClickListener {
                        //gotoDocumentsListActivity()
                    }
                } else {
                    mBinding!!.btnVerifyAccount.setOnClickListener {
                        val intent =
                            Intent(requireActivity(), VerifyDocumentsActivity::class.java)
                        startActivityForResult(
                            intent,
                            VerifyDocumentsActivity.REQUESTCODE_VERIFY_DOC
                        )
                    }
                }
            }
        } else {
            mBinding!!.btnVerifyAccount.setOnClickListener {
                val intent = Intent(requireActivity(), VerifyDocumentsActivity::class.java)
                startActivityForResult(intent, VerifyDocumentsActivity.REQUESTCODE_VERIFY_DOC)
            }
        }
    }

    private fun gotoDocumentsListActivity() {
        val intent = Intent(requireActivity(), DocumentsListActivity::class.java)
        startActivityForResult(intent, VerifyDocumentsActivity.REQUESTCODE_VERIFY_DOC)
    }

    private fun initWalletViews(responseModel: WalletInfo) {
        mBinding!!.addedAmount.text = String.format("₹%.2f", responseModel.depositAmount)
        mBinding!!.winningAmount.text = String.format("₹%.2f", responseModel.prizeAmount)
        mBinding!!.cashBonus.text = String.format("₹%.2f", responseModel.bonusAmount)
        mBinding!!.extraCashBonus.text = String.format("₹%.2f", responseModel.extraCash)

        mBinding!!.earnedPoints.text = String.format(
            "My Reward points: %s",
            responseModel.ninja_point
        )

        val totalBalance =
            responseModel.depositAmount + responseModel.prizeAmount + responseModel.bonusAmount
        mBinding!!.totalBalance.text = String.format("₹%.2f", totalBalance)
        mBinding!!.friendsCounts.text = String.format("%d", responseModel.refferalCounts)

        if (MyPreferences.getPaytmWithdrawBtn(requireActivity())) {
            mBinding!!.btnPaytmWithdraw.visibility = View.VISIBLE
        } else {
            mBinding!!.btnPaytmWithdraw.visibility = View.GONE
        }

        mBinding!!.btnPaytmWithdraw.setOnClickListener {
            val intent = Intent(requireContext(), PaytmWithdrawActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        getWalletBalances()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        getWalletBalances()
    }

    private fun getWalletBalances() {
        if (!MyUtils.isConnectedWithInternet(activity as AppCompatActivity)) {
            MyUtils.showToast(activity as AppCompatActivity, "No Internet connection found")
            return
        }

        mBinding!!.progressBar.visibility = View.VISIBLE

        val jsonRequest = JsonObject()
        jsonRequest.addProperty("user_id", MyPreferences.getUserID(requireActivity())!!)
        jsonRequest.addProperty("system_token", MyPreferences.getSystemToken(requireActivity())!!)

        WebServiceClient(requireActivity()).client.create(IApiMethod::class.java)
            .getWallet(jsonRequest)
            .enqueue(object : Callback<UsersPostDBResponse?> {
                override fun onFailure(call: Call<UsersPostDBResponse?>?, t: Throwable?) {
                    mBinding!!.progressBar.visibility = View.GONE
                }

                override fun onResponse(
                    call: Call<UsersPostDBResponse?>?,
                    response: Response<UsersPostDBResponse?>?
                ) {
                    mBinding!!.progressBar.visibility = View.GONE
                    if (isVisible) {
                        val res = response!!.body()
                        if (res != null) {
                            if (res.status) {
                                val responseModel = res.walletObjects
                                if (responseModel != null) {
                                    MyPreferences.setRazorPayId(requireActivity(), res.razorPay)
                                    MyPreferences.setShowPaytm(requireActivity(), res.paytm_show)
                                    MyPreferences.setShowGpay(requireActivity(), res.gpay_show)
                                    MyPreferences.setShowRazorPay(
                                        requireActivity(),
                                        res.rozarpay_show
                                    )

                                    MyPreferences.setShowPaytmWithdraw(
                                        requireActivity(),
                                        res.paytm_withdrawal
                                    )
                                    MyPreferences.setShowBankWithdraw(
                                        requireActivity(),
                                        res.bank_withdrawal
                                    )
                                    MyPreferences.setShowUPIWithdraw(
                                        requireActivity(),
                                        res.upi_withdrawal
                                    )

                                    MyPreferences.setMinWithdrawal(
                                        requireActivity(),
                                        res.minWithdrawal
                                    )

                                    MyPreferences.setPaytmWithdrawBtn(
                                        requireActivity(),
                                        res.paytm_withdrawal_btn
                                    )

                                    (activity!!.applicationContext as NinjaApplication).saveWalletInformation(
                                        responseModel
                                    )
                                    initViews()
                                }
                            } else {
                                if (res.code == 1001) {
                                    MyUtils.showMessage(requireActivity(), res.message)
                                    MyUtils.logoutApp(requireActivity())
                                } else {
                                    MyUtils.showMessage(requireActivity(), res.message)
                                }
                            }
                        }
                    }
                }
            })
    }
}