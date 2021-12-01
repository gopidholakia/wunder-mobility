package com.wunder.rental.usecase

import com.wunder.rental.model.CarDetail
import com.wunder.rental.remote.Api
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import io.reactivex.Observable
import javax.inject.Inject

@Module
@InstallIn(ViewModelComponent::class)
class GetCarDetailUseCase @Inject constructor(private val api: Api) {

    internal fun execute(carId: Int): Observable<CarDetail> {
        return api.fetchCarDetail(carId)
    }

}