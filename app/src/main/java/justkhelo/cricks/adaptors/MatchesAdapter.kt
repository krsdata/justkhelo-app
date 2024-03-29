package justkhelo.cricks.adaptors

import android.content.Context
import android.content.Intent
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import justkhelo.cricks.ContestActivity
import justkhelo.cricks.MainActivity
import justkhelo.cricks.R
import justkhelo.cricks.models.MatchesModels
import justkhelo.cricks.utils.BindingUtils
import justkhelo.cricks.utils.MyUtils
import pl.pzienowicz.autoscrollviewpager.AutoScrollViewPager


class MatchesAdapter(val context: Context, val tradeInfoModels: ArrayList<MatchesModels>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    var onItemClick: ((MatchesModels) -> Unit)? = null
    var mContext: Context? = context
    private var matchesListObject = tradeInfoModels

    companion object {
        const val TYPE_JOINED = 1
        const val TYPE_BANNERS = 2
        const val TYPE_UPCOMING_MATCHES = 3
    }

    override fun getItemViewType(position: Int): Int {
        val comparable = matchesListObject[position]
        return comparable.viewType
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (viewType == TYPE_JOINED) {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.matches_row_joined_matches, parent, false)
            return ViewHolderJoinedMatches(view)
        } else if (viewType == TYPE_BANNERS) {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.matches_row_banners_matches, parent, false)
            return BannersViewHolder(view)
        } else /*if (viewType == TYPE_UPCOMING_MATCHES)*/ {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.matches_row_upcoming_matches, parent, false)
            return UpcomingMatchesViewHolder(view)
        }
    }

    override fun onBindViewHolder(parent: RecyclerView.ViewHolder, position: Int) {
        val objectVal = matchesListObject[position]
        if (objectVal.viewType == TYPE_JOINED) {
            val viewJoinedMatches: ViewHolderJoinedMatches = parent as ViewHolderJoinedMatches
            viewJoinedMatches.recyclerView.layoutManager =
                LinearLayoutManager(mContext, RecyclerView.HORIZONTAL, false)
            if (objectVal.joinedMatchModel != null) {
                val adapter = JoinedMatchesAdapter(
                    mContext!!,
                    objectVal.joinedMatchModel!!
                )
                viewJoinedMatches.recyclerView.adapter = adapter
                adapter.onItemClick = { objects ->
                    val intent = Intent(mContext, ContestActivity::class.java)
                    intent.putExtra(ContestActivity.SERIALIZABLE_KEY_JOINED_CONTEST, objects)
                    mContext!!.startActivity(intent)
                }
            }

            Glide.with(context)
                .load(BindingUtils.BASE_URL_MAIN + "banners/joined_contest_bg.jpg")
                .placeholder(R.drawable.placeholder_player_teama)
                .into(viewJoinedMatches.backgroundImage)

            viewJoinedMatches.txtViewAll.setOnClickListener {
                (mContext as MainActivity).viewAllMatches()
            }
        } else if (objectVal.viewType == TYPE_BANNERS) {
            val objectVal = matchesListObject[position]
            val viewBanners: BannersViewHolder = parent as BannersViewHolder

            val scrollViewAdapter = BannerSliderAdapter(mContext!!, objectVal.matchBanners!!)
            viewBanners.recyclerView.adapter = scrollViewAdapter
            scrollViewAdapter.notifyDataSetChanged()

            if (objectVal.matchBanners!!.size == 1) {
                viewBanners.recyclerView.stopAutoScroll()
            } else {
                viewBanners.recyclerView.startAutoScroll()
            }

            viewBanners.recyclerView.setInterval(5000)
            viewBanners.recyclerView.setDirection(AutoScrollViewPager.Direction.RIGHT)
            viewBanners.recyclerView.setCycle(true)
            viewBanners.recyclerView.setBorderAnimation(true)
            viewBanners.recyclerView.visibility = View.VISIBLE

        } else if (objectVal.viewType == TYPE_UPCOMING_MATCHES) {
            val objectVal = matchesListObject[position]
            val viewUpcomingMatches: UpcomingMatchesViewHolder = parent as UpcomingMatchesViewHolder
            viewUpcomingMatches.recyclerView.layoutManager =
                LinearLayoutManager(mContext, RecyclerView.VERTICAL, false)

            if (objectVal.upcomingMatches != null && objectVal.upcomingMatches!!.size > 0) {
                viewUpcomingMatches.linearEmptyView.visibility = GONE
                val adapter = UpcomingMatchesAdapter(
                    mContext!!,
                    objectVal.upcomingMatches!!
                )
                viewUpcomingMatches.recyclerView.setHasFixedSize(true)
                viewUpcomingMatches.recyclerView.adapter = adapter
                adapter.onItemClick = { objects ->

                    val intent = Intent(mContext, ContestActivity::class.java)
                    intent.putExtra(ContestActivity.SERIALIZABLE_KEY_UPCOMING_MATCHES, objects)
                    mContext!!.startActivity(intent)
                }
            } else {
                MyUtils.logd("ADaptor", "Draw Empty View Here")
                viewUpcomingMatches.linearEmptyView.visibility = VISIBLE
            }
        }
    }

    fun setMatchesList(matchesList: java.util.ArrayList<MatchesModels>?) {
        this.matchesListObject = matchesList!!
        notifyDataSetChanged()
    }

    fun addLoadingView() {
        Handler().post {
            matchesListObject.add(MatchesModels())
            notifyItemInserted(matchesListObject.size - 1)
        }
    }

    fun removeLoadingView() {
        matchesListObject.removeAt(matchesListObject.size - 1)
        notifyItemRemoved(matchesListObject.size)
    }

    override fun getItemCount(): Int {
        return matchesListObject.size
    }

    inner class ViewHolderJoinedMatches(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val recyclerView: RecyclerView = itemView.findViewById(R.id.recycler_joined_matches)
        val txtViewAll: TextView = itemView.findViewById(R.id.txtViewAll)
        val backgroundImage: ImageView = itemView.findViewById(R.id.imageView4)
    }

    inner class BannersViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val recyclerView: AutoScrollViewPager = itemView.findViewById(R.id.recycler_banners)
    }

    inner class UpcomingMatchesViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val recyclerView: RecyclerView = itemView.findViewById(R.id.recycler_upcoming_matches)
        val linearEmptyView: LinearLayout = itemView.findViewById(R.id.linear_empty_view)

    }
}

