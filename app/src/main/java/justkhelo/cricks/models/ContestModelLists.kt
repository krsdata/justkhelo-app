package justkhelo.cricks.models

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.io.Serializable


data class ContestModelLists(

    @SerializedName("contestId")
    @Expose
    var id: Int = 0,

    @SerializedName("isCancelled")
    @Expose
    var isContestCancelled: Boolean = false,

    @SerializedName("match_id")
    @Expose
    var matchId: String = "",

    @SerializedName("totalWinningPrize")
    @Expose
    var totalWinningPrize: String = "",

    @SerializedName("entryFees")
    @Expose
    var entryFees: String = "",

    @SerializedName("match_status")
    @Expose
    var matchStatus: String = "",

    @SerializedName("totalSpots")
    @Expose
    var totalSpots: Int = 0,

    @SerializedName("filledSpots")
    @Expose
    var filledSpots: Int = 0,

    @SerializedName("firstPrice")
    @Expose
    var firstPrice: String = "",

    @SerializedName("winnerPercentage")
    @Expose
    var winnerPercentage: String = "",

    @SerializedName("winnerCount")
    @Expose
    var winnerCounts: String? = "",

    @SerializedName("maxAllowedTeam")
    @Expose
    var maxAllowedTeam: Int = 0,

    @SerializedName("usable_bonus")
    @Expose
    var usableBonus: String = "",

    @SerializedName("bonus_contest")
    @Expose
    var isBonusContest: Boolean = false,

    @SerializedName("cancellation")
    @Expose
    var cancellation: Boolean = false,

    @SerializedName("joinedTeams")
    @Expose
    var joinedTeams: ArrayList<MyTeamModels>? = null,

    @SerializedName("gift_url")
    @Expose
    var giftUrl: String = "",

    @SerializedName("is_leaderboard")
    @Expose
    var is_dashboard: Boolean = false,

    @SerializedName("discounted_price")
    @Expose
    var discounted_price: String = "",

    @SerializedName("extra_cash_usable")
    @Expose
    var extra_cash_usable: String = "",

    @SerializedName("offer_end_at")
    @Expose
    var offer_end_at: String = ""

) : Serializable, Cloneable