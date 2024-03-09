package justkhelo.cricks.models

import java.io.Serializable

data class MoreFragmentModel(

    var viewType: Int = 0,
    var webDataModel: ArrayList<WebDataModel>? = null,
    var localDataModel: ArrayList<MoreOptionsModel>? = null

) : Serializable