package justkhelo.cricks

import android.graphics.Bitmap
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import com.google.gson.JsonObject
import justkhelo.cricks.databinding.ActivityChangePasswordBinding
import justkhelo.cricks.models.UsersPostDBResponse
import justkhelo.cricks.network.IApiMethod
import justkhelo.cricks.network.WebServiceClient
import justkhelo.cricks.ui.BaseActivity
import justkhelo.cricks.utils.CustomeProgressDialog
import justkhelo.cricks.utils.MyPreferences
import justkhelo.cricks.utils.MyUtils
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class ChangePasswordActivity : BaseActivity() {

    private var mBinding: ActivityChangePasswordBinding? = null
    private var photoUrl: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        userInfo = (application as NinjaApplication).userInformations
        customeProgressDialog = CustomeProgressDialog(this)
        mBinding = DataBindingUtil.setContentView(
            this,
            R.layout.activity_change_password
        )
        mBinding!!.toolbar.title = "Change Password"
        mBinding!!.toolbar.setTitleTextColor(resources.getColor(R.color.white))
        mBinding!!.toolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp)
        setSupportActionBar(mBinding!!.toolbar)
        mBinding!!.toolbar.setNavigationOnClickListener(View.OnClickListener {
            finish()
        })

        mBinding!!.btnUpdateProfile.setOnClickListener(View.OnClickListener {

            updateProfile()
        })


    }

    override fun onBitmapSelected(bitmap: Bitmap) {
        TODO("Not yet implemented")
    }

    override fun onUploadedImageUrl(url: String) {
        TODO("Not yet implemented")
    }


    private fun updateProfile() {
        var currentPassword = mBinding!!.editCurrentPassword.text.toString()
        var editNewPassword = mBinding!!.editNewPassword.text.toString()
        var editConfirmNewPassword = mBinding!!.editConfirmPassword.text.toString()

        if (TextUtils.isEmpty(currentPassword)) {
            MyUtils.showToast(this@ChangePasswordActivity, "Please enter current password")
            return
        }
        if (currentPassword.length < 4) {
            MyUtils.showToast(
                this@ChangePasswordActivity,
                "Password cannot be less than 4 character"
            )
            return
        } else if (TextUtils.isEmpty(editNewPassword)) {
            MyUtils.showToast(this@ChangePasswordActivity, "Please enter new password")
            return
        } else if (editNewPassword.length < 8) {
            MyUtils.showToast(
                this@ChangePasswordActivity,
                "New Password cannot be less than 8 character"
            )
            return
        } else if (!editNewPassword.equals(editConfirmNewPassword)) {
            MyUtils.showToast(this@ChangePasswordActivity, "Both password does not matched")
            return
        }
        mBinding!!.progressBar.visibility = View.VISIBLE
        customeProgressDialog.show()
        /*var models = RequestModel()
        models.user_id = MyPreferences.getUserID(this)!!
        models.current_password = currentPassword
        models.new_password = editNewPassword*/

        val jsonRequest = JsonObject()
        jsonRequest.addProperty("user_id", MyPreferences.getUserID(this)!!)
        jsonRequest.addProperty("system_token", MyPreferences.getSystemToken(this)!!)
        jsonRequest.addProperty("current_password", currentPassword)
        jsonRequest.addProperty("new_password", editNewPassword)

        WebServiceClient(this).client.create(IApiMethod::class.java).changePassword(jsonRequest)
            .enqueue(object : Callback<UsersPostDBResponse?> {
                override fun onFailure(call: Call<UsersPostDBResponse?>?, t: Throwable?) {
                    customeProgressDialog.dismiss()
                    MyUtils.showMessage(this@ChangePasswordActivity, t!!.message)
                }

                override fun onResponse(
                    call: Call<UsersPostDBResponse?>?,
                    response: Response<UsersPostDBResponse?>?
                ) {
                    mBinding!!.progressBar.visibility = View.GONE
                    customeProgressDialog.dismiss()
                    val res = response!!.body()
                    if (res != null) {
                        if (res.status) {
                            Toast.makeText(
                                this@ChangePasswordActivity,
                                "Profile updated successfully",
                                Toast.LENGTH_LONG
                            ).show()
                            finish()
                        } else {
                            MyUtils.showMessage(this@ChangePasswordActivity, res.message)
                        }
                    }
                }
            })
    }
}