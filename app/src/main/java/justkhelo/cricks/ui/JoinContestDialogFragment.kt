package justkhelo.cricks.ui

import android.app.Activity.RESULT_OK
import android.app.ActivityOptions
import android.app.Dialog
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import justkhelo.cricks.*
import justkhelo.cricks.databinding.FragmentJoinContestConfirmationBinding
import justkhelo.cricks.models.MyTeamModels
import justkhelo.cricks.models.UpcomingMatchesModel
import justkhelo.cricks.models.UserInfo
import justkhelo.cricks.network.IApiMethod
import justkhelo.cricks.network.WebServiceClient
import justkhelo.cricks.models.ContestModelLists
import justkhelo.cricks.models.UsersPostDBResponse
import justkhelo.cricks.utils.BindingUtils
import justkhelo.cricks.utils.CustomeProgressDialog
import justkhelo.cricks.utils.MyPreferences
import justkhelo.cricks.utils.MyUtils
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class JoinContestDialogFragment(
    val myTeamArrayList: ArrayList<MyTeamModels>,
    val matchObject: UpcomingMatchesModel,
    val contestModel: ContestModelLists
) : DialogFragment() {

    private lateinit var userInfo: UserInfo
    private lateinit var customeProgressDialog: CustomeProgressDialog
    var walletAmount: Double = 0.0
    var bonusAmount: Double = 0.0
    var createdTeamIdList: ArrayList<Int>? = null
    private var mBinding: FragmentJoinContestConfirmationBinding? = null

    companion object {
        var DISCOUNT_ON_BONUS: Int = 0
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.dialog_theme)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mBinding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_join_contest_confirmation, container, false
        )
        return mBinding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        customeProgressDialog = CustomeProgressDialog(activity)
        mBinding!!.imgClose.setOnClickListener(View.OnClickListener {
            requireActivity().finish()
        })
        initWalletInfo()
        getWalletBalances()
    }

    private fun initWalletInfo() {
        val walletInfo = (requireActivity().applicationContext as NinjaApplication).walletInfo
        userInfo = (requireActivity().applicationContext as NinjaApplication).userInformations
        DISCOUNT_ON_BONUS = contestModel.usableBonus.toInt()
        walletAmount = walletInfo.walletAmount
        bonusAmount = walletInfo.bonusAmount
        createdTeamIdList = ArrayList<Int>()
        var totalEntryFees = 0.0
        var discountFromBonusAmount = 0.0
        var totalPayable = 0.0
        if (contestModel.isBonusContest) {
            mBinding!!.walletTotalAmount.text = String.format("Bonus Amount =₹%.2f", bonusAmount)
        } else {
            mBinding!!.walletTotalAmount.text =
                String.format("Amount Added + Bonus =₹%.2f", walletAmount + bonusAmount)
        }

        for (x in 0..myTeamArrayList.size - 1) {
            val objects = myTeamArrayList.get(x)
            if (objects.isSelected!!) {
                createdTeamIdList!!.add(objects.teamId!!.teamId)
                totalEntryFees += contestModel.entryFees.toInt()
            }
        }


        var actualPayable = 0.0
        if (contestModel.isBonusContest) {
            actualPayable = totalEntryFees
            mBinding!!.entryFees.text = "0"
            mBinding!!.usableCashbonus.text = String.format("₹%.2f", actualPayable)
        } else {

            discountFromBonusAmount = ((totalEntryFees * DISCOUNT_ON_BONUS)) / 100

            if (bonusAmount >= discountFromBonusAmount) {
                totalPayable = totalEntryFees - discountFromBonusAmount
            } else {
                discountFromBonusAmount = 0.0
                totalPayable = totalEntryFees - discountFromBonusAmount
            }
//            if (totalPayable <= walletAmount) {
//                actualPayable = 0.0
//            } else {
            actualPayable = totalPayable
            // }
            mBinding!!.entryFees.text = String.format("₹%.2f", totalEntryFees)
            mBinding!!.usableCashbonus.text = String.format("₹%.2f", discountFromBonusAmount)
        }


        //var finalAmount = walletAmount - totalPayable
        mBinding!!.usableTopay.text = String.format("₹%.2f", Math.abs(actualPayable))
        if (actualPayable > walletAmount) {
            mBinding!!.joinContest.text = "Pay Now"
            mBinding!!.joinContest.setBackgroundResource(R.drawable.default_flat_button_sportsfight)
        }
        mBinding!!.joinContest.setOnClickListener(View.OnClickListener {

            if (actualPayable > walletAmount && !contestModel.isBonusContest) {
                val intent = Intent(activity, AddMoneyActivity::class.java)
                intent.putExtra(AddMoneyActivity.ADD_EXTRA_AMOUNT, Math.abs(actualPayable) + 10)
                startActivityForResult(intent, MyBalanceActivity.REQUEST_CODE_ADD_MONEY)
                dismiss()
            } else {
                placeOrders(totalEntryFees, actualPayable, discountFromBonusAmount)
            }
        })

        mBinding!!.termsCondition.setOnClickListener(View.OnClickListener {
            val intent = Intent(requireActivity(), WebActivity::class.java)
            intent.putExtra(WebActivity.KEY_TITLE, BindingUtils.WEB_TITLE_TERMS_CONDITION)
            intent.putExtra(WebActivity.KEY_URL, BindingUtils.WEBVIEW_TNC)
            if (Build.VERSION.SDK_INT > 20) {
                val options =
                    ActivityOptions.makeSceneTransitionAnimation(activity)
                startActivity(intent, options.toBundle())
            } else {
                startActivity(intent)
            }
        })
    }

    private fun placeOrders(
        totalEntryFees: Double,
        totalPayable: Double,
        discountFromBonusAmount: Double
    ) {
        if (!isVisible) {
            return
        }
        if (!MyUtils.isConnectedWithInternet(activity as AppCompatActivity)) {
            MyUtils.showToast(activity as AppCompatActivity, "No Internet connection found")
            return
        }
        customeProgressDialog.show()

        val jsonRequest = JsonObject()
        jsonRequest.addProperty("user_id", MyPreferences.getUserID(requireActivity())!!)
        jsonRequest.addProperty("system_token", MyPreferences.getSystemToken(requireActivity())!!)
        jsonRequest.addProperty("match_id", matchObject.matchId)
        jsonRequest.addProperty("contest_id", contestModel.id)
        jsonRequest.addProperty("entryFees", totalEntryFees.toString())
        jsonRequest.addProperty("totalPaidAmount", totalPayable.toString())
        jsonRequest.addProperty("discountOnBonusAmount", discountFromBonusAmount.toString())

        val gson = Gson()
        val jsonString: String = gson.toJson(createdTeamIdList)
        val createdTeamIds: JsonArray = JsonParser().parse(jsonString).asJsonArray
        jsonRequest.add("created_team_id", createdTeamIds)

        WebServiceClient(requireActivity()).client.create(IApiMethod::class.java)
            .joinContest(jsonRequest)
            .enqueue(object : Callback<UsersPostDBResponse?> {
                override fun onFailure(call: Call<UsersPostDBResponse?>?, t: Throwable?) {
                    if (isVisible) {
                        customeProgressDialog.dismiss()
                    }
                }

                override fun onResponse(
                    call: Call<UsersPostDBResponse?>?,
                    response: Response<UsersPostDBResponse?>?
                ) {
                    if (isVisible) {
                        customeProgressDialog.dismiss()
                        val res = response!!.body()
                        if (res != null) {
                            if (res.status) {
                                activity!!.setResult(RESULT_OK)
                                activity!!.finish()
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

    override fun onStart() {
        super.onStart()
        val dialog: Dialog? = dialog
        if (dialog != null) {
            val width = ViewGroup.LayoutParams.MATCH_PARENT
            val height = ViewGroup.LayoutParams.MATCH_PARENT
            dialog.window!!.setLayout(width, height)
        }
    }

    private fun getWalletBalances() {
        //var userInfo = (activity as PlugSportsApplication).userInformations
        if (!MyUtils.isConnectedWithInternet(activity as AppCompatActivity)) {
            MyUtils.showToast(activity as AppCompatActivity, "No Internet connection found")
            return
        }
        customeProgressDialog.show()

        val jsonRequest = JsonObject()
        jsonRequest.addProperty("user_id", MyPreferences.getUserID(requireActivity())!!)
        jsonRequest.addProperty("system_token", MyPreferences.getSystemToken(requireActivity())!!)

        WebServiceClient(requireActivity()).client.create(IApiMethod::class.java).getWallet(jsonRequest)
            .enqueue(object : Callback<UsersPostDBResponse?> {
                override fun onFailure(call: Call<UsersPostDBResponse?>?, t: Throwable?) {
                    customeProgressDialog.dismiss()
                }

                override fun onResponse(
                    call: Call<UsersPostDBResponse?>?,
                    response: Response<UsersPostDBResponse?>?
                ) {
                    if (isVisible) {
                        customeProgressDialog.dismiss()
                        val res = response!!.body()
                        if (res != null) {
                            if (res.status) {
                                if (res.sessionExpired) {
                                    logoutApp("Session Expired Please login again!!", false)
                                } else {
                                    val responseModel = res.walletObjects
                                    if (responseModel != null) {
                                        MyPreferences.setRazorPayId(requireActivity(), res.razorPay)
                                        MyPreferences.setShowPaytm(
                                            requireActivity(),
                                            res.paytm_show
                                        )
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

                                        MyPreferences.setPaytmWithdrawBtn(
                                            requireActivity(),
                                            res.paytm_withdrawal_btn
                                        )

                                        MyPreferences.setMinWithdrawal(requireActivity(), res.minWithdrawal)

                                        (activity!!.applicationContext as NinjaApplication).saveWalletInformation(
                                            responseModel
                                        )
                                        initWalletInfo()
                                    }
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

    fun logoutApp(message: String, boolean: Boolean) {
        if (!MyUtils.isConnectedWithInternet(activity as AppCompatActivity)) {
            MyUtils.showToast(activity as AppCompatActivity, "No Internet connection found")
            return
        }
        genericAlertDialog(message, boolean)
    }

    private fun genericAlertDialog(message: String, boolean: Boolean) {
        val builder = AlertDialog.Builder(requireActivity())
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
            request.user_id = MyPreferences.getUserID(requireActivity())!!
            request.token = MyPreferences.getToken(requireActivity())!!*/

            val jsonRequest = JsonObject()
            jsonRequest.addProperty("user_id", MyPreferences.getUserID(requireActivity())!!)
            jsonRequest.addProperty("system_token", MyPreferences.getSystemToken(requireActivity())!!)

            WebServiceClient(requireActivity()).client.create(IApiMethod::class.java).logout(jsonRequest)
                .enqueue(object : Callback<UsersPostDBResponse?> {
                    override fun onFailure(call: Call<UsersPostDBResponse?>?, t: Throwable?) {
                        customeProgressDialog.dismiss()
                    }

                    override fun onResponse(
                        call: Call<UsersPostDBResponse?>?,
                        response: Response<UsersPostDBResponse?>?
                    ) {

                        customeProgressDialog.dismiss()
                        MyPreferences.clear(activity!!)
                        val intent = Intent(
                            activity!!,
                            SplashScreenActivity::class.java
                        )
                        intent.flags =
                            Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                        activity!!.finish()
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