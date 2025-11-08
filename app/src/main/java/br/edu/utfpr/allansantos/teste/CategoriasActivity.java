package br.edu.utfpr.allansantos.teste;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class CategoriasActivity extends AppCompatActivity {

    private RecyclerView recyclerViewCategorias;
    private CategoriaAdapter adapter;
    private List<Categoria> listaCategorias;
    private AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_categorias);
        setTitle(getString(R.string.category_list_title));

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        db = AppDatabase.getDatabase(getApplicationContext());

        recyclerViewCategorias = findViewById(R.id.recyclerViewCategorias);
        listaCategorias = new ArrayList<>();
        adapter = new CategoriaAdapter(listaCategorias);

        recyclerViewCategorias.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewCategorias.setAdapter(adapter);

        carregarCategorias();
    }

    private void carregarCategorias() {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            List<Categoria> categoriasDaBd = db.categoriaDao().getAll();
            runOnUiThread(() -> {
                listaCategorias.clear();
                listaCategorias.addAll(categoriasDaBd);
                adapter.notifyDataSetChanged();
            });
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}