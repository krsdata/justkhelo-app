package justkhelo.cricks.models

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.io.Serializable


data class ContestsParentModels(

    @SerializedName("contest_type_id")
    @Expose
    var contestTypeId: String = "",

    @SerializedName("contestTitle")
    @Expose
    var contestTitle: String = "",

    @SerializedName("contestSubTitle")
    @Expose
    var contestSubTitle: String = "",

    @SerializedName("icon_url")
    @Expose
    var icon_url: String = "",

    @SerializedName("contests")
    @Expose
    var allContestsRunning: ArrayList<ContestModelLists>? = null
) : Serializable, Cloneable