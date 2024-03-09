package justkhelo.cricks.ui.myaccounts

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.google.gson.JsonObject
import justkhelo.cricks.R
import justkhelo.cricks.databinding.FragmentPlayingHistoryBinding
import justkhelo.cricks.models.UsersPostDBResponse
import justkhelo.cricks.network.IApiMethod
import justkhelo.cricks.network.WebServiceClient
import justkhelo.cricks.utils.MyPreferences
import justkhelo.cricks.utils.MyUtils
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PlayingHistoryFragment : Fragment() {

    private var mBinding: FragmentPlayingHistoryBinding? = null

    companion object {
        fun newInstance(bundle: Bundle): PlayingHistoryFragment {
            val fragment = PlayingHistoryFragment()
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
            R.layout.fragment_playing_history, container, false
        )
        return mBinding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mBinding!!.progressBarMatches.visibility = View.GONE
    }

    override fun onResume() {
        super.onResume()
        getPlayingMatchHistory()
    }

    fun getPlayingMatchHistory() {
        if (!MyUtils.isConnectedWithInternet(activity as AppCompatActivity)) {
            MyUtils.showToast(activity as AppCompatActivity, "No Internet connection found")
            return
        }

        mBinding!!.progressBarMatches.visibility = View.VISIBLE

        val jsonRequest = JsonObject()
        jsonRequest.addProperty("user_id", MyPreferences.getUserID(requireActivity())!!)
        jsonRequest.addProperty("system_token", MyPreferences.getSystemToken(requireActivity())!!)

        WebServiceClient(requireActivity()).client.create(IApiMethod::class.java)
            .getPlayingMatchHistory(jsonRequest)
            .enqueue(object : Callback<UsersPostDBResponse?> {
                override fun onFailure(call: Call<UsersPostDBResponse?>?, t: Throwable?) {
                    mBinding!!.progressBarMatches.visibility = View.GONE
                }

                override fun onResponse(
                    call: Call<UsersPostDBResponse?>?,
                    response: Response<UsersPostDBResponse?>?
                ) {
                    if (isAdded) {
                        mBinding!!.progressBarMatches.visibility = View.GONE
                        val res = response!!.body()
                        if (res != null) {
                            if (res.status) {
                                val responseModel = res.responseObject
                                if (responseModel != null) {
                                    mBinding!!.txtMatchPlayed.text = responseModel.totalMatchPlayed
                                    mBinding!!.txtContestPlayed.text =
                                        responseModel.totalContestJoined
                                    mBinding!!.txtContestWin.text = responseModel.totalMatchWin
                                    mBinding!!.totalBalance.text =
                                        String.format("₹%s", responseModel.totalWinningAmount)
                                    mBinding!!.totalDeposit.text =
                                        String.format("₹%s", responseModel.totalMyDeposit)
                                    mBinding!!.totalWithdraw.text =
                                        String.format("₹%s", responseModel.totalMyWithdrawal)

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