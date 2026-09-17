package com.beardydev.lookhere.data.klipy

import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Base URL is constructed per-install with the Klipy app key already baked in
 * as a path segment (https://api.klipy.com/api/v1/{KLIPY_API_KEY}/), so
 * request paths here are relative to that and never mention the key.
 */
interface KlipyApi {
    @GET("gifs/search")
    suspend fun search(
        @Query("q") query: String,
        @Query("customer_id") customerId: String,
        @Query("content_filter") contentFilter: String = "high",
        @Query("format_filter") formatFilter: String = "gif",
        @Query("per_page") perPage: Int = 30,
        @Query("page") page: Int = 1,
        @Query("locale") locale: String? = null,
    ): KlipySearchResponse

    @GET("gifs/trending")
    suspend fun trending(
        @Query("customer_id") customerId: String,
        @Query("content_filter") contentFilter: String = "high",
        @Query("format_filter") formatFilter: String = "gif",
        @Query("per_page") perPage: Int = 30,
        @Query("page") page: Int = 1,
        @Query("locale") locale: String? = null,
    ): KlipySearchResponse
}
