package br.edu.utfpr.allansantos.teste;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ActionMode;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DespesasActivity extends AppCompatActivity {

    private RecyclerView recyclerViewDespesas;
    private DespesaAdapter adapter;
    private List<Despesa> listaDeDespesas;
    private Map<Long, String> mapaCategorias;

    private ActivityResultLauncher<Intent> activityResultLauncher;

    private ActionMode actionMode;
    private int posicaoSelecionada = -1;

    private AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_despesas);
        setTitle(getString(R.string.list_title));

        db = AppDatabase.getDatabase(getApplicationContext());

        recyclerViewDespesas = findViewById(R.id.recyclerViewDespesas);
        listaDeDespesas = new ArrayList<>();
        mapaCategorias = new HashMap<>();

        adapter = new DespesaAdapter(listaDeDespesas, mapaCategorias, this);

        adapter.setOnItemClickListener(new DespesaAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                if (position >= 0 && position < listaDeDespesas.size()) {
                    Despesa d = listaDeDespesas.get(position);
                    String nomeCategoria = mapaCategorias.getOrDefault(d.getCategoriaId(), getString(R.string.category_unknown));
                    String mensagem = getString(R.string.toast_expense_details,
                            d.getDescricao(),
                            d.getValor(),
                            nomeCategoria);
                    Toast.makeText(DespesasActivity.this, mensagem, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onItemLongClick(int position) {

                if (actionMode != null) return;
                if (position >= 0 && position < listaDeDespesas.size()) {
                    posicaoSelecionada = position;
                    actionMode = startSupportActionMode(actionModeCallback);
                }
            }
        });

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerViewDespesas.setLayoutManager(layoutManager);
        recyclerViewDespesas.setHasFixedSize(true);
        recyclerViewDespesas.setAdapter(adapter);

        carregarDados();

        activityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    carregarDados();
                });
    }

    private void carregarDados() {

        AppDatabase.databaseWriteExecutor.execute(() -> {

            List<Categoria> todasCategorias = db.categoriaDao().getAll();

            mapaCategorias.clear();
            for (Categoria c : todasCategorias) {
                mapaCategorias.put(c.getId(), c.getNome());
            }

            List<Despesa> despesasDaBd = db.despesaDao().getAll();

            runOnUiThread(() -> {
                listaDeDespesas.clear();
                listaDeDespesas.addAll(despesasDaBd);

                adapter.setMapaCategorias(mapaCategorias);
                ordenarListaLocal();
                adapter.notifyDataSetChanged();
            });
        });
    }

    private void ordenarListaLocal() {
        SharedPreferences shared = getSharedPreferences(SettingsActivity.ARQUIVO, Context.MODE_PRIVATE);
        String modo = shared.getString(SettingsActivity.MODO_ORDENACAO, SettingsActivity.ORDENAR_POR_DESCRICAO);

        if (modo.equals(SettingsActivity.ORDENAR_POR_DESCRICAO)) {

            Collections.sort(listaDeDespesas);
        } else {
            listaDeDespesas.sort((d1, d2) -> Double.compare(d2.getValor(), d1.getValor()));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_despesas, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.menuItemAdicionar) {
            abrirCadastro(MainActivity.MODO_NOVO, -1);
            return true;
        } else if (id == R.id.menuItemSettings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            activityResultLauncher.launch(intent);
            return true;
        } else if (id == R.id.menuItemCategorias) {
            startActivity(new Intent(this, CategoriasActivity.class));
            return true;
        } else if (id == R.id.menuItemSobre) {
            startActivity(new Intent(this, AutoriaActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void abrirCadastro(int modo, int posicao) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra(MainActivity.MODO, modo);

        if (posicao >= 0 && posicao < listaDeDespesas.size()) {
            intent.putExtra(MainActivity.DESPESA, listaDeDespesas.get(posicao));
            intent.putExtra(MainActivity.POSICAO, posicao);
        }
        activityResultLauncher.launch(intent);
    }

    private final ActionMode.Callback actionModeCallback = new ActionMode.Callback() {

        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            mode.getMenuInflater().inflate(R.menu.menu_contextual_despesa, menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            int id = item.getItemId();

            if (id == R.id.menuItemEditar) {
                if (posicaoSelecionada != -1) {
                    abrirCadastro(MainActivity.MODO_EDITAR, posicaoSelecionada);
                }
                mode.finish();
                return true;
            } else if (id == R.id.menuItemExcluir) {
                if (posicaoSelecionada != -1) {
                    confirmarExclusao(mode);
                } else {
                    mode.finish();
                }
                return true;
            }
            return false;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            actionMode = null;
            posicaoSelecionada = -1;
        }
    };

    private void confirmarExclusao(ActionMode mode) {

        if (posicaoSelecionada < 0 || posicaoSelecionada >= listaDeDespesas.size()) {
            Toast.makeText(this, R.string.toast_delete_error, Toast.LENGTH_SHORT).show();
            mode.finish();
            return;
        }

        Despesa despesaParaApagar = listaDeDespesas.get(posicaoSelecionada);

        new AlertDialog.Builder(this)
                .setTitle(R.string.dialog_delete_title)
                .setMessage(getString(R.string.dialog_delete_message, despesaParaApagar.getDescricao()))
                .setPositiveButton(R.string.dialog_delete_confirm, (dialog, which) -> {

                    AppDatabase.databaseWriteExecutor.execute(() -> {
                        db.despesaDao().delete(despesaParaApagar);

                        runOnUiThread(() -> {

                            listaDeDespesas.remove(posicaoSelecionada);
                            adapter.notifyItemRemoved(posicaoSelecionada);
                            Toast.makeText(this, R.string.toast_deleted, Toast.LENGTH_SHORT).show();
                            mode.finish();
                        });
                    });
                })
                .setNegativeButton(R.string.dialog_cancel, (dialog, which) -> {

                    mode.finish();
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }
}

