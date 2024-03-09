package justkhelo.cricks.models

import com.google.gson.annotations.SerializedName

data class ImageSliderResponseModel(
    @SerializedName("code")
    val code: Int,
    @SerializedName("data")
    val data: ArrayList<ImageSliderData>,
    @SerializedName("status")
    val status: Boolean
)

data class ImageSliderData(
    @SerializedName("id")
    val id: Int,
    @SerializedName("title")
    val title: String,
    @SerializedName("url")
    val url: String
)