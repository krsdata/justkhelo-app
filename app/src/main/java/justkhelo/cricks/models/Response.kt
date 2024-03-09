package justkhelo.cricks.models

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class Response(

    @SerializedName("total_team_joined")
    @Expose
    var totalTeamJoined: Int? = null,

    @SerializedName("total_match_played")
    @Expose
    var totalMatchPlayed: String? = "",

    @SerializedName("total_contest_joined")
    @Expose
    var totalContestJoined: String? = "",

    @SerializedName("total_unique_contest")
    @Expose
    var totalUniqueContest: Int? = null,

    @SerializedName("total_match_win")
    @Expose
    var totalMatchWin: String? = "",

    @SerializedName("total_winning_amount")
    @Expose
    var totalWinningAmount: String? = "",

    @SerializedName("matchdata")
    @Expose
    var matchdatalist: List<MatchesModels>? = null,

    @SerializedName("matchcontests")
    @Expose
    var matchContestlist: List<ContestsParentModels>? = null,

    @SerializedName("prizeBreakup")
    @Expose
    var prizeBreakUpModelsList: List<PrizeBreakUpModels>? = null,

    @SerializedName("players")
    @Expose
    var playersList: PlayerModels? = null,

    @SerializedName("myteam")
    @Expose
    var myTeamList: List<MyTeamModels>? = null,

    @SerializedName("my_joined_contest")
    @Expose
    var myJoinedContest: List<ContestModelLists>? = null,

    @SerializedName("myjoinedTeams")
    @Expose
    var myjoinedTeams: ArrayList<MyTeamModels>? = null,

    @SerializedName("myjoinedContest")
    @Expose
    var joinedContestDetails: ArrayList<ContestModelLists>? = null,

    @SerializedName("player_points")
    @Expose
    var playerPointsList: ArrayList<PlayersInfoModel>? = null,

    @SerializedName("pan_number")
    @Expose
    var panNumber: String = "",

    @SerializedName("pan_name")
    @Expose
    var panName: String = "",

    @SerializedName("pan_url")
    @Expose
    var panUrl: String = "",

    @SerializedName("bank_name")
    @Expose
    var bankName: String = "",

    @SerializedName("account_name")
    @Expose
    var accountName: String = "",

    @SerializedName("account_number")
    @Expose
    var accountNumber: String = "",

    @SerializedName("ifsc_code")
    @Expose
    var IFSCCode: String = "",

    @SerializedName("account_type")
    @Expose
    var accountType: String = "",

    @SerializedName("bank_url")
    @Expose
    var bankUrl: String = "",

    @SerializedName("paytm_number")
    @Expose
    var paytmNumber: String = "",

    @SerializedName("upi_id")
    @Expose
    var UPIId: String = "",

    @SerializedName("total_my_deposit")
    @Expose
    var totalMyDeposit: String = "0.0",

    @SerializedName("total_my_withdrawal")
    @Expose
    var totalMyWithdrawal: String = "0.0"

) : Serializable
