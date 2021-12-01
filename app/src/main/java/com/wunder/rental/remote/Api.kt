package com.wunder.rental.remote

import com.wunder.rental.model.CarDetail
import io.reactivex.Observable
import retrofit2.http.GET
import retrofit2.http.Path


/**
 * Retrofit interface
 */

interface Api {
    @GET("/wunderfleet-recruiting-dev/cars.json")
    fun fetchCars(): Observable<List<CarDetail>>

    @GET("/wunderfleet-recruiting-dev/cars/{carId}")
    fun fetchCarDetail(@Path("carId") carId: Int): Observable<CarDetail>
}