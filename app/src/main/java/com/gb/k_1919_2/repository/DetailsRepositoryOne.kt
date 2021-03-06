package com.gb.k_1919_2.repository

import com.gb.k_1919_2.repository.dto.WeatherDTO
import com.gb.k_1919_2.viewmodel.DetailsViewModel


interface DetailsRepositoryOne {
    fun getWeatherDetails(city:City,callback: DetailsViewModel.Callback)
}