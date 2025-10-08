package com.taiwanlife.teamwalk.remote

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
import com.taiwanlife.teamwalk.remote.response.api.LandingResponse
import com.taiwanlife.teamwalk.remote.response.api.ListTeamPkInfoResponse
import com.taiwanlife.teamwalk.remote.response.api.LoginResponse
import com.taiwanlife.teamwalk.remote.response.api.MyBadgeInfoResponse
import com.taiwanlife.teamwalk.remote.response.api.PersonalPkInfoResponse
import com.taiwanlife.teamwalk.remote.response.api.ProductExchangeDetailInfoResponse
import com.taiwanlife.teamwalk.remote.response.api.ProductExchangeInfoResponse
import com.taiwanlife.teamwalk.remote.response.api.ResponseWrapper
import com.taiwanlife.teamwalk.remote.response.api.SysParamInfoResponse
import com.taiwanlife.teamwalk.remote.response.api.UserInfoResponse
import com.taiwanlife.teamwalk.remote.response.api.model.SyncHealthInfoInfo
import com.taiwanlife.teamwalk.remote.service.APIService
import retrofit2.Response

class ApiRepository(
    private val apiService: APIService
) {

    /**
     * 使用者登入
     * @param applId 登入者帳號
     * @param ticket CSSO Ticket資訊
     * @param service CSSO service
     * @param appUuid app安裝後產生唯一識別碼
     * @param deviceId 裝置識別碼
     * @param pushId FCM推播Token
     */
    suspend fun login(
        applId: String,
        ticket: String,
        service: String,
        appUuid: String,
        deviceId: String,
        pushId: String
    ): Response<ResponseWrapper<LoginResponse>> {
        return apiService.login(LoginRequest(applId, ticket, service, appUuid, deviceId, pushId))
    }

    /**
     * Landing頁面儲存個人資料
     * @param referrerCode 他人的推薦碼
     * @param nickname 暱稱
     * @param bindingApple 綁定Apple
     * @param bindingAndroid 綁定Google
     * @param bindingFibit 綁定Fibit
     * @param bindingFibitToken 綁定Fibit Token
     * @param bindingGarminToken 綁定Garmin Token
     */
    suspend fun saveLandingInfo(
        referrerCode: String?,
        nickname: String?,
        bindingType: String?,
        bindingToken: String?,
    ): Response<ResponseWrapper<Unit>> {
        return apiService.saveLandingInfo(
            LandingPostRequest(
                referrerCode,
                nickname,
                bindingType,
                bindingToken
            )
        )
    }

    /**
     * 取得系統參數
     */
    suspend fun getSysParam(): Response<ResponseWrapper<SysParamInfoResponse>> {
        return apiService.getSysParam()
    }

    /**
     * 取得使用者資訊
     */
    suspend fun getUserInfo(): Response<ResponseWrapper<UserInfoResponse>> {
        return apiService.getUserInfo()
    }

    /**
     * 取得onboarding資訊
     */
    suspend fun getLanding(): Response<ResponseWrapper<LandingResponse>> {
        return apiService.getLanding()
    }

    /**
     * 同步健康數據
     * @param step 步數資料列表
     * @param sleep 睡眠資料列表
     */
    suspend fun syncHealthInfo(
        step: List<SyncHealthInfoInfo>,
        sleep: List<SyncHealthInfoInfo>
    ): Response<ResponseWrapper<Unit>> {
        return apiService.syncHealthInfo(SyncHealthInfoPostRequest(step, sleep))
    }

    /**
     * 取得所有縣市別跟行政區
     */
    suspend fun getDistrict(): Response<ResponseWrapper<List<DistrictInfoResponse>>> {
        return apiService.getDistrict()
    }

    /**
     * 取得首頁資料
     */
    suspend fun getMain(): Response<ResponseWrapper<GetMainInfoResponse>> {
        return apiService.getMain()
    }

    /**
     * 取得首頁健康資料
     */
    suspend fun getMainHealth(): Response<ResponseWrapper<GetMainHealthInfoResponse>> {
        return apiService.getMainHealth()
    }

    /**
     * 取得個人對戰資料
     */
    suspend fun listPersonalPK(): Response<ResponseWrapper<List<PersonalPkInfoResponse>>> {
        return apiService.listPersonalPK()
    }

    /**
     * 發起個人PK挑戰
     * @param invitedMemberId PK對象會員代碼
     * @param steps 發起PK步數
     * @param period 挑戰週期
     * @param coins 挑戰龍珠數
     */
    suspend fun submitPersonalPK(
        invitedMemberId: String,
        steps: Int,
        period: Int,
        coins: Int
    ): Response<ResponseWrapper<Unit>> {
        return apiService.submitPersonalPK(
            PersonalPkPostRequest(
                invitedMemberId,
                steps,
                period,
                coins
            )
        )
    }

    /**
     * 取消個人PK挑戰
     * @param pkId PK邀請代碼
     */
    suspend fun cancalInvitePersonalPK(
        pkId: String
    ): Response<ResponseWrapper<Unit>> {
        return apiService.cancalInvitePersonalPK(PersonalPkCancelInvitePostRequest(pkId))
    }

    /**
     * 取得對戰資料-勝率
     */
    suspend fun getPKSummary(): Response<ResponseWrapper<GetPkSummaryInfoResponse>> {
        return apiService.getPKSummary()
    }

    /**
     * 取得團隊對戰資料
     */
    suspend fun listTeamPK(): Response<ResponseWrapper<ListTeamPkInfoResponse>> {
        return apiService.listTeamPK()
    }

    /**
     * 決定團隊組隊邀請(受邀會員使用)
     * @param teamInviteId 團隊組隊邀請代碼
     * @param result 接受或拒絕(Y：接受 / N拒絕)
     */
    suspend fun submitTeamPK(
        teamInviteId: String,
        result: String
    ): Response<ResponseWrapper<Unit>> {
        return apiService.submitTeamPK(TeamPkPostRequest(teamInviteId, result))
    }

    /**
     * 取消團隊組隊邀請
     * @param teamInviteId 團隊組隊邀請代碼
     */
    suspend fun cancelInviteTeamPK(
        teamInviteId: String
    ): Response<ResponseWrapper<Unit>> {
        return apiService.cancelInviteTeamPK(CancelInviteTeamPkPostRequest(teamInviteId))
    }

    /**
     * 取得團隊對戰資料-明細
     * @param teamActId 團隊活動ID
     */
    suspend fun getTeamPK(
        teamActId: String
    ): Response<ResponseWrapper<GetTeamPkInfoResponse>> {
        return apiService.getTeamPK(teamActId)
    }

    /**
     * 取得參賽團隊資料-明細
     * @param teamId 團隊ID
     */
    suspend fun getTeam(
        teamId: String
    ): Response<ResponseWrapper<GetTeamInfoResponse>> {
        return apiService.getTeam(teamId)
    }

    /**
     * 取得我的團隊對戰資料-明細
     * @param teamActId 團隊活動ID
     */
    suspend fun getMyTeam(
        teamActId: String
    ): Response<ResponseWrapper<GetMyTeamInfoResponse>> {
        return apiService.getMyTeam(teamActId)
    }

    /**
     * 決定會員加入團隊邀請(隊長使用)
     * @param teamInviteId 團隊組隊邀請代碼
     * @param result 接受或拒絕(Y：接受 / N拒絕)
     */
    suspend fun submitTeamInvite(
        teamInviteId: String,
        result: String
    ): Response<ResponseWrapper<Unit>> {
        return apiService.submitTeamInvite(TeamInviteMyTeamPostRequest(teamInviteId, result))
    }

    /**
     * 邀請會員加入隊伍(隊長使用)
     * @param teamActId 團賽代碼
     * @param memberId 會員代碼
     */
    suspend fun inviteTeam(
        teamActId: String?,
        memberId: String?
    ): Response<ResponseWrapper<Unit>> {
        return apiService.inviteTeam(TeamInvitePostRequest(teamActId, memberId))
    }

    /**
     * 取得好友清單
     * @param isSuggest 是否為建議好友 (Y/N)
     */
    suspend fun listFriend(
        isSuggest: String
    ): Response<ResponseWrapper<List<FriendInfoResponse>>> {
        return apiService.listFriend(isSuggest)
    }

    /**
     * 取得好友邀請清單
     */
    suspend fun listInviteriend(): Response<ResponseWrapper<List<FriendInfoResponse>>> {
        return apiService.listInviteriend()
    }

    /**
     * 好友邀請確認
     * @param friendId 好友代碼
     * @param result 邀請結果(Y：接受、N：拒絕)
     */
    suspend fun submitFriendInvite(
        friendId: String,
        result: String
    ): Response<ResponseWrapper<Unit>> {
        return apiService.submitFriendInvite(InviteFriendPostRequest(friendId, result))
    }

    /**
     * 取得挑戰首頁
     */
    suspend fun getAdventure(): Response<ResponseWrapper<GetAdventureInfoResponse>> {
        return apiService.getAdventure()
    }

    /**
     * 取得挑戰地圖資訊
     * @param levelId 地圖等級ID
     */
    suspend fun getAdventureMap(
        levelId: String
    ): Response<ResponseWrapper<GetAdventureMapInfoResponse>> {
        return apiService.getAdventureMap(levelId)
    }

    /**
     * 取得我的資料
     */
    suspend fun getMy(): Response<ResponseWrapper<GetMyInfoResponse>> {
        return apiService.getMy()
    }

    /**
     * 修改個人資料
     * @param mobile 行動電話
     * @param email E-Mail
     * @param county 縣市
     * @param district 鄉鎮市區
     * @param nickname 暱稱
     * @param customImage 會員自行上傳頭像
     * @param buildinImage 會員內建頭像(改成代碼)
     * @param filter 會員內建濾鏡(0~5)
     * @param mood 心情小語
     * @param badgeRoleId 使用阿龍角色
     */
    suspend fun saveMyInfo(
        mobile: String?,
        email: String?,
        county: String?,
        district: String?,
        nickname: String?,
        customImage: String?,
        buildinImage: String?,
        filter: String?,
        mood: String?,
        badgeRoleId: String?
    ): Response<ResponseWrapper<Unit>> {
        return apiService.saveMyInfo(
            MyInfoPostRequest(
                mobile,
                email,
                county,
                district,
                nickname,
                customImage,
                buildinImage,
                filter,
                mood,
                badgeRoleId
            )
        )
    }

    /**
     * 領取任務獎勵
     * @param taskId 任務ID
     */
    suspend fun takeTaskBonus(
        taskId: String
    ): Response<ResponseWrapper<Unit>> {
        return apiService.takeTaskBonus(TakeTaskBonusPostRequest(taskId))
    }

    /**
     * 取得我的經驗值資料
     */
    suspend fun getMyExp(): Response<ResponseWrapper<List<ExpCoinHistoryInfoResponse>>> {
        return apiService.getMyExp()
    }

    /**
     * 取得我的龍珠資料
     */
    suspend fun getMyCoin(): Response<ResponseWrapper<List<ExpCoinHistoryInfoResponse>>> {
        return apiService.getMyCoin()
    }

    /**
     * 取得我的徽章資料
     */
    suspend fun getMyBadge(): Response<ResponseWrapper<List<MyBadgeInfoResponse>>> {
        return apiService.getMyBadge()
    }

    /**
     * 取得我的兌換券資料
     */
    suspend fun listMyProductExchange(): Response<ResponseWrapper<List<ProductExchangeInfoResponse>>> {
        return apiService.listMyProductExchange()
    }

    /**
     * 取得我的兌換券資-明細料
     * @param exchangeId 兌換ID
     */
    suspend fun getMyProductExchange(
        exchangeId: String
    ): Response<ResponseWrapper<ProductExchangeDetailInfoResponse>> {
        return apiService.getMyProductExchange(exchangeId)
    }

    /**
     * 取得保險首頁活動
     */
    suspend fun getInsuranceActivity(): Response<ResponseWrapper<GetInsuranceInfoResponse>> {
        return apiService.getInsuranceActivity()
    }
}