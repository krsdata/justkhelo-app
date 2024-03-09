package justkhelo.cricks.models

import java.io.Serializable

data class WebDataModel(

    var viewType: Int = 0,
    var title: String = "",
    var titleUrl: String = "",
    var iconUrl: String = ""

) : Serializable