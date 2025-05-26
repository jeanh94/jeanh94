package com.example.gestiondeoptica

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gestiondeoptica.db.AppDatabase
import com.example.gestiondeoptica.db.dao.LensDao
import com.example.gestiondeoptica.db.entity.Lens
import com.google.android.material.floatingactionbutton.FloatingActionButton

class LensInventoryActivity : AppCompatActivity() {

    private lateinit var rvLensList: RecyclerView
    private lateinit var fabAddLens: FloatingActionButton

    private lateinit var lensDao: LensDao
    private lateinit var lensAdapter: LensAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lens_inventory)

        title = getString(R.string.lens_inventory_title) // Set activity title

        rvLensList = findViewById(R.id.rv_lens_list)
        fabAddLens = findViewById(R.id.fab_add_lens)

        lensDao = AppDatabase.getDatabase(applicationContext).lensDao()

        setupRecyclerView()
        loadLenses()

        fabAddLens.setOnClickListener {
            val intent = Intent(this, AddEditLensActivity::class.java)
            startActivity(intent)
        }
    }

    private fun setupRecyclerView() {
        lensAdapter = LensAdapter()
        rvLensList.adapter = lensAdapter
        rvLensList.layoutManager = LinearLayoutManager(this)

        lensAdapter.setOnItemClickListener(object : LensAdapter.OnItemClickListener {
            override fun onItemClick(lens: Lens) {
                val intent = Intent(this@LensInventoryActivity, AddEditLensActivity::class.java).apply {
                    putExtra(AddEditLensActivity.EXTRA_LENS_ID, lens.id)
                }
                startActivity(intent)
            }
        })
    }

    private fun loadLenses() {
        lensDao.getAllLenses().observe(this) { lenses ->
            lensAdapter.submitList(lenses)
        }
    }

    override fun onResume() {
        super.onResume()
        // Data will be refreshed by LiveData automatically
    }
}
