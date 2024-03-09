package justkhelo.cricks.adaptors

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.edify.atrist.listener.PredictionCallback
import justkhelo.cricks.AddMoneyActivity
import justkhelo.cricks.R
import justkhelo.cricks.models.PredictData
import justkhelo.cricks.models.QuestionModel
import justkhelo.cricks.ui.predictWin.PredictWinFragment

class QuestionAnswerAdapter(
    val questionList: ArrayList<QuestionModel>,
    var selectedMatch: PredictData?,
    val context: Fragment
    // val customProgressDialog: CustomProgressDialog
) :
    RecyclerView.Adapter<QuestionAnswerAdapter.QuestionAnswerVH>() {

    inner class QuestionAnswerVH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textQuestion: TextView = itemView.findViewById(R.id.question)
        val radioGroup: RadioGroup = itemView.findViewById(R.id.radio_group)
        val winningAmount: TextView = itemView.findViewById(R.id.winning_amount)
//        var radioChecked = -1
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuestionAnswerVH {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_question_answer, parent, false)
        //   customeProgressDialog.dismiss()
        return QuestionAnswerVH(view)
    }

    override fun onBindViewHolder(holder: QuestionAnswerVH, position: Int) {
        if (questionList.isNotEmpty()) {
            if (questionList[position].Payper == null) {
                questionList[position].Payper = "0"
            }
            holder.textQuestion.text = questionList[position].question_title
            if (questionList[position].winningAmount != null) {
                holder.winningAmount.text = " ${questionList[position].winningAmount}"
            }
            else {
                holder.winningAmount.text = "0"
            }
            holder.radioGroup.removeAllViews()

            for (i in questionList[position].answers.values) {
                val radioButton = RadioButton(context.requireContext())
                if (i.title.lowercase() == "team a" && selectedMatch != null) {
                    radioButton.text = selectedMatch!!.teama_name
                } else if (i.title.lowercase() == "team b" && selectedMatch != null) {
                    radioButton.text = selectedMatch!!.teamb_name
                } else {
                    radioButton.text = i.title
                }

                radioButton.tag = i.answer_id
                radioButton.id = i.answer_id
//                if (i.isChecked) {
//                    holder.radioChecked = i.answer_id
//                }
                radioButton.isChecked = i.isChecked
                val param = ViewGroup.MarginLayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                param.setMargins(0, 10, 0, 10)
                radioButton.background = context.requireContext().resources.getDrawable(R.drawable.bg_rounded_gray)
                radioButton.setPadding(5, 3, 5, 3)
                radioButton.textSize = context.resources.getDimension(R.dimen.dimen_3sp)
                radioButton.layoutParams = param
                holder.radioGroup.addView(radioButton)
            }
            holder.radioGroup.setOnCheckedChangeListener { group, checkedId ->
                if (checkedId > 0) {
                    if (questionList[position].answers[checkedId.toString()] != null &&
                        !questionList[position].answers[checkedId.toString()]!!.isChecked) {
                        (context as PredictionCallback).onAnswerSelected(holder.adapterPosition, checkedId.toString())
                    }
                }
            }
        }
    }

    override fun getItemCount(): Int = questionList.size
}