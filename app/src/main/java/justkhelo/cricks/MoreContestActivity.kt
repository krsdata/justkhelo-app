package justkhelo.cricks

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.view.Menu
import android.view.View
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.viewpager.widget.ViewPager
import com.edify.atrist.listener.OnContestEvents
import com.edify.atrist.listener.OnContestLoadedListener
import com.edify.atrist.listener.OnMatchTimerStarted
import com.google.gson.JsonObject
import justkhelo.cricks.databinding.ActivityMoreContestBinding
import justkhelo.cricks.models.ContestsParentModels
import justkhelo.cricks.models.MyTeamModels
import justkhelo.cricks.models.UpcomingMatchesModel
import justkhelo.cricks.network.IApiMethod
import justkhelo.cricks.network.WebServiceClient
import justkhelo.cricks.ui.BaseActivity
import justkhelo.cricks.ui.contest.MoreContestFragment
import justkhelo.cricks.models.ContestModelLists
import justkhelo.cricks.models.UsersPostDBResponse
import justkhelo.cricks.utils.BindingUtils
import justkhelo.cricks.utils.MyPreferences
import justkhelo.cricks.utils.MyUtils
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*
import kotlin.collections.ArrayList


class MoreContestActivity : BaseActivity(), OnContestLoadedListener, OnContestEvents,
    ViewPager.OnPageChangeListener {

    private var allContestList: ArrayList<ContestsParentModels>? = null
    private lateinit var selectedObject: ContestsParentModels
    var matchObject: UpcomingMatchesModel? = null
    var isTimeUp: Boolean = false
    var joinedTeamList: java.util.ArrayList<MyTeamModels>? = null
    var contestObjects: ArrayList<ContestModelLists>? = null
    private var mBinding: ActivityMoreContestBinding? = null
    var selected_position = 0
    private val mFragmentList = ArrayList<Fragment>()
    private val mFragmentTitleList = ArrayList<String>()

    companion object {
        const val SERIALIZABLE_KEY_LIST_POSTIION: String = "contestposition"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_more_contest)
        matchObject =
            intent.getSerializableExtra(ContestActivity.SERIALIZABLE_KEY_UPCOMING_MATCHES) as UpcomingMatchesModel
        allContestList =
            intent.getSerializableExtra(ContestActivity.SERIALIZABLE_KEY_JOINED_CONTEST) as ArrayList<ContestsParentModels>
        selectedObject =
            intent.getSerializableExtra(SERIALIZABLE_KEY_LIST_POSTIION) as ContestsParentModels

        if (matchObject != null) {
            initViewUpcomingMatches()
        }

        mBinding!!.imageBack.setOnClickListener(View.OnClickListener {
            finish()
        })

        mBinding!!.imgWallet.setOnClickListener(View.OnClickListener {
            val intent = Intent(this@MoreContestActivity, MyBalanceActivity::class.java)
            startActivity(intent)
        })
        setupViewPager(mBinding!!.viewpagerContest)
        mBinding!!.tabs.setupWithViewPager(mBinding!!.viewpagerContest)
        mBinding!!.viewpagerContest.addOnPageChangeListener(this)

        mBinding!!.mycontestRefresh.setOnRefreshListener(SwipeRefreshLayout.OnRefreshListener {
            getAllContest()
        })
    }

    private fun initViewUpcomingMatches() {
        mBinding!!.teamsa.text = matchObject!!.teamAInfo!!.teamShortName
        mBinding!!.teamsb.text = matchObject!!.teamBInfo!!.teamShortName
    }

    override fun onResume() {
        super.onResume()
        getAllContest()

        if (matchObject!!.status == BindingUtils.MATCH_STATUS_UPCOMING) {
            pauseCountDown()
            startCountDown()
        } else {
            updateTimerHeader()
        }
    }

    private fun startCountDown() {
        BindingUtils.logD("TimerLogs", "initViewUpcomingMatches() called in ContestActivity")
        //matchObject!!.timestampStart = 1591371412 + 300
        BindingUtils.countDownStart(matchObject!!.timestampStart, object : OnMatchTimerStarted {

            override fun onTimeFinished() {
                if (!isTimeUp) {
                    isTimeUp = true
                    updateTimerHeader()
                    if (matchObject!!.status.equals(BindingUtils.MATCH_STATUS_UPCOMING)) {
                        showMatchTimeUpDialog()
                    }
                }
            }

            override fun onTicks(time: String) {
                mBinding!!.matchTimer.text = time
                mBinding!!.matchTimer.setTextColor(resources.getColor(R.color.colorPrimary))
                mBinding!!.watchTimerImg.visibility = View.VISIBLE
                BindingUtils.logD("TimerLogs", "ContestScreen: " + time)
            }
        })
    }

    private fun updateTimerHeader() {
        mBinding!!.matchTimer.text = matchObject!!.statusString.toUpperCase(Locale.ENGLISH)
        mBinding!!.matchTimer.setTextColor(resources.getColor(R.color.colorPrimary))
        mBinding!!.watchTimerImg.visibility = View.GONE
    }

    fun pauseCountDown() {
        BindingUtils.stopTimer()
    }

    override fun onPause() {
        super.onPause()
        pauseCountDown()
    }

    fun changeTabsPositions(postion: Int) {
        mBinding!!.viewpagerContest.currentItem = postion
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        mBinding!!.viewpagerContest.currentItem = selected_position
        //getAllContest()
    }

    private fun getAllContest() {
        //var userInfo = (activity as PlugSportsApplication).userInformations
        mBinding!!.mycontestRefresh.isRefreshing = true
        /*val models = RequestModel()
        models.user_id = MyPreferences.getUserID(this@MoreContestActivity)!!
        models.match_id = "" + matchObject!!.matchId
        models.token = MyPreferences.getToken(this@MoreContestActivity)!!
        val deviceToken: String? = MyPreferences.getDeviceToken(this@MoreContestActivity)
        models.deviceDetails = HardwareInfoManager(this@MoreContestActivity).collectData(deviceToken!!)*/

        val jsonRequest = JsonObject()
        jsonRequest.addProperty("user_id", MyPreferences.getUserID(this)!!)
        jsonRequest.addProperty("system_token", MyPreferences.getSystemToken(this)!!)
        jsonRequest.addProperty("deviceToken", MyPreferences.getDeviceToken(this))
        jsonRequest.addProperty("match_id", matchObject!!.matchId)

        WebServiceClient(this@MoreContestActivity).client.create(IApiMethod::class.java)
            .getContestByMatch(jsonRequest)
            .enqueue(object : Callback<UsersPostDBResponse?> {
                override fun onFailure(call: Call<UsersPostDBResponse?>?, t: Throwable?) {
                    mBinding!!.mycontestRefresh.isRefreshing = false
                }

                override fun onResponse(
                    call: Call<UsersPostDBResponse?>?,
                    response: Response<UsersPostDBResponse?>?
                ) {

                    mBinding!!.mycontestRefresh.isRefreshing = false
                    val res = response!!.body()
                    if (res != null) {
                        if (res.status) {
                            BindingUtils.currentTimeStamp = res.systemTime
                            val responseModel = res.responseObject
                            if (responseModel!!.matchContestlist != null && responseModel.matchContestlist!!.size > 0) {
                                allContestList =
                                    responseModel.matchContestlist as ArrayList<ContestsParentModels>?
                                setupViewPager(mBinding!!.viewpagerContest)
                            } else {
                                MyUtils.showToast(
                                    this@MoreContestActivity,
                                    "No Contest available for this match $res"
                                )
                            }
                        } else {
                            if (res.code == 1001) {
                                MyUtils.showMessage(this@MoreContestActivity, res.message)
                                MyUtils.logoutApp(this@MoreContestActivity)
                            } else {
                                MyUtils.showMessage(this@MoreContestActivity, res.message)
                            }
                        }
                    }
                }
            })
    }

    override fun onBitmapSelected(bitmap: Bitmap) {

    }

    override fun onUploadedImageUrl(url: String) {

    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        return true
    }

    private fun setupViewPager(viewPager: ViewPager) {
        mFragmentList.clear()
        mFragmentTitleList.clear()

        val adapter = ViewPagerAdapter(supportFragmentManager)
        viewPager.removeAllViews()
        for (i in allContestList!!.indices) {
            val contObject = allContestList!![i]

            val bundle = Bundle()
            bundle.putSerializable(ContestActivity.SERIALIZABLE_KEY_MATCH_OBJECT, matchObject)
            bundle.putSerializable(MoreContestFragment.CONTEST_LIST, contObject.allContestsRunning)
            adapter.addFragment(
                MoreContestFragment.newInstance(bundle),
                contObject.contestTitle + "(" + contObject.allContestsRunning!!.size + ")"
            )

            if (selectedObject.contestTitle == contObject.contestTitle) {
                selected_position = i
            }
        }

        viewPager.adapter = adapter
        viewPager.currentItem = selected_position
    }

    internal inner class ViewPagerAdapter(manager: FragmentManager) :
        FragmentPagerAdapter(manager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

        override fun getItem(position: Int): Fragment {
            return mFragmentList[position]
        }

        override fun getCount(): Int {
            return mFragmentList.size
        }

        fun addFragment(fragment: Fragment, title: String) {
            mFragmentList.add(fragment)
            mFragmentTitleList.add(title)
        }

        override fun getPageTitle(position: Int): CharSequence {
            return mFragmentTitleList[position]
        }
    }

    override fun onMyContest(contestModel: ArrayList<ContestModelLists>) {
        this.contestObjects = contestModel
        if (matchObject!!.status == BindingUtils.MATCH_STATUS_UPCOMING) {
            mBinding!!.tabs.getTabAt(1)!!.text =
                String.format("My Contest(%d)", contestModel.size)
        } else {
            mBinding!!.tabs.getTabAt(0)!!.text =
                String.format("My Contest(%d)", contestModel.size)
        }
    }

    override fun onMyTeam(count: ArrayList<MyTeamModels>) {
        this.joinedTeamList = count
        if (matchObject!!.status == BindingUtils.MATCH_STATUS_UPCOMING) {
            mBinding!!.tabs.getTabAt(2)!!.text =
                String.format("MyTeam(%d)", this.joinedTeamList!!.size)
        } else {
            mBinding!!.tabs.getTabAt(1)!!.text =
                String.format("MyTeam(%d)", this.joinedTeamList!!.size)
        }
    }

    override fun onContestJoinning(objects: ContestModelLists, position: Int) {
        if (!MyUtils.isConnectedWithInternet(this)) {
            MyUtils.showToast(this, "No Internet connection found")
            return
        }
        customeProgressDialog.show()
        /*val models = RequestModel()
        models.user_id = MyPreferences.getUserID(this)!!
        models.token =MyPreferences.getToken(this)!!
        models.match_id = "" + matchObject!!.matchId
        models.contest_id = "" + objects.id*/

        val jsonRequest = JsonObject()
        jsonRequest.addProperty("user_id", MyPreferences.getUserID(this)!!)
        jsonRequest.addProperty("system_token", MyPreferences.getSystemToken(this)!!)
        jsonRequest.addProperty("contest_id", objects.id)
        jsonRequest.addProperty("match_id", matchObject!!.matchId)

        WebServiceClient(this).client.create(IApiMethod::class.java).joinNewContestStatus(jsonRequest)
            .enqueue(object : Callback<UsersPostDBResponse?> {
                override fun onFailure(call: Call<UsersPostDBResponse?>?, t: Throwable?) {
                    customeProgressDialog.dismiss()
                }

                override fun onResponse(
                    call: Call<UsersPostDBResponse?>?,
                    response: Response<UsersPostDBResponse?>?
                ) {
                    customeProgressDialog.dismiss()
                    val res = response!!.body()
                    if (res != null) {
                        if (!res.status) {
                            if (res.code == 1001) {
                                MyUtils.showMessage(this@MoreContestActivity, res.message)
                                MyUtils.logoutApp(this@MoreContestActivity)
                            }
                            if (res.code == 401) {
                                MyUtils.showToast(
                                    this@MoreContestActivity,
                                    res.message
                                )
                            } else {
                                MyUtils.showMessage(
                                    this@MoreContestActivity,
                                    res.message
                                )
                            }
                        } else {
                            if (res.actionForTeam == 1) {
                                val intent =
                                    Intent(this@MoreContestActivity, CreateTeamActivity::class.java)
                                intent.putExtra(
                                    CreateTeamActivity.SERIALIZABLE_MATCH_KEY,
                                    matchObject
                                )
                                startActivityForResult(
                                    intent,
                                    CreateTeamActivity.CREATETEAM_REQUESTCODE
                                )
                            } else if (res.actionForTeam == 2) {
                                val intent =
                                    Intent(this@MoreContestActivity, SelectTeamActivity::class.java)
                                intent.putExtra(
                                    CreateTeamActivity.SERIALIZABLE_MATCH_KEY,
                                    matchObject
                                )
                                intent.putExtra(
                                    CreateTeamActivity.SERIALIZABLE_CONTEST_KEY,
                                    objects
                                )
                                intent.putExtra(
                                    CreateTeamActivity.SERIALIZABLE_SELECTED_TEAMS,
                                    res.selectedTeamModel
                                )
                                startActivityForResult(
                                    intent,
                                    CreateTeamActivity.CREATETEAM_REQUESTCODE
                                )
                            } else {
                                Toast.makeText(
                                    this@MoreContestActivity,
                                    res.message,
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                    }
                }
            })
    }

    override fun onShareContest(objects: ContestModelLists) {

    }

    override fun onPageScrollStateChanged(state: Int) {

    }

    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {

    }

    override fun onPageSelected(position: Int) {
        selected_position = position
    }
}