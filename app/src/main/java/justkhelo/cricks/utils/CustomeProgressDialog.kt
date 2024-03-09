package justkhelo.cricks.utils

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import justkhelo.cricks.R

class CustomeProgressDialog(context: Context?) : Dialog(context!!) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window!!.setBackgroundDrawableResource(android.R.color.transparent)
        setContentView(R.layout.dialog_progress)
        setCancelable(false)
        setCanceledOnTouchOutside(false)
    }
}