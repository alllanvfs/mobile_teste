package br.edu.utfpr.allansantos.teste;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    public static final String MODO = "MODO";
    public static final int MODO_NOVO = 1;
    public static final int MODO_EDITAR = 2;
    public static final String DESPESA = "DESPESA";
    public static final String POSICAO = "POSICAO";

    private EditText editTextDescricao, editTextValor;
    private TextView textViewDataSelecionada;
    private Spinner spinnerCategoria;
    private CheckBox checkBoxPago;
    private Spinner spinnerFormaPagamento;

    private int modo;
    private Despesa despesaAtual;

    private ArrayAdapter<String> categoriaAdapter;
    private List<String> listaNomesCategorias;
    private Map<String, Long> mapaCategorias;

    private LocalDate dataSelecionada;
    private DateTimeFormatter dateFormatter;

    private AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = AppDatabase.getDatabase(getApplicationContext());

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        editTextDescricao = findViewById(R.id.editTextDescricao);
        editTextValor = findViewById(R.id.editTextValor);
        textViewDataSelecionada = findViewById(R.id.textViewDataSelecionada);
        spinnerCategoria = findViewById(R.id.spinnerCategoria);
        checkBoxPago = findViewById(R.id.checkBoxPago);
        spinnerFormaPagamento = findViewById(R.id.spinnerFormaPagamento);

        dateFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM);

        textViewDataSelecionada.setOnClickListener(v -> mostrarDatePicker());

        ArrayAdapter<CharSequence> adapterPagamento = ArrayAdapter.createFromResource(this,
                R.array.formas_pagamento, android.R.layout.simple_spinner_item);
        adapterPagamento.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFormaPagamento.setAdapter(adapterPagamento);

        carregarCategorias();

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        modo = bundle.getInt(MODO, MODO_NOVO);

        if (modo == MODO_EDITAR) {
            setTitle(getString(R.string.edit_title));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                despesaAtual = bundle.getSerializable(DESPESA, Despesa.class);
            } else {
                despesaAtual = (Despesa) bundle.getSerializable(DESPESA);
            }
            preencherFormulario();
        } else {
            setTitle(getString(R.string.add_title));
            despesaAtual = new Despesa();
            dataSelecionada = LocalDate.now();
            atualizarTextoData();
        }
    }

    private void mostrarDatePicker() {
        LocalDate dataParaMostrar = (dataSelecionada != null) ? dataSelecionada : LocalDate.now();
        new DatePickerDialog(this,
                (view, year, month, dayOfMonth) -> {
                    dataSelecionada = LocalDate.of(year, month + 1, dayOfMonth);
                    atualizarTextoData();
                },
                dataParaMostrar.getYear(),
                dataParaMostrar.getMonthValue() - 1,
                dataParaMostrar.getDayOfMonth()).show();
    }

    private void atualizarTextoData() {
        if (dataSelecionada != null) {
            textViewDataSelecionada.setText(dataSelecionada.format(dateFormatter));
        } else {
            textViewDataSelecionada.setText(getString(R.string.form_date_hint));
        }
    }

    private void carregarCategorias() {
        listaNomesCategorias = new ArrayList<>();
        mapaCategorias = new HashMap<>();

        categoriaAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, listaNomesCategorias);
        categoriaAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategoria.setAdapter(categoriaAdapter);

        AppDatabase.databaseWriteExecutor.execute(() -> {
            List<Categoria> categoriasDaBd = db.categoriaDao().getAll();

            List<String> nomesTraduzidos = new ArrayList<>();
            Map<String, Long> mapaTraduzido = new HashMap<>();

            Resources res = getResources();
            String packageName = getPackageName();

            for (Categoria cat : categoriasDaBd) {
                int resId = res.getIdentifier(cat.getNome(), "string", packageName);
                String nomeTraduzido;

                if (resId != 0) {
                    nomeTraduzido = getString(resId);
                } else {
                    nomeTraduzido = cat.getNome();
                }

                nomesTraduzidos.add(nomeTraduzido);
                mapaTraduzido.put(nomeTraduzido, cat.getId());
            }

            runOnUiThread(() -> {
                listaNomesCategorias.clear();
                listaNomesCategorias.addAll(nomesTraduzidos);
                mapaCategorias.clear();
                mapaCategorias.putAll(mapaTraduzido);

                categoriaAdapter.notifyDataSetChanged();

                if (modo == MODO_EDITAR && despesaAtual != null) {
                    selecionarCategoriaSpinner(despesaAtual.getCategoriaId());
                }
            });
        });
    }

    private void preencherFormulario() {
        if (despesaAtual != null) {
            editTextDescricao.setText(despesaAtual.getDescricao());
            editTextValor.setText(String.valueOf(despesaAtual.getValor()));
            dataSelecionada = despesaAtual.getData();
            atualizarTextoData();
        }
    }

    private void selecionarCategoriaSpinner(long categoriaId) {
        String nomeTraduzidoParaSelecionar = null;
        for (Map.Entry<String, Long> entry : mapaCategorias.entrySet()) {
            if (entry.getValue() == categoriaId) {
                nomeTraduzidoParaSelecionar = entry.getKey();
                break;
            }
        }

        if (nomeTraduzidoParaSelecionar != null) {
            for (int i = 0; i < categoriaAdapter.getCount(); i++) {
                String nomeNoSpinner = categoriaAdapter.getItem(i);
                if (nomeTraduzidoParaSelecionar.equals(nomeNoSpinner)) {
                    spinnerCategoria.setSelection(i);
                    break;
                }
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.menuItemSalvar) {
            salvarDespesa();
            return true;
        } else if (id == R.id.menuItemLimpar) {
            limparCampos();
            return true;
        } else if (id == android.R.id.home) {
            setResult(Activity.RESULT_CANCELED);
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void limparCampos() {
        editTextDescricao.setText(null);
        editTextValor.setText(null);
        if (categoriaAdapter.getCount() > 0) spinnerCategoria.setSelection(0);
        dataSelecionada = LocalDate.now();
        atualizarTextoData();
        checkBoxPago.setChecked(false);
        spinnerFormaPagamento.setSelection(0);
        editTextDescricao.requestFocus();
        Toast.makeText(this, R.string.toast_cleared, Toast.LENGTH_SHORT).show();
    }

    private void salvarDespesa() {
        String descricao = editTextDescricao.getText().toString();
        String valorString = editTextValor.getText().toString();
        String categoriaTraduzida = (String) spinnerCategoria.getSelectedItem();

        if (descricao.trim().isEmpty() || valorString.trim().isEmpty() || dataSelecionada == null || categoriaTraduzida == null) {
            Toast.makeText(this, R.string.toast_fill_all_fields, Toast.LENGTH_SHORT).show();
            return;
        }

        Long categoriaId = mapaCategorias.get(categoriaTraduzida);
        if (categoriaId == null) {
            Toast.makeText(this, R.string.toast_fill_all_fields, Toast.LENGTH_SHORT).show();
            return;
        }

        double valor;
        try {
            valor = Double.parseDouble(valorString);
        } catch (NumberFormatException e) {
            Toast.makeText(this, R.string.toast_invalid_value, Toast.LENGTH_SHORT).show();
            return;
        }

        despesaAtual.setDescricao(descricao);
        despesaAtual.setValor(valor);
        despesaAtual.setCategoriaId(categoriaId);
        despesaAtual.setData(dataSelecionada);

        AppDatabase.databaseWriteExecutor.execute(() -> {
            if (modo == MODO_NOVO) {
                db.despesaDao().insert(despesaAtual);
            } else {
                db.despesaDao().update(despesaAtual);
            }

            runOnUiThread(() -> {
                setResult(Activity.RESULT_OK);
                finish();
            });
        });
    }
}

