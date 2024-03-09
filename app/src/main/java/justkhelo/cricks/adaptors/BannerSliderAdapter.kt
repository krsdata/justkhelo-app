package justkhelo.cricks.adaptors

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.drawable.Drawable
import android.util.Log
import android.view.*
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.viewpager.widget.PagerAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import justkhelo.cricks.OfferActivity
import justkhelo.cricks.R
import justkhelo.cricks.models.MatchBannersModel

class BannerSliderAdapter(
    val context: Context,
    private val tradeInfoModels: ArrayList<MatchBannersModel>
) :
    PagerAdapter() {
    var mContext: Context = context
    private var arrayList = tradeInfoModels
    var inflater: LayoutInflater = LayoutInflater.from(mContext)
    lateinit var sdialog: Dialog
    val TAG: String = BannerSliderAdapter::class.java.simpleName

    override fun getCount(): Int {
        return arrayList.size
    }

    override fun getItemPosition(`object`: Any): Int {
        return POSITION_NONE
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as View)
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view === `object`
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        var itemView: View? = null
        val viewHolder: ViewHolder
        if (itemView == null) {
            itemView = inflater.inflate(R.layout.matches_row_banners_inner, container, false)
            viewHolder = ViewHolder(itemView!!)
            itemView.tag = viewHolder
        } else {
            viewHolder = itemView.tag as ViewHolder
        }
        try {
            val objectVal = arrayList[position]
            Glide.with(context)
                .load(objectVal.bannerUrl)
                .placeholder(R.drawable.rectangle_left_top_curve)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(viewHolder.target)

            viewHolder.target.setOnClickListener(ImageClick(position))

        } catch (e: Exception) {
            e.printStackTrace()
        }
        container.addView(itemView)
        return itemView
    }

    internal class ViewHolder(view: View) {
        var target: ImageView = view.findViewById(R.id.image_banner)
    }

    inner class ImageClick(pos: Int) : View.OnClickListener {
        var position: Int = pos
        override fun onClick(v: View?) {
            //showAlert(arrayList[position].bannerUrl)
            val intent = Intent(mContext, OfferActivity::class.java)
            mContext.startActivity(intent)
        }
    }

    private fun showAlert(offerImage: String) {
        sdialog = Dialog(mContext, R.style.MyDialog)
        sdialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        sdialog.window!!.attributes.windowAnimations = R.style.PauseDialogAnimation
        sdialog.setContentView(R.layout.dialog_banner_image)
        sdialog.setCancelable(false)
        sdialog.show()
        val close: ImageView = sdialog.findViewById(R.id.dialog_close)
        val offerImageView: ImageView = sdialog.findViewById(R.id.dialog_offer_image)
        val progressBar: ProgressBar = sdialog.findViewById(R.id.progress_bar)

        close.setOnClickListener {
            Log.e(TAG, "offerImage =======> $offerImage")
            sdialog.dismiss()
        }

        Glide.with(mContext).load(offerImage).listener(object : RequestListener<Drawable?> {
            override fun onLoadFailed(
                e: GlideException?,
                model: Any,
                target: Target<Drawable?>,
                isFirstResource: Boolean
            ): Boolean {
                return false
            }

            override fun onResourceReady(
                resource: Drawable?,
                model: Any?,
                target: Target<Drawable?>?,
                dataSource: com.bumptech.glide.load.DataSource?,
                isFirstResource: Boolean
            ): Boolean {
                progressBar.visibility = View.GONE
                return false
            }
        }).into(offerImageView)

        sdialog.setOnKeyListener(DialogInterface.OnKeyListener { dialog, keyCode, keyEvent ->
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                dialog.dismiss()
            }
            true
        })
    }
}