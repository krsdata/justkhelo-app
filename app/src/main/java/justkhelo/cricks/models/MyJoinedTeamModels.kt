package justkhelo.cricks.models

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.io.Serializable


data class MyJoinedTeamModels(

    var isSelected: Boolean? = false,

    @SerializedName("joined_contest_id")
    @Expose
    var contestId: Int = 0,

    @SerializedName("team_name")
    @Expose
    var teamName: String? = "",

    @SerializedName("createdTeamId")
    @Expose
    var createdteamId: Int = 0,

    @SerializedName("team_won_status")
    @Expose
    var teamWonStatus: String? = "",

    @SerializedName("points")
    @Expose
    var teamPoints: String? = "",

    @SerializedName("rank")
    @Expose
    var teamRanks: String? = "",

    @SerializedName("bat")
    @Expose
    var batsmen: ArrayList<Int>? = null,

    @SerializedName("bowl")
    @Expose
    var bowlers: ArrayList<Int>? = null,

    @SerializedName("all")
    @Expose
    var allRounders: ArrayList<Int>? = null,

    @SerializedName("wkbat")
    @Expose
    var wicketKeeperBatsMan: ArrayList<PlayersInfoModel>? = null,

    @SerializedName("wk")
    @Expose
    var wicketKeepers: ArrayList<Int>? = null,

    @SerializedName("c")
    @Expose
    var captain: RoleTypeModel? = null,

    @SerializedName("vc")
    @Expose
    var viceCaptain: RoleTypeModel? = null,

    @SerializedName("t")
    @Expose
    var trump: RoleTypeModel? = null,

    @SerializedName("created_team")
    @Expose
    var teamId: MyTeamId? = null,

    @SerializedName("team")
    @Expose
    var teamsInfo: ArrayList<TeamModel>? = null
) : Serializable, Cloneable