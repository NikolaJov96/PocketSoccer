package com.example.pocketsoccer;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModelProviders;
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

import com.example.pocketsoccer.database.entity.TwoUsersScore;
import com.example.pocketsoccer.game_model.GameViewModel;

class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {

    interface RecyclerViewClickListener {
        void RecyclerViewClicked(String p1, String p2);
    }
    private RecyclerViewClickListener recyclerViewClickListener;

    private LiveData<List<TwoUsersScore>> scores;

    static class ViewHolder extends RecyclerView.ViewHolder {

        View view;
        TextView player1Name;
        TextView player2Name;
        TextView score1;
        TextView score2;

        ViewHolder(View view) {
            super(view);
            this.view = view;
            this.player1Name = view.findViewById(R.id.item_p1);
            this.player2Name = view.findViewById(R.id.item_p2);
            this.score1 = view.findViewById(R.id.item_score1);
            this.score2 = view.findViewById(R.id.item_score2);
        }
    }

    RecyclerViewAdapter(LiveData<List<TwoUsersScore>> scores, RecyclerViewClickListener recyclerViewClickListener) {
        this.recyclerViewClickListener = recyclerViewClickListener;
        this.scores = scores;
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
        viewHolder.score1.setText(MainActivity.mainActivity.getResources().getString(R.string.one_number_format, scores.getValue().get(i).getFirstPlayerScore()));
        viewHolder.score2.setText(MainActivity.mainActivity.getResources().getString(R.string.one_number_format, scores.getValue().get(2).getSecondPlayerScore()));
        viewHolder.view.setOnClickListener(v ->
                recyclerViewClickListener.RecyclerViewClicked(
                        scores.getValue().get(i).getFirstPlayerName(),
                        scores.getValue().get(i).getSecondPlayerName()));
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

public class PlayerStatisticsActivity extends AppCompatActivity implements RecyclerViewAdapter.RecyclerViewClickListener {

    private Button resetButton;
    private Button backButton;

    private RecyclerView recyclerView;
    private RecyclerView.Adapter adapter;

    private GameViewModel model;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player_statistics);

        model = ViewModelProviders.of(this).get(GameViewModel.class);
        model.getAllTwoPlayerScores().observe(this, scores -> {
            adapter = new RecyclerViewAdapter(model.getAllTwoPlayerScores(), this);
            recyclerView.setAdapter(adapter);
            adapter.notifyDataSetChanged();
        });

        resetButton = findViewById(R.id.reset_stats_button);
        resetButton.setOnClickListener(v -> model.deleteAllScores());

        backButton = findViewById(R.id.back_stats_button);
        backButton.setOnClickListener(v -> finish());

        recyclerView = findViewById(R.id.player_statistics_recycler_view);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
    }

    @Override
    public void RecyclerViewClicked(String p1, String p2) {
        Intent intent = new Intent(this, TwoPlayerStatisticsActivity.class);
        intent.putExtra(GameActivity.PLAYER_1_EXTRA, p1);
        intent.putExtra(GameActivity.PLAYER_2_EXTRA, p2);
        startActivity(intent);
    }
}
