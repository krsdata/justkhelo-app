package justkhelo.cricks.ui.dashboard

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import com.google.android.material.tabs.TabLayout.TabLayoutOnPageChangeListener
import com.google.gson.JsonObject
import justkhelo.cricks.NinjaApplication
import justkhelo.cricks.R
import justkhelo.cricks.databinding.FragmentMyaccountsBinding
import justkhelo.cricks.models.UsersPostDBResponse
import justkhelo.cricks.network.IApiMethod
import justkhelo.cricks.network.WebServiceClient
import justkhelo.cricks.ui.BaseFragment
import justkhelo.cricks.ui.myaccounts.MyAccountBalanceFragment
import justkhelo.cricks.ui.myaccounts.PlayingHistoryFragment
import justkhelo.cricks.ui.myaccounts.TransactionFragment
import justkhelo.cricks.utils.MyPreferences
import justkhelo.cricks.utils.MyUtils
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MyAccountFragment : BaseFragment() {

    private var mBinding: FragmentMyaccountsBinding? = null
    private lateinit var fragment: Fragment

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mBinding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_myaccounts, container, false
        )
        return mBinding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mBinding!!.accountTabs.addTab(mBinding!!.accountTabs.newTab().setText("Balance"))
        mBinding!!.accountTabs.addTab(mBinding!!.accountTabs.newTab().setText("History"))
        mBinding!!.accountTabs.addTab(mBinding!!.accountTabs.newTab().setText("Transaction"))

        val adapter = MyAccountViewPagerAdapter(requireActivity().supportFragmentManager)

        mBinding!!.accountViewpager.adapter = adapter
        mBinding!!.accountViewpager.addOnPageChangeListener(TabLayoutOnPageChangeListener(mBinding!!.accountTabs))

        mBinding!!.accountTabs.addOnTabSelectedListener(object : OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                mBinding!!.accountViewpager.currentItem = tab.position
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })

        mBinding!!.accountTabs.getTabAt(0)?.select()
    }

    fun getWalletBalances() {
        if (!MyUtils.isConnectedWithInternet(activity as AppCompatActivity)) {
            MyUtils.showToast(activity as AppCompatActivity, "No Internet connection found")
            return
        }

        val jsonRequest = JsonObject()
        jsonRequest.addProperty("user_id", MyPreferences.getUserID(requireActivity())!!)
        jsonRequest.addProperty("system_token", MyPreferences.getSystemToken(requireActivity())!!)

        WebServiceClient(requireActivity()).client.create(IApiMethod::class.java)
            .getWallet(jsonRequest)
            .enqueue(object : Callback<UsersPostDBResponse?> {
                override fun onFailure(call: Call<UsersPostDBResponse?>?, t: Throwable?) {
                    //mBinding!!.progressBarMatches.visibility = View.GONE
                }

                override fun onResponse(
                    call: Call<UsersPostDBResponse?>?,
                    response: Response<UsersPostDBResponse?>?
                ) {
                    //mBinding!!.progressBarMatches.visibility = View.GONE
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

                                    MyPreferences.setMinWithdrawal(requireActivity(), res.minWithdrawal)

                                    MyPreferences.setPaytmWithdrawBtn(
                                        requireActivity(),
                                        res.paytm_withdrawal_btn
                                    )

                                    (activity!!.applicationContext as NinjaApplication).saveWalletInformation(
                                        responseModel
                                    )
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        fragment.onActivityResult(requestCode, resultCode, data)
    }

    internal inner class MyAccountViewPagerAdapter(manager: FragmentManager) :
        FragmentStatePagerAdapter(
            manager,
            BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT
        ) {
        override fun getCount(): Int {
            return mBinding!!.accountTabs.tabCount
        }

        override fun getItem(position: Int): Fragment {
            return when (position) {
                0 -> {
                    fragment = MyAccountBalanceFragment()
                    fragment
                }
                1 -> {
                    fragment = PlayingHistoryFragment()
                    fragment
                }
                2 -> {
                    fragment = TransactionFragment()
                    fragment
                }
                else -> {
                    fragment = MyAccountBalanceFragment()
                    fragment
                }
            }
        }
    }
}