package justkhelo.cricks.models

import justkhelo.cricks.utils.HardwareInfo
import java.io.Serializable

data class DocumentsModel(

    var user_id: String = "",
    var documentType: String = "",

    var panCardName: String = "",
    var panCardNumber: String = "",
    var pancardDocumentUrl: String = "",
    var deviceDetails: HardwareInfo? = null,
    var aadharCardName: String = "",
    var aadharCardNumber: String = "",
    var aadharCardDocumentUrlFront: String = "",
    var aadharCardDocumentUrlBack: String = "",

    var bankName: String = "",
    var accountHolderName: String = "",
    var accountNumber: String = "",
    var ifscCode: String = "",
    var accountType: String = "",
    var bankPassbookUrl: String = "",
    var paytmNumber: String = "",
    var upi_id: String = "",
    var system_token: String = ""

) : Serializable, Cloneable {
    public override fun clone(): DocumentsModel {
        return super.clone() as DocumentsModel
    }
}