package justkhelo.cricks.models

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class PrizeBreakUpModels(

    @SerializedName("range")
    @Expose
    val rangeName: String = "",

    @SerializedName("price")
    @Expose
    val winnersPrice: String = "",

    @SerializedName("prize_url")
    @Expose
    val prize_url: String = ""
)