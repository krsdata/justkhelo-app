package justkhelo.cricks.models

import java.util.*

data class Contact(
    var id: String? = null,
    var name: String? = null,
    var phoneList: ArrayList<Phone>? = null,
    var Emails: ArrayList<String>? = null,
    var websites: ArrayList<String>? = null)
