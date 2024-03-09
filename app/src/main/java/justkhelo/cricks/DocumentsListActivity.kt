package justkhelo.cricks

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import androidx.databinding.DataBindingUtil
import com.bumptech.glide.Glide
import com.google.gson.JsonObject
import justkhelo.cricks.databinding.ActivityViewDocumentBinding
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


class DocumentsListActivity : BaseActivity() {

    private var mBinding: ActivityViewDocumentBinding? = null
    var mContext: Context? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        customeProgressDialog = CustomeProgressDialog(this)
        userInfo = (application as NinjaApplication).userInformations
        mBinding = DataBindingUtil.setContentView(
            this,
            R.layout.activity_view_document
        )
        mContext = this
        mBinding!!.toolbar.title = "My Documents"
        mBinding!!.toolbar.setTitleTextColor(resources.getColor(R.color.white))
        mBinding!!.toolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp)
        setSupportActionBar(mBinding!!.toolbar)
        mBinding!!.toolbar.setNavigationOnClickListener(View.OnClickListener {
            finish()
        })

        mBinding!!.contactUs.setOnClickListener(View.OnClickListener {
            val intent = Intent(mContext!!, SupportActivity::class.java)
            startActivity(intent)
        })
        getDocumentsList()
    }

    private fun getDocumentsList() {
        if (!MyUtils.isConnectedWithInternet(this)) {
            MyUtils.showToast(this, "No Internet connection found")
            return
        }
        customeProgressDialog.show()
        val jsonRequest = JsonObject()
        jsonRequest.addProperty("match_id", MyPreferences.getUserID(this)!!)
        jsonRequest.addProperty("player_id", MyPreferences.getSystemToken(this)!!)

        WebServiceClient(this).client.create(IApiMethod::class.java)
            .getApprovedDocuments(jsonRequest)
            .enqueue(object : Callback<UsersPostDBResponse?> {
                override fun onFailure(call: Call<UsersPostDBResponse?>?, t: Throwable?) {
                    customeProgressDialog.dismiss()
                }

                override fun onResponse(
                    call: Call<UsersPostDBResponse?>?,
                    response: Response<UsersPostDBResponse?>?
                ) {
                    customeProgressDialog.dismiss()
                    val res = response!!.body()
                    if (res != null) {
                        if (res.status) {
                            val data = res.responseObject
                            if (data!!.UPIId != null && data.UPIId != "") {
                                mBinding!!.upiIdLayout.visibility = View.VISIBLE
                                mBinding!!.upiIdView.visibility = View.VISIBLE
                                mBinding!!.upiNumber.text = data.UPIId
                            } else {
                                mBinding!!.upiIdLayout.visibility = View.GONE
                                mBinding!!.upiIdView.visibility = View.GONE
                            }

                            mBinding!!.paytmNumber.text = data.paytmNumber

                            mBinding!!.panName.text = data.panName
                            mBinding!!.panNumbers.text = data.panNumber

                            mBinding!!.bankName.text = data.bankName
                            mBinding!!.bankAccountName.text = data.accountName
                            mBinding!!.bankAccountNumber.text = data.accountNumber
                            mBinding!!.bankAccountType.text = data.IFSCCode

                            Glide.with(mContext!!).load(data.bankUrl)
                                .into(mBinding!!.chequeBookImage)
                            Glide.with(mContext!!).load(data.panUrl).into(mBinding!!.imgDocType)

                        } else {
                            if (res.code == 1001) {
                                MyUtils.showMessage(this@DocumentsListActivity, res.message)
                                MyUtils.logoutApp(this@DocumentsListActivity)
                            } else {
                                MyUtils.showToast(
                                    this@DocumentsListActivity,
                                    res.message
                                )
                            }
                        }
                    } else {
                        MyUtils.showToast(
                            this@DocumentsListActivity,
                            "Something went wrong. Please contact to support."
                        )
                    }
                }
            })
    }

    override fun onBitmapSelected(bitmap: Bitmap) {
    }

    override fun onUploadedImageUrl(url: String) {
    }
}