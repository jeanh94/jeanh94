package com.example.gestiondeoptica

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gestiondeoptica.db.AppDatabase
import com.example.gestiondeoptica.db.dao.FrameDao
import com.example.gestiondeoptica.db.entity.Frame
import com.google.android.material.floatingactionbutton.FloatingActionButton

class FrameInventoryActivity : AppCompatActivity() {

    private lateinit var rvFrameList: RecyclerView
    private lateinit var fabAddFrame: FloatingActionButton

    private lateinit var frameDao: FrameDao
    private lateinit var frameAdapter: FrameAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_frame_inventory)

        title = getString(R.string.frame_inventory_title) // Set activity title

        rvFrameList = findViewById(R.id.rv_frame_list)
        fabAddFrame = findViewById(R.id.fab_add_frame)

        frameDao = AppDatabase.getDatabase(applicationContext).frameDao()

        setupRecyclerView()
        loadFrames()

        fabAddFrame.setOnClickListener {
            val intent = Intent(this, AddEditFrameActivity::class.java)
            startActivity(intent)
        }
    }

    private fun setupRecyclerView() {
        frameAdapter = FrameAdapter()
        rvFrameList.adapter = frameAdapter
        rvFrameList.layoutManager = LinearLayoutManager(this)

        frameAdapter.setOnItemClickListener(object : FrameAdapter.OnItemClickListener {
            override fun onItemClick(frame: Frame) {
                val intent = Intent(this@FrameInventoryActivity, AddEditFrameActivity::class.java).apply {
                    putExtra(AddEditFrameActivity.EXTRA_FRAME_ID, frame.id)
                }
                startActivity(intent)
            }
        })
    }

    private fun loadFrames() {
        frameDao.getAllFrames().observe(this) { frames ->
            frameAdapter.submitList(frames)
        }
        // Optionally, you could also observe frameDao.getLowStockFrames(10)
        // and update UI or show a separate list/notification.
        // For now, low stock is visually indicated in the adapter.
    }

    override fun onResume() {
        super.onResume()
        // Data will be refreshed by LiveData automatically
        // but if you had a non-LiveData source, you'd refresh here.
    }
}
