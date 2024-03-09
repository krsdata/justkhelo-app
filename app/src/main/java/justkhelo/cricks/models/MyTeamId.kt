package justkhelo.cricks.models

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class MyTeamId(
    @SerializedName("team_id")
    @Expose
    var teamId: Int = 0
) : Serializable, Cloneable
