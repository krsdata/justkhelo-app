package justkhelo.cricks.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.gson.JsonObject
import justkhelo.cricks.SplashScreenActivity
import justkhelo.cricks.models.UsersPostDBResponse
import justkhelo.cricks.network.IApiMethod
import justkhelo.cricks.network.WebServiceClient
import justkhelo.cricks.utils.CustomeProgressDialog
import justkhelo.cricks.utils.MyPreferences
import justkhelo.cricks.utils.MyUtils
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

open class BaseFragment : Fragment() {

    var customeProgressDialog: CustomeProgressDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        customeProgressDialog = CustomeProgressDialog(activity)
    }

    fun logoutApp(message: String, boolean: Boolean) {
        if (!MyUtils.isConnectedWithInternet(activity as AppCompatActivity)) {
            MyUtils.showToast(activity as AppCompatActivity, "No Internet connection found")
            return
        }
        genericAlertDialog(message, boolean)
    }


    fun genericAlertDialog(message: String, boolean: Boolean) {
        val builder = AlertDialog.Builder(requireActivity())
        builder.setMessage(message)
        builder.setIcon(android.R.drawable.ic_dialog_alert)
        if (boolean) {
            builder.setNegativeButton("Cancel", null)
        }
        builder.setPositiveButton("OK") { dialogInterface, which ->

            customeProgressDialog!!.show()
            val jsonRequest = JsonObject()
            jsonRequest.addProperty("user_id", MyPreferences.getUserID(requireActivity())!!)
            jsonRequest.addProperty("system_token", MyPreferences.getSystemToken(requireActivity())!!)

            WebServiceClient(requireActivity()).client.create(IApiMethod::class.java)
                .logout(jsonRequest)
                .enqueue(object : Callback<UsersPostDBResponse?> {
                    override fun onFailure(call: Call<UsersPostDBResponse?>?, t: Throwable?) {
                        customeProgressDialog!!.dismiss()
                    }

                    override fun onResponse(
                        call: Call<UsersPostDBResponse?>?,
                        response: Response<UsersPostDBResponse?>?
                    ) {

                        customeProgressDialog!!.dismiss()
                        MyPreferences.clear(requireActivity())
                        val intent = Intent(
                            requireActivity(),
                            SplashScreenActivity::class.java
                        )
                        intent.flags =
                            Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                        activity!!.finish()
                    }

                })
        }
        val alertDialog: AlertDialog = builder.create()
        alertDialog.setCancelable(false)
        alertDialog.setCanceledOnTouchOutside(false)
        alertDialog.show()
    }
}