package colm.example.pocketsoccer;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

class RecyclerViewItem {

    public String player1Name;
    public String player2Name;
    public int player1Score;
    public int player2Score;

    public RecyclerViewItem(String player1Name, String player2Name, int player1Score, int player2Score) {
        this.player1Name = player1Name;
        this.player2Name = player2Name;
        this.player1Score = player1Score;
        this.player2Score = player2Score;
    }
}

class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {

    private RecyclerViewItem[] items;

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public TextView player1Name;
        public TextView player2Name;
        public TextView score;

        public ViewHolder(View view) {
            super(view);
            this.player1Name = view.findViewById(R.id.item_p1);
            this.player2Name = view.findViewById(R.id.item_p2);
            this.score = view.findViewById(R.id.item_score);
        }
    }

    public RecyclerViewAdapter(RecyclerViewItem[] items) {
        this.items = items;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.recycler_view_item_layout, viewGroup, false);
        ViewHolder vh = new ViewHolder(view);
        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        viewHolder.player1Name.setText(items[i].player1Name);
        viewHolder.player2Name.setText(items[i].player2Name);
        viewHolder.score.setText(items[i].player1Score + " : " + items[i].player2Score);
    }

    @Override
    public int getItemCount() {
        return items.length;
    }

}

public class PlayerStatisticsActivity extends AppCompatActivity {

    private Button resetButton;
    private Button backButton;

    private RecyclerView recyclerView;
    private RecyclerView.Adapter adapter;
    private RecyclerView.LayoutManager layoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player_statistics);

        resetButton = findViewById(R.id.reset_stats_button);
        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // reset stats
            }
        });

        backButton = findViewById(R.id.back_stats_button);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        recyclerView = findViewById(R.id.player_statistics_recycler_view);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        RecyclerViewItem items[] = new RecyclerViewItem[3];
        items[0] = new RecyclerViewItem("Nikola", "Milos", 1, 2);
        items[1] = new RecyclerViewItem("Nikola", "Petar", 3, 4);
        items[2] = new RecyclerViewItem("Milos", "Petar", 5, 6);

        adapter = new RecyclerViewAdapter(items);
        recyclerView.setAdapter(adapter);
    }
}
