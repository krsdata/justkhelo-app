package justkhelo.cricks.utils

import android.content.Context
import android.os.CountDownTimer
import android.util.Log
import com.edify.atrist.listener.OnMatchTimerStarted
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import justkhelo.cricks.BuildConfig
import justkhelo.cricks.models.UsersPostDBResponse
import justkhelo.cricks.network.IApiMethod
import justkhelo.cricks.network.WebServiceClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.*

class BindingUtils {

    companion object {
        const val UNLIMITED_SPOT_MARGIN: Int = 6
        const val MINIMUM_DEPOSIT_AMOUNT: Int = 25
        private var isCountedObjectCreated: Boolean = false
        var timer: CountDownTimer? = null
        const val REUEST_STATUS_CODE_FRAUD: Int = 420
        const val MATCH_STATUS_UPCOMING: Int = 1
        const val MATCH_STATUS_LIVE: Int = 3

        const val BANK_DOCUMENTS_STATUS_REJECTED: Int = 3
        const val BANK_DOCUMENTS_STATUS_VERIFIED: Int = 2
        const val BANK_DOCUMENTS_STATUS_APPROVAL_PENDING: Int = 1
        const val BANNERS_KEY_ADD: String = "ADD"
        const val BANNERS_KEY_REFFER: String = "reffer"
        const val BANNERS_KEY_SUPPORT: String = "support"
        const val BANNERS_KEY_BROWSERS: String = "browser"
        const val EMAIL: String = "info@justkhelo.com"
        const val PHONE_NUMBER: String = "=+919329922484"
        const val GOOGLE_TEZ_PACKAGE_NAME = "com.google.android.apps.nbu.paisa.user"
        const val
                PAYMENT_GOOGLEPAY_UPI = "7999587754@okbizaxis"
        const val PAYMENT_RAZOR_PAY_KEY = "rzp_live_SiMilNQfyJNzJe"
        const val BASE_URL_MAIN = "https://justkhelo.com/"
        const val BASE_URL_API = "https://api.justkhelo.com/"
        const val BASE_URL_MAIN_API = "https://api.justkhelo.com/api/v3/"
        const val WEBVIEW_FANTASY_POINTS = BASE_URL_MAIN + "fantasy-points-system/index.html"
        const val WEBVIEW_FANTASY_HOW_TO_PLAY = BASE_URL_MAIN + "how-to-play?request=mobile"
        const val WEBVIEW_TNC = BASE_URL_MAIN + "terms-and-conditions?request=mobile"
        const val WEBVIEW_PRIVACY = BASE_URL_MAIN + "privacy-policy?request=mobile"
        const val WEBVIEW_ABOUT_US = BASE_URL_MAIN + "about-us?request=mobile"
        const val WEBVIEW_LEGALITY = BASE_URL_MAIN + "legality?request=mobile"
        const val WEBVIEW_FAQ = BASE_URL_MAIN + "faqs?request=mobile"
        const val WEBVIEW_OFFERS = BASE_URL_MAIN + "offers?request=mobile"
        const val WEBVIEW_TOP_REFERRAL_USER = BASE_URL_MAIN + "topReferralUser?request=mobile"
        const val NOTIFICATION_ID_BIG_IMAGE = 101
        const val BILTY_APK_LINK: String = BASE_URL_API + "apk"
        const val WEB_TITLE_PRIVACY_POLICY: String = "Privacy Policy"
        const val WEB_TITLE_TERMS_CONDITION: String = "Terms & Conditions"
        const val WEB_TITLE_HOW_TO_PLAY: String = "How To Play"
        const val WEB_TITLE_ABOUT_US: String = "About Us"
        const val WEB_TITLE_FANTASY_POINTS: String = "Fantasy Point System"
        const val WEB_TITLE_LEGALITY: String = "LEGALITY"
        const val WEB_TITLE_FAQ: String = "FAQs"
        const val WEB_TITLE_OFFERS: String = "Offers"
       // const val WEB_TITLE_TOP_REFERRAL_USER: String = "Top Referral Users"
        const val EXTRA_DATA_GET_WALLET: String = "EXTRA_DATA_GET_WALLET"
        var currentTimeStamp: Long = 0

        fun logD(tag: String, message: String) {
            if (BuildConfig.MLOG) {
                Log.e("MX:$tag", message)
            }
        }

        fun countDownStart(
            starttimeStamp: Long,
            listeners: OnMatchTimerStarted
        ) {
            if (starttimeStamp > currentTimeStamp && !isCountedObjectCreated) {
                isCountedObjectCreated = true
                //BindingUtils.logD("TimerLogs","Count Down timer Called")
                timer = object : CountDownTimer(starttimeStamp, 1000) {
                    override fun onTick(millisUntilFinished: Long) {
                        val itemLong = starttimeStamp
                        val date = Date(itemLong * 1000L)
                        //val date2 = Date(currentTimeStamp*1000L)
                        val date2 = Date()
                        if (date2.before(date)) {
                            val l8 = date.time - date2.time
                            val l2 = l8 / 86400000L
                            java.lang.Long.signum(l2)
                            val l3 = l8 - 86400000L * l2
                            val l4 = l3 / 3600000L
                            val l5 = l3 - 3600000L * l4
                            try {
                                val l6 = l5 / 60000L
                                val l7 = (l5 - 60000L * l6) / 1000L
                                //val customTextView2: CustomTextView = customTextView
                                val stringBuilder = StringBuilder()
                                if (l2 != 0L) {
                                    stringBuilder.append(l2)
                                    stringBuilder.append("d ")
                                }

                                if (l4 != 0L) {
                                    stringBuilder.append(l4)
                                    stringBuilder.append("h ")
                                }
                                if (l6 != 0L) {
                                    stringBuilder.append(l6)
                                    stringBuilder.append("m ")
                                }
                                stringBuilder.append(l7)
                                stringBuilder.append("s left")
                                // upcomingMatchesAdapter.notifyItemChanged(viewType)
                                listeners.onTicks(stringBuilder.toString())
                            } catch (exception: Exception) {
                                // MyUtils.logd("timestamp",exception.message)
                                exception.printStackTrace()
                            }
                        } else {
                            listeners.onTimeFinished()
                        }

                    }

                    override fun onFinish() {
                        listeners.onTimeFinished()
                        isCountedObjectCreated = false
                    }
                }
                timer!!.start()
            } else {
                listeners.onTimeFinished()
                isCountedObjectCreated = false
            }
        }

        fun stopTimer() {
            if (timer != null) {
                isCountedObjectCreated = false
                timer!!.cancel()
                timer = null
            }
        }

        fun countDownStartForAdaptors(
            startTimeStamp: Long,
            listeners: OnMatchTimerStarted
        ) {
            if (startTimeStamp > currentTimeStamp) {
                //BindingUtils.logD("TimerLogs","Count Down timer Called Adaptors")
                val timerAdapters = object : CountDownTimer(startTimeStamp, 1000) {
                    override fun onTick(millisUntilFinished: Long) {
                        val date = Date(startTimeStamp * 1000L)
                        //val date2 = Date(currentTimeStamp*1000L)
                        val date2 = Date()
                        if (date2.before(date)) {
                            val l8 = date.time - date2.time
                            val l2 = l8 / 86400000L
                            java.lang.Long.signum(l2)
                            val l3 = l8 - 86400000L * l2
                            val l4 = l3 / 3600000L
                            val l5 = l3 - 3600000L * l4
                            try {
                                val l6 = l5 / 60000L
                                val l7 = (l5 - 60000L * l6) / 1000L
                                //val customTextView2: CustomTextView = customTextView
                                val stringBuilder = StringBuilder()
                                if (l2 != 0L) {
                                    stringBuilder.append(l2)
                                    stringBuilder.append("d ")
                                }

                                if (l4 != 0L) {
                                    stringBuilder.append(l4)
                                    stringBuilder.append("h ")
                                }
                                if (l6 != 0L) {
                                    stringBuilder.append(l6)
                                    stringBuilder.append("m ")
                                }
                                stringBuilder.append(l7)
                                stringBuilder.append("s left")
                                listeners.onTicks(stringBuilder.toString())
                            } catch (exception: Exception) {
                                // MyUtils.logd("TimerLogs", exception.message)
                                exception.printStackTrace()
                            }
                        } else {
                            listeners.onTimeFinished()
                        }
                    }

                    override fun onFinish() {
                        listeners.onTimeFinished()
                    }
                }
                timerAdapters.start()
            } else {
                listeners.onTimeFinished()
            }
        }

/*
        fun countDownStartForAdaptors2(
            startTimeStamp: Long,
            listeners: OnMatchTimerStarted
        ) {
            if (startTimeStamp > currentTimeStamp) {
                //BindingUtils.logD("TimerLogs","Count Down timer Called Adaptors")
                val timerAdapters = object : CountDownTimer(startTimeStamp, 1000) {
                    override fun onTick(millisUntilFinished: Long) {
                        val date = Date(startTimeStamp * 1000L)
                        //val date2 = Date(currentTimeStamp*1000L)
                        val date2 = Date()
                        if (date2.before(date)) {
                            val l8 = date.time - date2.time
                            val l2 = l8 / 86400000L
                            java.lang.Long.signum(l2)
                            val l3 = l8 - 86400000L * l2
                            val l4 = l3 / 3600000L
                            val l5 = l3 - 3600000L * l4
                            try {
                                val l6 = l5 / 60000L
                                val l7 = (l5 - 60000L * l6) / 1000L
                                //val customTextView2: CustomTextView = customTextView
                                val stringBuilder = StringBuilder()
                                if (l2 != 0L) {
                                    if ((l2/10).toInt() == 0) {
                                        stringBuilder.append("0")
                                    }
                                    stringBuilder.append(l2)
                                    stringBuilder.append("d ")
                                }

                                if (l4 != 0L) {
                                    if ((l4/10).toInt() == 0) {
                                        stringBuilder.append("0")
                                    }
                                    stringBuilder.append(l4)
                                    stringBuilder.append(":")
                                }
                                if (l6 != 0L) {
                                    if ((l6/10).toInt() == 0) {
                                        stringBuilder.append("0")
                                    }
                                    stringBuilder.append(l6)
                                    stringBuilder.append(":")
                                }
                                stringBuilder.append(l7)
                              //  stringBuilder.append("s left")
                                listeners.onTicks(stringBuilder.toString())
                            } catch (exception: Exception) {
                                // MyUtils.logd("TimerLogs", exception.message)
                                exception.printStackTrace()
                            }
                        } else {
                            listeners.onTimeFinished()
                        }
                    }

                    override fun onFinish() {
                        listeners.onTimeFinished()
                    }
                }
                timerAdapters.start()
            } else {
                listeners.onTimeFinished()
            }
        }
*/

         fun countDownStartForAdaptors2(matchStartMillis: Long, listeners: OnMatchTimerStarted) {

            val currentTime = System.currentTimeMillis()
            if (currentTime > matchStartMillis) {
               listeners.onTimeFinished()
            } else {
                val timerToRun = matchStartMillis - currentTime
                val countdown_timer = object : CountDownTimer(timerToRun, 1000) {
                    override fun onFinish() {
                        listeners.onTimeFinished()
                    }

                    override fun onTick(millisUntilFinished: Long) {
                        // Used for formatting digit to be in 2 digits only
                        val f: NumberFormat = DecimalFormat("00")
                        val hour = (millisUntilFinished / 3600000) % 24
                        val min = (millisUntilFinished / 60000) % 60
                        val sec = (millisUntilFinished / 1000) % 60
                        val timeRemains =
                            (f.format(hour) + ":" + f.format(min) + ":" + f.format(sec));
                        listeners.onTicks(timeRemains)
                    }
                }
                countdown_timer!!.start()
            }
        }


        fun sendEventLogs(
            context: Context,
            match_id: String,
            contest_id: String,
            user_id: String,
            team_id: Int,
            userInfo: justkhelo.cricks.models.UserInfo,
            eventName: String
        ) {
            val jsonRequest = JsonObject()
            jsonRequest.addProperty("event_name", eventName)
            jsonRequest.addProperty("match_id", match_id)
            jsonRequest.addProperty("contest_id", contest_id)
            jsonRequest.addProperty("team_id", team_id)
            jsonRequest.addProperty("user_id", user_id)
            jsonRequest.addProperty("device_id", MyPreferences.getDeviceToken(context)!!)

            val gson = Gson()
            val userInfoString: String = gson.toJson(userInfo).toString()
            val deviceDetailsString: String = gson.toJson(
                HardwareInfoManager(context).collectData(
                    MyPreferences.getDeviceToken(
                        context
                    )!!
                )
            ).toString()
            val userInfoJson: JsonObject = JsonParser().parse(userInfoString).asJsonObject
            val deviceDetailsJson: JsonObject = JsonParser().parse(deviceDetailsString).asJsonObject

            jsonRequest.add("user_info", userInfoJson)
            jsonRequest.add("deviceDetails", deviceDetailsJson)

            WebServiceClient(context).client.create(IApiMethod::class.java)
                .sendEventLogs(jsonRequest)
                .enqueue(object : Callback<UsersPostDBResponse?> {
                    override fun onFailure(call: Call<UsersPostDBResponse?>?, t: Throwable?) {

                    }

                    override fun onResponse(
                        call: Call<UsersPostDBResponse?>?,
                        response: Response<UsersPostDBResponse?>?
                    ) {
                    }
                })
        }
    }

    object PAYTM {
        const val host = "https://securegw.paytm.in/"
        const val callBackUrl = host + "theia/paytmCallback?ORDER_ID="
        const val PaymentUrl = host + "theia/api/v1/showPaymentPage"
    }
}