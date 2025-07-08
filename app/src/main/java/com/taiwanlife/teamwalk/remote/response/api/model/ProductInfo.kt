package com.taiwanlife.teamwalk.remote.response.api.model

import com.google.gson.annotations.SerializedName

data class ProductInfo(
    /**
     * 商品ID
     */
    @SerializedName("product_id")
    val productId: String,
    /**
     * 商品名稱
     */
    @SerializedName("prod_name")
    val prodName: String,
    /**
     * 兌換期限
     */
    @SerializedName("end_date")
    val endDate: String,
    /**
     * 首頁圖片
     */
    @SerializedName("home_img")
    val homeImg: String,
    /**
     * 商品詳情圖檔1
     */
    @SerializedName("detail_img_1")
    val detailImg1: String?,
    /**
     * 商品詳情圖檔2
     */
    @SerializedName("detail_img_2")
    val detailImg2: String?,
    /**
     * 商品詳情圖檔3
     */
    @SerializedName("detail_img_3")
    val detailImg3: String?,
    /**
     * 商品詳情圖檔4
     */
    @SerializedName("detail_img_4")
    val detailImg4: String?,
    /**
     * 商品詳情圖檔5
     */
    @SerializedName("detail_img_5")
    val detailImg5: String?,
    /**
     * 商品說明
     */
    @SerializedName("description")
    val description: String?,
    /**
     * 使用說明
     */
    @SerializedName("usage")
    val usage: String?,
    /**
     * 注意事項
     */
    @SerializedName("notes")
    val notes: String?,
    /**
     * 是否為熱門商品
     */
    @SerializedName("is_popular")
    val isPopular: String?,
    /**
     * 兌換龍珠幣
     */
    @SerializedName("original_coins")
    val originalCoins: Int,
    /**
     * 原市價
     */
    @SerializedName("cost_unit_price")
    val costUnitPrice: Int,
    /**
     * 限兌數量
     */
    @SerializedName("limit_exchange_amt")
    val limitExchangeAmt: Int,
    /**
     * 餘下數量
     */
    @SerializedName("quantity")
    val quantity: Int
)