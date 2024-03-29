package justkhelo.cricks.ui.myaccounts

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.JsonObject
import justkhelo.cricks.NinjaApplication
import justkhelo.cricks.R
import justkhelo.cricks.databinding.FragmentTransactionHistoryBinding
import justkhelo.cricks.models.TransactionModel
import justkhelo.cricks.models.UsersPostDBResponse
import justkhelo.cricks.network.IApiMethod
import justkhelo.cricks.network.WebServiceClient
import justkhelo.cricks.utils.MyPreferences
import justkhelo.cricks.utils.MyUtils
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class TransactionFragment : Fragment() {

    private lateinit var adapter: TransactionAdaptor
    private var mBinding: FragmentTransactionHistoryBinding? = null
    var transactionList = ArrayList<TransactionModel>()

    companion object {
        fun newInstance(bundle: Bundle): TransactionFragment {
            val fragment = TransactionFragment()
            fragment.arguments = bundle
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        mBinding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_transaction_history, container, false
        )

        return mBinding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val transactionHistory =
            (requireActivity().applicationContext as NinjaApplication).getTransactionHistory
        if (transactionHistory.size > 0) {
            transactionList.clear()
            transactionList.addAll(transactionHistory)
        }
        adapter = TransactionAdaptor(requireActivity(), transactionList)

        mBinding!!.recyclerView.layoutManager =
            LinearLayoutManager(activity, RecyclerView.VERTICAL, false)
        val dividerItemDecoration = DividerItemDecoration(
            mBinding!!.recyclerView.context,
            RecyclerView.VERTICAL
        )
        mBinding!!.recyclerView.addItemDecoration(dividerItemDecoration)

        mBinding!!.recyclerView.adapter = adapter
    }

    override fun onResume() {
        super.onResume()
        getMyTransaction()
    }

    private fun getMyTransaction() {
        if (!MyUtils.isConnectedWithInternet(activity as AppCompatActivity)) {
            MyUtils.showToast(activity as AppCompatActivity, "No Internet connection found")
            return
        }
        mBinding!!.progressBarTransaction.visibility = View.VISIBLE

        val jsonRequest = JsonObject()
        jsonRequest.addProperty("user_id", MyPreferences.getUserID(requireActivity())!!)
        jsonRequest.addProperty("system_token", MyPreferences.getSystemToken(requireActivity())!!)

        WebServiceClient(requireActivity()).client.create(IApiMethod::class.java)
            .getTransactionHistory(jsonRequest)
            .enqueue(object : Callback<UsersPostDBResponse?> {
                override fun onFailure(call: Call<UsersPostDBResponse?>?, t: Throwable?) {
                    if (isVisible) {
                        mBinding!!.progressBarTransaction.visibility = View.GONE
                    }
                }

                override fun onResponse(
                    call: Call<UsersPostDBResponse?>?,
                    response: Response<UsersPostDBResponse?>?
                ) {
                    if (!isVisible) {
                        return
                    }
                    mBinding!!.progressBarTransaction.visibility = View.GONE
                    val res = response!!.body()
                    if (res != null) {
                        if (res.status) {
                            val responseModel = res.transactionHistory
                            if (responseModel != null) {
                                if (responseModel.transactionList != null && responseModel.transactionList!!.size > 0) {
                                    (activity!!.applicationContext as NinjaApplication).saveTransactionHistory(
                                        responseModel.transactionList!!
                                    )
                                    transactionList.clear()
                                    transactionList.addAll(responseModel.transactionList!!)
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

    inner class TransactionAdaptor(
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
                viewHolder.transactionAmount.setTextColor(activity!!.resources.getColor(R.color.green))
                viewHolder.transactionAmount?.text =
                    objectVal.debitCreditStatus + "₹" + objectVal.depositAmount
            } else {
                viewHolder.transactionAmount.setTextColor(activity!!.resources.getColor(R.color.red))
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