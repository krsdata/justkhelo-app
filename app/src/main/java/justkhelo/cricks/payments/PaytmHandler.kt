package justkhelo.cricks.payments

import android.content.Context
import android.text.TextUtils
import androidx.appcompat.app.AppCompatActivity
import justkhelo.cricks.models.ResponseModel
import justkhelo.cricks.network.IApiMethod
import justkhelo.cricks.network.RetrofitClient
import justkhelo.cricks.requestmodels.RequestPaytmModel
import justkhelo.cricks.utils.MyPreferences
import justkhelo.cricks.utils.MyUtils
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*
import kotlin.collections.HashMap

class PaytmHandler(private val mContext: Context, private val mListeners: OnCheckSumGenerated) {
    internal var mAmount = ""
    private var mOrderId: String? = null
    private var callbackUrl: String? = null

    fun paytmPayment(id: String, amount: Double) {
        val mid = MyPreferences.getPaytmMid(mContext)!!
        val callback = MyPreferences.getPaytmCallback(mContext)!!
        if (!TextUtils.isEmpty(mid)) {
            MID = mid
        }
        if (!TextUtils.isEmpty(callback)) {
            CALLBACK_URL = callback
        }

        mAmount = amount.toString()
        val rand = Random()
        CUST_ID = "CUST_00" + MyPreferences.getUserID(mContext)!!
        MOBILE_NO = MyPreferences.getMobile(mContext)
        EMAIL_ID = MyPreferences.getEmail(mContext)
        mOrderId = String.format(Locale.ENGLISH, "%06d", rand.nextInt(1000000))
        callbackUrl = CALLBACK_URL + mOrderId!!
        val requestModel = RequestPaytmModel(
            mOrderId!!,
            CUST_ID,
            mAmount,
            EMAIL_ID!!,
            MOBILE_NO!!,
            callbackUrl!!,
            MID,
            INDUSTRY_TYPE_ID,
            CHANNEL_ID,
            WEBSITE
        )

        if (!MyUtils.isConnectedWithInternet(mContext as AppCompatActivity)) {
            MyUtils.showToast(mContext, "No Internet connection found")
            return
        }
        RetrofitClient(mContext).client.create(IApiMethod::class.java)
            .getPaytmChecksum(requestModel)
            .enqueue(object : Callback<ResponseModel?> {
                override fun onFailure(call: Call<ResponseModel?>?, t: Throwable?) {
                    mListeners.payNowError(t)
                }

                override fun onResponse(
                    call: Call<ResponseModel?>?,
                    response: Response<ResponseModel?>?
                ) {
                    val res = response!!.body()
                    if (res != null) {
                        payNow(res.checksum)
                    }
                }
            })
    }

    private fun payNow(checksum: String) {
        val pmap =
            HashMap<String, String>()
        pmap["MID"] = MID
        pmap["INDUSTRY_TYPE_ID"] = INDUSTRY_TYPE_ID
        pmap["CHANNEL_ID"] = CHANNEL_ID
        pmap["WEBSITE"] = WEBSITE

        pmap["ORDER_ID"] = mOrderId!!
        pmap["CUST_ID"] = CUST_ID
        pmap["MOBILE_NO"] = MOBILE_NO!!
        pmap["EMAIL"] = EMAIL_ID!!
        pmap["TXN_AMOUNT"] = mAmount
        pmap["CALLBACK_URL"] = callbackUrl!!
        pmap["CHECKSUMHASH"] = checksum

        mListeners.payNow(pmap)
    }

    interface OnCheckSumGenerated {
        fun payNow(pmap: HashMap<String, String>)
        fun payNowError(t: Throwable?)
    }

    companion object {
        private var MID = "SDBEXq82035234210571"
        private val INDUSTRY_TYPE_ID = "Retail"
        private val CHANNEL_ID = "WAP"
        private val WEBSITE = "DEFAULT"
        private var CUST_ID = ""
        private var MOBILE_NO: String? = ""
        private var EMAIL_ID: String? = ""
        private var CALLBACK_URL = "https://securegw.paytm.in/theia/paytmCallback?ORDER_ID="
    }
}