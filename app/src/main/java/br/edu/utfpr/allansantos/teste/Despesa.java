package br.edu.utfpr.allansantos.teste;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;
import androidx.annotation.NonNull;
import java.io.Serializable;
import java.time.LocalDate;


@Entity(tableName = "despesas",
        foreignKeys = @ForeignKey(entity = Categoria.class,
                parentColumns = "id",
                childColumns = "categoriaId",
                onDelete = ForeignKey.RESTRICT))
@TypeConverters({Converters.class})

public class Despesa implements Serializable, Comparable<Despesa> {

    @PrimaryKey(autoGenerate = true)
    private long id;

    @NonNull
    private String descricao;

    private double valor;

    @ColumnInfo(index = true)
    private long categoriaId;

    private LocalDate data;

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    @NonNull public String getDescricao() { return descricao; }
    public void setDescricao(@NonNull String descricao) { this.descricao = descricao; }
    public double getValor() { return valor; }
    public void setValor(double valor) { this.valor = valor; }
    public long getCategoriaId() { return categoriaId; }
    public void setCategoriaId(long categoriaId) { this.categoriaId = categoriaId; }
    public LocalDate getData() { return data; }
    public void setData(LocalDate data) { this.data = data; }

    public Despesa() {}

    public Despesa(@NonNull String descricao, double valor, long categoriaId, LocalDate data) {
        this.descricao = descricao;
        this.valor = valor;
        this.categoriaId = categoriaId;
        this.data = data;
    }

    @Override
    public int compareTo(@NonNull Despesa outraDespesa) {
        if (this.descricao == null && outraDespesa.getDescricao() == null) {
            return 0;
        } else if (this.descricao == null) {
            return -1;
        } else if (outraDespesa.getDescricao() == null) {
            return 1;
        } else {
            return this.descricao.compareToIgnoreCase(outraDespesa.getDescricao());
        }
    }

    @NonNull
    @Override
    public String toString() {
        return descricao + " (" + valor + ")";
    }
}

