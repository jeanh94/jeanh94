package com.example.gestiondeoptica.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.gestiondeoptica.db.dao.CustomerDao
import com.example.gestiondeoptica.db.dao.OrderDao
import com.example.gestiondeoptica.db.dao.FrameDao
import com.example.gestiondeoptica.db.dao.LensDao
import com.example.gestiondeoptica.db.dao.CustomerDebtDao
import com.example.gestiondeoptica.db.dao.MonthlyTaxDao
import com.example.gestiondeoptica.db.entity.Customer
import com.example.gestiondeoptica.db.entity.Order
import com.example.gestiondeoptica.db.entity.Frame
import com.example.gestiondeoptica.db.entity.Lens
import com.example.gestiondeoptica.db.entity.CustomerDebt
import com.example.gestiondeoptica.db.entity.DebtPayment
import com.example.gestiondeoptica.db.entity.MonthlyTax

@Database(entities = [Customer::class, Order::class, Frame::class, Lens::class, CustomerDebt::class, DebtPayment::class, MonthlyTax::class], version = 4, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun customerDao(): CustomerDao
    abstract fun orderDao(): OrderDao
    abstract fun frameDao(): FrameDao
    abstract fun lensDao(): LensDao
    abstract fun customerDebtDao(): CustomerDebtDao
    abstract fun monthlyTaxDao(): MonthlyTaxDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "gestion_optica_database"
                )
                // Wipes and rebuilds instead of migrating if no Migration object.
                // Migration is not part of this task.
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                // return instance
                instance
            }
        }
    }
}
