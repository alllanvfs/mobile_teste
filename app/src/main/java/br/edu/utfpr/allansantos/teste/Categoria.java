package br.edu.utfpr.allansantos.teste;

import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;
import androidx.annotation.NonNull;

@Entity(tableName = "categorias",
        indices = {@Index(value = {"nome"}, unique = true)})
public class Categoria {

    @PrimaryKey(autoGenerate = true)
    private long id;

    @NonNull
    private String nome;

    public Categoria(@NonNull String nome) {
        this.nome = nome;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    @NonNull public String getNome() { return nome; }
    public void setNome(@NonNull String nome) { this.nome = nome; }

    @NonNull
    @Override
    public String toString() {
        return nome;
    }
}
