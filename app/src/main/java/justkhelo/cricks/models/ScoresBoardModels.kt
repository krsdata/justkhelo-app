package justkhelo.cricks.models

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class ScoresBoardModels(
    @SerializedName("status")
    @Expose
    var matchStatus: Int = 0,

    @SerializedName("status_note")
    @Expose
    var statusNote: String = "",

    @SerializedName("teama")
    @Expose
    var teama: TeamAInfo? = null,

    @SerializedName("teamb")
    @Expose
    var teamb: TeamBInfo? = null
): Serializable
