package justkhelo.cricks.models

data class DataModel(
    val leaderBoard: ArrayList<LeaderBoardModel>,
    val match_name: String = "",
    val rank: ArrayList<RankModel>
)