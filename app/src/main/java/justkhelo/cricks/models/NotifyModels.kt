package justkhelo.cricks.models

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.io.Serializable


data class NotifyModels(

    @SerializedName("title")
    @Expose
    val notificationTitle: String = "",

    @SerializedName("messages")
    @Expose
    val notificationMessages: String = "",

    @SerializedName("created_date")
    @Expose
    val activationDate: String = ""
) : Serializable, Cloneable {
    public override fun clone(): NotifyModels {
        return super.clone() as NotifyModels
    }


}
