package com.example.gestiondeoptica

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class InventoryMenuActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_inventory_menu)

        title = getString(R.string.inventory_menu_screen_title)

        val btnManageFrames = findViewById<Button>(R.id.btn_menu_manage_frames)
        val btnManageLenses = findViewById<Button>(R.id.btn_menu_manage_lenses)

        btnManageFrames.setOnClickListener {
            startActivity(Intent(this, FrameInventoryActivity::class.java))
        }

        btnManageLenses.setOnClickListener {
            startActivity(Intent(this, LensInventoryActivity::class.java))
        }
    }
}
