package justkhelo.cricks.models

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class TeamModel(
    @SerializedName("name")
    @Expose
    var teamName: String = "",

    @SerializedName("count")
    @Expose
    var count: Int = 0
) : Serializable, Cloneable