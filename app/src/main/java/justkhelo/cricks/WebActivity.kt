package justkhelo.cricks

import android.content.Context
import android.os.Bundle
import android.transition.Slide
import android.view.Gravity
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import justkhelo.cricks.databinding.WebviewBinding
import justkhelo.cricks.utils.CustomeProgressDialog


class WebActivity : AppCompatActivity(), SwipeRefreshLayout.OnRefreshListener {
    private var mBinding: WebviewBinding? = null
    var mContext: Context? = null
    private var URL: String? = null
    private var userId: String? = ""

    companion object {
        var TAG: String = WebActivity::class.java.simpleName
        const val KEY_TITLE: String = "key_title"
        const val KEY_URL: String = "key_url"
        const val USER_ID: String = "user_id"
    }

    public override fun onCreate(savedInstanceState: Bundle?) {
        setEnterAnimations()
        super.onCreate(savedInstanceState)
        mBinding = DataBindingUtil.setContentView(
            this,
            R.layout.webview
        )

        mContext = this

        mBinding!!.toolbar.title = intent.getStringExtra(KEY_TITLE)
        mBinding!!.toolbar.setTitleTextColor(resources.getColor(R.color.white))
        mBinding!!.toolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp)
        setSupportActionBar(mBinding!!.toolbar)
        mBinding!!.toolbar.setNavigationOnClickListener(View.OnClickListener {
            finish()
        })

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            mBinding!!.refreshLayout.setColorSchemeColors(
                mContext!!.resources.getColor(
                    R.color.colorPrimary,
                    null
                )
            )
        } else {
            mBinding!!.refreshLayout.setColorSchemeColors(mContext!!.resources.getColor(R.color.colorPrimary))
        }
        mBinding!!.refreshLayout.setOnRefreshListener(this)

        mBinding!!.progressBar.visibility = View.VISIBLE

        URL = intent.getStringExtra(KEY_URL)
        userId = intent.getStringExtra(USER_ID)
        loadURL()
    }

    private fun setEnterAnimations() {
        val slide = Slide()
        slide.slideEdge = Gravity.BOTTOM
        slide.duration = 400
        slide.interpolator = DecelerateInterpolator()
        window.exitTransition = slide
        window.enterTransition = slide
    }

    private fun loadURL() {
        mBinding!!.webBody.webViewClient = MyWebViewClient()
        mBinding!!.webBody.settings.javaScriptEnabled = true
        if (userId != null && !userId.equals("")) {
            mBinding!!.webBody.loadUrl(URL + userId)
        } else {
            mBinding!!.webBody.loadUrl(URL!!)
        }
    }

    private inner class MyWebViewClient : WebViewClient() {

        override fun shouldOverrideUrlLoading(
            view: WebView,
            url: String
        ): Boolean {
            view.loadUrl(url)
            return true
        }

        override fun onPageFinished(view: WebView, url: String) {
            if (mBinding!!.refreshLayout.isRefreshing) {
                mBinding!!.refreshLayout.isRefreshing = false
            }
            if (mBinding!!.progressBar.isVisible) {
                mBinding!!.progressBar.visibility = View.GONE
            }
        }
    }

    override fun onRefresh() {
        loadURL()
    }
}