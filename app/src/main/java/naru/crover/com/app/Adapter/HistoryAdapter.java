package naru.crover.com.app.Adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import naru.crover.com.app.Models.History;
import naru.crover.com.app.R;

import naru.crover.com.app.ViewHolder.HistoryViewHolder;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryViewHolder> {

    private Context context;
    private List<History> histories;

    public HistoryAdapter(Context context, List<History> histories) {
        this.context = context;
        this.histories = histories;
    }

    @NonNull
    @Override
    public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.history_layout, parent, false);
        return new HistoryViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull HistoryViewHolder holder, int position) {
        History history = histories.get(position);
        holder.destination.setText("Destination: "+ history.getDestination());
        holder.cost.setText("Cost: "+history.getCost());
    }

    @Override
    public int getItemCount() {
        return histories.size();
    }
}
