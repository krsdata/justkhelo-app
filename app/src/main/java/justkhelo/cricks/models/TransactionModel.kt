package justkhelo.cricks.models

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.io.Serializable


data class TransactionModel(


    @SerializedName("amount")
    @Expose
    val depositAmount: String = "",

    @SerializedName("payment_mode")
    @Expose
    var paymentMode: String = "",

    @SerializedName("payment_status")
    @Expose
    var paymentStatus: String = "",

    @SerializedName("transaction_id")
    @Expose
    var transactionId: String = "",

    @SerializedName("payment_type")
    @Expose
    var paymentType: String = "",

    @SerializedName("debit_credit_status")
    @Expose
    var debitCreditStatus: String = "",

    @SerializedName("date")
    @Expose
    var createdDate: String = ""
) : Serializable, Cloneable