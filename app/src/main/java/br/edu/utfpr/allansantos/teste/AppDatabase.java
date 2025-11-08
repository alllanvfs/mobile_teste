package br.edu.utfpr.allansantos.teste;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.annotation.NonNull;

import java.time.LocalDate;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(entities = {Despesa.class, Categoria.class}, version = 1, exportSchema = false)
@TypeConverters({Converters.class})
public abstract class AppDatabase extends RoomDatabase {

    public abstract DespesaDao despesaDao();
    public abstract CategoriaDao categoriaDao();

    private static volatile AppDatabase INSTANCE;
    private static final int NUMBER_OF_THREADS = 4;

    static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "despesas_database")
                            .addCallback(sRoomDatabaseCallback)
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    private static RoomDatabase.Callback sRoomDatabaseCallback = new RoomDatabase.Callback() {
        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
            super.onCreate(db);

            databaseWriteExecutor.execute(() -> {
                CategoriaDao catDao = INSTANCE.categoriaDao();
                DespesaDao despesaDao = INSTANCE.despesaDao();

                long cat1Id = catDao.insert(new Categoria("category_food"));
                long cat2Id = catDao.insert(new Categoria("category_transport"));
                catDao.insert(new Categoria("category_leisure"));
                catDao.insert(new Categoria("category_health"));
                catDao.insert(new Categoria("category_bills"));
                catDao.insert(new Categoria("category_education"));
                catDao.insert(new Categoria("category_other"));

                despesaDao.insert(new Despesa("Lunch", 25.50, cat1Id, LocalDate.now()));
                despesaDao.insert(new Despesa("Gas", 100.0, cat2Id, LocalDate.now().minusDays(1)));
            });
        }
    };
}

