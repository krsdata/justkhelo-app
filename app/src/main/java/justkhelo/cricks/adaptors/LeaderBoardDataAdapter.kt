package justkhelo.cricks.adaptors

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import justkhelo.cricks.R
import justkhelo.cricks.ViewUserDetailActivity
import justkhelo.cricks.models.LeaderBoardModel
import justkhelo.cricks.utils.MyPreferences

class LeaderBoardDataAdapter(
    val mContext: Context,
    var userDataList: ArrayList<LeaderBoardModel>
) :
    RecyclerView.Adapter<LeaderBoardDataAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(mContext)
            .inflate(R.layout.contest_leader_board_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        try {
            val dataModel = userDataList[position]
            holder.playerRanks.text = dataModel.ranks
            holder.userPoints.text = "${dataModel.max_point} pts"
            Glide.with(mContext)
                .load(dataModel.profilePhotoUrl)
                .placeholder(R.drawable.ic_profile)
                .into(holder.profileImage)

            if (MyPreferences.getUserID(mContext) == dataModel.user_id) {
                holder.userName.text = "You"
            } else {
                if (dataModel.team_name == "") {
                    holder.userName.text = dataModel.user_name
                } else {
                    holder.userName.text = dataModel.team_name
                }
            }

            holder.leadersRows.setOnClickListener(UserClick(position))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun getItemCount(): Int {
        return userDataList.size
    }

    fun updateDataRecord(dataList: ArrayList<LeaderBoardModel>) {
        userDataList = dataList
        notifyDataSetChanged()
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val profileImage: ImageView = itemView.findViewById(R.id.profile_image)
        val userName: TextView = itemView.findViewById(R.id.team_name)
        val userPoints: TextView = itemView.findViewById(R.id.points)
        val playerRanks: TextView = itemView.findViewById(R.id.player_rank)
        val leadersRows: LinearLayout = itemView.findViewById(R.id.leaders_rows)
    }

    inner class UserClick(val pos: Int) : View.OnClickListener {
        override fun onClick(v: View?) {
            val userId = userDataList[pos].user_id
            val matchName = userDataList[pos].series_id
            val intent = Intent(mContext, ViewUserDetailActivity::class.java)
            intent.putExtra("userId", userId)
            intent.putExtra("matchName", matchName)
            mContext.startActivity(intent)

        }
    }
}
