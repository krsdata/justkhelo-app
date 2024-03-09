package justkhelo.cricks.ui.leadersboard

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.JsonObject
import justkhelo.cricks.ContestActivity
import justkhelo.cricks.R
import justkhelo.cricks.databinding.FragmentPrizeBreakupBinding
import justkhelo.cricks.models.ContestModelLists
import justkhelo.cricks.models.PrizeBreakUpModels
import justkhelo.cricks.models.UpcomingMatchesModel
import justkhelo.cricks.models.UsersPostDBResponse
import justkhelo.cricks.network.IApiMethod
import justkhelo.cricks.network.WebServiceClient
import justkhelo.cricks.utils.MyPreferences
import justkhelo.cricks.utils.MyUtils
import justkhelo.cricks.utils.setServerImage
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PrizeBreakupFragment : Fragment() {

    private var mBinding: FragmentPrizeBreakupBinding? = null
    lateinit var adapter: PrizeBreakUpAdapter
    var prizeBreakupList = ArrayList<PrizeBreakUpModels>()

    var matchObject: UpcomingMatchesModel? = null
    var contestObject: ContestModelLists? = null

    companion object {
        fun newInstance(bundle: Bundle): PrizeBreakupFragment {
            val fragment = PrizeBreakupFragment()
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
            R.layout.fragment_prize_breakup, container, false
        )
        return mBinding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mBinding!!.prizeViewRecycler.layoutManager =
            LinearLayoutManager(activity, RecyclerView.VERTICAL, false)
        adapter = PrizeBreakUpAdapter(
            requireActivity(),
            prizeBreakupList
        )
        mBinding!!.prizeViewRecycler.adapter = adapter
        if (!MyUtils.isConnectedWithInternet(activity as AppCompatActivity)) {
            MyUtils.showToast(activity as AppCompatActivity, "No Internet connection found")
            return
        }
        if (contestObject!!.winnerCounts!!.toInt() == 0) {
            mBinding!!.winnerGlory.visibility = View.VISIBLE
        } else {
            mBinding!!.winnerGlory.visibility = View.GONE
            getPrizeBreakup()
        }
    }

    private fun getPrizeBreakup() {
        mBinding!!.progressBar.visibility = View.VISIBLE

        val jsonRequest = JsonObject()
        jsonRequest.addProperty("user_id", MyPreferences.getUserID(requireActivity())!!)
        jsonRequest.addProperty("system_token", MyPreferences.getSystemToken(requireActivity())!!)
        jsonRequest.addProperty("match_id", matchObject!!.matchId)
        jsonRequest.addProperty("contest_id", contestObject!!.id)

        WebServiceClient(requireActivity()).client.create(IApiMethod::class.java)
            .getPrizeBreakUp(jsonRequest)
            .enqueue(object : Callback<UsersPostDBResponse?> {
                override fun onFailure(call: Call<UsersPostDBResponse?>?, t: Throwable?) {

                }

                override fun onResponse(
                    call: Call<UsersPostDBResponse?>?,
                    response: Response<UsersPostDBResponse?>?
                ) {
                    val res = response!!.body()
                    if (res != null) {
                        if (res.status) {
                            if (isVisible) {
                                mBinding!!.progressBar.visibility = View.GONE
                                val responseModel = res.responseObject
                                if (responseModel!!.prizeBreakUpModelsList!!.size > 0) {
                                    prizeBreakupList.clear()

                                    prizeBreakupList.addAll(responseModel.prizeBreakUpModelsList!!)
                                    adapter.notifyDataSetChanged()
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

    inner class PrizeBreakUpAdapter(
        val context: Context,
        rangeModels: ArrayList<PrizeBreakUpModels>
    ) :
        RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        var onItemClick: ((PrizeBreakUpModels) -> Unit)? = null
        private var matchesListObject = rangeModels

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.prize_breakup_rows, parent, false)
            return MyMatchViewHolder(view)
        }

        override fun onBindViewHolder(parent: RecyclerView.ViewHolder, viewType: Int) {
            val objectVal = matchesListObject[viewType]
            val viewHolder: MyMatchViewHolder = parent as MyMatchViewHolder
            viewHolder.rankRange.text = objectVal.rangeName
            viewHolder.winnerPrize.text = String.format("₹%s", objectVal.winnersPrice)
            if (objectVal.prize_url != null && objectVal.prize_url != "") {
                viewHolder.priceImage.setServerImage(objectVal.prize_url, applyCircle = false)
                viewHolder.priceImage.visibility = View.VISIBLE
                viewHolder.plusText.visibility = View.VISIBLE
            } else {
                viewHolder.priceImage.visibility = View.GONE
                viewHolder.plusText.visibility = View.GONE
            }
        }

        override fun getItemCount(): Int {
            return matchesListObject.size
        }

        inner class MyMatchViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val rankRange: TextView = itemView.findViewById(R.id.rank_range)
            val winnerPrize: TextView = itemView.findViewById(R.id.winner_rpize)
            val plusText: TextView = itemView.findViewById(R.id.plus_text)
            val priceImage: ImageView = itemView.findViewById(R.id.price_image)
        }
    }
}