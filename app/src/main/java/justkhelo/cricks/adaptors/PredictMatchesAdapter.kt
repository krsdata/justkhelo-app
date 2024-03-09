package justkhelo.cricks.adaptors

import android.app.Activity
import android.content.Context
import android.util.DisplayMetrics
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.edify.atrist.listener.OnMatchTimerStarted
import justkhelo.cricks.R
import justkhelo.cricks.models.PredictData
import justkhelo.cricks.utils.BindingUtils
import justkhelo.cricks.utils.MyUtils

class PredictMatchesAdapter(
    val matchesList: ArrayList<PredictData>,
    val context: Context,
    val matchClick: MatchClick,
    val activity: Activity
) : RecyclerView.Adapter<PredictMatchesAdapter.MatchesVh>() {
    inner class MatchesVh(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textMatch1Team1: TextView = itemView.findViewById(R.id.txt_match1_team1)
        val textMatch1Team2: TextView = itemView.findViewById(R.id.txt_match1_team2)
        val logoTeam1: ImageView = itemView.findViewById(R.id.match1_img1)
        val logoTeam2: ImageView = itemView.findViewById(R.id.match1_img2)
        val matchTitle: TextView = itemView.findViewById(R.id.match_title)
        val textTime: TextView = itemView.findViewById(R.id.time1)
        val selector: View = itemView.findViewById(R.id.selector)

        init {
            matchClick.onMatchClick(0)
            matchesList[0].isSelected = true
            itemView.setOnClickListener {
                matchClick.onMatchClick(adapterPosition)
                for (i in matchesList) {
                    i.isSelected = matchesList.indexOf(i) == adapterPosition
                }
                notifyDataSetChanged()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MatchesVh {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_matches_predict_win, parent, false)
        val displayMetrics = DisplayMetrics()
        activity.windowManager.defaultDisplay.getMetrics(displayMetrics)

        var width = displayMetrics.widthPixels
        var height = displayMetrics.ydpi
        view.layoutParams.width = (width/2- context.resources.getDimension(R.dimen._15sdp)).toInt()
        return MatchesVh(view)
    }

    override fun onBindViewHolder(holder: MatchesVh, position: Int) {
        holder.textMatch1Team1.text = matchesList[position].teama_name
        holder.textMatch1Team2.text = matchesList[position].teamb_name
        if (matchesList[position].isSelected) {
            holder.selector.setBackgroundColor(context.resources.getColor(R.color.selected_match))
        }
        else {
            holder.selector.setBackgroundColor(context.resources.getColor(R.color.unselected_match))
        }
        Glide.with(context)
            .load(matchesList[position].teama_logo)
            .into(holder.logoTeam1)
        Glide.with(context)
            .load(matchesList[position].teamb_logo)
            .into(holder.logoTeam2)
        holder.matchTitle.text = matchesList[position].match_title
        BindingUtils.countDownStartForAdaptors2(MyUtils.parseDate(matchesList!![position].match_start_time)
        ,
            object : OnMatchTimerStarted {
                override fun onTimeFinished() {
                    holder.textTime.text = ""
                }

                override fun onTicks(time: String) {
                    holder.textTime.text = time
                }

            })


    }

    override fun getItemCount(): Int = matchesList.size
    interface MatchClick {
        fun onMatchClick(position: Int)
    }
}