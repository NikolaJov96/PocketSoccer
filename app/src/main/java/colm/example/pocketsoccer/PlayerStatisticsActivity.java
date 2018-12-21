package colm.example.pocketsoccer;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.util.List;

import colm.example.pocketsoccer.game_model.GameViewModel;
import colm.example.pocketsoccer.database.entity.Score;

class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {

    private LiveData<List<Score>> scores;

    private Context context;

    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView player1Name;
        TextView player2Name;
        TextView score;

        ViewHolder(View view) {
            super(view);
            this.player1Name = view.findViewById(R.id.item_p1);
            this.player2Name = view.findViewById(R.id.item_p2);
            this.score = view.findViewById(R.id.item_score);
        }
    }

    RecyclerViewAdapter(LiveData<List<Score>> scores, Context context) {
        this.scores = scores;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.recycler_view_item_layout, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        viewHolder.player1Name.setText(scores.getValue().get(i).getFirstPlayerName());
        viewHolder.player2Name.setText(scores.getValue().get(i).getSecondPlayerName());
        viewHolder.score.setText(context.getString(
                R.string.stats_score_string_template,
                scores.getValue().get(i).getFirstPlayerScore(),
                scores.getValue().get(i).getSecondPlayerScore()));
    }

    @Override
    public int getItemCount() {
        if (scores.getValue() == null) {
            return 0;
        } else {
            return scores.getValue().size();
        }
    }

}

public class PlayerStatisticsActivity extends AppCompatActivity {

    private Button resetButton;
    private Button backButton;

    private RecyclerView recyclerView;
    private RecyclerView.Adapter adapter;
    private RecyclerView.LayoutManager layoutManager;

    private GameViewModel model;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player_statistics);

        model = ViewModelProviders.of(this).get(GameViewModel.class);
        Context context = this;
        model.getAllScores().observe(this, scores -> {
            adapter = new RecyclerViewAdapter(model.getAllScores(), context);
            recyclerView.setAdapter(adapter);
            adapter.notifyDataSetChanged();
        });

        resetButton = findViewById(R.id.reset_stats_button);
        resetButton.setOnClickListener(v -> model.deleteAllScores());

        backButton = findViewById(R.id.back_stats_button);
        backButton.setOnClickListener(v -> finish());

        recyclerView = findViewById(R.id.player_statistics_recycler_view);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        adapter = new RecyclerViewAdapter(model.getAllScores(), this);
        recyclerView.setAdapter(adapter);
    }
}
