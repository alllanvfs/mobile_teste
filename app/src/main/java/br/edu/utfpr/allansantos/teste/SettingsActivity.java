package br.edu.utfpr.allansantos.teste;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

public class SettingsActivity extends AppCompatActivity {

    public static final String ARQUIVO = "br.edu.utfpr.allansantos.teste.PREFERENCIAS";
    public static final String MODO_ORDENACAO = "MODO_ORDENACAO";
    public static final String ORDENAR_POR_DESCRICAO = "DESCRICAO";
    public static final String ORDENAR_POR_VALOR = "VALOR";

    private RadioGroup radioGroupOrdenacao;
    private RadioButton radioButtonPorDescricao, radioButtonPorValor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        setTitle(getString(R.string.settings_title));

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        radioGroupOrdenacao = findViewById(R.id.radioGroupOrdenacao);
        radioButtonPorDescricao = findViewById(R.id.radioButtonPorDescricao);
        radioButtonPorValor = findViewById(R.id.radioButtonPorValor);

        carregarPreferencia();

        radioGroupOrdenacao.setOnCheckedChangeListener((group, checkedId) -> {
            salvarPreferencia(checkedId);
        });

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                setResult(Activity.RESULT_OK);
                finish();
            }
        });
    }

    private void carregarPreferencia() {
        SharedPreferences shared = getSharedPreferences(ARQUIVO, Context.MODE_PRIVATE);
        String modo = shared.getString(MODO_ORDENACAO, ORDENAR_POR_DESCRICAO);

        if (modo.equals(ORDENAR_POR_DESCRICAO)) {
            radioButtonPorDescricao.setChecked(true);
        } else {
            radioButtonPorValor.setChecked(true);
        }
    }

    private void salvarPreferencia(int checkedId) {
        SharedPreferences shared = getSharedPreferences(ARQUIVO, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = shared.edit();

        String modo = ORDENAR_POR_DESCRICAO;
        if (checkedId == R.id.radioButtonPorValor) {
            modo = ORDENAR_POR_VALOR;
        }

        editor.putString(MODO_ORDENACAO, modo);
        editor.apply();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            setResult(Activity.RESULT_OK);
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}

