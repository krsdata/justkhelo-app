package justkhelo.cricks.ui.contest.adaptors

import android.app.Activity
import android.content.Intent
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.edify.atrist.listener.OnContestEvents
import com.edify.atrist.listener.OnMatchTimerStarted
import justkhelo.cricks.LeadersBoardActivity
import justkhelo.cricks.R
import justkhelo.cricks.models.ContestModelLists
import justkhelo.cricks.models.UpcomingMatchesModel
import justkhelo.cricks.utils.BindingUtils


class ContestListAdapter(
    val context: Activity,
    val contestModelList: ArrayList<ContestModelLists>,
    matchObjectModel: UpcomingMatchesModel,
    val listener: OnContestEvents?,
    val colorCode: Int
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    var onItemClick: ((ContestModelLists) -> Unit)? = null
    private var matchesListObject = contestModelList

    var matchObject = matchObjectModel
    private val TYPE_IPL_FINAL: Int = 1
    private val TYPE_NORMAL: Int = 100

    override fun getItemViewType(position: Int): Int {
        return if (matchesListObject[position].giftUrl == "") {
            TYPE_NORMAL
        } else {
            TYPE_IPL_FINAL
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (viewType == TYPE_NORMAL) {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.contest_rows_inner, parent, false)
            return DataViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.contest_rows_ipl_final, parent, false)
            return ImageViewHolder(view)
        }
    }

    override fun onBindViewHolder(parent: RecyclerView.ViewHolder, position: Int) {
        val objectVal = matchesListObject[position]
        if (getItemViewType(position) == TYPE_NORMAL) {
            val viewHolder: DataViewHolder = parent as DataViewHolder
            viewHolder.contestPrizePool.text =
                String.format("%s%s", "₹", objectVal.totalWinningPrize)

            if (objectVal.entryFees.toInt() == 0 && objectVal.winnerCounts!!.toInt() > 0) {
                viewHolder.contestEntryPrize.text = "Free"
                viewHolder.winningPercentage.text = "" + objectVal.winnerCounts
                viewHolder.discountTimer.visibility = View.GONE
                viewHolder.discountedPrice.visibility = View.GONE
                viewHolder.timerLayout.visibility = View.INVISIBLE
            } else if (objectVal.entryFees.toInt() == 0 && objectVal.winnerCounts!!.toInt() == 0) {
                viewHolder.contestEntryPrize.text = "Join"
                viewHolder.winningPercentage.text = "Practice"
                viewHolder.discountTimer.visibility = View.GONE
                viewHolder.discountedPrice.visibility = View.GONE
                viewHolder.timerLayout.visibility = View.INVISIBLE
            } else {
                if (objectVal.discounted_price != "" && objectVal.discounted_price.toDouble() > 0) {
                    viewHolder.discountedPrice.text =
                        String.format(" %s%s ", "₹", objectVal.discounted_price)
                    viewHolder.contestEntryPrize.text =
                        String.format("%s%s", "₹", objectVal.entryFees)
                    viewHolder.winningPercentage.text = objectVal.winnerCounts
                    viewHolder.discountedPrice.visibility = View.VISIBLE
                    viewHolder.discountedPrice.paintFlags =
                        viewHolder.discountedPrice.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG

                    if (objectVal.offer_end_at != "0") {
                        viewHolder.discountTimer.visibility = View.VISIBLE
                        viewHolder.timerLayout.visibility = View.VISIBLE
                        BindingUtils.countDownStartForAdaptors(objectVal.offer_end_at.toLong(),
                            object : OnMatchTimerStarted {
                                override fun onTimeFinished() {
                                    viewHolder.discountTimer.text = ""
                                    viewHolder.timerLayout.visibility = View.INVISIBLE
                                }

                                override fun onTicks(time: String) {
                                    viewHolder.discountTimer.text = time.replace("left", "")
                                }
                            })
                    } else {
                        viewHolder.discountTimer.text = ""
                        viewHolder.timerLayout.visibility = View.INVISIBLE
                    }
                } else {
                    viewHolder.contestEntryPrize.text =
                        String.format("%s%s", "₹", objectVal.entryFees)
                    viewHolder.winningPercentage.text = objectVal.winnerCounts
                    viewHolder.discountTimer.visibility = View.GONE
                    viewHolder.discountedPrice.visibility = View.GONE
                    viewHolder.timerLayout.visibility = View.INVISIBLE
                }
            }
            viewHolder.firstPrize.text = String.format("%s%s", "₹", objectVal.firstPrice)

            if (objectVal.maxAllowedTeam > 1) {
                viewHolder.allowedTeamType.text = "M"
                viewHolder.linearMulti.visibility = View.VISIBLE
                viewHolder.contestMultiPlayer.text = String.format("%d", objectVal.maxAllowedTeam)
            } else {
                viewHolder.allowedTeamType.text = "S"
                viewHolder.linearMulti.visibility = View.VISIBLE
                viewHolder.contestMultiPlayer.text = String.format("%d", objectVal.maxAllowedTeam)
            }

            if (objectVal.usableBonus.toInt() == 0) {
                viewHolder.linearBonus.visibility = View.GONE
            } else {
                viewHolder.linearBonus.visibility = View.VISIBLE
                viewHolder.contestBonus.text = String.format("%s%s", objectVal.usableBonus, "%")
            }

            if (objectVal.totalSpots == 0) {
                viewHolder.contestProgress.max =
                    objectVal.filledSpots + BindingUtils.UNLIMITED_SPOT_MARGIN
                viewHolder.contestProgress.progress = objectVal.filledSpots
                viewHolder.totalSpot.text = String.format("unlimited spots")
                viewHolder.totalSpotLeft.text =
                    String.format("%d spot filled", objectVal.filledSpots)
            } else {
                viewHolder.contestProgress.max = objectVal.totalSpots
                viewHolder.contestProgress.progress = objectVal.filledSpots
                if (objectVal.totalSpots == objectVal.filledSpots) {
                    viewHolder.totalSpot.text = "Contest full"
                    viewHolder.totalSpot.textSize = 18.0f
                    viewHolder.totalSpotLeft.text = ""
                } else {
                    viewHolder.totalSpot.text = String.format("%d spots", objectVal.totalSpots)
                    viewHolder.totalSpotLeft.text =
                        String.format(
                            "%d  spot left",
                            objectVal.totalSpots - objectVal.filledSpots
                        )
                }
            }

            viewHolder.linearContestViews.setOnClickListener {
                val intent = Intent(context, LeadersBoardActivity::class.java)
                intent.putExtra(LeadersBoardActivity.SERIALIZABLE_MATCH_KEY, matchObject)
                intent.putExtra(LeadersBoardActivity.SERIALIZABLE_CONTEST_KEY, objectVal)
                context.startActivityForResult(intent, LeadersBoardActivity.CREATETEAM_REQUESTCODE)
            }

            if (objectVal.cancellation) {
                viewHolder.contestCancellation.visibility = View.GONE
            } else {
                viewHolder.contestCancellation.visibility = View.VISIBLE
            }
            viewHolder.contestEntryPrize.setOnClickListener {
                listener!!.onContestJoinning(objectVal, position)
            }

            if (objectVal.is_dashboard) {
                viewHolder.contestLeaderBoardLabel.visibility = View.GONE
            } else {
                viewHolder.contestLeaderBoardLabel.visibility = View.GONE
            }
        } else {
            val viewHolder: ImageViewHolder = parent as ImageViewHolder
            viewHolder.contestPrizePool.text =
                String.format("%s%s", "₹", objectVal.totalWinningPrize)

            if (objectVal.entryFees.toInt() == 0 && objectVal.winnerCounts!!.toInt() > 0) {
                viewHolder.contestEntryPrize.text = "Free"
                viewHolder.winningPercentage.text = "" + objectVal.winnerCounts
            } else if (objectVal.entryFees.toInt() == 0 && objectVal.winnerCounts!!.toInt() == 0) {
                viewHolder.contestEntryPrize.text = "Join"
                viewHolder.winningPercentage.text = "Practice"
            } else {
                viewHolder.contestEntryPrize.text = String.format("%s%s", "₹", objectVal.entryFees)
                viewHolder.winningPercentage.text = "" + objectVal.winnerCounts
            }
            viewHolder.firstPrize.text = String.format("%s%s", "₹", objectVal.firstPrice)

            if (objectVal.maxAllowedTeam > 1) {
                viewHolder.allowedTeamType.text = "M"
                viewHolder.linearMulti.visibility = View.VISIBLE
                viewHolder.contestMultiPlayer.text = String.format("%d", objectVal.maxAllowedTeam)
            } else {
                viewHolder.allowedTeamType.text = "S"
                viewHolder.linearMulti.visibility = View.VISIBLE
                viewHolder.contestMultiPlayer.text = String.format("%d", objectVal.maxAllowedTeam)
            }

            if (objectVal.usableBonus.toInt() == 0) {
                viewHolder.linearBonus.visibility = View.GONE
            } else {
                viewHolder.linearBonus.visibility = View.VISIBLE
                viewHolder.contestBonus.text = String.format("%s%s", objectVal.usableBonus, "%")
            }


            Glide.with(context)
                .load(objectVal.giftUrl)
                //.placeholder(R.drawable.phone_image)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(viewHolder.giftImage)

            if (objectVal.totalSpots == 0) {
                viewHolder.contestProgress.max =
                    objectVal.filledSpots + BindingUtils.UNLIMITED_SPOT_MARGIN
                viewHolder.contestProgress.progress = objectVal.filledSpots
                viewHolder.totalSpot.text = String.format("unlimited spots")
                viewHolder.totalSpotLeft.text = String.format("%d", objectVal.filledSpots)
            } else {
                viewHolder.contestProgress.max = objectVal.totalSpots
                viewHolder.contestProgress.progress = objectVal.filledSpots
                if (objectVal.totalSpots == objectVal.filledSpots) {
                    viewHolder.totalSpot.text = "Contest full"
                    viewHolder.totalSpot.textSize = 18.0f
                    viewHolder.totalSpotLeft.text = ""
                } else {
                    viewHolder.totalSpot.text = String.format("%d", objectVal.totalSpots)
                    viewHolder.totalSpotLeft.text =
                        String.format("%d", objectVal.filledSpots)
                }
            }

            viewHolder.linearContestViews.setOnClickListener {
                val intent = Intent(context, LeadersBoardActivity::class.java)
                intent.putExtra(LeadersBoardActivity.SERIALIZABLE_MATCH_KEY, matchObject)
                intent.putExtra(LeadersBoardActivity.SERIALIZABLE_CONTEST_KEY, objectVal)
                context.startActivityForResult(intent, LeadersBoardActivity.CREATETEAM_REQUESTCODE)
            }

            if (objectVal.cancellation) {
                viewHolder.contestCancellation.visibility = View.GONE
            } else {
                viewHolder.contestCancellation.visibility = View.VISIBLE
            }
            viewHolder.contestEntryPrize.setOnClickListener {
                listener!!.onContestJoinning(objectVal, position)
            }

            if (objectVal.is_dashboard) {
                viewHolder.contestLeaderBoardLabel.visibility = View.GONE
            } else {
                viewHolder.contestLeaderBoardLabel.visibility = View.GONE
            }
        }
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

        val linearContestViews: LinearLayout = itemView.findViewById(R.id.linear_trades_status)
        val contestPrizePool: TextView = itemView.findViewById(R.id.contest_prize_pool)
        val contestEntryPrize: TextView = itemView.findViewById(R.id.contest_entry_prize)
        val firstPrize: TextView = itemView.findViewById(R.id.first_prize)
        val winningPercentage: TextView = itemView.findViewById(R.id.winning_percentage)

        val contestCancellation: TextView = itemView.findViewById(R.id.contest_cancellation)
        val allowedTeamType: TextView = itemView.findViewById(R.id.allowedTeamType)

        val linearMulti: LinearLayout = itemView.findViewById(R.id.linear_Multi)
        val linearBonus: LinearLayout = itemView.findViewById(R.id.linear_bonues)
        val contestBonus: TextView = itemView.findViewById(R.id.contest_bonus)

        val totalSpotLeft: TextView = itemView.findViewById(R.id.total_spot_left)
        val totalSpot: TextView = itemView.findViewById(R.id.total_spot)
        val contestProgress: ProgressBar = itemView.findViewById(R.id.contest_progress)
        val contestLeaderBoardLabel: TextView =
            itemView.findViewById(R.id.contest_leader_board_label)

        val contestMultiPlayer: TextView = itemView.findViewById(R.id.contest_multiplayer)
        val discountTimer: TextView = itemView.findViewById(R.id.discountTimer)
        val discountedPrice: TextView = itemView.findViewById(R.id.discountedPrice)
        val timerLayout: LinearLayout = itemView.findViewById(R.id.timerLayout)
    }

    inner class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        init {
            itemView.setOnClickListener {
                onItemClick?.invoke(matchesListObject[adapterPosition])
            }
        }

        val linearContestViews: LinearLayout = itemView.findViewById(R.id.linear_trades_status)
        val contestPrizePool: TextView = itemView.findViewById(R.id.contest_prize_pool)
        val contestEntryPrize: TextView = itemView.findViewById(R.id.contest_entry_prize)
        val firstPrize: TextView = itemView.findViewById(R.id.first_prize)
        val winningPercentage: TextView = itemView.findViewById(R.id.winning_percentage)

        val contestCancellation: TextView = itemView.findViewById(R.id.contest_cancellation)
        val allowedTeamType: TextView = itemView.findViewById(R.id.allowedTeamType)

        val linearMulti: LinearLayout = itemView.findViewById(R.id.linear_Multi)
        val linearBonus: LinearLayout = itemView.findViewById(R.id.linear_bonues)
        val contestBonus: TextView = itemView.findViewById(R.id.contest_bonus)

        val totalSpotLeft: TextView = itemView.findViewById(R.id.total_spot_left)
        val totalSpot: TextView = itemView.findViewById(R.id.total_spot)
        val contestProgress: ProgressBar = itemView.findViewById(R.id.contest_progress)
        val giftImage: ImageView = itemView.findViewById(R.id.gift_image)
        val contestLeaderBoardLabel: TextView =
            itemView.findViewById(R.id.contest_leader_board_label)
        val contestMultiPlayer: TextView = itemView.findViewById(R.id.contest_multiplayer)
    }
}