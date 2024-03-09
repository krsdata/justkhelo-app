package justkhelo.cricks.models

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.io.Serializable


data class PlayerModels(

    @SerializedName("bat")
    @Expose
    var batsmen: ArrayList<PlayersInfoModel>? = null,

    @SerializedName("bowl")
    @Expose
    var bowlers: ArrayList<PlayersInfoModel>? = null,

    @SerializedName("all")
    @Expose
    var allRounders: ArrayList<PlayersInfoModel>? = null,

    @SerializedName("wkbat")
    @Expose
    var wicketKeeperBatsMan: ArrayList<PlayersInfoModel>? = null,

    @SerializedName("wk")
    @Expose
    var wicketKeepers: ArrayList<PlayersInfoModel>? = null
) : Serializable, Cloneable
