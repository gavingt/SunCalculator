package com.gavinsappcreations.sunrisesunsettimes

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.gavinsappcreations.sunrisesunsettimes.R


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        title = "Sunrise and Sunset Times"
    }

}
