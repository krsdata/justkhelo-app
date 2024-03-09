package justkhelo.cricks.models

data class ContestLeaderBoardModel(
    val id: Int,
    val match_name: String = "",
    var isSelect: Boolean = false
)