package justkhelo.cricks.ui.contest

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.edify.atrist.listener.OnContestEvents
import com.edify.atrist.listener.OnContestLoadedListener
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import justkhelo.cricks.*
import justkhelo.cricks.databinding.FragmentAllContestBinding
import justkhelo.cricks.models.*
import justkhelo.cricks.network.IApiMethod
import justkhelo.cricks.network.WebServiceClient
import justkhelo.cricks.ui.contest.adaptors.ContestAdapter
import justkhelo.cricks.ui.contest.adaptors.ContestListAdapter
import justkhelo.cricks.utils.BindingUtils
import justkhelo.cricks.utils.HardwareInfoManager
import justkhelo.cricks.utils.MyPreferences
import justkhelo.cricks.utils.MyUtils
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ContestFragment : Fragment() {

    private var objectMatches: UpcomingMatchesModel? = null
    var matchObject: UpcomingMatchesModel? = null
    var mListenerContestEvents: OnContestEvents? = null
    private lateinit var mListener: OnContestLoadedListener
    private var mBinding: FragmentAllContestBinding? = null
    lateinit var adapter: ContestAdapter
    private lateinit var spotSizeFilterAdaptor: ContestListAdapter
    var allContestListData = ArrayList<ContestsParentModels>()
    var filterSpotsListData = ArrayList<ContestModelLists>()
    var filterArrayList = ArrayList<ContestCategoryModel>()
    var isEntryAscending = false
    private var isVisibleToUser: Boolean = false
    private lateinit var filterAdapter: FilterAdapter
    private var pos = 0

    companion object {
        fun newInstance(bundle: Bundle): ContestFragment {
            val fragment = ContestFragment()
            fragment.arguments = bundle
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        objectMatches =
            requireArguments().get(ContestActivity.SERIALIZABLE_KEY_MATCH_OBJECT) as UpcomingMatchesModel
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mBinding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_all_contest, container, false
        )
        return mBinding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        matchObject = objectMatches
        mBinding!!.contestViewRecycler.layoutManager =
            LinearLayoutManager(activity, RecyclerView.VERTICAL, false)
        adapter = ContestAdapter(
            requireActivity(),
            allContestListData,
            matchObject,
            mListenerContestEvents!!
        )
        mBinding!!.linearEmptyContest.visibility = View.GONE
        mBinding!!.contestViewRecycler.adapter = adapter

        registerSpotSizeSelection()

        mBinding!!.recyclerBySpotsize.layoutManager =
            LinearLayoutManager(activity, RecyclerView.VERTICAL, false)

        spotSizeFilterAdaptor = ContestListAdapter(
            requireActivity(),
            filterSpotsListData,
            matchObject!!,
            mListenerContestEvents,
            0
        )
        mBinding!!.recyclerBySpotsize.adapter = spotSizeFilterAdaptor

        mBinding!!.linearEmptyContest.visibility = View.GONE
        mBinding!!.contestViewRecycler.adapter = adapter

        mBinding!!.filterRecyclerView.layoutManager =
            LinearLayoutManager(activity, RecyclerView.HORIZONTAL, false)
        filterAdapter = FilterAdapter(requireActivity(), filterArrayList)

        mBinding!!.filterRecyclerView.adapter = filterAdapter

        mBinding!!.btnCreateTeam.setOnClickListener(View.OnClickListener {
            val intent = Intent(activity, CreateTeamActivity::class.java)
            intent.putExtra(CreateTeamActivity.SERIALIZABLE_MATCH_KEY, matchObject)
            requireActivity().startActivityForResult(
                intent,
                CreateTeamActivity.CREATETEAM_REQUESTCODE
            )
        })

        mBinding!!.btnEmptyView.setOnClickListener(View.OnClickListener {
            val intent = Intent(activity, WebActivity::class.java)
            intent.putExtra(WebActivity.KEY_TITLE, BindingUtils.WEB_TITLE_PRIVACY_POLICY)
            intent.putExtra(WebActivity.KEY_URL, BindingUtils.WEBVIEW_PRIVACY)
            requireActivity().startActivity(intent)
        })

        mBinding!!.contestFilterRefresh.setColorSchemeResources(R.color.colorPrimary)
        mBinding!!.contestRefresh.setColorSchemeResources(R.color.colorPrimary)

        mBinding!!.contestRefresh.setOnRefreshListener(SwipeRefreshLayout.OnRefreshListener {
            getAllContest(false)
        })

        mBinding!!.contestFilterRefresh.setOnRefreshListener {
            getFilteredContest()
        }
    }

    private fun registerSpotSizeSelection() {

        mBinding!!.sortBy2spots.setOnClickListener {

            mBinding!!.sortBy2spots.setBackgroundResource(R.drawable.circle_app_color)
            mBinding!!.sortBy2spots.setTextColor(resources.getColor(R.color.white))

            mBinding!!.sortBy3spots.setBackgroundResource(R.drawable.circle_grey)
            mBinding!!.sortBy3spots.setTextColor(resources.getColor(R.color.black))

            mBinding!!.sortBy4spots.setBackgroundResource(R.drawable.circle_grey)
            mBinding!!.sortBy4spots.setTextColor(resources.getColor(R.color.black))

            mBinding!!.filterByAll.setBackgroundResource(R.drawable.circle_grey)
            mBinding!!.filterByAll.setTextColor(resources.getColor(R.color.black))

            mBinding!!.linearEntryPrizeSort.setBackgroundResource(R.drawable.circle_grey)
            mBinding!!.prizeArrow.visibility = View.GONE
            mBinding!!.rupees.setTextColor(resources.getColor(R.color.black))

            showRecyclerListBySpotSize(2)
        }

        mBinding!!.sortBy3spots.setOnClickListener {

            mBinding!!.sortBy2spots.setBackgroundResource(R.drawable.circle_grey)
            mBinding!!.sortBy2spots.setTextColor(resources.getColor(R.color.black))

            mBinding!!.sortBy3spots.setBackgroundResource(R.drawable.circle_app_color)
            mBinding!!.sortBy3spots.setTextColor(resources.getColor(R.color.white))

            mBinding!!.sortBy4spots.setBackgroundResource(R.drawable.circle_grey)
            mBinding!!.sortBy4spots.setTextColor(resources.getColor(R.color.black))

            mBinding!!.filterByAll.setBackgroundResource(R.drawable.circle_grey)
            mBinding!!.filterByAll.setTextColor(resources.getColor(R.color.black))

            mBinding!!.linearEntryPrizeSort.setBackgroundResource(R.drawable.circle_grey)
            mBinding!!.prizeArrow.visibility = View.GONE
            mBinding!!.rupees.setTextColor(resources.getColor(R.color.black))

            showRecyclerListBySpotSize(3)
        }

        mBinding!!.sortBy4spots.setOnClickListener {

            mBinding!!.sortBy2spots.setBackgroundResource(R.drawable.circle_grey)
            mBinding!!.sortBy2spots.setTextColor(resources.getColor(R.color.black))

            mBinding!!.sortBy3spots.setBackgroundResource(R.drawable.circle_grey)
            mBinding!!.sortBy3spots.setTextColor(resources.getColor(R.color.black))

            mBinding!!.sortBy4spots.setBackgroundResource(R.drawable.circle_app_color)
            mBinding!!.sortBy4spots.setTextColor(resources.getColor(R.color.white))

            mBinding!!.filterByAll.setBackgroundResource(R.drawable.circle_grey)
            mBinding!!.filterByAll.setTextColor(resources.getColor(R.color.black))

            mBinding!!.linearEntryPrizeSort.setBackgroundResource(R.drawable.circle_grey)
            mBinding!!.prizeArrow.visibility = View.GONE
            mBinding!!.rupees.setTextColor(resources.getColor(R.color.black))

            showRecyclerListBySpotSize(4)
        }

        mBinding!!.linearEntryPrizeSort.setOnClickListener {

            mBinding!!.sortBy2spots.setBackgroundResource(R.drawable.circle_grey)
            mBinding!!.sortBy2spots.setTextColor(resources.getColor(R.color.black))

            mBinding!!.sortBy3spots.setBackgroundResource(R.drawable.circle_grey)
            mBinding!!.sortBy3spots.setTextColor(resources.getColor(R.color.black))

            mBinding!!.sortBy4spots.setBackgroundResource(R.drawable.circle_grey)
            mBinding!!.sortBy4spots.setTextColor(resources.getColor(R.color.black))

            mBinding!!.filterByAll.setBackgroundResource(R.drawable.circle_grey)
            mBinding!!.filterByAll.setTextColor(resources.getColor(R.color.black))

            mBinding!!.linearEntryPrizeSort.setBackgroundResource(R.drawable.circle_app_color)
            mBinding!!.prizeArrow.visibility = View.VISIBLE

            mBinding!!.rupees.setTextColor(resources.getColor(R.color.white))

            filterByEntryPrize()
        }

        mBinding!!.filterByAll.setOnClickListener {
            selectAllContest()
        }
    }

    private fun selectAllContest() {
        showAllContestRecycler()

        mBinding!!.linearEntryPrizeSort.setBackgroundResource(R.drawable.circle_grey)
        mBinding!!.rupees.setTextColor(resources.getColor(R.color.black))
        mBinding!!.prizeArrow.visibility = View.GONE

        mBinding!!.sortBy2spots.setBackgroundResource(R.drawable.circle_grey)
        mBinding!!.sortBy2spots.setTextColor(resources.getColor(R.color.black))

        mBinding!!.sortBy3spots.setBackgroundResource(R.drawable.circle_grey)
        mBinding!!.sortBy3spots.setTextColor(resources.getColor(R.color.black))

        mBinding!!.sortBy4spots.setBackgroundResource(R.drawable.circle_grey)
        mBinding!!.sortBy4spots.setTextColor(resources.getColor(R.color.black))

        mBinding!!.filterByAll.setBackgroundResource(R.drawable.circle_app_color)
        mBinding!!.filterByAll.setTextColor(resources.getColor(R.color.white))
    }

    private fun showAllContestRecycler() {
        mBinding!!.contestViewRecycler.visibility = View.VISIBLE
        mBinding!!.contestRefresh.visibility = View.VISIBLE
        mBinding!!.contestFilterRefresh.visibility = View.GONE
        mBinding!!.recyclerBySpotsize.visibility = View.GONE
    }

    private fun showFilteredContestRecycler() {
        mBinding!!.contestViewRecycler.visibility = View.GONE
        mBinding!!.contestRefresh.visibility = View.GONE
        mBinding!!.contestFilterRefresh.visibility = View.VISIBLE
        mBinding!!.recyclerBySpotsize.visibility = View.VISIBLE
    }

    private fun filterByEntryPrize() {

        isEntryAscending = !isEntryAscending
        if (isEntryAscending) {
            mBinding!!.prizeArrow.setImageResource(R.drawable.ic_baseline_arrow_upward_24)
        } else {
            mBinding!!.prizeArrow.setImageResource(R.drawable.ic_baseline_arrow_downward_24)
        }
        mBinding!!.contestViewRecycler.visibility = View.GONE
        mBinding!!.recyclerBySpotsize.visibility = View.VISIBLE
        filterSpotsListData.clear()
        val filterValues = ArrayList<ContestModelLists>()
        for (i in 0..allContestListData.size - 1) {
            val values = allContestListData.get(i).allContestsRunning
            if (values != null && values.size > 0) {

                if (isEntryAscending) {
                    val sortedEntryPrizes = values.sortedBy { it -> it.entryFees }
                    sortedEntryPrizes.forEach { s -> filterValues.add(s) }
                } else {
                    val sortedEntryPrizes = values.sortedByDescending { it -> it.entryFees }
                    sortedEntryPrizes.forEach { s -> filterValues.add(s) }
                }
            }
        }

        if (isEntryAscending) {
            val objectPrize = filterValues.sortedBy { it -> it.entryFees }
            objectPrize.forEach { s -> filterSpotsListData.add(s) }
        } else {
            val objectPrize = filterValues.sortedByDescending { it -> it.entryFees }
            objectPrize.forEach { s -> filterSpotsListData.add(s) }
        }
        spotSizeFilterAdaptor.notifyDataSetChanged()
        mBinding!!.recyclerBySpotsize.scheduleLayoutAnimation()
    }

    private fun showRecyclerListBySpotSize(spotSize: Int) {
        mBinding!!.contestViewRecycler.visibility = View.GONE
        mBinding!!.recyclerBySpotsize.visibility = View.VISIBLE
        filterSpotsListData.clear()
        for (i in 0..allContestListData.size - 1) {
            val values = allContestListData.get(i).allContestsRunning
            if (values != null && values.size > 0) {
                for (j in 0..values.size - 1) {
                    val spotObject = values.get(j)
                    if (4 == spotSize && spotObject.totalSpots >= 4) {
                        filterSpotsListData.add(spotObject)
                    } else {
                        if (spotObject.totalSpots == spotSize) {
                            filterSpotsListData.add(spotObject)
                        }
                    }
                }
            }
        }

        spotSizeFilterAdaptor.notifyDataSetChanged()
        mBinding!!.recyclerBySpotsize.smoothScrollToPosition(spotSizeFilterAdaptor.itemCount - 1)
    }

    override fun onResume() {
        super.onResume()
        pos = 0
        for (i in filterArrayList.indices) {
            filterArrayList[i].isStatus = pos == i
        }

        filterAdapter.updateRecord(filterArrayList)

        if (!MyUtils.isConnectedWithInternet(activity as AppCompatActivity)) {
            MyUtils.showToast(activity as AppCompatActivity, "No Internet connection found")
            return
        }
        getAllContest(true)
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        this.isVisibleToUser = isVisibleToUser
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnContestLoadedListener) {
            mListener = context
        } else {
            throw RuntimeException(
                "$context must implement OnContestLoadedListener"
            )
        }

        if (context is OnContestEvents) {
            mListenerContestEvents = context
        } else {
            throw RuntimeException(
                "$context must implement OnContestLoadedListener"
            )
        }
    }

    private fun getAllContest(isLoading: Boolean) {
        //var userInfo = (activity as PlugSportsApplication).userInformations
        selectAllContest()
        mBinding!!.contestRefresh.isRefreshing = false
        //mBinding!!.filterBar.visibility = View.GONE
        if (isLoading)
            mBinding!!.progressBar.visibility = View.VISIBLE

        /*val models = RequestModel()
        models.user_id = MyPreferences.getUserID(requireActivity())!!
        models.match_id = "" + matchObject!!.matchId
        models.token = MyPreferences.getToken(requireActivity())!!
        val deviceToken: String? = MyPreferences.getDeviceToken(requireActivity())
        models.deviceDetails = HardwareInfoManager(activity).collectData(deviceToken!!)*/

        val jsonRequest = JsonObject()
        jsonRequest.addProperty("user_id", MyPreferences.getUserID(requireActivity())!!)
        jsonRequest.addProperty("system_token", MyPreferences.getSystemToken(requireActivity())!!)
        jsonRequest.addProperty("match_id", matchObject!!.matchId)

        val gson = Gson()
        val jsonString: String = gson.toJson(
            HardwareInfoManager(requireActivity()).collectData(
                MyPreferences.getDeviceToken(requireActivity())!!
            )
        )
        val deviceDetails: JsonObject = JsonParser().parse(jsonString).asJsonObject
        jsonRequest.add("deviceDetails", deviceDetails)

        WebServiceClient(requireActivity()).client.create(IApiMethod::class.java)
            .getContestByMatch(jsonRequest)
            .enqueue(object : Callback<UsersPostDBResponse?> {
                override fun onFailure(call: Call<UsersPostDBResponse?>?, t: Throwable?) {
                    if (isVisible) {
                        MyUtils.showToast(activity!! as AppCompatActivity, "Something went wrong!!")
                        mBinding!!.contestRefresh.isRefreshing = false
                        mBinding!!.progressBar.visibility = View.GONE
                    }
                }

                override fun onResponse(
                    call: Call<UsersPostDBResponse?>?,
                    response: Response<UsersPostDBResponse?>?
                ) {
                    if (!isVisible) {
                        return
                    }
                    mBinding!!.contestRefresh.isRefreshing = false
                    mBinding!!.progressBar.visibility = View.GONE
                    val res = response!!.body()
                    if (res != null && res.appMaintainance) {
                        val intent = Intent(activity, MaintainanceActivity::class.java)
                        intent.flags =
                            Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                        activity!!.finish()
                    } else {
                        if (res != null) {
                            if (res.status) {
                                BindingUtils.currentTimeStamp = res.systemTime
                                val responseModel = res.responseObject
                                if (responseModel!!.matchContestlist != null && responseModel.matchContestlist!!.size > 0) {
                                    allContestListData.clear()
                                    allContestListData.addAll(responseModel.matchContestlist!!)

                                    filterArrayList.clear()

                                    val model = ContestCategoryModel("All", true)
                                    filterArrayList.add(model)

                                    for (i in responseModel.matchContestlist!!.indices) {
                                        val categoryModel = ContestCategoryModel(
                                            responseModel.matchContestlist!![i].contestTitle,
                                            false
                                        )
                                        filterArrayList.add(categoryModel)
                                    }

                                    filterAdapter.updateRecord(filterArrayList)

                                    adapter.setMatchesList(allContestListData)
                                    mListener.onMyTeam(responseModel.myjoinedTeams!!)
                                    mListener.onMyContest(responseModel.joinedContestDetails!!)
                                } else {
                                    MyUtils.showToast(
                                        activity!! as AppCompatActivity,
                                        "No Contest available for this match $res"
                                    )
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
                    updateEmptyViews()
                }
            })
    }

    fun updateEmptyViews() {
        if (allContestListData.size == 0) {
            mBinding!!.linearEmptyContest.visibility = View.VISIBLE
        } else {
            mBinding!!.linearEmptyContest.visibility = View.GONE
        }
    }

    inner class FilterAdapter(
        val mContext: Activity,
        var arrayList: ArrayList<ContestCategoryModel>
    ) :
        RecyclerView.Adapter<FilterAdapter.ViewHolderJoinedContest>() {

        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ): FilterAdapter.ViewHolderJoinedContest {
            val view = LayoutInflater.from(mContext)
                .inflate(R.layout.contest_filter_new, parent, false)
            return ViewHolderJoinedContest(view)
        }

        override fun onBindViewHolder(
            holder: FilterAdapter.ViewHolderJoinedContest,
            position: Int
        ) {
            try {
                val categoryModel: ContestCategoryModel = arrayList[position]
                holder.contestTitle.text = categoryModel.name
                if (categoryModel.isStatus) {
                    holder.filterLayout.background =
                        mContext.resources.getDrawable(R.drawable.new_filter_color_back)
                    holder.contestTitle.setTextColor(mContext.resources.getColor(R.color.white))
                } else {
                    holder.contestTitle.setTextColor(mContext.resources.getColor(R.color.crop__selector_focused))
                    holder.filterLayout.background =
                        mContext.resources.getDrawable(R.drawable.new_filter_back)
                }
                holder.filterLayout.setOnClickListener(ClickView(position))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        inner class ClickView(position: Int) : View.OnClickListener {
            var pos = position

            override fun onClick(v: View?) {
                updateContestData(pos)
            }
        }

        override fun getItemCount(): Int {
            return arrayList.size
        }

        fun updateRecord(filterArrayList: ArrayList<ContestCategoryModel>) {
            this.arrayList = filterArrayList
            notifyDataSetChanged()
        }

        inner class ViewHolderJoinedContest(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val contestTitle: TextView = itemView.findViewById(R.id.contest_title)
            val filterLayout: LinearLayout = itemView.findViewById(R.id.filter_layout)
        }
    }

    private fun updateContestData(pos: Int) {
        for (i in filterArrayList.indices) {
            filterArrayList[i].isStatus = pos == i
        }

        //filterAdapter.notifyDataSetChanged()
        filterAdapter.updateRecord(filterArrayList)

        this.pos = pos

        if (pos == 0) {
            showAllContestRecycler()
        } else {

            val actualPosition = pos - 1

            showFilteredContestRecycler()
            filterSpotsListData.clear()

            for (i in allContestListData.indices) {
                if (actualPosition == i) {
                    val values = allContestListData[i].allContestsRunning
                    if (values != null) {
                        filterSpotsListData.addAll(values)
                    }
                }
            }
            spotSizeFilterAdaptor.notifyDataSetChanged()
        }
    }

    private fun getFilteredContest() {

        val jsonRequest = JsonObject()
        jsonRequest.addProperty("user_id", MyPreferences.getUserID(requireActivity())!!)
        jsonRequest.addProperty("system_token", MyPreferences.getSystemToken(requireActivity())!!)
        jsonRequest.addProperty("match_id", matchObject!!.matchId)

        val gson = Gson()
        val jsonString: String = gson.toJson(
            HardwareInfoManager(requireActivity()).collectData(
                MyPreferences.getDeviceToken(requireActivity())!!
            )
        )
        val deviceDetails: JsonObject = JsonParser().parse(jsonString).asJsonObject
        jsonRequest.add("deviceDetails", deviceDetails)

        WebServiceClient(requireActivity()).client.create(IApiMethod::class.java)
            .getContestByMatch(jsonRequest)
            .enqueue(object : Callback<UsersPostDBResponse?> {
                override fun onFailure(call: Call<UsersPostDBResponse?>?, t: Throwable?) {
                    if (isVisible) {
                        MyUtils.showToast(activity!! as AppCompatActivity, "Something went wrong!!")
                        mBinding!!.contestFilterRefresh.isRefreshing = false
                    }
                }

                override fun onResponse(
                    call: Call<UsersPostDBResponse?>?,
                    response: Response<UsersPostDBResponse?>?
                ) {
                    if (!isVisible) {
                        return
                    }
                    mBinding!!.contestFilterRefresh.isRefreshing = false
                    val res = response!!.body()
                    if (res != null && res.appMaintainance) {
                        val intent = Intent(activity, MaintainanceActivity::class.java)
                        intent.flags =
                            Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                        activity!!.finish()
                    } else
                        if (res != null) {
                            if (res.status) {
                                BindingUtils.currentTimeStamp = res.systemTime
                                val responseModel = res.responseObject
                                if (responseModel!!.matchContestlist != null && responseModel.matchContestlist!!.isNotEmpty()) {
                                    allContestListData.clear()
                                    allContestListData.addAll(responseModel.matchContestlist!!)
                                    filterArrayList.clear()

                                    val model = ContestCategoryModel("All", false)
                                    filterArrayList.add(model)

                                    val actualPosition = pos - 1

                                    for (i in responseModel.matchContestlist!!.indices) {
                                        if (actualPosition == i) {
                                            val categoryModel = ContestCategoryModel(
                                                responseModel.matchContestlist!![i].contestTitle,
                                                true
                                            )
                                            filterArrayList.add(categoryModel)
                                        } else {
                                            val categoryModel = ContestCategoryModel(
                                                responseModel.matchContestlist!![i].contestTitle,
                                                false
                                            )
                                            filterArrayList.add(categoryModel)
                                        }
                                    }

                                    filterAdapter.updateRecord(filterArrayList)


                                    showFilteredContestRecycler()
                                    filterSpotsListData.clear()

                                    for (i in allContestListData.indices) {
                                        if (actualPosition == i) {
                                            val values = allContestListData[i].allContestsRunning
                                            if (values != null) {
                                                filterSpotsListData.addAll(values)
                                            }
                                        }
                                    }
                                    spotSizeFilterAdaptor.notifyDataSetChanged()

                                    mListener.onMyTeam(responseModel.myjoinedTeams!!)
                                    mListener.onMyContest(responseModel.joinedContestDetails!!)
                                } else {
                                    MyUtils.showToast(
                                        activity!! as AppCompatActivity,
                                        "No Contest available for this match $res"
                                    )
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
}