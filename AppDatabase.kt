import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(version = 1, entities = [], exportSchema = false)
abstract class AppDatabase : RoomDatabase()
{
    //abstract val myDAO: MyDAO

    companion object
    {
        val databaseName = "app_database"
        val instance get() = INSTANCE as AppDatabase

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase
        {
            synchronized(this)
            {
                return INSTANCE
                    ?: Room.databaseBuilder(context, AppDatabase::class.java, databaseName)
                        .fallbackToDestructiveMigration()
                        .allowMainThreadQueries()
                        .build()
                        .also { INSTANCE = it }
            }
        }

        fun create(context: Context)
        {
            INSTANCE = getDatabase(context)
        }
    }
}

/*
-- build.gradle (:app) --

apply plugin: 'kotlin-kapt'

dependencies {

    // Room https://developer.android.com/training/data-storage/room
    def room_version = "2.4.2"

    implementation "androidx.room:room-runtime:$room_version"
    annotationProcessor "androidx.room:room-compiler:$room_version"
    kapt "androidx.room:room-compiler:2.4.2"
    ...
}
*/