package com.example.testproject

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.securestorage.DataStoreManager

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
val store:DataStoreManager=DataStoreManager(context = baseContext)

        store.saveData("Test","saif")

        val returnData=store.readData("Test")
//        println(returnData)

    }
}