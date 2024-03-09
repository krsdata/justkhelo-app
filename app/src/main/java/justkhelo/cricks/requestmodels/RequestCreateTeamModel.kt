package justkhelo.cricks.requestmodels

import com.google.gson.annotations.SerializedName
import java.io.Serializable


data class RequestCreateTeamModel(
    @SerializedName("user_id")
    var user_id: String = "",
    @SerializedName("token")
    var token: String = "",
    @SerializedName("match_id")
    var match_id: String = "",
    @SerializedName("create_team_id")
    var create_team_id: Int = 0,
    @SerializedName("trump")
    var trump: Int = 0,
    @SerializedName("captain")
    var captain: Int = 0,
    @SerializedName("vice_captain")
    var vice_captain: Int = 0,
    @SerializedName("teams")
    var teams: ArrayList<Int>? = null,
    @SerializedName("team_id")
    var team_id: ArrayList<Int>? = null,
    @SerializedName("system_token")
    var system_token: String = ""
) : Serializable