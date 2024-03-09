package justkhelo.cricks

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.databinding.DataBindingUtil
import justkhelo.cricks.databinding.ActivityTeamPreviewBinding
import justkhelo.cricks.models.PlayersInfoModel
import justkhelo.cricks.utils.BindingUtils
import justkhelo.cricks.utils.CustomeProgressDialog

class PreviewTeamLeaderActivity : AppCompatActivity() {

    private lateinit var customeProgressDialog: CustomeProgressDialog
    private var teamId: Int = 0
    private var teamName: String = ""
    private lateinit var hasmapPlayers: HashMap<String, ArrayList<PlayersInfoModel>>
    private var mBinding: ActivityTeamPreviewBinding? = null
    private val listWicketKeeper = ArrayList<PlayersInfoModel>()
    private val listBatsMan = ArrayList<PlayersInfoModel>()
    private val listAllRounder = ArrayList<PlayersInfoModel>()
    private val listBowler = ArrayList<PlayersInfoModel>()
    private var mContext: Context? = null

    companion object {
        const val SERIALIZABLE_TEAM_PREVIEW_KEY: String = "teampreview"
        const val KEY_TEAM_NAME: String = "team_name"
        const val KEY_TEAM_ID: String = "team_id"
        const val KEY_USER_ID: String = "user_id"
        const val KEY_CONTEST_ID: String = "contest_id"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = DataBindingUtil.setContentView(
            this,
            R.layout.activity_team_preview
        )

        mContext = this
        customeProgressDialog = CustomeProgressDialog(mContext)
        if (intent.hasExtra(KEY_TEAM_NAME)) {
            teamName = intent.getStringExtra(KEY_TEAM_NAME)!!
        }
        if (intent.hasExtra(KEY_TEAM_ID)) {
            teamId = intent.getIntExtra(KEY_TEAM_ID, 0)
        }
        hasmapPlayers =
            intent.getSerializableExtra(SERIALIZABLE_TEAM_PREVIEW_KEY) as HashMap<String, ArrayList<PlayersInfoModel>>

        mBinding!!.imgClose.setOnClickListener {
            finish()
        }

        /*mBinding!!.teamOne.text = matchObject.teamAInfo!!.teamShortName.toString()
        mBinding!!.teamTwo.text = matchObject.teamBInfo!!.teamShortName.toString()*/

        /*mBinding!!.fantasyPointsWebsview.setOnClickListener {
            val intent = Intent(mContext, WebActivity::class.java)
            intent.putExtra(WebActivity.KEY_TITLE, BindingUtils.WEB_TITLE_FANTASY_POINTS)
            intent.putExtra(WebActivity.KEY_URL, BindingUtils.WEBVIEW_FANTASY_POINTS)
            startActivity(intent)
        }*/

        mBinding!!.teamName.text = teamName
        mBinding!!.pointsBar.visibility = View.GONE
        mBinding!!.imgRefresh.visibility = View.GONE

        setupPlayersOnGrounds()
    }

    private fun setupPlayersOnGrounds() {
        mBinding!!.totalPointsValue.text = calculatePoints()
        addWicketKeeper()
        addBatsman()
        addAllRounder()
        addBowler()

        /*mBinding!!.teamOneCount.text = String.format("%d", teamACount)
        mBinding!!.teamTwoCount.text = String.format("%d", teamBCount)*/

        val gridViewAdapterWicket =
            GridViewAdapter(
                mContext!!,
                listWicketKeeper,
                listWicketKeeper[0].teamId
            )
        mBinding!!.gridWicketKeeper.numColumns = listWicketKeeper.size
        mBinding!!.gridWicketKeeper.adapter = gridViewAdapterWicket

        var sizeofColumn = 0
        val gridViewAdapterBatsMan =
            GridViewAdapter(
                mContext!!,
                listBatsMan,
                listBatsMan[0].teamId
            )

        if (listBatsMan.size > 4) {
            sizeofColumn = 3
        } else {
            sizeofColumn = listBatsMan.size
        }
        mBinding!!.gridBatsman.numColumns = sizeofColumn
        mBinding!!.gridBatsman.adapter = gridViewAdapterBatsMan

        val gridViewAdapterAllRounder =
            GridViewAdapter(
                mContext!!,
                listAllRounder,
                listAllRounder[0].teamId
            )
        if (listAllRounder.size > 4) {
            sizeofColumn = 3
        } else {
            sizeofColumn = listAllRounder.size
        }
        mBinding!!.gridAllRounders.numColumns = sizeofColumn
        mBinding!!.gridAllRounders.adapter = gridViewAdapterAllRounder

        val gridViewAdapterBowler =
            GridViewAdapter(
                mContext!!,
                listBowler,
                listBowler[0].teamId
            )
        if (listBowler.size > 4) {
            sizeofColumn = 3
        } else {
            sizeofColumn = listBowler.size
        }
        mBinding!!.gridBowlers.numColumns = sizeofColumn
        mBinding!!.gridBowlers.adapter = gridViewAdapterBowler
        setGridViewOnItemClickListener()
    }

