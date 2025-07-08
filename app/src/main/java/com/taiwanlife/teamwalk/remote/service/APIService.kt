package com.taiwanlife.teamwalk.remote.service

import com.taiwanlife.teamwalk.remote.request.api.CancelInviteTeamPkPostRequest
import com.taiwanlife.teamwalk.remote.request.api.InviteFriendPostRequest
import com.taiwanlife.teamwalk.remote.request.api.LandingPostRequest
import com.taiwanlife.teamwalk.remote.request.api.LoginRequest
import com.taiwanlife.teamwalk.remote.request.api.MyInfoPostRequest
import com.taiwanlife.teamwalk.remote.request.api.PersonalPkCancelInvitePostRequest
import com.taiwanlife.teamwalk.remote.request.api.PersonalPkPostRequest
import com.taiwanlife.teamwalk.remote.request.api.SyncHealthInfoPostRequest
import com.taiwanlife.teamwalk.remote.request.api.TakeTaskBonusPostRequest
import com.taiwanlife.teamwalk.remote.request.api.TeamInviteMyTeamPostRequest
import com.taiwanlife.teamwalk.remote.request.api.TeamInvitePostRequest
import com.taiwanlife.teamwalk.remote.request.api.TeamPkPostRequest
import com.taiwanlife.teamwalk.remote.response.api.DistrictInfoResponse
import com.taiwanlife.teamwalk.remote.response.api.ExpCoinHistoryInfoResponse
import com.taiwanlife.teamwalk.remote.response.api.FriendInfoResponse
import com.taiwanlife.teamwalk.remote.response.api.GetAdventureInfoResponse
import com.taiwanlife.teamwalk.remote.response.api.GetAdventureMapInfoResponse
import com.taiwanlife.teamwalk.remote.response.api.GetInsuranceInfoResponse
import com.taiwanlife.teamwalk.remote.response.api.GetMainHealthInfoResponse
import com.taiwanlife.teamwalk.remote.response.api.GetMainInfoResponse
import com.taiwanlife.teamwalk.remote.response.api.GetMyInfoResponse
import com.taiwanlife.teamwalk.remote.response.api.GetMyTeamInfoResponse
import com.taiwanlife.teamwalk.remote.response.api.GetPkSummaryInfoResponse
import com.taiwanlife.teamwalk.remote.response.api.GetTeamInfoResponse
import com.taiwanlife.teamwalk.remote.response.api.GetTeamPkInfoResponse
import com.taiwanlife.teamwalk.remote.response.api.ListTeamPkInfoResponse
import com.taiwanlife.teamwalk.remote.response.api.LoginResponse
import com.taiwanlife.teamwalk.remote.response.api.MyBadgeInfoResponse
import com.taiwanlife.teamwalk.remote.response.api.PersonalPkInfoResponse
import com.taiwanlife.teamwalk.remote.response.api.ProductExchangeDetailInfoResponse
import com.taiwanlife.teamwalk.remote.response.api.ProductExchangeInfoResponse
import com.taiwanlife.teamwalk.remote.response.api.ResponseWrapper
import com.taiwanlife.teamwalk.remote.response.api.SysParamInfoResponse
import com.taiwanlife.teamwalk.remote.response.api.UserInfoResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface APIService {

    /**
     * 使用者登入
     */
    @POST("login")
    suspend fun login(@Body loginRequest: LoginRequest): Response<ResponseWrapper<LoginResponse>>

    /**
     * Landing頁面儲存個人資料
     */
    @POST("landing")
    suspend fun saveLandingInfo(
        @Body landingPostRequest: LandingPostRequest
    ): Response<ResponseWrapper<Unit>>

    /**
     * 取得系統參數
     */
    @GET("sys-param")
    suspend fun getSysParam(): Response<ResponseWrapper<SysParamInfoResponse>>

    /**
     * 取得使用者資訊
     */
    @GET("user-info")
    suspend fun getUserInfo(): Response<ResponseWrapper<UserInfoResponse>>

    /**
     * 同步健康數據
     */
    @POST("sync-health-info")
    suspend fun syncHealthInfo(
        @Body syncHealthInfoPostRequest: SyncHealthInfoPostRequest
    ): Response<ResponseWrapper<Unit>>

    /**
     * 取得所有縣市別跟行政區
     */
    @GET("district")
    suspend fun getDistrict(): Response<ResponseWrapper<List<DistrictInfoResponse>>>

    /**
     * 取得首頁資料
     */
    @GET("index")
    suspend fun getMain(): Response<ResponseWrapper<GetMainInfoResponse>>

    /**
     * 取得首頁健康資料
     */
    @GET("index-health")
    suspend fun getMainHealth(): Response<ResponseWrapper<GetMainHealthInfoResponse>>

    /**
     * 取得個人對戰資料
     */
    @GET("personal-pk")
    suspend fun listPersonalPK(): Response<ResponseWrapper<List<PersonalPkInfoResponse>>>

    /**
     * 發起個人PK挑戰
     */
    @POST("personal-pk")
    suspend fun submitPersonalPK(
        @Body personalPkPostRequest: PersonalPkPostRequest
    ): Response<ResponseWrapper<Unit>>

    /**
     * 取消個人PK挑戰
     */
    @POST("personal-pk-cancel-invite")
    suspend fun cancalInvitePersonalPK(
        @Body personalPkCancelInvitePostRequest: PersonalPkCancelInvitePostRequest
    ): Response<ResponseWrapper<Unit>>

    /**
     * 取得對戰資料-勝率
     */
    @GET("pk-summary")
    suspend fun getPKSummary(): Response<ResponseWrapper<GetPkSummaryInfoResponse>>

    /**
     * 取得團隊對戰資料
     */
    @GET("team-pk")
    suspend fun listTeamPK(): Response<ResponseWrapper<ListTeamPkInfoResponse>>

    /**
     * 決定團隊組隊邀請(受邀會員使用)
     */
    @POST("team-pk")
    suspend fun submitTeamPK(
        @Body teamPkPostRequest: TeamPkPostRequest
    ): Response<ResponseWrapper<Unit>>

    /**
     * 取消團隊組隊邀請
     */
    @POST("team-pk-cancel-invite")
    suspend fun cancelInviteTeamPK(
        @Body cancelInviteTeamPkPostRequest: CancelInviteTeamPkPostRequest
    ): Response<ResponseWrapper<Unit>>

    /**
     * 取得團隊對戰資料-明細
     */
    @GET("team-pk/{team_act_id}")
    suspend fun getTeamPK(
        @Path("team_act_id") teamActId: String
    ): Response<ResponseWrapper<GetTeamPkInfoResponse>>

    /**
     * 取得參賽團隊資料-明細
     */
    @GET("team/{team_id}")
    suspend fun getTeam(
        @Path("team_id") teamId: String
    ): Response<ResponseWrapper<GetTeamInfoResponse>>

    /**
     * 取得我的團隊對戰資料-明細
     */
    @GET("team/my/{team_act_id}")
    suspend fun getMyTeam(
        @Path("team_act_id") teamActId: String
    ): Response<ResponseWrapper<GetMyTeamInfoResponse>>

    /**
     * 決定會員加入團隊邀請(隊長使用)
     */
    @POST("team/my")
    suspend fun submitTeamInvite(
        @Body teamInviteMyTeamPostRequest: TeamInviteMyTeamPostRequest
    ): Response<ResponseWrapper<Unit>>

    /**
     * 邀請會員加入隊伍(隊長使用)
     */
    @POST("team/invite")
    suspend fun inviteTeam(
        @Body teamInvitePostRequest: TeamInvitePostRequest
    ): Response<ResponseWrapper<Unit>>

    /**
     * 取得好友清單
     */
    @GET("friend")
    suspend fun listFriend(
        @Query("is_suggest") isSuggest: String
    ): Response<ResponseWrapper<List<FriendInfoResponse>>>

    /**
     * 取得好友邀請清單
     */
    @GET("invite-friend")
    suspend fun listInviteriend(): Response<ResponseWrapper<List<FriendInfoResponse>>>

    /**
     * 好友邀請確認
     */
    @POST("invite-friend")
    suspend fun submitFriendInvite(
        @Body inviteFriendPostRequest: InviteFriendPostRequest
    ): Response<ResponseWrapper<Unit>>

    /**
     * 取得挑戰首頁
     */
    @GET("adventure")
    suspend fun getAdventure(): Response<ResponseWrapper<GetAdventureInfoResponse>>

    /**
     * 取得挑戰地圖資訊
     */
    @GET("adventure/map/{level_id}")
    suspend fun getAdventureMap(
        @Path("level_id") levelId: String
    ): Response<ResponseWrapper<GetAdventureMapInfoResponse>>

    /**
     * 取得我的資料
     */
    @GET("my")
    suspend fun getMy(): Response<ResponseWrapper<GetMyInfoResponse>>

    /**
     * 修改個人資料
     */
    @POST("my")
    suspend fun saveMyInfo(
        @Body myInfoPostRequest: MyInfoPostRequest
    ): Response<ResponseWrapper<Unit>>

    /**
     * 領取任務獎勵
     */
    @POST("take-task-bonus")
    suspend fun takeTaskBonus(
        @Body takeTaskBonusPostRequest: TakeTaskBonusPostRequest
    ): Response<ResponseWrapper<Unit>>

    /**
     * 取得我的經驗值資料
     */
    @GET("my-exp")
    suspend fun getMyExp(): Response<ResponseWrapper<List<ExpCoinHistoryInfoResponse>>>

    /**
     * 取得我的龍珠資料
     */
    @GET("my-coin")
    suspend fun getMyCoin(): Response<ResponseWrapper<List<ExpCoinHistoryInfoResponse>>>

    /**
     * 取得我的徽章資料
     */
    @GET("my-badge")
    suspend fun getMyBadge(): Response<ResponseWrapper<List<MyBadgeInfoResponse>>>

    /**
     * 取得我的兌換券資料
     */
    @GET("my-product-exchange")
    suspend fun listMyProductExchange(): Response<ResponseWrapper<List<ProductExchangeInfoResponse>>>

    /**
     * 取得我的兌換券資-明細料
     */
    @GET("my-product-exchange/{exchange_id}")
    suspend fun getMyProductExchange(
        @Path("exchange_id") exchangeId: String
    ): Response<ResponseWrapper<ProductExchangeDetailInfoResponse>>

    /**
     * 取得保險首頁活動
     */
    @GET("insurance/activity")
    suspend fun getInsuranceActivity(): Response<ResponseWrapper<GetInsuranceInfoResponse>>
}