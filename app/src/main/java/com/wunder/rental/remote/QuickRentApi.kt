package com.wunder.rental.remote

import com.wunder.rental.model.RequestQuickRental
import io.reactivex.Completable
import retrofit2.http.Body
import retrofit2.http.POST


/**
 * Retrofit interface for quick rent
 */

interface QuickRentApi {
    @POST("/default/wunderfleet-recruiting-mobile-dev-quick-rental")
    fun requestForQuickRental(@Body carId: RequestQuickRental): Completable
}