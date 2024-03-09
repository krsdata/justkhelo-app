package justkhelo.cricks.adaptors

import android.content.Context
import android.graphics.Color
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.edify.atrist.listener.OnMatchTimerStarted
import kotlinx.coroutines.InternalCoroutinesApi
import justkhelo.cricks.R
import justkhelo.cricks.models.UpcomingMatchesModel
import justkhelo.cricks.utils.BindingUtils
import java.util.*


class UpcomingMatchesAdapter(
    val context: Context,
    val tradeinfoModels: ArrayList<UpcomingMatchesModel>
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    var onItemClick: ((UpcomingMatchesModel) -> Unit)? = null
    private var matchesListObject = tradeinfoModels
    private val TYPE_NEW: Int = 1
    private val TYPE_NORMAL: Int = 100

    override fun getItemViewType(position: Int): Int {
        return if (matchesListObject[position].show_new_design) {
            TYPE_NEW
        } else {
            TYPE_NORMAL
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_NORMAL) {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.matches_row_upcoming_inner, parent, false)
            OldDataViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.matches_row_upcoming_inner_new, parent, false)
            DataViewHolder(view)
        }
    }

    @InternalCoroutinesApi
    override fun onBindViewHolder(parent: RecyclerView.ViewHolder, viewType: Int) {
        val objectVal = matchesListObject[viewType]

        if (getItemViewType(viewType) == TYPE_NORMAL) {
            val viewHolder: OldDataViewHolder = parent as OldDataViewHolder
            if (objectVal.isLineup) {
                viewHolder.matchTitle.visibility = View.VISIBLE
            } else {
                viewHolder.matchTitle.visibility = View.INVISIBLE
            }
            viewHolder.tournamentTitle.text = objectVal.leagueTitle
            // viewHolder?.matchProgress?.text = ""+objectVal.timestampEnd
            viewHolder.opponent1.text = objectVal.teamAInfo!!.teamShortName
            viewHolder.opponent2.text = objectVal.teamBInfo!!.teamShortName

            if (objectVal.freeContest) {
                viewHolder.freeView.visibility = View.VISIBLE
            } else {
                viewHolder.freeView.visibility = View.GONE
            }
            if (!TextUtils.isEmpty(objectVal.dateStart)) {
                viewHolder.matchtime.visibility = View.VISIBLE
                viewHolder.matchtime.text = objectVal.dateStart
            } else {
                viewHolder.matchtime.visibility = View.GONE
            }

            viewHolder.teamAColorView.setBackgroundColor(getRandomColor())
            viewHolder.teamBColorView.setBackgroundColor(getRandomColor())

            BindingUtils.countDownStartForAdaptors(objectVal.timestampStart,
                object : OnMatchTimerStarted {
                    override fun onTimeFinished() {
                        viewHolder.matchProgress.text = objectVal.statusString
                    }

                    override fun onTicks(time: String) {
                        viewHolder.matchProgress.text = time
                    }

                })
            if (!TextUtils.isEmpty(objectVal.contestName)) {
                viewHolder.upcomingLinearContestView.visibility = View.VISIBLE
                viewHolder.contestName.text = objectVal.contestName
                viewHolder.contestPrice.text = objectVal.contestPrize
            } else {
                viewHolder.upcomingLinearContestView.visibility = View.INVISIBLE
            }

            Glide.with(context)
                .load(objectVal.teamAInfo!!.logoUrl)
                .placeholder(R.drawable.placeholder_player_teama)
                .disallowHardwareConfig()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(viewHolder.teamALogo)

            Glide.with(context)
                .load(objectVal.teamBInfo!!.logoUrl)
                .placeholder(R.drawable.placeholder_player_teama)
                .disallowHardwareConfig()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(viewHolder.teamBLogo)
        } else {
            val viewHolder: DataViewHolder = parent as DataViewHolder
            if (objectVal.isLineup) {
                viewHolder.matchTitle.visibility = View.VISIBLE
            } else {
                viewHolder.matchTitle.visibility = View.INVISIBLE
            }
            viewHolder.tournamentTitle.text = objectVal.leagueTitle
            // viewHolder?.matchProgress?.text = ""+objectVal.timestampEnd
            viewHolder.opponent1.text = objectVal.teamAInfo!!.teamShortName
            viewHolder.opponent2.text = objectVal.teamBInfo!!.teamShortName

            if (objectVal.freeContest) {
                viewHolder.freeView.visibility = View.VISIBLE
                viewHolder.freeView.text = objectVal.dyanamic_message
            } else {
                viewHolder.freeView.visibility = View.GONE
            }
            if (!TextUtils.isEmpty(objectVal.dateStart)) {
                viewHolder.matchTime.visibility = View.VISIBLE
                viewHolder.matchTime.text = objectVal.dateStart
            } else {
                viewHolder.matchTime.visibility = View.GONE
            }

            viewHolder.teamAColorView.setBackgroundColor(getRandomColor())
            viewHolder.teamBColorView.setBackgroundColor(getRandomColor())

            BindingUtils.countDownStartForAdaptors(objectVal.timestampStart,
                object : OnMatchTimerStarted {
                    override fun onTimeFinished() {
                        viewHolder.matchProgress.text = objectVal.statusString
                    }

                    override fun onTicks(time: String) {
                        viewHolder.matchProgress.text = time
                    }
                })
            if (!TextUtils.isEmpty(objectVal.contestName)) {
                viewHolder.upcomingLinearContestView.visibility = View.VISIBLE
                viewHolder.contestName.text = objectVal.contestName
                viewHolder.contestPrice.text = objectVal.contestPrize
            } else {
                viewHolder.upcomingLinearContestView.visibility = View.INVISIBLE
            }

            Glide.with(context)
                .load(objectVal.teamAInfo!!.logoUrl)
                .placeholder(R.drawable.placeholder_player_teama)
                .disallowHardwareConfig()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(viewHolder.teamALogo)

            Glide.with(context)
                .load(objectVal.teamBInfo!!.logoUrl)
                .placeholder(R.drawable.placeholder_player_teama)
                .disallowHardwareConfig()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(viewHolder.teamBLogo)

            if (objectVal.is_dashboard) {
                viewHolder.contestLeaderBoard.visibility = View.VISIBLE
            } else {
                viewHolder.contestLeaderBoard.visibility = View.INVISIBLE
            }
        }
    }

    private fun getRandomColor(): Int {
        val rnd = Random()
        return Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256))
    }

    override fun getItemCount(): Int {
        return matchesListObject.size
    }

    inner class DataViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        init {
            itemView.setOnClickListener {
                onItemClick?.invoke(matchesListObject[adapterPosition])
            }
        }

        val teamALogo: ImageView = itemView.findViewById(R.id.teama_logo)
        val teamBLogo: ImageView = itemView.findViewById(R.id.teamb_logo)
        val matchTitle: TextView = itemView.findViewById(R.id.upcoming_match_title)
        val tournamentTitle: TextView = itemView.findViewById(R.id.tournament_title)
        val teamAColorView: View = itemView.findViewById(R.id.countrycolorview)
        val teamBColorView: View = itemView.findViewById(R.id.countrybcolorview)
        val opponent1: TextView = itemView.findViewById(R.id.upcoming_opponent1)
        val opponent2: TextView = itemView.findViewById(R.id.upcoming_opponent2)
        val freeView: TextView = itemView.findViewById(R.id.free_view)
        val matchTime: TextView = itemView.findViewById(R.id.match_time)
        val matchProgress: TextView = itemView.findViewById(R.id.upcoming_match_progress)
        val upcomingLinearContestView: LinearLayout =
            itemView.findViewById(R.id.upcoming_linear_contest_view)
        val contestName: TextView = itemView.findViewById(R.id.upcoming_contest_name)
        val contestPrice: TextView = itemView.findViewById(R.id.upcoming_contest_price)
        val contestLeaderBoard: ImageView = itemView.findViewById(R.id.contest_leader_board)

    }

    inner class OldDataViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        init {
            itemView.setOnClickListener {
                onItemClick?.invoke(matchesListObject[adapterPosition])
            }
        }

        val teamALogo: ImageView = itemView.findViewById(R.id.teama_logo)
        val teamBLogo: ImageView = itemView.findViewById(R.id.teamb_logo)
        val matchTitle: TextView = itemView.findViewById(R.id.upcoming_match_title)
        val tournamentTitle: TextView = itemView.findViewById(R.id.tournament_title)
        val teamAColorView: View = itemView.findViewById<View>(R.id.countrycolorview)
        val teamBColorView: View = itemView.findViewById<View>(R.id.countrybcolorview)
        val opponent1: TextView = itemView.findViewById(R.id.upcoming_opponent1)
        val opponent2: TextView = itemView.findViewById(R.id.upcoming_opponent2)
        val freeView: TextView = itemView.findViewById(R.id.free_view)
        val matchtime: TextView = itemView.findViewById(R.id.match_time)
        val matchProgress: TextView = itemView.findViewById(R.id.upcoming_match_progress)
        val upcomingLinearContestView: LinearLayout =
            itemView.findViewById(R.id.upcoming_linear_contest_view)
        val contestName: TextView = itemView.findViewById(R.id.upcoming_contest_name)
        val contestPrice: TextView = itemView.findViewById(R.id.upcoming_contest_price)
    }
}