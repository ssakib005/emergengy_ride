package naru.crover.com.app.ViewHolder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import naru.crover.com.app.R;

public class HistoryViewHolder extends RecyclerView.ViewHolder {

    public TextView name, destination, cost;

    public HistoryViewHolder(View itemView) {
        super(itemView);
        destination = itemView.findViewById(R.id.destinationID);
        cost = itemView.findViewById(R.id.costID);
    }
}
