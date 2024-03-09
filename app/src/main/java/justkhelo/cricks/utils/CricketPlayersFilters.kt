package justkhelo.cricks.utils

import android.content.Context
import justkhelo.cricks.CreateTeamActivity.Companion.CREATE_TEAM_ALLROUNDER
import justkhelo.cricks.CreateTeamActivity.Companion.CREATE_TEAM_BATSMAN
import justkhelo.cricks.CreateTeamActivity.Companion.CREATE_TEAM_BOWLER
import justkhelo.cricks.CreateTeamActivity.Companion.CREATE_TEAM_WICKET_KEEPER
import justkhelo.cricks.R
import justkhelo.cricks.models.PlayersInfoModel
import justkhelo.cricks.models.UpcomingMatchesModel

class CricketPlayersFilters(context: Context) {

    companion object {

        fun getPlayersbyOddEvenPositions(
            filterPlayersLists: ArrayList<PlayersInfoModel>,
            matchObject: UpcomingMatchesModel,
            playersType: String
        ): ArrayList<PlayersInfoModel> {

            val finalobjects: ArrayList<PlayersInfoModel> = ArrayList()
            val teamAPlayerList: ArrayList<PlayersInfoModel> = ArrayList()
            val teamBPlayerList: ArrayList<PlayersInfoModel> = ArrayList()

            for (x in 0 until filterPlayersLists.size) {
                val playerInfoObject = filterPlayersLists[x]
                if (matchObject.teamAInfo!!.teamId == playerInfoObject.teamId) {
                    if (playersType.equals(CREATE_TEAM_WICKET_KEEPER)) {
                        playerInfoObject.setPlayerIcon(R.drawable.ic_player_wk_teama)
                    } else if (playersType.equals(CREATE_TEAM_BATSMAN)) {
                        playerInfoObject.setPlayerIcon(R.drawable.ic_player_bat_teama)
                    } else if (playersType.equals(CREATE_TEAM_ALLROUNDER)) {
                        playerInfoObject.setPlayerIcon(R.drawable.ic_player_all_teama)
                    } else if (playersType.equals(CREATE_TEAM_BOWLER)) {
                        playerInfoObject.setPlayerIcon(R.drawable.ic_player_bowler_teama)
                    }
                    teamAPlayerList.add(playerInfoObject)
                }
                if (matchObject.teamBInfo!!.teamId == playerInfoObject.teamId) {
                    if (playersType.equals(CREATE_TEAM_WICKET_KEEPER)) {
                        playerInfoObject.setPlayerIcon(R.drawable.ic_player_wk_teamb)
                    } else if (playersType.equals(CREATE_TEAM_BATSMAN)) {
                        playerInfoObject.setPlayerIcon(R.drawable.ic_player_bat_teamb)
                    } else if (playersType.equals(CREATE_TEAM_ALLROUNDER)) {
                        playerInfoObject.setPlayerIcon(R.drawable.ic_player_all_teamb)
                    } else if (playersType.equals(CREATE_TEAM_BOWLER)) {
                        playerInfoObject.setPlayerIcon(R.drawable.ic_player_bowler_teamb)
                    }
                    teamBPlayerList.add(playerInfoObject)
                }
            }
            val t1 = teamAPlayerList.size
            val t2 = teamBPlayerList.size
            if (t1 > t2) {
                for (x in 0 until t1) {
                    finalobjects.add(teamAPlayerList[x])
                    if (x < t2) {
                        finalobjects.add(teamBPlayerList[x])
                    }
                }
            } else {
                for (x in 0 until t2) {
                    finalobjects.add(teamBPlayerList[x])
                    if (x < t1) {
                        finalobjects.add(teamAPlayerList[x])
                    }
                }
            }

            return finalobjects
        }

        fun getPlayersbyMaxSelection(
            filteredWicketKeepers: java.util.ArrayList<PlayersInfoModel>,
            isEntryAscending: Boolean
        ): List<PlayersInfoModel> {
            if (isEntryAscending) {
                return filteredWicketKeepers.sortedBy { it -> it.analyticsModel!!.selectionPc }
            } else {
                return filteredWicketKeepers.sortedByDescending { it -> it.analyticsModel!!.selectionPc }
            }
        }

        fun getPlayersbyMaxPoints(
            filteredWicketKeepers: java.util.ArrayList<PlayersInfoModel>,
            isEntryAscending: Boolean
        ): List<PlayersInfoModel> {
            if (isEntryAscending) {
                return filteredWicketKeepers.sortedBy { it -> it.playerSeriesPoints }
            } else {
                return filteredWicketKeepers.sortedByDescending { it -> it.playerSeriesPoints }
            }
        }

        fun getPlayersbyMaxCredits(
            filteredWicketKeepers: java.util.ArrayList<PlayersInfoModel>,
            isEntryAscending: Boolean
        ): List<PlayersInfoModel> {
            if (isEntryAscending) {
                return filteredWicketKeepers.sortedBy { it -> it.fantasyPlayerRating }
            } else {
                return filteredWicketKeepers.sortedByDescending { it -> it.fantasyPlayerRating }
            }
        }
    }
}