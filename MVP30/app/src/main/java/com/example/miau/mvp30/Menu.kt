package com.example.miau.mvp30

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageButton

class Menu : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)
        val bprof: ImageButton = findViewById(R.id.bprof) as ImageButton
        bprof.setOnClickListener {
            // Handler code here.
            val intent = Intent(this, RoomCreate::class.java)
            startActivity(intent)
        }

        val bpup: ImageButton = findViewById(R.id.bpup) as ImageButton
        bpup.setOnClickListener {
            // Handler code here.
            val intent = Intent(this, Access::class.java)
            startActivity(intent)
        }
    }
}
