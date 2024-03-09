package justkhelo.cricks.ui.createteam.adaptors

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import justkhelo.cricks.PlayerInfoActivity
import justkhelo.cricks.R
import justkhelo.cricks.models.PlayersInfoModel
import justkhelo.cricks.models.UpcomingMatchesModel


class PlayersContestAdapter(
    val context: Context,
    val playerList: ArrayList<PlayersInfoModel>,
    matchModel: UpcomingMatchesModel
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    var onItemClick: ((PlayersInfoModel) -> Unit)? = null
    private var playerListObject = playerList
    private var matchObject = matchModel

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.createteam_row_players, parent, false)
        return DataViewHolder(view)
    }

    override fun onBindViewHolder(parent: RecyclerView.ViewHolder, viewType: Int) {
        val objectVal = playerListObject[viewType]
        val viewHolder: DataViewHolder = parent as DataViewHolder
        if (objectVal.analyticsModel != null) {
            viewHolder.playerSelectionPercentage.text =
                "Sel by ${objectVal.analyticsModel!!.selectionPc}%"
        } else {
            viewHolder.playerSelectionPercentage.text = ""
        }
        viewHolder.playerName.text = objectVal.shortName
        viewHolder.teamName.text = objectVal.teamShortName
        if (matchObject.teamAInfo!!.teamId == objectVal.teamId) {
            viewHolder.teamName.background =
                context.resources.getDrawable(R.drawable.ract_white_background)
            viewHolder.teamName.setTextColor(context.resources.getColor(R.color.black))
        } else {
            viewHolder.teamName.background =
                context.resources.getDrawable(R.drawable.ract_black_background)
            viewHolder.teamName.setTextColor(context.resources.getColor(R.color.white))
        }
        viewHolder.fantasyPoints.text = "${objectVal.fantasyPlayerRating}"
        viewHolder.playerPoints.text = "${objectVal.playerSeriesPoints}"

        if (objectVal.isPlaying11 /*&& matchObject.isLineup*/) {
            viewHolder.anouncedIndicatorCircle.setBackgroundResource(R.drawable.circle_green)
            viewHolder.anouncedIndicatorText.text = "Playing"
            viewHolder.anouncedIndicatorText.setTextColor(context.resources.getColor(R.color.green))
        } else {
            viewHolder.anouncedIndicatorText.text = ""
            viewHolder.anouncedIndicatorCircle.setBackgroundResource(R.drawable.circle_red)
        }

        Glide.with(context)
            .load(objectVal.playerImage)
            .placeholder(R.drawable.player_blue)
            .into(viewHolder.playerImage)

        if (objectVal.isSelected) {
            viewHolder.addImage.setImageResource(R.drawable.ic_do_not_disturb_on_black_24dp)
            viewHolder.linearTradesStatus.setBackgroundColor(context.resources.getColor(R.color.highlighted_text_material_dark))
        } else {
            viewHolder.addImage.setImageResource(R.drawable.ic_add_circle_outline_black_24dp)
            viewHolder.linearTradesStatus.setBackgroundColor(context.resources.getColor(R.color.white))
        }

        viewHolder.relPlayerImage.setOnClickListener{
            val intent = Intent(context, PlayerInfoActivity::class.java)
            intent.putExtra("matchData", matchObject)
            intent.putExtra("playerData", objectVal)
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return playerListObject.size
    }

    inner class DataViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        init {
            itemView.setOnClickListener {
                onItemClick?.invoke(playerListObject[adapterPosition])
            }
        }

        val linearTradesStatus: LinearLayout = itemView.findViewById(R.id.linear_trades_status)
        val playerSelectionPercentage: TextView =
            itemView.findViewById(R.id.player_selection_percentage)
        val playerName: TextView = itemView.findViewById(R.id.player_name)
        val playerImage: ImageView = itemView.findViewById(R.id.player_image)
        val teamName: TextView = itemView.findViewById(R.id.team_name)
        val fantasyPoints: TextView = itemView.findViewById(R.id.fantasy_points)
        val playerPoints: TextView = itemView.findViewById(R.id.player_points)
        val anouncedIndicatorCircle: TextView =
            itemView.findViewById(R.id.anounced_indicator_circle)
        val anouncedIndicatorText: TextView = itemView.findViewById(R.id.anounced_indicator_text)
        val addImage: ImageView = itemView.findViewById(R.id.add_image)
        val relPlayerImage: RelativeLayout = itemView.findViewById(R.id.rel_player_image)
    }
}

