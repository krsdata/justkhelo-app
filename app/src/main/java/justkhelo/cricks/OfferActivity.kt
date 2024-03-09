package justkhelo.cricks

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.gson.JsonObject
import justkhelo.cricks.databinding.ActivityOfferBinding
import justkhelo.cricks.network.IApiMethod
import justkhelo.cricks.network.WebServiceClient
import justkhelo.cricks.utils.MyPreferences
import justkhelo.cricks.utils.MyUtils
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class OfferActivity : AppCompatActivity(), SwipeRefreshLayout.OnRefreshListener {

    companion object {
        val TAG: String = OfferActivity::class.java.simpleName
    }

    private var mBinding: ActivityOfferBinding? = null
    private var mContext: Context? = null
    private lateinit var adapter: OfferAdapter
    private var jsonDataList: ArrayList<JSONObject> = ArrayList<JSONObject>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = DataBindingUtil.setContentView(
            this,
            R.layout.activity_offer
        )

        mContext = this

        mBinding!!.toolbar.setTitleTextColor(resources.getColor(R.color.white))
        mBinding!!.toolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp)
        setSupportActionBar(mBinding!!.toolbar)
        mBinding!!.toolbar.setNavigationOnClickListener {
            finish()
        }

        mBinding!!.refreshLayout.setColorSchemeColors(mContext!!.resources.getColor(R.color.colorPrimary))
        mBinding!!.refreshLayout.setOnRefreshListener(this)

        mBinding!!.recyclerView.layoutManager =
            LinearLayoutManager(mContext, RecyclerView.VERTICAL, false)
        mBinding!!.recyclerView.visibility = View.VISIBLE

        mBinding!!.progressBar.visibility = View.GONE
    }

    override fun onResume() {
        super.onResume()
        getAllOffers(true)
    }

    private fun getAllOffers(b: Boolean) {

        if (!MyUtils.isConnectedWithInternet(mContext!!)) {
            MyUtils.showMessage(mContext!!, "Please check your Internet Connection")
            return
        }

        if (b) {
            mBinding!!.progressBar.visibility = View.VISIBLE
        }

        val jsonRequest = JsonObject()
        jsonRequest.addProperty("user_id", MyPreferences.getUserID(mContext!!)!!)
        jsonRequest.addProperty("system_token", MyPreferences.getSystemToken(this)!!)
        jsonRequest.addProperty("version_code", NinjaApplication.appVersion)

        WebServiceClient(mContext!!).client.create(IApiMethod::class.java)
            .getMessages(jsonRequest)
            .enqueue(object : Callback<JsonObject?> {
                override fun onFailure(call: Call<JsonObject?>?, t: Throwable?) {
                    mBinding!!.progressBar.visibility = View.GONE
                    mBinding!!.refreshLayout.isRefreshing = false
                }

                override fun onResponse(
                    call: Call<JsonObject?>?,
                    response: Response<JsonObject?>?
                ) {
                    mBinding!!.progressBar.visibility = View.GONE
                    mBinding!!.refreshLayout.isRefreshing = false

                    val resObje = response!!.body().toString()
                    val jsonObject = JSONObject(resObje)
                    if (jsonObject.optBoolean("status")) {
                        try {
                            jsonDataList.clear()
                            val array = jsonObject.getJSONArray("data")
                            val data = array.getJSONObject(3)
                            val offerArray = data.getJSONArray("offer_array")
                            for (i in 0 until offerArray.length()) {
                                val json: JSONObject = offerArray[i] as JSONObject
                                // message_status
                                if (json.getInt("message_status") == 1) {
                                    jsonDataList.add(json)
                                }
                            }

                            adapter = OfferAdapter(mContext!!, jsonDataList)
                            mBinding!!.recyclerView.adapter = adapter
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    } else {
                        if (jsonObject.getInt("code") == 1001) {
                            MyUtils.showMessage(mContext!!, jsonObject.getString("message"))
                            MyUtils.logoutApp(this@OfferActivity)
                        } else {
                            MyUtils.showMessage(mContext!!, jsonObject.getString("message"))
                        }
                    }
                }
            })
    }

    override fun onRefresh() {
        getAllOffers(false)
    }

    inner class OfferAdapter(val context: Context, val jsonDataList: ArrayList<JSONObject>) :
        RecyclerView.Adapter<OfferAdapter.DataViewHolder>() {

        inner class DataViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            var offerImage: ImageView = itemView.findViewById(R.id.offer_image)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DataViewHolder {
            val view = LayoutInflater.from(context)
                .inflate(R.layout.offer_list_item, parent, false)
            return DataViewHolder(view)
        }

        override fun onBindViewHolder(holder: DataViewHolder, position: Int) {
            try {
                Glide.with(context).load(jsonDataList[position].getString("image_url"))
                    .diskCacheStrategy(
                        DiskCacheStrategy.ALL
                    ).into(holder.offerImage)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        override fun getItemCount(): Int {
            return jsonDataList.size
        }
    }
}