package justkhelo.cricks.models

data class LeaderBoardModel(
    val max_point: String = "",
    val points: String = "",
    val team_count: String = "",
    val team_name: String = "",
    val user_id: String = "",
    val user_name: String = "",
    val ranks: String = "",
    val series_id: String = "",
    val uid: Long,
    val profilePhotoUrl: String = ""
)