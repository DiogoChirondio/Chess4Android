package pt.isel.pdm.chess4android.history

import androidx.room.*
import pt.isel.pdm.chess4android.*
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.*

@Entity(tableName = "history_puzzle")
data class PuzzleEntity(
    @PrimaryKey val id: String,
    val png: String,
    val sol: String,
    var solved: Boolean,
    val timestamp: Date = Date.from(Instant.now().truncatedTo(ChronoUnit.DAYS))
) {
    fun isTodayPuzzle(): Boolean =
        timestamp.toInstant().compareTo(Instant.now().truncatedTo(ChronoUnit.DAYS)) == 0
}

class Converters {
    @TypeConverter
    fun fromTimestamp(value: Long) = Date(value)

    @TypeConverter
    fun dateToTimestamp(date: Date) = date.time
}

@Dao
interface HistoryPuzzleDao{
    @Insert
    fun insert(puzzle: PuzzleEntity)

    @Delete
    fun delete(puzzle: PuzzleEntity)

    @Query("UPDATE history_puzzle SET solved = 1 WHERE id = :update_id")
    fun updateSolvedState(update_id: String)

    @Query("SELECT * FROM history_puzzle ORDER BY id DESC LIMIT 100")
    fun getAll(): List<PuzzleEntity>

    @Query("SELECT * FROM history_puzzle ORDER BY id DESC LIMIT :count")
    fun getLast(count: Int): List<PuzzleEntity>
}

@Database(entities = [PuzzleEntity::class], version = 1)
@TypeConverters(Converters::class)
abstract class HistoryDatabase: RoomDatabase(){
    abstract fun getHistoryPuzzleDao(): HistoryPuzzleDao
}