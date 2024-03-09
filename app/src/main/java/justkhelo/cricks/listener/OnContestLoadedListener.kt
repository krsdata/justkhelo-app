package com.edify.atrist.listener

import justkhelo.cricks.models.MyTeamModels
import justkhelo.cricks.models.ContestModelLists

interface OnContestLoadedListener {
    fun onMyContest(contestModel: ArrayList<ContestModelLists>)
    fun onMyTeam(count: ArrayList<MyTeamModels>)
}