package justkhelo.cricks.models

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class WalletInfo(


    @SerializedName("wallet_amount")
    @Expose
    var walletAmount: Double = 0.0,

    @SerializedName("bonus_amount")
    @Expose
    var bonusAmount: Double = 0.0,

    @SerializedName("deposit_amount")
    @Expose
    var depositAmount: Double = 0.0,

    @SerializedName("prize_amount")
    @Expose
    var prizeAmount: Double = 0.0,

    @SerializedName("referral_amount")
    @Expose
    var referralAmount: Double = 0.0,

    @SerializedName("is_account_verified")
    @Expose
    var accountStatus: AccountDocumentStatus? = null,

    @SerializedName("refferal_friends_count")
    @Expose
    var refferalCounts: Int = 0,

    @SerializedName("bank_account_verified")
    @Expose
    var bankAccountVerified: Int = 0,

    @SerializedName("min_deposit")
    @Expose
    var minDeposit: Int = 0,

    @SerializedName("pmid")
    @Expose
    var paytmMid: String = "",

    @SerializedName("g_pay")
    @Expose
    var gPay: String = "",

    @SerializedName("call_url")
    @Expose
    var callUrl: String = "",

    @SerializedName("transaction")
    @Expose
    var transactionList: ArrayList<TransactionModel>? = null,

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

    @SerializedName("ninja_point")
    @Expose
    var ninja_point: String = "",

    @SerializedName("bank_withdrawal")
    @Expose
    var bank_withdrawal: Boolean = false,

    @SerializedName("paytm_withdrawal")
    @Expose
    var paytm_withdrawal: Boolean = false,

    @SerializedName("upi_withdrawal")
    @Expose
    var upi_withdrawal: Boolean = false,

    @SerializedName("extra_cash")
    @Expose
    var extraCash: Double = 0.0,

    @SerializedName("min_withdrawal")
    @Expose
    var minWithdrawal: Int = 0

) : Serializable, Cloneable