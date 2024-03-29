package com.example.pocketsoccer;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
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

import com.example.pocketsoccer.database.entity.Score;
import com.example.pocketsoccer.database.entity.TwoUsersScore;
import com.example.pocketsoccer.game_model.GameViewModel;

class TwoPlayersRecyclerViewAdapter extends RecyclerView.Adapter<TwoPlayersRecyclerViewAdapter.ViewHolder> {

    private LiveData<List<Score>> scores;

    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView player1Name;
        TextView player2Name;
        TextView score1;
        TextView score2;
        TextView time;

        ViewHolder(View view) {
            super(view);
            this.player1Name = view.findViewById(R.id.item_p1);
            this.player2Name = view.findViewById(R.id.item_p2);
            this.score1 = view.findViewById(R.id.item_score1);
            this.score2 = view.findViewById(R.id.item_score2);
            this.time = view.findViewById(R.id.game_duration);
        }
    }

    TwoPlayersRecyclerViewAdapter(LiveData<List<Score>> scores) {
        this.scores = scores;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.two_player_recycler_view_item_layout, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        viewHolder.player1Name.setText(scores.getValue().get(i).getFirstPlayerName());
        viewHolder.player2Name.setText(scores.getValue().get(i).getSecondPlayerName());
        viewHolder.score1.setText(MainActivity.mainActivity.getResources().getString(R.string.one_number_format, scores.getValue().get(i).getFirstPlayerScore()));
        viewHolder.score2.setText(MainActivity.mainActivity.getResources().getString(R.string.one_number_format, scores.getValue().get(i).getSecondPlayerScore()));
        int t = scores.getValue().get(i).getGameDuration();
        viewHolder.time.setText(MainActivity.mainActivity.getResources().getString(R.string.time_print_format, t / 60, t % 60));
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

public class TwoPlayerStatisticsActivity extends AppCompatActivity {

    private Button resetButton;
    private Button backButton;

    private RecyclerView recyclerView;
    private RecyclerView.Adapter adapter;
    private RecyclerView.LayoutManager layoutManager;

    private TextView scoreTextView;

    private GameViewModel model;

    private String p1;
    private String p2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_two_player_statistics);

        Intent intent = getIntent();
        if (!intent.hasExtra(GameActivity.PLAYER_1_EXTRA) || !intent.hasExtra(GameActivity.PLAYER_2_EXTRA)) return;
        p1 = intent.getStringExtra(GameActivity.PLAYER_1_EXTRA);
        p2 = intent.getStringExtra(GameActivity.PLAYER_2_EXTRA);

        model = ViewModelProviders.of(this).get(GameViewModel.class);
        model.updateFilter(new GameViewModel.FilterStruct(p1, p2));
        model.getAllScores().observe(this, scores -> {
            adapter = new TwoPlayersRecyclerViewAdapter(model.getAllScores());
            recyclerView.setAdapter(adapter);
            adapter.notifyDataSetChanged();
        });
        model.getAllTwoPlayerScores().observe(this, score -> {
            if (model.getAllTwoPlayerScores().getValue() != null) {
                for (TwoUsersScore twoUsersScore : model.getAllTwoPlayerScores().getValue()) {
                    if (twoUsersScore.getFirstPlayerName().equals(p1) && twoUsersScore.getSecondPlayerName().equals(p2)) {
                        scoreTextView.setText(MainActivity.mainActivity.getResources().getString(R.string.scores_print_format,
                                p1, twoUsersScore.getFirstPlayerScore(), twoUsersScore.getSecondPlayerScore(), p2));
                        break;
                    }
                }
            }
        });

        resetButton = findViewById(R.id.reset_stats_button);
        resetButton.setOnClickListener(v -> model.deleteTowPlayers(p1, p2));

        backButton = findViewById(R.id.back_stats_button);
        backButton.setOnClickListener(v -> finish());

        recyclerView = findViewById(R.id.player_statistics_recycler_view);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        if (model.getAllScores() != null) {
            adapter = new TwoPlayersRecyclerViewAdapter(model.getAllScores());
            recyclerView.setAdapter(adapter);
        }

        scoreTextView = findViewById(R.id.score_text_view);
    }
}
