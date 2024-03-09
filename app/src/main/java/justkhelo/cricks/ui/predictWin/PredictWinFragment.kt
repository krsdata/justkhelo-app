package justkhelo.cricks.ui.predictWin

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.edify.atrist.listener.PredictionCallback
import com.google.gson.JsonObject
import justkhelo.cricks.AddMoneyActivity
import justkhelo.cricks.NinjaApplication
import justkhelo.cricks.R
import justkhelo.cricks.UpdatePayment
import justkhelo.cricks.adaptors.PredictBannerAdapter
import justkhelo.cricks.adaptors.PredictMatchesAdapter
import justkhelo.cricks.adaptors.QuestionAnswerAdapter
import justkhelo.cricks.databinding.FragmentPredictWinBinding
import justkhelo.cricks.models.*
import justkhelo.cricks.network.IApiMethod
import justkhelo.cricks.network.WebServiceClient
import justkhelo.cricks.utils.CustomeProgressDialog
import justkhelo.cricks.utils.MyPreferences
import justkhelo.cricks.utils.MyUtils
import org.json.JSONObject
import pl.pzienowicz.autoscrollviewpager.AutoScrollViewPager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.DecimalFormat
import java.text.NumberFormat

class PredictWinFragment : Fragment(), PredictMatchesAdapter.MatchClick, PredictionCallback {
    private lateinit var updatePayment: UpdatePayment
    private val TAG = "PredictWinFragment"
    private var countdown_timer: CountDownTimer? = null
    private val questionList: ArrayList<QuestionModel> = ArrayList()
    private var mBinding: FragmentPredictWinBinding? = null
    private var matchesList: ArrayList<PredictData>? = ArrayList()
    private lateinit var matchAdapter: PredictMatchesAdapter
    private lateinit var questionAdapter: QuestionAnswerAdapter
    lateinit var customeProgressDialog: CustomeProgressDialog
    lateinit var selectedAnswer: AnswerModel
    lateinit var selectedQuestion: QuestionModel
    var walletAmount: Double = 0.0
    var bonusAmount: Double = 0.0
    lateinit var startForResult: ActivityResultLauncher<Intent>
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        customeProgressDialog = CustomeProgressDialog(activity)
        mBinding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_predict_win, container, false
        )
        getPredictData()
        initViews()
        return mBinding!!.root
    }


    private fun initViews() {
        mBinding!!.addMoney.setOnClickListener {
            activity?.startActivity(Intent(activity, AddMoneyActivity::class.java))
        }
        mBinding!!.howToPredict.setOnClickListener {
            val dialogBuilder = AlertDialog.Builder(requireContext(), R.style.MyDialog)
            val dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.fragment_how_to_predict, null, false)
            dialogBuilder.setView(dialogView)
            val dialog = dialogBuilder.create()
            dialogView.findViewById<TextView>(R.id.ok)
                .setOnClickListener {
                    dialog.dismiss()
                }
            dialogView.findViewById<ImageView>(R.id.img_close)
                .setOnClickListener {
                    dialog.dismiss()
                }

            dialog.create()
            dialog.show()
        }
        getSliderData()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        startForResult =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult())
            { result: ActivityResult ->
                Log.d("just checking", "onresult")
                if (result.resultCode == Activity.RESULT_OK) {
                    //  you will get result here in result.data
                    Log.d(
                        TAG,
                        "Result from payment activity found with OK, selectedAns: " + selectedAnswer
                    )
                    sendPredictAnswer(
                        questionAdapter.selectedMatch?.matchId!!,
                        selectedAnswer.answer_id.toString(),
                        selectedQuestion.question_id,
                        selectedQuestion.Payper,
                        questionAdapter.selectedMatch!!.mainMatchId
                    )


                } else {
                    updatePayment.onUpdatePayment()
                    Log.d(
                        TAG,
                        "Result from payment activity found with non OK, selectedAns: " + selectedAnswer
                    )
                }
            }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (!MyUtils.isConnectedWithInternet(requireContext())) {
            MyUtils.showToast(activity as AppCompatActivity, "No Internet connection found")
        } else {
            Log.e(TAG, "onCreate called.")
            initAdapters()
            initWallet()
            onAttach(requireContext())
        }
    }

    override fun onResume() {
        super.onResume()
        initWallet()
    }

    private fun getSliderData() {
        WebServiceClient(requireActivity()).client.create(IApiMethod::class.java)
            .getQuizBanner()
            .enqueue(object : Callback<ImageSliderResponseModel?> {
                override fun onResponse(
                    call: Call<ImageSliderResponseModel?>,
                    response: Response<ImageSliderResponseModel?>
                ) {
                    val res = response.body()
                    var matchBanners: ArrayList<ImageSliderData>
                    if (res!!.status) {
                        matchBanners = res.data
                        val predictBannerAdapter = PredictBannerAdapter(
                            context!!,
                            matchBanners
                        )
                        mBinding!!.banner.adapter = predictBannerAdapter
                        predictBannerAdapter.notifyDataSetChanged()

                        if (matchBanners.size == 1) {
                            mBinding!!.banner.stopAutoScroll()
                        } else {
                            mBinding!!.banner.startAutoScroll()
                        }

                        mBinding!!.banner.setInterval(5000)
                        mBinding!!.banner.setDirection(AutoScrollViewPager.Direction.RIGHT)
                        mBinding!!.banner.setCycle(true)
                        mBinding!!.banner.setBorderAnimation(true)
                        mBinding!!.banner.visibility = View.VISIBLE


                    } else {
                        Log.d(TAG, response.message())
                    }
                }

                override fun onFailure(call: Call<ImageSliderResponseModel?>, t: Throwable) {
                    Log.d(TAG, t.message.toString())
                }
            })
    }

    private fun getPredictData() {
        customeProgressDialog.show()
        WebServiceClient(requireActivity()).client.create(IApiMethod::class.java)
            .getPrediction()
            .enqueue(object : Callback<PredictModel?> {
                override fun onResponse(
                    call: Call<PredictModel?>,
                    response: Response<PredictModel?>
                ) {
                    customeProgressDialog.dismiss()
                    if (!response.body()?.predictDataModel?.values.isNullOrEmpty()) {
                        mBinding!!.predictWin.visibility = View.VISIBLE
                        mBinding!!.txtNoMatches.visibility = View.GONE
                        matchesList!!.clear()
                        for (i in response.body()?.predictDataModel?.values!!) {
                            val matchStartMillis = MyUtils.parseDate(i.match_start_time)
                            if (System.currentTimeMillis() < matchStartMillis) {
                                matchesList!!.add(i)
                            }
                        }
                        matchAdapter.notifyDataSetChanged()

                    }
                    else {
                        mBinding!!.predictWin.visibility = View.GONE
                        mBinding!!.txtNoMatches.visibility = View.VISIBLE
                    }

                }

                override fun onFailure(call: Call<PredictModel?>, t: Throwable) {
                    customeProgressDialog.dismiss()
                }

            })
    }

    private fun initAdapters() {
        mBinding!!.recyclerMatches.layoutManager =
            LinearLayoutManager(activity, RecyclerView.HORIZONTAL, false)
        matchAdapter =
            PredictMatchesAdapter(matchesList!!, requireContext(), this, requireActivity())
        mBinding!!.recyclerMatches.adapter = matchAdapter
        mBinding!!.recyclerQustionAnswer.layoutManager =
            LinearLayoutManager(activity, RecyclerView.VERTICAL, false)
        questionAdapter = QuestionAnswerAdapter(
            questionList,
            null,
            this
        )
        mBinding!!.recyclerQustionAnswer.adapter = questionAdapter
    }

    private fun initWallet() {
        val walletInfo = (requireActivity().applicationContext as NinjaApplication).walletInfo
        walletAmount = walletInfo.walletAmount
        bonusAmount = walletInfo.bonusAmount
    }

    override fun onMatchClick(position: Int) {
        questionList.clear()
        stopTimer()
        if (!matchesList?.get(position)?.questions?.values.isNullOrEmpty()) {
            for (i in matchesList?.get(position)?.questions?.values!!) {
                questionList.add(i)
            }
            questionAdapter.selectedMatch = matchesList!![position]
            val matchStartMillis = MyUtils.parseDate(matchesList!![position].match_start_time)
            startTimer(matchStartMillis)
        }
        questionAdapter.notifyDataSetChanged()
    }

    private fun startTimer(matchStartMillis: Long) {
        stopTimer()
        val currentTime = System.currentTimeMillis()
        if (currentTime > matchStartMillis) {
            //Match is already started
            mBinding!!.timeRemains.text = "No matches"
            mBinding!!.txtRemaining.visibility = View.GONE
        } else {
            val timerToRun = matchStartMillis - currentTime
            countdown_timer = object : CountDownTimer(timerToRun, 1000) {
                override fun onFinish() {
                    mBinding!!.timeRemains.text = "No matches"
                    mBinding!!.txtRemaining.visibility = View.GONE
                }

                override fun onTick(millisUntilFinished: Long) {
                    // Used for formatting digit to be in 2 digits only
                    val f: NumberFormat = DecimalFormat("00")
                    val hour = (millisUntilFinished / 3600000) % 24
                    val min = (millisUntilFinished / 60000) % 60
                    val sec = (millisUntilFinished / 1000) % 60

                    mBinding!!.timeRemains.text =
                        (f.format(hour) + ":" + f.format(min) + ":" + f.format(sec));
                    mBinding!!.txtRemaining.visibility = View.VISIBLE

                }
            }
            countdown_timer!!.start()
        }
    }

    private fun stopTimer() {
        if (countdown_timer != null) {
            countdown_timer!!.cancel()
        }
    }

    override fun onAnswerSelected(position: Int, selectedAnswerId: String) {
        Log.d(TAG, "selectedAnswerId: " + selectedAnswerId)

        selectedQuestion = questionList[position]
        Log.d(TAG, "selectedQuestion: " + selectedQuestion.toString())

        questionList[position].answers[selectedAnswerId]!!.isChecked = false
        mBinding!!.recyclerQustionAnswer.post { questionAdapter.notifyItemChanged(position) }

        selectedAnswer = questionList[position].answers[selectedAnswerId]!!
        val dialogBuilder = AlertDialog.Builder(requireContext(), R.style.MyDialog)
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.fragment_join_contest_confirmation, null, false)
        dialogBuilder.setView(dialogView)
        dialogView.findViewById<TextView>(R.id.entry_fees).text = questionList[position].Payper
        dialogView.findViewById<TextView>(R.id.usable_topay).text = questionList[position].Payper
        val dialog = dialogBuilder.create()

        dialogView.findViewById<ImageView>(R.id.img_close)
            .setOnClickListener(object : View.OnClickListener {
                override fun onClick(p0: View?) {
                    selectedAnswer.isChecked = false
                    questionAdapter.notifyItemChanged(position)
                    dialog.dismiss()
                }
            })

        dialogView.findViewById<TextView>(R.id.join_contest)
            .setOnClickListener(object : View.OnClickListener {
                override fun onClick(p0: View?) {
                    dialog.dismiss()
                    if (questionList[position].Payper.toDouble() > walletAmount) {
                        Log.d(TAG, "${questionList[position].Payper.toDouble()} > ${walletAmount}")
                        val intent = Intent(requireContext(), AddMoneyActivity::class.java)
                        intent.putExtra("answer_id", selectedAnswerId)
                        intent.putExtra(
                            "question_id",
                            questionList[position].question_id
                        )
                        intent.putExtra(
                            "match_id",
                            questionAdapter.selectedMatch?.matchId.toString()
                        )
                        Log.d(
                            TAG,
                            "Difference amount: ${(questionList[position].Payper.toDouble() - walletAmount).toInt()}"
                        )
                        intent.putExtra(
                            AddMoneyActivity.ADD_EXTRA_AMOUNT,
                            (questionList[position].Payper.toDouble() - walletAmount).toInt()
                        )
                        startForResult.launch(intent)
                    } else {
                        sendPredictAnswer(
                            questionAdapter.selectedMatch?.matchId!!,
                            selectedAnswerId,
                            questionList[position].question_id,
                            questionList[position].Payper,
                            questionAdapter.selectedMatch!!.mainMatchId
                        )
                        // placeOrders(totalEntryFees, actualPayable, discountFromBonusAmount)
                    }
                }
            })
        dialog.show()
    }

    private fun sendPredictAnswer(
        matchId: Int,
        answerId: String,
        questionId: Int,
        amount: String,
        mainMatchId: Int
    ) {
        if (!MyUtils.isConnectedWithInternet(requireActivity())) {
            MyUtils.showToast(requireContext() as AppCompatActivity, "No Internet connection found")
            return
        }
        customeProgressDialog.show()
        val jsonRequest = JsonObject()
        jsonRequest.addProperty("user_id", MyPreferences.getUserID(requireContext())!!)
        jsonRequest.addProperty("match_id", matchId)
        jsonRequest.addProperty("answer_id", answerId)
        jsonRequest.addProperty("question_id", questionId)
        jsonRequest.addProperty("amount", amount)
        jsonRequest.addProperty("main_match_id", mainMatchId)
        //   jsonRequest.addProperty("transaction_id", "transactionId")
        WebServiceClient(requireContext()).client.create(IApiMethod::class.java)
            .joinQuizContest(jsonRequest)
            .enqueue(object : Callback<JsonObject?> {
                override fun onResponse(call: Call<JsonObject?>, response: Response<JsonObject?>) {
                    val res = JSONObject(response.body().toString())
                    //  customeProgressDialog.dismiss()
                    if (res.getBoolean("status")) {
                        customeProgressDialog.dismiss()
                        Toast.makeText(
                            context,
                            "You have successfully participated in this contest.",
                            Toast.LENGTH_SHORT
                        ).show()
                        initWallet()
                        customeProgressDialog.dismiss()
                        for (key in selectedQuestion.answers.keys) {
                            selectedQuestion.answers[key]?.isChecked = false
                        }
                        selectedQuestion.answers[selectedAnswer.answer_id.toString()]?.isChecked =
                            true
                        questionAdapter.notifyItemChanged(questionList.indexOf(selectedQuestion))
                        updatePayment.onUpdatePayment()
                    } else {
                        customeProgressDialog.dismiss()
                        Toast.makeText(context, "something went wrong", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<JsonObject?>, t: Throwable) {
                    customeProgressDialog.dismiss()
                    Log.d("updatePredictContest", t.message.toString())
                    updatePayment.onUpdatePayment()
                }

            })
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is UpdatePayment) {
            updatePayment = context as UpdatePayment
        } else {
            throw RuntimeException("$context must implement OnFragmentInteractionListener")
        }
    }
}

