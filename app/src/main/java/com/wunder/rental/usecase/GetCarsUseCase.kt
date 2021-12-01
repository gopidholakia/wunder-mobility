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
class GetCarsUseCase @Inject constructor(private val api: Api) {

    internal fun execute(): Observable<List<CarDetail>> {
        return api.fetchCars()
    }

}