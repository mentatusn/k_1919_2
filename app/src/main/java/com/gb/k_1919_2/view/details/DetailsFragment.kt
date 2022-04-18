package com.gb.k_1919_2.view.details

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.gb.k_1919_2.BuildConfig
import com.gb.k_1919_2.databinding.FragmentDetailsBinding
import com.gb.k_1919_2.repository.*
import com.gb.k_1919_2.repository.dto.WeatherDTO
import com.gb.k_1919_2.utlis.*
import com.gb.k_1919_2.viewmodel.ResponseState
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import okhttp3.*
import java.io.IOException


class DetailsFragment : Fragment(), OnServerResponse, OnServerResponseListener {


    private var _binding: FragmentDetailsBinding? = null
    private val binding: FragmentDetailsBinding
        get() {
            return _binding!!
        }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(receiver)
    }

    val receiver = object :BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            intent?.let { intent ->
                intent.getParcelableExtra<WeatherDTO>(KEY_BUNDLE_SERVICE_BROADCAST_WEATHER)?.let {
                    onResponse(it)
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDetailsBinding.inflate(inflater, container, false)
        //return inflater.inflate(R.layout.fragment_main, container, false)
        return binding.root
    }


    lateinit var currentCityName: String
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(receiver,
            IntentFilter(KEY_WAVE_SERVICE_BROADCAST)
        )
        arguments?.getParcelable<Weather>(KEY_BUNDLE_WEATHER)?.let {
            currentCityName = it.city.name
            //Thread{
            //WeatherLoader(this@DetailsFragment,this@DetailsFragment).loadWeather(it.city.lat, it.city.lon)
            //}.start()

           /* requireActivity().startService(Intent(requireContext(),DetailsService::class.java).apply {
                putExtra(KEY_BUNDLE_LAT,it.city.lat)
                putExtra(KEY_BUNDLE_LON,it.city.lon)
            })*/
            getWeather(it.city.lat,it.city.lon)
        }
    }

    private fun getWeather(lat:Double, lon:Double){
        binding.loadingLayout.visibility = View.VISIBLE // renderData(Loading)

        val client = OkHttpClient()
        val builder = Request.Builder()
        builder.addHeader(YANDEX_API_KEY, BuildConfig.WEATHER_API_KEY)
        builder.url("$YANDEX_DOMAIN${YANDEX_ENDPOINT}lat=$lat&lon=$lon")
        val request = builder.build()
        val callback:Callback = object : Callback{
            override fun onFailure(call: Call, e: IOException) {
                // TODO HW
                //renderData()
            }
            override fun onResponse(call: Call, response: Response) {
                if(response.isSuccessful){
                    val weatherDTO: WeatherDTO = Gson().fromJson(response.body()!!.string(), WeatherDTO::class.java)
                    requireActivity().runOnUiThread {
                        renderData(weatherDTO)
                    }
                }else{
                    // TODO HW
                }
            }
        }
        val call = client.newCall(request)
        Thread{
            // работа 1
            val response = call.execute()
            // работа 2 с использованием response
        }.start()

        call.enqueue(callback)
    }

    private fun renderData(weather: WeatherDTO) {
        with(binding) {
            loadingLayout.visibility = View.GONE
            cityName.text = currentCityName
            with(weather) { //  TODO HW что-то не так
                temperatureValue.text = weather.factDTO.temperature.toString()
                feelsLikeValue.text = weather.factDTO.feelsLike.toString()
                cityCoordinates.text = "${weather.infoDTO.lat} ${weather.infoDTO.lon}"
            }
            Snackbar.make(mainView, "Получилось", Snackbar.LENGTH_LONG)
                .show()  //  TODO HW можно вынести в функцию-расширение
            mainView.showSnackBar()  //  TODO HW можно вынести в функцию-расширение
        }

        //Toast.makeText(requireContext(),"РАБОТАЕТ",Toast.LENGTH_SHORT).show()
    }

    //  TODO HW
    fun View.showSnackBar() {

    }

    companion object {
        @JvmStatic
        fun newInstance(bundle: Bundle): DetailsFragment {
            val fragment = DetailsFragment()
            fragment.arguments = bundle
            return fragment
        }
    }

    override fun onResponse(weatherDTO: WeatherDTO) {
        renderData(weatherDTO)
    }

    override fun onError(error: ResponseState) {
        // TODO HW выводим ошибку
    }
}

