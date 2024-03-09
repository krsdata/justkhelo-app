package justkhelo.cricks.models

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class RoleTypeModel(
    @SerializedName("pid")
    @Expose
    var playerId: Int = 0,

    @SerializedName("name")
    @Expose
    var playerName: String = ""
) : Serializable, Cloneable
