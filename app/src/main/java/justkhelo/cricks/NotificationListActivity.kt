package justkhelo.cricks

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.JsonObject
import justkhelo.cricks.databinding.ActivityNotificationListBinding
import justkhelo.cricks.models.NotifyModels
import justkhelo.cricks.models.UsersPostDBResponse
import justkhelo.cricks.models.WalletInfo
import justkhelo.cricks.network.IApiMethod
import justkhelo.cricks.network.WebServiceClient
import justkhelo.cricks.utils.CustomeProgressDialog
import justkhelo.cricks.utils.MyPreferences
import justkhelo.cricks.utils.MyUtils
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class NotificationListActivity : AppCompatActivity() {
    private lateinit var adapter: NotificationListAdaptor
    var allNotificationList = ArrayList<NotifyModels>()
    private lateinit var walletInfo: WalletInfo
    private var customeProgressDialog: CustomeProgressDialog? = null
    private var mBinding: ActivityNotificationListBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = DataBindingUtil.setContentView(
            this,
            R.layout.activity_notification_list
        )
        walletInfo = (application as NinjaApplication).walletInfo
        mBinding!!.toolbar.title = "Notifications"
        mBinding!!.toolbar.setTitleTextColor(resources.getColor(R.color.white))
        mBinding!!.toolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp)
        setSupportActionBar(mBinding!!.toolbar)
        mBinding!!.toolbar.setNavigationOnClickListener(View.OnClickListener {
            finish()
        })
        customeProgressDialog = CustomeProgressDialog(this)
        adapter = NotificationListAdaptor(this, allNotificationList)
        mBinding!!.recyclerNotificationList.layoutManager =
            LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        var itemDecoration = DividerItemDecoration(this, LinearLayout.VERTICAL)
        mBinding!!.recyclerNotificationList.addItemDecoration(itemDecoration)
        mBinding!!.recyclerNotificationList.adapter = adapter
        adapter.onItemClick = { objects ->

        }
        if (!MyUtils.isConnectedWithInternet(this)) {
            MyUtils.showToast(this, "No Internet connection found")
            return
        }
        getNotificationList()
    }

    fun getNotificationList() {
        customeProgressDialog!!.show()

        val jsonRequest = JsonObject()
        jsonRequest.addProperty("user_id", MyPreferences.getUserID(this)!!)
        jsonRequest.addProperty("system_token", MyPreferences.getSystemToken(this)!!)

        WebServiceClient(this).client.create(IApiMethod::class.java).getNotification(jsonRequest)
            .enqueue(object : Callback<UsersPostDBResponse?> {
                override fun onFailure(call: Call<UsersPostDBResponse?>?, t: Throwable?) {
                    customeProgressDialog!!.dismiss()
                }

                override fun onResponse(
                    call: Call<UsersPostDBResponse?>?,
                    response: Response<UsersPostDBResponse?>?
                ) {
                    customeProgressDialog!!.dismiss()
                    val res = response!!.body()
                    if (res != null) {
                        if (res.status) {
                            val responseModel = res.notificationList
                            if (responseModel != null && responseModel.size > 0) {
                                allNotificationList.addAll(responseModel)
                                adapter.notifyDataSetChanged()
                            }
                        } else {
                            if (res.code == 1001) {
                                MyUtils.showMessage(this@NotificationListActivity, res.message)
                                MyUtils.logoutApp(this@NotificationListActivity)
                            } else {
                                MyUtils.showMessage(this@NotificationListActivity, res.message)
                            }
                        }
                    }
                }
            })
    }


    inner class NotificationListAdaptor(
        val context: Context,
        val tradeinfoModels: ArrayList<NotifyModels>
    ) :
        RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        var onItemClick: ((NotifyModels) -> Unit)? = null
        private var optionListObject = tradeinfoModels


        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            var view = LayoutInflater.from(parent.context)
                .inflate(R.layout.row_notification, parent, false)
            return DataViewHolder(view)

        }

        override fun onBindViewHolder(parent: RecyclerView.ViewHolder, viewType: Int) {
            var objectVal = optionListObject[viewType]
            val viewHolder: DataViewHolder = parent as DataViewHolder
            viewHolder.notificationTitle?.text = objectVal.notificationTitle
            viewHolder.notificationMessage?.text = objectVal.notificationMessages
        }


        override fun getItemCount(): Int {
            return optionListObject.size
        }


        inner class DataViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            init {
                itemView.setOnClickListener {
                    onItemClick?.invoke(optionListObject[adapterPosition])
                }
            }

            val notificationMessage = itemView.findViewById<TextView>(R.id.notification_message)
            val notificationTitle = itemView.findViewById<TextView>(R.id.notification_title)
        }


    }

}
