package br.edu.utfpr.allansantos.teste;

import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.List;
import java.util.Map;

public class DespesaAdapter extends RecyclerView.Adapter<DespesaAdapter.DespesaViewHolder> {

    private List<Despesa> listaDespesas;
    private OnItemClickListener listener;
    private Map<Long, String> mapaCategorias;
    private DateTimeFormatter dateFormatter;
    private Context context;

    public interface OnItemClickListener {
        void onItemClick(int position);
        void onItemLongClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public DespesaAdapter(List<Despesa> listaDespesas, Map<Long, String> mapaCategorias, Context context) {
        this.listaDespesas = listaDespesas;
        this.mapaCategorias = mapaCategorias;
        this.context = context;
        this.dateFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT);
    }

    public void setMapaCategorias(Map<Long, String> mapaCategorias) {
        this.mapaCategorias = mapaCategorias;
    }

    @NonNull
    @Override
    public DespesaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.linha_lista_despesas, parent, false);
        return new DespesaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DespesaViewHolder holder, int position) {
        Despesa despesaAtual = listaDespesas.get(position);
        holder.textViewDescricao.setText(despesaAtual.getDescricao());

        String categoriaKey = mapaCategorias.getOrDefault(despesaAtual.getCategoriaId(), null);
        String nomeCategoria;

        if (categoriaKey != null) {
            Resources res = context.getResources();
            int resId = res.getIdentifier(categoriaKey, "string", context.getPackageName());

            if (resId != 0) {
                nomeCategoria = context.getString(resId);
            } else {
                nomeCategoria = context.getString(R.string.category_unknown);
            }
        } else {
            nomeCategoria = context.getString(R.string.category_unknown);
        }

        holder.textViewCategoria.setText(nomeCategoria);

        holder.textViewValor.setText(String.format("R$ %.2f", despesaAtual.getValor()));

        if (despesaAtual.getData() != null && holder.textViewData != null) {
            holder.textViewData.setText(despesaAtual.getData().format(dateFormatter));
        } else if (holder.textViewData != null) {
            holder.textViewData.setText("");
        }
    }

    @Override
    public int getItemCount() {
        return listaDespesas.size();
    }

    public class DespesaViewHolder extends RecyclerView.ViewHolder {
        public TextView textViewDescricao;
        public TextView textViewCategoria;
        public TextView textViewValor;
        public TextView textViewData;

        public DespesaViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewDescricao = itemView.findViewById(R.id.textViewDescricaoItem);
            textViewCategoria = itemView.findViewById(R.id.textViewCategoriaItem);
            textViewValor = itemView.findViewById(R.id.textViewValorItem);

            itemView.setOnClickListener(view -> {
                if (listener != null) {
                    int pos = getBindingAdapterPosition();
                    if (pos != RecyclerView.NO_POSITION) {
                        listener.onItemClick(pos);
                    }
                }
            });

            itemView.setOnLongClickListener(view -> {
                if (listener != null) {
                    int pos = getBindingAdapterPosition();
                    if (pos != RecyclerView.NO_POSITION) {
                        listener.onItemLongClick(pos);
                        return true;
                    }
                }
                return false;
            });
        }
    }
}

