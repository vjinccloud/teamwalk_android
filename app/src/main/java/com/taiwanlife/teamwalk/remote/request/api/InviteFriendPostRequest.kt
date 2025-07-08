package com.taiwanlife.teamwalk.remote.request.api

import com.google.gson.annotations.SerializedName
import com.taiwanlife.teamwalk.remote.request.BaseRequest

data class InviteFriendPostRequest(
    /**
     * 好友代碼
     */
    @SerializedName("friend_id")
    val friendId: String,
    /**
     * 邀請結果(Y：接受、N：拒絕)
     */
    @SerializedName("result")
    val result: String
) : BaseRequest()