package justkhelo.cricks

import android.content.Context
import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import com.google.gson.JsonObject
import justkhelo.cricks.databinding.ActivityPlayerInfoBinding
import justkhelo.cricks.models.PlayersInfoModel
import justkhelo.cricks.models.UpcomingMatchesModel
import justkhelo.cricks.network.IApiMethod
import justkhelo.cricks.network.WebServiceClient
import justkhelo.cricks.ui.BaseActivity
import justkhelo.cricks.utils.CustomeProgressDialog
import justkhelo.cricks.utils.MyUtils
import justkhelo.cricks.utils.MyUtils.Companion.isConnectedWithInternet
import justkhelo.cricks.utils.MyUtils.Companion.showToast
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*
import kotlin.collections.ArrayList

class PlayerInfoActivity : BaseActivity() {

    var mContext: Context? = null
    var mBinding: ActivityPlayerInfoBinding? = null
    var playerMatchData = ArrayList<JSONObject>()
    var adapter: PlayerInfoAdapter? = null

    private var matchObject: UpcomingMatchesModel? = null
    private var playersInfoModel: PlayersInfoModel? = null

    companion object {
        private val TAG = PlayerInfoActivity::class.java.simpleName
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = DataBindingUtil.setContentView(
            this,
            R.layout.activity_player_info
        )
        customeProgressDialog = CustomeProgressDialog(this)
        userInfo = (application as NinjaApplication).userInformations
        mContext = this

        mBinding!!.toolbar.title = "Player Info"
        mBinding!!.toolbar.setTitleTextColor(resources.getColor(R.color.white))
        mBinding!!.toolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp)
        setSupportActionBar(mBinding!!.toolbar)
        mBinding!!.toolbar.setNavigationOnClickListener {
            finish()
        }

        matchObject = intent.getSerializableExtra("matchData") as UpcomingMatchesModel?
        playersInfoModel = intent.getSerializableExtra("playerData") as PlayersInfoModel?

        getPlayerInfo()
    }

    private fun getPlayerInfo() {
        if (!isConnectedWithInternet(this)) {
            showToast(this, "No Internet connection found")
            return
        }
        customeProgressDialog.show()
        val jsonRequest = JsonObject()
        jsonRequest.addProperty("match_id", matchObject!!.matchId)
        jsonRequest.addProperty("player_id", playersInfoModel!!.playerId)

        WebServiceClient(this).client.create(IApiMethod::class.java)
            .getPlayerDetails(jsonRequest)
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
                            val data = res.getJSONObject("data")
                            val playerInfoObject = data.getJSONObject("player_info")

                            mBinding!!.playerName.text = playerInfoObject.getString("title")
                            mBinding!!.playerRole.text = playerInfoObject.getString("playing_role")
                            mBinding!!.playerTeam.text = playerInfoObject.getString("team_name")
                            mBinding!!.playerPoints.text =
                                playerInfoObject.getString("player_points")
                            mBinding!!.playerCredits.text =
                                playerInfoObject.getString("fantasy_player_rating")

                            if (playersInfoModel!!.isPlaying11) {
                                mBinding!!.playerInLineup.setBackgroundResource(R.drawable.circle_green)
                            } else {
                                mBinding!!.playerInLineup.setBackgroundResource(R.drawable.circle_red)
                            }

                            val jsonArray = data.getJSONArray("match_stat")
                            playerMatchData.clear()
                            for (i in 0 until jsonArray.length()) {
                                playerMatchData.add(jsonArray.getJSONObject(i))
                            }

                            adapter = PlayerInfoAdapter(
                                mContext!!,
                                R.layout.player_info_item,
                                playerMatchData
                            )
                            mBinding!!.listView.adapter = adapter

                            MyUtils.showMessage(this@PlayerInfoActivity, res.getString("message"))
                        } else {
                            if (res.getInt("code") == 1001) {
                                MyUtils.showMessage(
                                    this@PlayerInfoActivity,
                                    res.getString("message")
                                )
                                MyUtils.logoutApp(this@PlayerInfoActivity)
                            } else {
                                MyUtils.showToast(
                                    this@PlayerInfoActivity,
                                    res.getString("message")
                                )
                            }
                        }

                    } else {
                        MyUtils.showToast(
                            this@PlayerInfoActivity,
                            "Something went wrong. Please contact to support."
                        )
                    }
                }
            })
    }

    override fun onBitmapSelected(bitmap: Bitmap) {

    }

    override fun onUploadedImageUrl(url: String) {

    }

    class PlayerInfoAdapter(
        var mContext: Context,
        var res: Int,
        private val objectArrayList: ArrayList<JSONObject>
    ) :
        ArrayAdapter<JSONObject?>(mContext, res, objectArrayList as List<JSONObject>) {
        var holder: ViewHolder? = null
        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            var view = convertView
            if (view == null) {
                view = LayoutInflater.from(mContext).inflate(res, parent, false)
                holder = ViewHolder(view)
                view.tag = holder
            } else {
                holder = view.tag as ViewHolder
            }
            try {
                val model = objectArrayList[position]
                holder!!.matchName.text = model.optString("match_name")
                holder!!.playerCredits.text = model.optString("selection")
                holder!!.playerPoints.text = model.optString("player_points")
                holder!!.matchTime.text = model.optString("date")
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return view!!
        }

        inner class ViewHolder(view: View) {
            var matchName: TextView = view.findViewById(R.id.match_name)
            var playerCredits: TextView = view.findViewById(R.id.player_credits)
            var playerPoints: TextView = view.findViewById(R.id.player_points)
            var matchTime: TextView = view.findViewById(R.id.match_time)
        }
    }
}