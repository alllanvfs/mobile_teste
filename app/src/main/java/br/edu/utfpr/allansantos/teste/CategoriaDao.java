package br.edu.utfpr.allansantos.teste;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import androidx.room.Delete;
import java.util.List;

@Dao
public interface CategoriaDao {

    @Insert
    long insert(Categoria categoria);

    @Update
    void update(Categoria categoria);

    @Delete
    void delete(Categoria categoria);

    @Query("SELECT * FROM categorias WHERE id = :id")
    Categoria findById(long id);

    @Query("SELECT * FROM categorias ORDER BY nome ASC")
    List<Categoria> getAll();

    @Query("SELECT * FROM categorias WHERE nome = :nome LIMIT 1")
    Categoria findByName(String nome);
}