    private fun calculatePoints(): String {
        var totalPoints = 0.0
        if (hasmapPlayers.containsKey(CreateTeamActivity.CREATE_TEAM_WICKET_KEEPER)) {
            val wkKeeper = hasmapPlayers[CreateTeamActivity.CREATE_TEAM_WICKET_KEEPER]
            for (x in 0 until wkKeeper!!.size) {
                val obj = wkKeeper[x]
                totalPoints += obj.playerPoints.toDouble()
            }
        }

        if (hasmapPlayers.containsKey(CreateTeamActivity.CREATE_TEAM_BATSMAN)) {
            val btslist = hasmapPlayers[CreateTeamActivity.CREATE_TEAM_BATSMAN]
            for (x in 0 until btslist!!.size) {
                val obj = btslist[x]
                totalPoints += obj.playerPoints.toDouble()
            }
        }

        if (hasmapPlayers.containsKey(CreateTeamActivity.CREATE_TEAM_ALLROUNDER)) {
            val allList = hasmapPlayers[CreateTeamActivity.CREATE_TEAM_ALLROUNDER]
            for (x in 0 until allList!!.size) {
                val obj = allList[x]
                totalPoints += obj.playerPoints.toDouble()
            }
        }
        if (hasmapPlayers.containsKey(CreateTeamActivity.CREATE_TEAM_BOWLER)) {
            val bowlList = hasmapPlayers[CreateTeamActivity.CREATE_TEAM_BOWLER]
            for (x in 0 until bowlList!!.size) {
                val obj = bowlList[x]
                totalPoints += obj.playerPoints.toDouble()
            }
        }
        return totalPoints.toString()
    }

    private fun setGridViewOnItemClickListener() {
        mBinding!!.gridWicketKeeper.setOnItemClickListener { parent, view, position, id ->

        }
        mBinding!!.gridBatsman.setOnItemClickListener { parent, view, position, id ->

        }
        mBinding!!.gridAllRounders.setOnItemClickListener { parent, view, position, id ->

        }
        mBinding!!.gridBowlers.setOnItemClickListener { parent, view, position, id ->

        }
    }

    private fun addWicketKeeper() {
        listWicketKeeper.clear()
        if (hasmapPlayers.containsKey(CreateTeamActivity.CREATE_TEAM_WICKET_KEEPER)) {
            val listOfPlayers = hasmapPlayers[CreateTeamActivity.CREATE_TEAM_WICKET_KEEPER]!!
            for (i in 0 until listOfPlayers.size) {
                val playerObject = listOfPlayers[i]
                playerObject.setPlayerIcon(R.drawable.player_blue)
            }
            listWicketKeeper.addAll(listOfPlayers)
        }
    }

    private fun addBatsman() {
        listBatsMan.clear()
        if (hasmapPlayers.containsKey(CreateTeamActivity.CREATE_TEAM_BATSMAN)) {
            val listOfPlayers = hasmapPlayers[CreateTeamActivity.CREATE_TEAM_BATSMAN]!!
            for (i in 0 until listOfPlayers.size) {
                val playerObject = listOfPlayers[i]
                playerObject.setPlayerIcon(R.drawable.player_blue)
            }
            listBatsMan.addAll(listOfPlayers)
        }
    }

