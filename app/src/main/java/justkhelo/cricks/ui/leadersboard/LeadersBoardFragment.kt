package justkhelo.cricks.ui.leadersboard

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.gson.JsonObject
import justkhelo.cricks.*
import justkhelo.cricks.databinding.FragmentLeadersBoardBinding
import justkhelo.cricks.models.UpcomingMatchesModel
import justkhelo.cricks.models.UserInfo
import justkhelo.cricks.network.IApiMethod
import justkhelo.cricks.network.WebServiceClient
import justkhelo.cricks.models.ContestModelLists
import justkhelo.cricks.models.PlayersInfoModel
import justkhelo.cricks.models.UsersPostDBResponse
import justkhelo.cricks.models.LeadersBoardModels
import justkhelo.cricks.utils.BindingUtils
import justkhelo.cricks.utils.CustomeProgressDialog
import justkhelo.cricks.utils.MyPreferences
import justkhelo.cricks.utils.MyUtils
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class LeadersBoardFragment : Fragment() {

    private lateinit var userInfo: UserInfo
    private lateinit var customeProgressDialog: CustomeProgressDialog
    private var teamName: String? = ""
    private var mBinding: FragmentLeadersBoardBinding? = null
    var adapter: LeadersBoardAdapter? = null
    var leadersBoardList = ArrayList<LeadersBoardModels>()

    var matchObject: UpcomingMatchesModel? = null
    var contestObject: ContestModelLists? = null

    companion object {
        fun newInstance(bundle: Bundle): LeadersBoardFragment {
            val fragment = LeadersBoardFragment()
            fragment.arguments = bundle
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        contestObject =
            requireArguments().get(ContestActivity.SERIALIZABLE_KEY_CONTEST_OBJECT) as ContestModelLists
        matchObject =
            requireArguments().get(ContestActivity.SERIALIZABLE_KEY_MATCH_OBJECT) as UpcomingMatchesModel
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mBinding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_leaders_board, container, false
        )
        return mBinding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        customeProgressDialog = CustomeProgressDialog(activity)
        mBinding!!.progressBar.visibility = View.GONE
        mBinding!!.prizeLeadersboardRecycler.layoutManager =
            LinearLayoutManager(activity, RecyclerView.VERTICAL, false)
        userInfo = (requireActivity().applicationContext as NinjaApplication).userInformations
        val dividerItemDecoration = DividerItemDecoration(
            mBinding!!.prizeLeadersboardRecycler.context,
            RecyclerView.VERTICAL
        )
        mBinding!!.prizeLeadersboardRecycler.addItemDecoration(dividerItemDecoration)

        adapter = LeadersBoardAdapter(requireActivity(), leadersBoardList)
        mBinding!!.prizeLeadersboardRecycler.adapter = adapter
        adapter!!.onItemClick = { objects ->
            teamName = String.format("%s(%s)", objects.userInfo!!.fullName, objects.teamName)
            if (TextUtils.isEmpty(objects.userId)) {
                MyUtils.showToast(
                    requireActivity() as AppCompatActivity,
                    "System issue please contact Admin."
                )
            } else {
                if (objects.userId.equals(MyPreferences.getUserID(requireActivity()))) {
                    getPoints(objects.teamId, objects.userId)
                } else
                    if (matchObject!!.status != BindingUtils.MATCH_STATUS_UPCOMING) {
                        getPoints(objects.teamId, objects.userId)
                    } else {
                        MyUtils.showToast(
                            requireActivity() as AppCompatActivity,
                            "You cannot see other players teams, until match started"
                        )
                    }
            }
        }
        mBinding!!.swipeRefreshLeaderboard.setOnRefreshListener {
            getLeadersBoards()
        }
        setTotalTeamCounts("0")
        getLeadersBoards()
    }

    private fun setTotalTeamCounts(value: String) {
        mBinding!!.totalTeamCounts.text = String.format("ALL TEAMS (%s)", value)
    }

    private fun getPoints(teamId: Int, user_id: String) {
        customeProgressDialog.show()

        val jsonRequest = JsonObject()
        jsonRequest.addProperty("user_id", MyPreferences.getUserID(requireActivity())!!)
        jsonRequest.addProperty("system_token", MyPreferences.getSystemToken(requireActivity())!!)
        jsonRequest.addProperty("team_id", teamId)

        WebServiceClient(requireActivity()).client.create(IApiMethod::class.java)
            .getPoints(jsonRequest)
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

                                BindingUtils.sendEventLogs(
                                    activity!!,
                                    "" + matchObject!!.matchId,
                                    "" + contestObject!!.id,
                                    user_id,
                                    teamId,
                                    (requireActivity().applicationContext as NinjaApplication).userInformations,
                                    "Last Seen"
                                )

                                val intent = Intent(activity, TeamPreviewActivity::class.java)
                                intent.putExtra(TeamPreviewActivity.KEY_TEAM_NAME, teamName)
                                intent.putExtra(TeamPreviewActivity.KEY_TEAM_ID, teamId)
                                intent.putExtra(
                                    TeamPreviewActivity.KEY_CONTEST_ID,
                                    contestObject!!.id.toString()
                                )
                                intent.putExtra(TeamPreviewActivity.KEY_USER_ID, user_id)
                                intent.putExtra(
                                    CreateTeamActivity.SERIALIZABLE_MATCH_KEY,
                                    matchObject
                                )
                                intent.putExtra(
                                    TeamPreviewActivity.SERIALIZABLE_TEAM_PREVIEW_KEY,
                                    hasmapPlayers
                                )
                                startActivity(intent)
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
            })
    }

    fun getLeadersBoards() {
        if (!isVisible) {
            return
        }
        mBinding!!.progressBar.visibility = View.VISIBLE

        val jsonRequest = JsonObject()
        jsonRequest.addProperty("user_id", MyPreferences.getUserID(requireActivity())!!)
        jsonRequest.addProperty("system_token", MyPreferences.getSystemToken(requireActivity())!!)
        jsonRequest.addProperty("contest_id", contestObject!!.id)
        jsonRequest.addProperty("match_id", matchObject!!.matchId)

        WebServiceClient(requireActivity()).client.create(IApiMethod::class.java)
            .getLeaderBoard(jsonRequest)
            .enqueue(object : Callback<UsersPostDBResponse?> {
                override fun onFailure(call: Call<UsersPostDBResponse?>?, t: Throwable?) {
                    if (!isVisible) {
                        return
                    }
                    mBinding!!.swipeRefreshLeaderboard.isRefreshing = false
                }

                override fun onResponse(
                    call: Call<UsersPostDBResponse?>?,
                    response: Response<UsersPostDBResponse?>?
                ) {
                    if (!isVisible) {
                        return
                    }
                    mBinding!!.progressBar.visibility = View.GONE
                    mBinding!!.swipeRefreshLeaderboard.isRefreshing = false
                    val res = response!!.body()
                    if (res != null) {
                        if (res.status) {
                            val responseModel = res.leaderBoardList
                            if (responseModel != null) {
                                if (responseModel.size > 0) {
                                    leadersBoardList.clear()
                                    leadersBoardList.addAll(responseModel)
                                    adapter!!.notifyDataSetChanged()
                                    setTotalTeamCounts(res.teamCount)
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
            })
    }

    inner class LeadersBoardAdapter(
        val context: Context,
        rangeModels: ArrayList<LeadersBoardModels>
    ) :
        RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        var onItemClick: ((LeadersBoardModels) -> Unit)? = null
        private var matchesListObject = rangeModels

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.leaders_board_rows, parent, false)
            return MyMatchViewHolder(view)
        }

        override fun onBindViewHolder(parent: RecyclerView.ViewHolder, viewType: Int) {
            val objectVal = matchesListObject[viewType]
            val viewHolder: MyMatchViewHolder = parent as MyMatchViewHolder
            viewHolder.teamName.text =
                String.format("%s(%s)", objectVal.userInfo!!.teamName, objectVal.teamName)
            viewHolder.userPoints.text = objectVal.teamPoints
            viewHolder.playeRanks.text = objectVal.teamRanks
            if (matchObject!!.status == BindingUtils.MATCH_STATUS_LIVE) {
                viewHolder.teamWonStatus.text = "Winning Zone"
            } else {
                viewHolder.teamWonStatus.text = String.format(
                    "Won ₹%s",
                    objectVal.teamWonStatus
                )
            }

            if (objectVal.teamRanks.toInt() == 0) {
                viewHolder.playeRanks.visibility = View.INVISIBLE
                viewHolder.userPoints.visibility = View.INVISIBLE
            } else {
                viewHolder.playeRanks.visibility = View.VISIBLE
                viewHolder.userPoints.visibility = View.VISIBLE
            }

            if (!TextUtils.isEmpty(objectVal.teamWonStatus)) {
                if (objectVal.teamWonStatus.toDouble() > 0) {
                    viewHolder.teamWonStatus.visibility = View.VISIBLE
                } else {
                    viewHolder.teamWonStatus.visibility = View.INVISIBLE
                }
            }

            if (!TextUtils.isEmpty(objectVal.userInfo.profileImage)) {
                Glide.with(context)
                    .load(objectVal.userInfo.profileImage)
                    .placeholder(R.drawable.placeholder_player_teama)
                    .into(viewHolder.profileImage)
            } else {
                viewHolder.profileImage.setImageResource(R.drawable.placeholder_player_teama)
            }
        }

        override fun getItemCount(): Int {
            return matchesListObject.size
        }

        inner class MyMatchViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            init {
                itemView.setOnClickListener {
                    onItemClick?.invoke(matchesListObject[adapterPosition])
                }
            }

            val profileImage = itemView.findViewById<ImageView>(R.id.profile_image)
            val teamName = itemView.findViewById<TextView>(R.id.team_name)
            val userPoints = itemView.findViewById<TextView>(R.id.points)
            val playeRanks = itemView.findViewById<TextView>(R.id.player_rank)
            val teamWonStatus = itemView.findViewById<TextView>(R.id.team_won_status)
            val imgMatchStatus = itemView.findViewById<ImageView>(R.id.match_status)
            val rankLayout = itemView.findViewById<LinearLayout>(R.id.rankLayout)
        }
    }
}