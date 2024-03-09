package justkhelo.cricks.models

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class UsersPostDBResponse(

    @SerializedName("status")
    @Expose
    var status: Boolean = false,

    @SerializedName("order_id")
    @Expose
    var orderId: String = "",

    @SerializedName("maintainance")
    @Expose
    var appMaintainance: Boolean = false,

    @SerializedName("session_expired")
    @Expose
    var sessionExpired: Boolean = false,

    @SerializedName("url")
    @Expose
    var updatedApkUrl: String = "",

    @SerializedName("splashScreen")
    @Expose
    var splash: String = "",

    @SerializedName("force_update")
    @Expose
    var forceupdate: Boolean = false,

    @SerializedName("release_note")
    @Expose
    var releaseNote: String = "",

    @SerializedName("action")
    @Expose
    var actionForTeam: Int = 0,

    @SerializedName("team_list")
    @Expose
    var selectedTeamModel: ArrayList<SelectedTeamModels>? = null,

    @SerializedName("notification_list")
    @Expose
    var notificationList: ArrayList<NotifyModels>? = null,

    @SerializedName("system_time")
    @Expose
    var systemTime: Long = 0,

    @SerializedName("total_points")
    @Expose
    var totalPoints: String = "",

    @SerializedName("message")
    @Expose
    var message: String = "",

    @SerializedName("code")
    @Expose
    var code: Int = 0,

    @SerializedName("walletInfo")
    @Expose
    var walletObjects: WalletInfo? = null,

    @SerializedName("transaction_history")
    @Expose
    var transactionHistory: WalletInfo? = null,

    @SerializedName("referal_user")
    @Expose
    var referalUserList: ArrayList<RefferalUsersModel>? = null,

    @SerializedName("response")
    @Expose
    var responseObject: Response? = null,

    @SerializedName("scores")
    @Expose
    var scoresModel: ScoresBoardModels? = null,

    @SerializedName("leaderBoard")
    @Expose
    var leaderBoardList: ArrayList<LeadersBoardModels>? = null,

    @SerializedName("rozar_key")
    @Expose
    var razorPay: String = "",

    @SerializedName("paytm_show")
    @Expose
    var paytm_show: Boolean = false,

    @SerializedName("rozarpay_show")
    @Expose
    var rozarpay_show: Boolean = false,

    @SerializedName("gpay_show")
    @Expose
    var gpay_show: Boolean = false,

    @SerializedName("bank_withdrawal")
    @Expose
    var bank_withdrawal: Boolean = false,

    @SerializedName("paytm_withdrawal")
    @Expose
    var paytm_withdrawal: Boolean = false,

    @SerializedName("upi_withdrawal")
    @Expose
    var upi_withdrawal: Boolean = false,

    @SerializedName("offer_image")
    @Expose
    var offerImage: String = "",

    @SerializedName("total_team")
    @Expose
    var teamCount: String = "",

    @SerializedName("min_withdrawal")
    @Expose
    var minWithdrawal: Int = 0,

    @SerializedName("paytm_withdrawal_btn")
    @Expose
    var paytm_withdrawal_btn:Boolean = false,

) : Serializable, Cloneable