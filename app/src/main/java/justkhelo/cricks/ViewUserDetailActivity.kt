package justkhelo.cricks

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.JsonObject
import justkhelo.cricks.databinding.ActivityViewUserDetailBinding
import justkhelo.cricks.models.*
import justkhelo.cricks.network.IApiMethod
import justkhelo.cricks.network.WebServiceClient
import justkhelo.cricks.utils.BindingUtils
import justkhelo.cricks.utils.MyPreferences
import justkhelo.cricks.utils.MyUtils
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ViewUserDetailActivity : AppCompatActivity() {

    private var mContext: Context? = null
    private var mBinding: ActivityViewUserDetailBinding? = null
    private var userList = ArrayList<UserDataModel>()
    private var userDataAdapter: UserDataAdapter? = null
    private var userName: String = ""
    private var start_end_date: String = ""
    private var league_title: String = ""
    private var team_name: String = ""
    private var points: String = ""
    private var rank: String = ""

    companion object {
        private val TAG = ViewUserDetailActivity::class.java.simpleName
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_view_user_detail)
        mContext = this

        mBinding!!.toolbar.title = "LeaderBoard User Info"
        mBinding!!.toolbar.setTitleTextColor(resources.getColor(R.color.white))
        mBinding!!.toolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp)
        setSupportActionBar(mBinding!!.toolbar)
        mBinding!!.toolbar.setNavigationOnClickListener {
            finish()
        }

        mBinding!!.refreshLayout.setColorSchemeColors(mContext!!.resources.getColor(R.color.colorPrimary))
        mBinding!!.refreshLayout.setOnRefreshListener {
            getLeaderBoardUser(false)
        }

        mBinding!!.recyclerView.layoutManager =
            LinearLayoutManager(mContext!!, RecyclerView.VERTICAL, false)


        val dividerItemDecoration = DividerItemDecoration(
            mBinding!!.recyclerView.context,
            RecyclerView.VERTICAL
        )
        mBinding!!.recyclerView.addItemDecoration(dividerItemDecoration)

        getLeaderBoardUser(true)
    }

    private fun getLeaderBoardUser(showLoader: Boolean) {
        if (showLoader) {
            mBinding!!.progressBar.visibility = View.VISIBLE
        }
        val jsonRequest = JsonObject()
        jsonRequest.addProperty("user_id", intent.getStringExtra("userId"))
        jsonRequest.addProperty("series_id", intent.getStringExtra("matchName"))
        jsonRequest.addProperty("system_token", MyPreferences.getSystemToken(mContext!!)!!)

        WebServiceClient(mContext!!).client.create(IApiMethod::class.java)
            .getLeaderBoardUser(jsonRequest)
            .enqueue(object : Callback<JsonObject?> {
                override fun onFailure(call: Call<JsonObject?>?, t: Throwable?) {
                    MyUtils.showToast(this@ViewUserDetailActivity, "Something went wrong!!")
                    mBinding!!.refreshLayout.isRefreshing = false
                    mBinding!!.progressBar.visibility = View.GONE
                }

                override fun onResponse(
                    call: Call<JsonObject?>?,
                    response: Response<JsonObject?>?
                ) {
                    mBinding!!.refreshLayout.isRefreshing = false
                    mBinding!!.progressBar.visibility = View.GONE
                    val res = JSONObject(response!!.body().toString())
                    if (res.getBoolean("status")) {
                        val dataArray = res.getJSONArray("data")

                        start_end_date = res.getString("series_end_date")
                        league_title = res.getString("league_title")
                        userName = res.getString("user_name")
                        team_name = res.getString("team_name")
                        points = res.getString("points")
                        rank = res.getString("rank")

                        mBinding!!.userName.text = userName
                        mBinding!!.teamName.text = team_name
                        mBinding!!.userPoint.text = points
                        mBinding!!.userRank.text = rank
                        mBinding!!.leagueName.text = league_title
                        mBinding!!.leagueDate.text = start_end_date

                        userList.clear()

                        for (i in 0 until dataArray.length()) {
                            val dataObject = dataArray[i] as JSONObject

                            userList.add(
                                UserDataModel(
                                    dataObject.getString("match_name"),
                                    dataObject.getString("match_start_date"),
                                    dataObject.getString("team"),
                                    dataObject.getString("points"),
                                    dataObject.getString("team_id")
                                )
                            )
                        }

                        if (userDataAdapter == null) {
                            userDataAdapter =
                                UserDataAdapter(mContext!!, userList)
                            mBinding!!.recyclerView.adapter = userDataAdapter
                        } else {
                            userDataAdapter!!.updateDataRecord(userList)
                        }
                    } else {
                        if (res.getInt("code") == 1001) {
                            MyUtils.showMessage(
                                this@ViewUserDetailActivity,
                                res.getString("message")
                            )
                            MyUtils.logoutApp(this@ViewUserDetailActivity)
                        } else {
                            MyUtils.showToast(
                                this@ViewUserDetailActivity,
                                res.getString("message")
                            )
                        }
                    }
                }
            })
    }

    inner class UserDataAdapter(val mContext: Context, var userList: ArrayList<UserDataModel>) :
        RecyclerView.Adapter<UserDataAdapter.ViewHolder>() {

        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            var matchName: TextView = itemView.findViewById(R.id.match_name)
            var matchTime: TextView = itemView.findViewById(R.id.match_start_date)
            var points: TextView = itemView.findViewById(R.id.points)
            var teamName: TextView = itemView.findViewById(R.id.team_name)
            var leadersRows: LinearLayout = itemView.findViewById(R.id.leaders_rows)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.user_info_item, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            try {
                val userModel = userList[position]
                holder.matchName.text = userModel.match_name
                holder.matchTime.text = userModel.match_start_date
                holder.points.text = userModel.points
                holder.teamName.text = userModel.team

                holder.leadersRows.setOnClickListener(UserClick((position)))

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        override fun getItemCount(): Int {
            return userList.size
        }

        fun updateDataRecord(userList: ArrayList<UserDataModel>) {
            this.userList = userList
            notifyDataSetChanged()
        }

        inner class UserClick(val pos: Int) : View.OnClickListener {
            override fun onClick(v: View?) {
                getPoints(userList[pos].team_id.toInt(), userList[pos].team)
            }
        }
    }

    private fun getPoints(teamId: Int, teamName: String) {

        mBinding!!.progressBar.visibility = View.VISIBLE

        val jsonRequest = JsonObject()
        jsonRequest.addProperty("system_token", MyPreferences.getSystemToken(mContext!!)!!)
        jsonRequest.addProperty("team_id", teamId)

        WebServiceClient(mContext!!).client.create(IApiMethod::class.java)
            .getPoints(jsonRequest)
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
                        if (res.status) {
                            //var totalPoints = res.totalPoints
                            val responseModel = res.responseObject
                            if (responseModel != null) {
                                val playerPointsList = responseModel.playerPointsList
                                val hasmapPlayers: HashMap<String, ArrayList<PlayersInfoModel>> =
                                    HashMap<String, ArrayList<PlayersInfoModel>>()

                                val wktKeeperList: ArrayList<PlayersInfoModel> =
                                    ArrayList<PlayersInfoModel>()
                                val batsManList: ArrayList<PlayersInfoModel> =
                                    ArrayList<PlayersInfoModel>()
                                val allRounderList: ArrayList<PlayersInfoModel> =
                                    ArrayList<PlayersInfoModel>()
                                val allbowlerList: ArrayList<PlayersInfoModel> =
                                    ArrayList<PlayersInfoModel>()

                                for (x in 0 until playerPointsList!!.size) {
                                    val plyObj = playerPointsList[x]
                                    if (plyObj.playerRole.equals("wk")) {
                                        wktKeeperList.add(plyObj)
                                    } else if (plyObj.playerRole.equals("bat")) {
                                        batsManList.add(plyObj)
                                    } else if (plyObj.playerRole.equals("all")) {
                                        allRounderList.add(plyObj)
                                    } else if (plyObj.playerRole.equals("bowl")) {
                                        allbowlerList.add(plyObj)
                                    }
                                }
                                hasmapPlayers[CreateTeamActivity.CREATE_TEAM_WICKET_KEEPER] =
                                    wktKeeperList
                                hasmapPlayers[CreateTeamActivity.CREATE_TEAM_BATSMAN] = batsManList
                                hasmapPlayers[CreateTeamActivity.CREATE_TEAM_ALLROUNDER] =
                                    allRounderList
                                hasmapPlayers[CreateTeamActivity.CREATE_TEAM_BOWLER] = allbowlerList

                                val intent = Intent(mContext, PreviewTeamLeaderActivity::class.java)
                                intent.putExtra(TeamPreviewActivity.KEY_TEAM_NAME, teamName)
                                intent.putExtra(TeamPreviewActivity.KEY_TEAM_ID, teamId)
                                intent.putExtra(
                                    TeamPreviewActivity.SERIALIZABLE_TEAM_PREVIEW_KEY,
                                    hasmapPlayers
                                )
                                startActivity(intent)
                            }
                        } else {
                            if (res.code == 1001) {
                                MyUtils.showMessage(mContext!!, res.message)
                                MyUtils.logoutApp(this@ViewUserDetailActivity)
                            } else {
                                MyUtils.showMessage(mContext!!, res.message)
                            }
                        }
                    }
                }
            })
    }
}