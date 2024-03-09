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
import justkhelo.cricks.databinding.ActivityMyTransactionBinding
import justkhelo.cricks.models.TransactionModel
import justkhelo.cricks.models.UsersPostDBResponse
import justkhelo.cricks.network.IApiMethod
import justkhelo.cricks.network.WebServiceClient
import justkhelo.cricks.utils.CustomeProgressDialog
import justkhelo.cricks.utils.MyPreferences
import justkhelo.cricks.utils.MyUtils
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class MyTransactionHistoryActivity : AppCompatActivity() {

    private lateinit var adapter: MoreOptionsAdaptor
    private var mBinding: ActivityMyTransactionBinding? = null
    var customeProgressDialog: CustomeProgressDialog? = null
    var transactionList = ArrayList<TransactionModel>()
    var mContext: Context? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = DataBindingUtil.setContentView(
            this,
            R.layout.activity_my_transaction
        )

        mContext = this

        mBinding!!.toolbar.title = "Transaction"
        mBinding!!.toolbar.setTitleTextColor(resources.getColor(R.color.white))
        mBinding!!.toolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp)
        setSupportActionBar(mBinding!!.toolbar)
        mBinding!!.toolbar.setNavigationOnClickListener {
            finish()
        }

        customeProgressDialog = CustomeProgressDialog(mContext)

        mBinding!!.transactionHistoryRecycler.layoutManager =
            LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        val itemDecoration = DividerItemDecoration(this, LinearLayout.VERTICAL)
        mBinding!!.transactionHistoryRecycler.addItemDecoration(itemDecoration)
        val transactionHistory =
            (applicationContext as NinjaApplication).getTransactionHistory
        if (transactionHistory != null && transactionHistory.size > 0) {
            transactionList.clear()
            transactionList.addAll(transactionHistory)
        }

        adapter = MoreOptionsAdaptor(this, transactionList)
        mBinding!!.transactionHistoryRecycler.adapter = adapter
        if (!MyUtils.isConnectedWithInternet(this)) {
            MyUtils.showToast(this, "No Internet connection found")
            return
        }
        getMyTransaction()
    }

    private fun getMyTransaction() {
        mBinding!!.emptyview.visibility = View.GONE
        mBinding!!.progressBar.visibility = View.VISIBLE

        val jsonRequest = JsonObject()
        jsonRequest.addProperty("user_id", MyPreferences.getUserID(this)!!)
        jsonRequest.addProperty("system_token", MyPreferences.getSystemToken(this)!!)
        jsonRequest.addProperty("page_no", 1)

        WebServiceClient(this).client.create(IApiMethod::class.java)
            .getTransactionHistory(jsonRequest)
            .enqueue(object : Callback<UsersPostDBResponse?> {
                override fun onFailure(call: Call<UsersPostDBResponse?>?, t: Throwable?) {
                    mBinding!!.emptyview.visibility = View.VISIBLE
                    mBinding!!.progressBar.visibility = View.GONE
                }

                override fun onResponse(
                    call: Call<UsersPostDBResponse?>?,
                    response: Response<UsersPostDBResponse?>?
                ) {
                    mBinding!!.progressBar.visibility = View.GONE
                    val res = response!!.body()
                    if (res != null) {
                        if (res.status) {
                            val responseModel = res.transactionHistory
                            if (responseModel != null) {
                                if (responseModel.transactionList != null && responseModel.transactionList!!.size > 0) {
                                    (applicationContext as NinjaApplication).saveTransactionHistory(
                                        responseModel.transactionList!!
                                    )
                                    transactionList.addAll(responseModel.transactionList!!)
                                    adapter.notifyDataSetChanged()
                                    mBinding!!.emptyview.visibility = View.GONE

                                } else {
                                    mBinding!!.emptyview.visibility = View.VISIBLE
                                }
                            } else {
                                mBinding!!.emptyview.visibility = View.VISIBLE
                            }
                        } else {
                            if (res.code == 1001) {
                                MyUtils.showMessage(this@MyTransactionHistoryActivity, res.message)
                                MyUtils.logoutApp(this@MyTransactionHistoryActivity)
                            } else {
                                MyUtils.showMessage(this@MyTransactionHistoryActivity, res.message)
                            }
                        }
                    }
                }
            })
    }

    inner class MoreOptionsAdaptor(
        val context: Context,
        val tradeinfoModels: ArrayList<TransactionModel>
    ) :
        RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        var onItemClick: ((TransactionModel) -> Unit)? = null
        private var optionListObject = tradeinfoModels

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.row_recent_transactions, parent, false)
            return DataViewHolder(view)
        }

        override fun onBindViewHolder(parent: RecyclerView.ViewHolder, viewType: Int) {
            val objectVal = optionListObject[viewType]
            val viewHolder: DataViewHolder = parent as DataViewHolder
            viewHolder.transactionNote?.text = objectVal.paymentType
            viewHolder.transactionId?.text = objectVal.transactionId
            viewHolder.transactionDate?.text = objectVal.createdDate

            if (objectVal.debitCreditStatus.equals("+")) {
                viewHolder.transactionAmount.setTextColor(context.resources.getColor(R.color.green))
                viewHolder.transactionAmount?.text =
                    objectVal.debitCreditStatus + "₹" + objectVal.depositAmount
            } else {
                viewHolder.transactionAmount.setTextColor(context.resources.getColor(R.color.red))
                viewHolder.transactionAmount?.text =
                    objectVal.debitCreditStatus + "₹" + objectVal.depositAmount
            }
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

            val transactionNote = itemView.findViewById<TextView>(R.id.transaction_note)
            val transactionId = itemView.findViewById<TextView>(R.id.transaction_id)
            val transactionDate = itemView.findViewById<TextView>(R.id.transaction_date)
            val transactionAmount = itemView.findViewById<TextView>(R.id.transaction_amount)
        }
    }
}