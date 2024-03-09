package justkhelo.cricks.models

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class PredictModel(
    @SerializedName("code")
    @Expose
    val code: String,
    @SerializedName("status")
    @Expose
    val status: String,
    @SerializedName("data")
    @Expose
    var predictDataModel: HashMap<String, PredictData>
)
data class PredictData(
    @SerializedName("id")
    val matchId: Int,
    @SerializedName("match_id")
    val mainMatchId: Int,
    @SerializedName("match_start_time")
    val match_start_time: String,
    @SerializedName("match_start_time_timestamp")
    val match_start_time_timestamp: String,
    @SerializedName("match_title")
    val match_title: String,
    @SerializedName("questions")
    val questions: HashMap<String,QuestionModel>,
    @SerializedName("teama_code")
    val teama_code: String,
    @SerializedName("teama_logo")
    val teama_logo: String,
    @SerializedName("teama_name")
    val teama_name: String,
    @SerializedName("teamb_code")
    val teamb_code: String,
    @SerializedName("teamb_logo")
    val teamb_logo: String,
    @SerializedName("teamb_name")
    val teamb_name: String,
    var isSelected: Boolean = false
)

data class QuestionModel(
    @SerializedName("answers")
    val answers: HashMap<String, AnswerModel>,
    @SerializedName("percentage")
    val percentage: Any,
    @SerializedName("question_id")
    val question_id: Int,
    @SerializedName("question_title")
    val question_title: String,
    @SerializedName("Payper")
    var Payper: String,
    @SerializedName("winning_amount")
    val winningAmount:String
)

data class AnswerModel(
    @SerializedName("answer_id")
    val answer_id: Int,
    @SerializedName("title")
    val title: String,
    var isChecked: Boolean
)