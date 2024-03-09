package justkhelo.cricks.ui.contest.adaptors

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.edify.atrist.listener.OnContestEvents
import justkhelo.cricks.R
import justkhelo.cricks.models.ContestsParentModels
import justkhelo.cricks.models.UpcomingMatchesModel
import justkhelo.cricks.models.ContestModelLists


class ContestAdapter(
    val context: Activity,
    val contestInfoModel: ArrayList<ContestsParentModels>,
    matchObject: UpcomingMatchesModel?,
    val listener: OnContestEvents?
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    var mContext: Context? = context
    var matchObject = matchObject
    var onItemClick: ((ContestsParentModels) -> Unit)? = null
    private var matchesListObject = contestInfoModel

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.contest_row_header, parent, false)
        return ViewHolderJoinedContest(view)
    }

    override fun onBindViewHolder(parent: RecyclerView.ViewHolder, viewType: Int) {
        val objectVal = matchesListObject[viewType]
        val viewJoinedMatches: ViewHolderJoinedContest = parent as ViewHolderJoinedContest

        viewJoinedMatches.contestTitle.text = objectVal.contestTitle
        viewJoinedMatches.contestSubTitle.text = objectVal.contestSubTitle

        viewJoinedMatches.recyclerView.layoutManager =
            LinearLayoutManager(mContext, RecyclerView.VERTICAL, false)
        var colorCode = context.resources.getColor(R.color.white)
        if (objectVal.contestTitle.contains("Practise")) {
            colorCode = context.resources.getColor(R.color.highlighted_text_material_dark)
        }

        if (objectVal.icon_url == null || objectVal.icon_url == "null" || objectVal.icon_url == "") {
            viewJoinedMatches.contestIconImageView.visibility = View.GONE
            viewJoinedMatches.contestIconTextView.visibility = View.GONE
        } else if (objectVal.icon_url.contains("http") || objectVal.icon_url.contains("https")) {
            viewJoinedMatches.contestIconImageView.visibility = View.VISIBLE
            viewJoinedMatches.contestIconTextView.visibility = View.GONE

            Glide.with(context)
                .load(objectVal.icon_url)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(viewJoinedMatches.contestIconImageView)

        } else {
            viewJoinedMatches.contestIconImageView.visibility = View.GONE
            viewJoinedMatches.contestIconTextView.visibility = View.VISIBLE
            viewJoinedMatches.contestIconTextView.text = objectVal.icon_url
        }

        viewJoinedMatches.viewMoreLayout.visibility = View.GONE
        val adapter = ContestListAdapter(
            context,
            objectVal.allContestsRunning!!,
            matchObject!!,
            listener,
            colorCode
        )
        viewJoinedMatches.recyclerView.adapter = adapter

        adapter.onItemClick = { objects ->

        }
    }

    private fun getFirst3Values(allContestsRunning: java.util.ArrayList<ContestModelLists>): java.util.ArrayList<ContestModelLists> {
        return if (allContestsRunning.size > 2) {
            val values = ArrayList<ContestModelLists>()
            for (i in 0..1) {
                values.add(allContestsRunning[i])
            }
            values
        } else {
            allContestsRunning
        }
    }

    fun setMatchesList(matchesList: ArrayList<ContestsParentModels>?) {
        this.matchesListObject = matchesList!!
        // this.mContext = mContext
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return matchesListObject.size
    }

    inner class ViewHolderJoinedContest(itemView: View) : RecyclerView.ViewHolder(itemView) {
        init {
            itemView.setOnClickListener {
                onItemClick?.invoke(matchesListObject[adapterPosition])
            }
        }

        val contestTitle: TextView = itemView.findViewById(R.id.contest_title)
        val contestSubTitle: TextView = itemView.findViewById(R.id.contest_sub_title)
        val recyclerView: RecyclerView = itemView.findViewById(R.id.recycler_all_contest)
        val moreContestClick: TextView = itemView.findViewById(R.id.more_contest_click)
        val viewMoreLayout: RelativeLayout = itemView.findViewById(R.id.view_more_layout)

        val contestIconImageView: ImageView = itemView.findViewById(R.id.contest_icon_url)
        val contestIconTextView: TextView = itemView.findViewById(R.id.contest_icon_text)
    }
}