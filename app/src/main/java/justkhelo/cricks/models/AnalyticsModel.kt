package justkhelo.cricks.models

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class AnalyticsModel(

    @SerializedName("selection")
    @Expose
    var selectionPc: Double = 0.0,

    @SerializedName("captain")
    @Expose
    var captainPc: Double = 0.0,

    @SerializedName("vice_captain")
    @Expose
    var viceCaptainPc: Double = 0.0,

    @SerializedName("trump")
    @Expose
    var trumpPc: Double = 0.0
) : Serializable, Cloneable
