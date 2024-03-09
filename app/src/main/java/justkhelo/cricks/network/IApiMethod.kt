package justkhelo.cricks.network

import com.google.gson.JsonObject
import justkhelo.cricks.models.*
import justkhelo.cricks.requestmodels.RequestCreateTeamModel
import justkhelo.cricks.requestmodels.RequestPaytmModel
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.*

interface IApiMethod {

    @Headers("Content-Type: application/json")
    @POST("api/v3/detectDevice")
    fun updateDeviceInfo(@Body request: JsonObject): Call<JsonObject>


    @Headers("Content-Type: application/json")
    @POST("api/v1/login")
    fun customerLogin(@Body request: JsonObject): Call<ResponseModel>

    @Headers("Content-Type: application/json")
    @POST("member/logout")
    fun logout(@Body request: JsonObject): Call<UsersPostDBResponse>

    @Headers("Content-Type: application/json")
    @POST("getMatch")
    fun getAllMatches(@Body request: JsonObject): Call<UsersPostDBResponse>

    @Headers("Content-Type: application/json")
    @POST("getContestByMatch")
    fun getContestByMatch(@Body request: JsonObject): Call<UsersPostDBResponse>

    @Headers("Content-Type: application/json")
    @POST("getPlayer")
    fun getPlayer(@Body request: JsonObject): Call<UsersPostDBResponse>

    @Headers("Content-Type: application/json")
    @POST("createTeam")
    fun createTeam(@Body request: RequestCreateTeamModel): Call<UsersPostDBResponse>

    @Headers("Content-Type: application/json")
    @POST("getMyTeam")
    fun getMyTeam(@Body request: JsonObject): Call<UsersPostDBResponse>

    @Headers("Content-Type: application/json")
    @POST("getMyContest")
    fun getMyContest(@Body request: JsonObject): Call<UsersPostDBResponse>

    @Headers("Content-Type: application/json")
    @POST("joinContest")
    fun joinContest(@Body request: JsonObject): Call<UsersPostDBResponse>

    @Headers("Content-Type: application/json")
    @POST("import_contact")
    fun importContact(@Body request: JsonObject): Call<UsersPostDBResponse>



    @Headers("Content-Type: application/json")
    @POST("getWallet")
    fun getWallet(@Body request: JsonObject): Call<UsersPostDBResponse>

    @Headers("Content-Type: application/json")
    @POST("leaderBoard")
    fun getLeaderBoard(@Body request: JsonObject): Call<UsersPostDBResponse>


    @Headers("Content-Type: application/json")
    @POST("getPrizeBreakup")
    fun getPrizeBreakUp(@Body request: JsonObject): Call<UsersPostDBResponse>

    @Headers("Content-Type: application/json")
    @POST("forgotPassword")
    fun forgotPassword(@Body request: JsonObject): Call<UsersPostDBResponse>

    @Headers("Content-Type: application/json")
    @POST("deviceNotification")
    fun deviceNotification(@Body request: JsonObject): Call<UsersPostDBResponse>

    @Headers("Content-Type: application/json")
    @POST("addMoney")
    fun addMoney(@Body request: JsonObject): Call<UsersPostDBResponse>

    @Headers("Content-Type: application/json")
    @POST("getPoints")
    fun getPoints(@Body request: JsonObject): Call<UsersPostDBResponse>

    @Headers("Content-Type: application/json")
    @POST("joinNewContestStatus")
    fun joinNewContestStatus(@Body request: JsonObject): Call<UsersPostDBResponse>

    @Headers("Content-Type: application/json")
    @POST("getScore")
    fun getScore(@Body request: JsonObject): Call<UsersPostDBResponse>

    @Headers("Content-Type: application/json")
    @POST("transactionHistory")
    fun getTransactionHistory(@Body request: JsonObject): Call<UsersPostDBResponse>

    @Headers("Content-Type: application/json")
    @POST("api/v1/apkUpdate")
    fun apkUpdate(@Body request: JsonObject): Call<JsonObject>

    @Headers("Content-Type: application/json")
    @POST("paytm/generateChecksum.php")
    fun getPaytmChecksum(@Body request: RequestPaytmModel): Call<ResponseModel>

    @Headers("Content-Type: application/json")
    @POST("cloneMyTeam")
    fun copyTeam(@Body request: JsonObject): Call<UsersPostDBResponse>

    @Headers("Content-Type: application/json")
    @POST("getMatchHistory")
    fun getMatchHistory(@Body request: JsonObject): Call<UsersPostDBResponse>

    /*@FormUrlEncoded
    @POST("uploadbase64Image")
    fun uploadImage(
        @Field("image_bytes") imageBytes: String,
        @Field("user_id") userid: String,
        @Field("documents_type") documentsType: String
    ): Call<ResponseModel>*/

