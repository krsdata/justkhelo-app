package justkhelo.cricks.ui.login

import android.app.Dialog
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import com.google.gson.JsonObject
import justkhelo.cricks.R
import justkhelo.cricks.databinding.FragmentForgotPasswordBinding
import justkhelo.cricks.network.IApiMethod
import justkhelo.cricks.network.WebServiceClient
import justkhelo.cricks.models.UsersPostDBResponse
import justkhelo.cricks.utils.CustomeProgressDialog
import justkhelo.cricks.utils.MyPreferences
import justkhelo.cricks.utils.MyUtils
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ForgotPasswordFragment(val emailAddress: String) : DialogFragment() {

    private lateinit var customeProgressDialog: CustomeProgressDialog
    private var mBinding: FragmentForgotPasswordBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.dialog_theme)
    }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mBinding  = DataBindingUtil.inflate(inflater,
            R.layout.fragment_forgot_password, container, false)
        return mBinding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mBinding!!.editEmail.setText(emailAddress)
        customeProgressDialog = CustomeProgressDialog(activity)
        mBinding!!.imgClose.setOnClickListener(View.OnClickListener {
            dismiss()
        })
        mBinding!!.btnForgotpassword.setOnClickListener(View.OnClickListener {
            val emailAddress = mBinding!!.editEmail.text.toString()
            if(TextUtils.isEmpty(emailAddress) || !MyUtils.isEmailValid(emailAddress)){
                MyUtils.showMessage(requireActivity() as AppCompatActivity,"Please enter valid email address")
            }else {
                if (!MyUtils.isConnectedWithInternet(activity as AppCompatActivity)) {
                    MyUtils.showToast(activity as AppCompatActivity, "No Internet connection found")
                } else {
                    customeProgressDialog.show()

                    val jsonRequest = JsonObject()
                    jsonRequest.addProperty("system_token", MyPreferences.getSystemToken(requireActivity())!!)
                    jsonRequest.addProperty("email", emailAddress)

                    WebServiceClient(requireActivity()).client.create(IApiMethod::class.java).forgotPassword(jsonRequest)
                        .enqueue(object : Callback<UsersPostDBResponse?> {
                            override fun onFailure(
                                call: Call<UsersPostDBResponse?>?,
                                t: Throwable?
                            ) {
                                customeProgressDialog.dismiss()
                            }

                            override fun onResponse(
                                call: Call<UsersPostDBResponse?>?,
                                response: Response<UsersPostDBResponse?>?
                            ) {
                                customeProgressDialog.dismiss()
                                val res = response!!.body()
                                if (res != null && res.status) {
                                    MyUtils.showMessage(activity!!, res.message)
                                    dismiss()
                                } else {
                                    if (res != null) {
                                        MyUtils.showMessage(activity!!, res.message)
                                    } else {
                                        MyUtils.showMessage(
                                            activity!!,
                                            "Server not responding, please contact admin"
                                        )
                                    }
                                }

                            }

                        })
                }
            }
        })

    }


    override fun onStart() {
        super.onStart()
        val dialog: Dialog? = dialog
        if (dialog != null) {
            val width = ViewGroup.LayoutParams.MATCH_PARENT
            val height = ViewGroup.LayoutParams.MATCH_PARENT
            dialog.window!!.setLayout(width, height)
        }
    }


}