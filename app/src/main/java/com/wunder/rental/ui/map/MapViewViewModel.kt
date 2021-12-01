package com.wunder.rental.ui.map

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.wunder.rental.model.CarDetail
import com.wunder.rental.usecase.GetCarDetailUseCase
import com.wunder.rental.usecase.GetCarsUseCase
import com.wunder.rental.usecase.RequestQuickRentUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject


@HiltViewModel
class MapViewViewModel @Inject constructor(
    private val getCarsUseCase: GetCarsUseCase,
    private val getCarDetailUseCase: GetCarDetailUseCase,
    private val requestQuickRentUseCase: RequestQuickRentUseCase
) :
    ViewModel() {
    private val disposables = CompositeDisposable()
    internal val error = MutableLiveData<String>()

    private val _carList = MutableLiveData<List<CarDetail>>()
    internal val carList: LiveData<List<CarDetail>>
        get() = _carList

    private val _carDetails = MutableLiveData<CarDetail>()
    val carDetails: LiveData<CarDetail>
        get() = _carDetails

    private val _isBooked = MutableLiveData<Boolean>()
    val isBooked: LiveData<Boolean>
        get() = _isBooked

    internal fun fetchCars() {
        disposables.add(getCarsUseCase.execute().subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { _carList.value = it },
                { error.value = it.message }
            ))
    }

    internal fun fetchCarDetail(carId: Int) {
        disposables.add(getCarDetailUseCase.execute(carId).subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { _carDetails.value = it },
                { error.value = it.message }
            ))
    }

    internal fun requestQuickRent(carId: String) {
        disposables.add(requestQuickRentUseCase.execute(carId).subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { _isBooked.value = true },
                { error.value = it.message }
            ))
    }

    override fun onCleared() {
        super.onCleared()
        disposables.dispose()
    }

}