# MVVM Clean Architecture
![alt text](https://user-images.githubusercontent.com/21035435/69536839-9f4c8e80-0fa0-11ea-85ee-d7823e5a46b0.png)

## Communication between layers
-	UI calls method from ViewModel / Presenter.
-	ViewModel executes Use case.
-	Use case combines data from Wunder Repositories.
-	Each Repository returns data from a Data Source (Remote).
-	Information flows back to the UI where we display the Data

## Scenario
- Fetch Car list from Fetch Car List and display them on Map with Marker Pin.
- Display User’s current location.
- When user clicks on Marker, it’ll prompt for Car detail from Fetch Car Detail and let user to book with Quick Rental .

## Improvements
-	Create base components to share common code for common use case.
-	Make it modular for more feature sets

## Tech Stack
#### Architecture Components
-	[ViewModel](https://developer.android.com/topic/libraries/architecture/viewmodel) 
-	[LiveData](https://developer.android.com/topic/libraries/architecture/livedata)
-	[Data Binding](https://developer.android.com/topic/libraries/data-binding)
-	[Navigation](https://developer.android.com/guide/navigation)

#### Third Party 
-	[Play Services](https://developers.google.com/maps/documentation/android-sdk/start)
-	[RxAndroid](https://github.com/ReactiveX/RxAndroid)
-	[Hilt](https://developer.android.com/training/dependency-injection/hilt-android)
-	[Retrofit](https://square.github.io/retrofit/)
-	[OkHttp](https://square.github.io/okhttp/)
-	[Glide](https://github.com/bumptech/glide)
