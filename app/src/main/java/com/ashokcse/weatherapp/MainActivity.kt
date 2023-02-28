package com.ashokcse.weatherapp

import android.content.Context
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.opengl.Visibility
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.squareup.picasso.Picasso
import org.json.JSONException
import java.io.IOException
import java.util.*


class MainActivity : AppCompatActivity() {

    val apiKey: String = "enter-your-api-key-for-weather-data"
    lateinit var cityNameEdt: EditText
    lateinit var cityName: String
    lateinit var searchIV: ImageView
    lateinit var containerRL: RelativeLayout
    lateinit var locationTV: TextView
    lateinit var dateTV: TextView
    lateinit var currentWeatherIV: ImageView
    lateinit var currentTempTv: TextView
    lateinit var minTempTV: TextView
    lateinit var maxTempTV: TextView
    lateinit var sunRiseTV: TextView
    lateinit var sunsetTV: TextView
    lateinit var windSpeedTV: TextView
    lateinit var pressureTV: TextView
    lateinit var cloudTV: TextView
    lateinit var stormTV: TextView
    lateinit var loadingPB: ProgressBar

    var PERMISSION_CODE = 1
    var locationManager: LocationManager? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
            );
        }
        setContentView(R.layout.activity_main)

        cityNameEdt = findViewById(R.id.idEdtCityName);
        searchIV = findViewById(R.id.idIVSearch);
        containerRL = findViewById(R.id.idRLContainer);
        locationTV = findViewById(R.id.idTVLocation);
        dateTV = findViewById(R.id.idTVDate);
        currentWeatherIV = findViewById(R.id.idIVWeatherCondition);
        currentTempTv = findViewById(R.id.idTVTemperature);
        minTempTV = findViewById(R.id.idTVMinTemp);
        maxTempTV = findViewById(R.id.idTVMaxTemp);
        sunRiseTV = findViewById(R.id.idTVSunRiseTime);
        sunsetTV = findViewById(R.id.idTVSunsetTime);
        pressureTV = findViewById(R.id.idTVPressure);
        windSpeedTV = findViewById(R.id.idTVWindSpeed);
        stormTV = findViewById(R.id.idTVStorm);
        cloudTV = findViewById(R.id.idTVCloudy);
        loadingPB = findViewById(R.id.idPBLoading);



        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        if (ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) !=
            PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this@MainActivity,
                arrayOf(
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION
                ), PERMISSION_CODE
            )
        }

        val location: Location? = locationManager!!.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
        if (location!=null){
            cityName = getLocationName(location.latitude,location.longitude)
            getWeatherInfo(cityName)
        }

      searchIV.setOnClickListener {
          cityName = cityNameEdt.text.toString()
          if (cityName.isNotEmpty()){
              getWeatherInfo(cityName)
          }
      }
    }


    fun getWeatherInfo(cityName: String){

        val url = "https://api.weatherapi.com/v1/forecast.json?key="+ apiKey +"&q="+ cityName +"&aqi=yes&days=1&alerts=yes"
        val queue = Volley.newRequestQueue(this)
        val request = JsonObjectRequest(Request.Method.GET,url,null,{
            response->
            try {
                loadingPB.visibility = View.GONE
                containerRL.visibility = View.VISIBLE
                val locationObj = response.getJSONObject("location")
                val name: String = locationObj.getString("name")
                locationTV.text = name
                val currentObj = response.getJSONObject("current")
                val date: String = currentObj.getString("last_updated")
                dateTV.text = date
                val temperature: String = currentObj.getDouble("temp_c").toString()+"C"
                currentTempTv.text = temperature
                val forecastObj = response.getJSONObject("forecast")
                val forecastDay = forecastObj.getJSONArray("forecastday").getJSONObject(0)
                val dayObj = forecastDay.getJSONObject("day")
                val minTemp: String = dayObj.getDouble("mintemp_c").toString()+"C"
                val maxTemp: String = dayObj.getDouble("maxtemp_c").toString()+"C"

                minTempTV.text = minTemp
                maxTempTV.text = maxTemp

                val astroObj = forecastDay.getJSONObject("astro")
                val sunRise: String = astroObj.getString("sunrise")
                val sunSet: String = astroObj.getString("sunset")
                sunRiseTV.text = sunRise
                sunsetTV.text = sunSet

                val hourArray = forecastDay.getJSONArray("hour")
                for (i in 0 until hourArray.length()){
                    val hourObj = hourArray.getJSONObject(i)
                    var img = hourObj.getJSONObject("condition").getString("icon")
                    img = img.substring(2)
                    Picasso.get().load("http://$img").into(currentWeatherIV)
                    val windSpeed = hourObj.getString("wind_kph")+"kph"
                    windSpeedTV.text = windSpeed

                    val pressure = hourObj.getString("pressure_in")
                    pressureTV.text = pressure

                    val clouds = hourObj.getInt("cloud").toString()
                    cloudTV.text = clouds

                    val storm = hourObj.getInt("chance_of_rain")
                    if (storm.equals(0)){
                        stormTV.text = "No Storm"
                    }else{
                        stormTV.text = "Storm"
                    }
                }



            }catch (e: JSONException){
                e.printStackTrace()

            }
        },{
            error->
            Toast.makeText(this,"Fail to Get Weather Data",Toast.LENGTH_SHORT).show()
        })

        queue.add(request)


    }

    fun getLocationName(latitude: Double, longitude: Double): String {
        var cityName = "Not Found"
        var gcd = Geocoder(baseContext, Locale.getDefault())
        try {
            val addresses: List<Address> = gcd.getFromLocation(
                latitude, longitude, 10
            ) as List<Address>
            cityName = addresses.get(0).locality
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return cityName
    }
}