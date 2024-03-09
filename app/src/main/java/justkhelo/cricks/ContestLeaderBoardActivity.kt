package justkhelo.cricks

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.gson.JsonObject
import justkhelo.cricks.adaptors.LeaderBoardDataAdapter
import justkhelo.cricks.databinding.ActivityContestLeaderBoardBinding
import justkhelo.cricks.models.*
import justkhelo.cricks.network.IApiMethod
import justkhelo.cricks.network.WebServiceClient
import justkhelo.cricks.utils.CustomeProgressDialog
import justkhelo.cricks.utils.MyPreferences
import justkhelo.cricks.utils.MyUtils
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*
import kotlin.collections.ArrayList

class ContestLeaderBoardActivity : AppCompatActivity(), AdapterView.OnItemSelectedListener {

    private lateinit var leaderboardList: ArrayList<LeaderBoardModel>
    private var mContext: Context? = null
    private var mBinding: ActivityContestLeaderBoardBinding? = null
    private var dataList = ArrayList<DataModel>()
    private var dataAdapter: LeaderBoardDataAdapter? = null
    private lateinit var walletInfo: WalletInfo
    private lateinit var customeProgressDialog: CustomeProgressDialog

    companion object {
        var TAG: String = ContestLeaderBoardActivity::class.java.simpleName
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_contest_leader_board)
        mContext = this
        walletInfo = (application as NinjaApplication).walletInfo
        mBinding!!.txtWalletAmount.text = "${(walletInfo.walletAmount + walletInfo.bonusAmount)} pts"
        mBinding!!.contestFilterRefresh.setColorSchemeColors(mContext!!.resources.getColor(R.color.colorPrimary))
        customeProgressDialog = CustomeProgressDialog(mContext)
        mBinding!!.spinner.onItemSelectedListener = this
        mBinding!!.contestFilterRefresh.setOnRefreshListener {
            getAllLeaderBoard(false)
        }
        mBinding!!.imgBack.setOnClickListener{
            onBackPressed()
        }
        dataList = ArrayList()
        leaderboardList = ArrayList()
        mBinding!!.recyclerView.layoutManager =
            LinearLayoutManager(mContext!!, RecyclerView.VERTICAL, false)
        dataAdapter = LeaderBoardDataAdapter(this@ContestLeaderBoardActivity, leaderboardList)
        mBinding!!.recyclerView.adapter = dataAdapter
    }

    override fun onResume() {
        super.onResume()
        getAllLeaderBoard(true)
    }

    private fun getAllLeaderBoard(showLoader: Boolean) {
        if (showLoader) {
            customeProgressDialog.show()
        }
        val jsonRequest = JsonObject()
        jsonRequest.addProperty("user_id", MyPreferences.getUserID(mContext!!)!!)
        // jsonRequest.addProperty("system_token", MyPreferences.getSystemToken(mContext!!)!!)

        WebServiceClient(mContext!!).client.create(IApiMethod::class.java)
            .globalLeaderBoard(jsonRequest)
            .enqueue(object : Callback<JsonObject?> {
                override fun onFailure(call: Call<JsonObject?>?, t: Throwable?) {
                    MyUtils.showToast(this@ContestLeaderBoardActivity, "Something went wrong!!")
                    mBinding!!.contestFilterRefresh.isRefreshing = false
                    customeProgressDialog.dismiss()
                }

                override fun onResponse(
                    call: Call<JsonObject?>?,
                    response: Response<JsonObject?>?
                ) {
                    mBinding!!.contestFilterRefresh.isRefreshing = false
                    customeProgressDialog.dismiss()
                    //mBinding!!.progressBar.visibility = View.GONE
                    val res = JSONObject(response!!.body().toString())
                    if (res.getBoolean("status")) {
                        val dataArray = res.getJSONArray("data")

                        dataList.clear()
                        for (i in 0 until dataArray.length()) {
                            val dataObject = dataArray.getJSONObject(i)
                            val rankArray = dataObject.getJSONArray("rank")
                            val userArray = dataObject.getJSONArray("leaderBoard")

                            val rankList = ArrayList<RankModel>()
                            val userList = ArrayList<LeaderBoardModel>()

                            for (j in 0 until rankArray.length()) {
                                val rankObject = rankArray.getJSONObject(j)
                                rankList.add(
                                    RankModel(
                                        rankObject.getString("key"),
                                        rankObject.getString("value")
                                    )
                                )
                            }

                            for (j in 0 until userArray.length()) {
                                val userObject = userArray.getJSONObject(j)
                                userList.add(
                                    LeaderBoardModel(
                                        userObject.getString("max_point"),
                                        userObject.getString("points"),
                                        userObject.getString("team_count"),
                                        userObject.getString("team_name"),
                                        userObject.getString("user_id"),
                                        userObject.getString("user_name"),
                                        userObject.getString("ranks"),
                                        userObject.getString("series_id"),
                                        userObject.getLong("uid"),
                                      //  userObject.getString("profile_photo_url")
                                    )
                                )

                            }

                            dataList.add(
                                DataModel(
                                    userList,
                                    dataObject.getString("match_name"),
                                    rankList
                                )
                            )
                            updateLeaderBoardData(0)
                        }


                        val matchList = ArrayList<String>()
                        for (i in dataList) {
                            matchList.add(i.match_name)
                        }

                        val adapter = ArrayAdapter(
                            this@ContestLeaderBoardActivity,
                            R.layout.spinner_list, matchList
                        )
                        adapter.setDropDownViewResource(R.layout.spinner_list)
                        mBinding!!.spinner.adapter = adapter

                    } else {
                        Log.d("response", response.message())
                    }
                }
            })
    }

    private fun updateLeaderBoardData(pos: Int) {
        leaderboardList = dataList[pos].leaderBoard
        mBinding!!.txtRank1.text = "Rs.${dataList[pos].rank[0].value}"
        mBinding!!.txtRank2.text = "Rs. ${dataList[pos].rank[1].value}"
        mBinding!!.txtRank3.text = "Rs. ${dataList[pos].rank[2].value}"
        mBinding!!.imgFirstWinner.visibility = View.INVISIBLE
        mBinding!!.txtWinner1Score.visibility = View.INVISIBLE
        mBinding!!.txt1.visibility = View.INVISIBLE
        mBinding!!.txtWinner1.visibility = View.INVISIBLE
        mBinding!!.imgKing.visibility = View.INVISIBLE
        mBinding!!.imgSecondWinner.visibility = View.INVISIBLE
        mBinding!!.txtWinner2Score.visibility = View.INVISIBLE
        mBinding!!.txt2.visibility = View.INVISIBLE
        mBinding!!.txtWinner2.visibility = View.INVISIBLE
        mBinding!!.imgThirdWinner.visibility = View.INVISIBLE
        mBinding!!.txtWinner3Score.visibility = View.INVISIBLE
        mBinding!!.txt3.visibility = View.INVISIBLE
        mBinding!!.txtWinner3.visibility = View.INVISIBLE
        for (i in leaderboardList) {
            if (i.ranks == "1") {
                mBinding!!.imgFirstWinner.visibility = View.VISIBLE
                mBinding!!.txtWinner1Score.visibility = View.VISIBLE
                mBinding!!.txt1.visibility = View.VISIBLE
                mBinding!!.txtWinner1.visibility = View.VISIBLE
                mBinding!!.imgKing.visibility = View.VISIBLE
                Glide.with(this)
                    .load(i.profilePhotoUrl)
                    .placeholder(R.drawable.ic_profile)
                    .into(mBinding!!.imgFirstWinner)

                mBinding!!.txtWinner1.text = i.user_name
                mBinding!!.txtWinner1Score.text = "${i.max_point} pts"
            }
            if (i.ranks == "2") {
                mBinding!!.imgSecondWinner.visibility = View.VISIBLE
                mBinding!!.txtWinner2Score.visibility = View.VISIBLE
                mBinding!!.txt2.visibility = View.VISIBLE
                mBinding!!.txtWinner2.visibility = View.VISIBLE
                Glide.with(this)
                    .load(i.profilePhotoUrl)
                    .placeholder(R.drawable.ic_profile)
                    .into(mBinding!!.imgSecondWinner)
                mBinding!!.txtWinner2.text = i.user_name
                mBinding!!.txtWinner2Score.text = "${i.max_point} pts"
            }
            if (i.ranks == "3") {
                mBinding!!.imgThirdWinner.visibility = View.VISIBLE
                mBinding!!.txtWinner3Score.visibility = View.VISIBLE
                mBinding!!.txt3.visibility = View.VISIBLE
                mBinding!!.txtWinner3.visibility = View.VISIBLE
                Glide.with(this)
                    .load(i.profilePhotoUrl)
                    .placeholder(R.drawable.ic_profile)
                    .into(mBinding!!.imgThirdWinner)
                mBinding!!.txtWinner3.text = i.user_name
                mBinding!!.txtWinner3Score.text = "${i.max_point} pts"
            }
        }
        mBinding!!.recyclerView.smoothScrollToPosition(0)
        dataAdapter!!.updateDataRecord(leaderboardList)
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        updateLeaderBoardData(position)
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
        updateLeaderBoardData(0)
        Log.d("check", "nothing clicked")
    }
}