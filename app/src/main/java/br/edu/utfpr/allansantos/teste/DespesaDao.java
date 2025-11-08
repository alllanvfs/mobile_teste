package br.edu.utfpr.allansantos.teste;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import androidx.room.Delete;
import java.util.List;

@Dao
public interface DespesaDao {

    @Insert
    long insert(Despesa despesa);

    @Update
    void update(Despesa despesa);

    @Delete
    void delete(Despesa despesa);

    @Query("SELECT * FROM despesas WHERE id = :id")
    Despesa findById(long id);

    @Query("SELECT * FROM despesas")
    List<Despesa> getAll();

    @Query("DELETE FROM despesas")
    void deleteAll();
}