    private fun addAllRounder() {
        listAllRounder.clear()
        if (hasmapPlayers.containsKey(CreateTeamActivity.CREATE_TEAM_ALLROUNDER)) {
            val listOfPlayers = hasmapPlayers[CreateTeamActivity.CREATE_TEAM_ALLROUNDER]!!
            for (i in 0 until listOfPlayers.size) {
                val playerObject = listOfPlayers[i]
                playerObject.setPlayerIcon(R.drawable.player_blue)
            }
            listAllRounder.addAll(listOfPlayers)
        }
    }

    private fun addBowler() {
        listBowler.clear()
        if (hasmapPlayers.containsKey(CreateTeamActivity.CREATE_TEAM_BOWLER)) {
            val listOfPlayers = hasmapPlayers[CreateTeamActivity.CREATE_TEAM_BOWLER]!!
            for (i in 0 until listOfPlayers.size) {
                val playerObject = listOfPlayers[i]
                playerObject.setPlayerIcon(R.drawable.player_blue)
            }
            listBowler.addAll(listOfPlayers)
        }
    }

    inner class GridViewAdapter(
        val context: Context,
        val listImageURLs: List<PlayersInfoModel>,
        val teamId: Int
    ) :
        BaseAdapter() {

        override fun getItem(position: Int): Any {
            return listImageURLs[position]
        }

        override fun getItemId(position: Int): Long {
            return 0
        }

        override fun getCount(): Int {
            return listImageURLs.size
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            var convertView = convertView
            val viewHolder: ViewHolder
            if (convertView == null) {
                convertView =
                    LayoutInflater.from(context)
                        .inflate(R.layout.preview_player_info, parent, false)
                viewHolder =
                    ViewHolder()
                viewHolder.imageView = convertView!!.findViewById(R.id.imageView)
                viewHolder.playerName = convertView.findViewById(R.id.player_name)
                viewHolder.playerFantasyPoints = convertView.findViewById(R.id.player_points)
                viewHolder.playerRole = convertView.findViewById(R.id.player_role)
                viewHolder.playing11 = convertView.findViewById(R.id.playing11)
                convertView.tag = viewHolder
            } else {
                viewHolder = convertView.tag as ViewHolder
            }
            val objects = listImageURLs.get(position)
            viewHolder.playerName.text = objects.shortName
            viewHolder.playerFantasyPoints.text = objects.playerPoints + " Pt"

            if (teamId == objects.teamId) {
                viewHolder.playerName.background =
                    ResourcesCompat.getDrawable(
                        context.resources,
                        R.drawable.ract_white_background,
                        null
                    )
                viewHolder.playerName.setTextColor(context.resources.getColor(R.color.black))
            } else {
                viewHolder.playerName.background =
                    ResourcesCompat.getDrawable(
                        context.resources,
                        R.drawable.ract_black_background,
                        null
                    )
                viewHolder.playerName.setTextColor(context.resources.getColor(R.color.white))
            }

            if (objects.isPlaying11) {
                viewHolder.playing11.visibility = View.GONE
            } else {
                viewHolder.playing11.visibility = View.VISIBLE
            }
            if (objects.isCaptain) {
                viewHolder.playerRole.visibility = View.VISIBLE
                viewHolder.playerRole.text = "C"
            } else if (objects.isViceCaptain) {
                viewHolder.playerRole.visibility = View.VISIBLE
                viewHolder.playerRole.text = "VC"
            } else {
                viewHolder.playerRole.visibility = View.GONE
            }
            return convertView
        }

        inner class ViewHolder {
            lateinit var imageView: ImageView
            lateinit var playerName: TextView
            lateinit var playerFantasyPoints: TextView
            lateinit var playerRole: TextView
            lateinit var playing11: TextView
        }
    }
}