    @Headers("Content-Type: application/json")
    @POST("saveDocuments")
    fun saveBankDetails(@Body request: DocumentsModel): Call<ResponseModel>

    @Headers("Content-Type: application/json")
    @POST("verification")
    fun verification(@Body request: DocumentsModel): Call<ResponseModel>

    @Headers("Content-Type: application/json")
    @POST("myReferralDetails")
    fun myRefferalsList(@Body request: JsonObject): Call<UsersPostDBResponse>

    @Headers("Content-Type: application/json")
    @POST("updateProfile")
    fun updateProfile(@Body request: JsonObject): Call<UsersPostDBResponse>

    @Headers("Content-Type: application/json")
    @POST("getProfile")
    fun getProfile(@Body request: JsonObject): Call<ResponseModel>

    @Headers("Content-Type: application/json")
    @POST("generateOtp")
    fun generateOtp(@Body request: JsonObject): Call<UsersPostDBResponse>

    @Headers("Content-Type: application/json")
    @POST("verifyOtp")
    fun verifyOtp(@Body request: JsonObject): Call<UsersPostDBResponse>

    @Headers("Content-Type: application/json")
    @POST("mChangePassword")
    fun changePassword(@Body request: JsonObject): Call<UsersPostDBResponse>

    @Headers("Content-Type: application/json")
    @POST("getPlayingMatchHistory")
    fun getPlayingMatchHistory(@Body request: JsonObject): Call<UsersPostDBResponse>

    @Headers("Content-Type: application/json")
    @POST("getNotification")
    fun getNotification(@Body request: JsonObject): Call<UsersPostDBResponse>

    @Headers("Content-Type: application/json")
    @POST("changeMobile")
    fun switchNumbers(@Body request: JsonObject): Call<UsersPostDBResponse>

    @Headers("Content-Type: application/json")
    @POST("verification")
    fun getDocumentsList(@Body request: JsonObject): Call<UsersPostDBResponse>

    @Headers("Content-Type: application/json")
    @POST("withdrawAmount")
    fun withdrawAmount(@Body request: JsonObject): Call<UsersPostDBResponse>

    @Headers("Content-Type: application/json")
    @POST("createRazorPayOrder")
    fun createRazorPayOrder(@Body request: JsonObject): Call<UsersPostDBResponse>

    @Headers("Content-Type: application/json")
    @POST("messageApi")
    fun getMessages(@Body request: JsonObject): Call<JsonObject>

    @Headers("Content-Type: application/json")
    @POST("eventLog")
    fun sendEventLogs(@Body request: JsonObject): Call<UsersPostDBResponse>

    @Headers("Content-Type: application/json")
    @POST("saveAllDocuments")
    fun saveAllDocuments(@Body request: JsonObject): Call<ResponseModel>

    @Headers("Content-Type: application/json")
    @POST("withdrawAmountNinja")
    fun withdrawAmountNew(@Body request: JsonObject): Call<UsersPostDBResponse>

    @Headers("Content-Type: application/json")
    @POST("verificationNinja")
    fun getApprovedDocuments(@Body request: JsonObject): Call<UsersPostDBResponse>

    @Multipart
    @POST("uploadbase64Image")
    fun saveDocumentImage(
        @PartMap() partMap: Map<String, @JvmSuppressWildcards RequestBody>,
        @Part bankImage: MultipartBody.Part?
    ): Call<ResponseModel>

    @Headers("Content-Type: application/json")
    @POST("globalLeaderBoard")
    fun globalLeaderBoard(@Body request: JsonObject): Call<JsonObject>

    @Headers("Content-Type: application/json")
    @POST("getLeaderBoardUser")
    fun getLeaderBoardUser(@Body request: JsonObject): Call<JsonObject>

    @Headers("Content-Type: application/json")
    @POST("playerDetails")
    fun getPlayerDetails(@Body request: JsonObject): Call<JsonObject>

    @Headers("Content-Type: application/json")
    @POST("initiateTransaction")
    fun initiateTransaction(@Body request: JsonObject): Call<JsonObject>

    @Headers("Content-Type: application/json")
    @POST("validateCoupon")
    fun validateCoupon(@Body request: JsonObject): Call<JsonObject>

    @GET("getPredictContest")
    fun getPrediction(): Call<PredictModel>

    @GET("setPredictContest")
    fun setPrediction(): Call<PredictModel>

    @POST("joinQuizContest")
    fun joinQuizContest(@Body request: JsonObject): Call<JsonObject>

    @GET("getQuizBanner")
    fun getQuizBanner(): Call<ImageSliderResponseModel>
}