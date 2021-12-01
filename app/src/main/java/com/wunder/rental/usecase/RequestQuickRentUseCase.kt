package com.wunder.rental.usecase

import com.wunder.rental.model.RequestQuickRental
import com.wunder.rental.remote.QuickRentApi
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import io.reactivex.Completable
import javax.inject.Inject

@Module
@InstallIn(ViewModelComponent::class)
class RequestQuickRentUseCase @Inject constructor(private val quickRentApi: QuickRentApi) {

    internal fun execute(carId: String): Completable {
        return quickRentApi.requestForQuickRental(RequestQuickRental(carId))
    }

}