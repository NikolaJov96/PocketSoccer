package com.example.pocketsoccer;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.Serializable;

import com.example.pocketsoccer.game_model.GameAssetManager;

public class NewGameDialog extends DialogFragment {

    public class NewGameDialogData implements Serializable {
        public String p1Name;
        public String p2Name;
        public boolean p1Cpu;
        public boolean p2Cpu;
        public int p1Flag;
        public int p2Flag;
    }

    public interface NewGameDialogListener {
        void onFinishNewGameDialog(NewGameDialogData data);
    }

    private CheckBox checkBoxP1;
    private CheckBox checkBoxP2;
    private EditText editTextP1;
    private EditText editTextP2;
    private ImageButton btnLeftP1;
    private ImageView flag1View;
    private ImageButton btnRightP1;
    private ImageButton btnLeftP2;
    private ImageView flag2View;
    private ImageButton btnRightP2;
    private Button btnCancel;
    private Button btnStart;

    private NewGameDialogData data;

    private NewGameDialogListener callbackListener;

    public NewGameDialog() {
        data = new NewGameDialogData();
        data.p1Flag = 0;
        data.p2Flag = 1;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        callbackListener = (NewGameDialogListener) context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.new_game_dialog_layout, container);

        checkBoxP1 = view.findViewById(R.id.player1_cpu);
        checkBoxP1.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                data.p1Name = editTextP1.getText().toString();
                editTextP1.setText(getString(R.string.cpu_1_name));
                editTextP1.setEnabled(false);
            } else {
                editTextP1.setText(data.p1Name);
                editTextP1.setEnabled(true);
            }
        });

        checkBoxP2 = view.findViewById(R.id.player2_cpu);
        checkBoxP2.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                data.p2Name = editTextP2.getText().toString();
                editTextP2.setText(getString(R.string.cpu_2_name));
                editTextP2.setEnabled(false);
            } else {
                editTextP2.setText(data.p2Name);
                editTextP2.setEnabled(true);
            }
        });

        editTextP1 = view.findViewById(R.id.player1_name);

        editTextP2 = view.findViewById(R.id.player2_name);

        int numberOfFlags = GameAssetManager.NUMBER_OF_FLAGS;
        btnLeftP1 = view.findViewById(R.id.left_flag1_button);
        btnLeftP1.setOnClickListener(v -> {
            data.p1Flag = (data.p1Flag + numberOfFlags - 1) % numberOfFlags;
            updateImages();
        });
        flag1View = view.findViewById(R.id.flag1_view);
        btnRightP1 = view.findViewById(R.id.right_flag1_button);
        btnRightP1.setOnClickListener(v -> {
            data.p1Flag = (data.p1Flag + 1) % numberOfFlags;
            updateImages();
        });

        btnLeftP2 = view.findViewById(R.id.left_flag2_button);
        btnLeftP2.setOnClickListener(v -> {
            data.p2Flag = (data.p2Flag + numberOfFlags - 1) % numberOfFlags;
            updateImages();
        });
        flag2View = view.findViewById(R.id.flag2_view);
        btnRightP2 = view.findViewById(R.id.right_flag2_button);
        btnRightP2.setOnClickListener(v -> {
            data.p2Flag = (data.p2Flag + 1) % numberOfFlags;
            updateImages();
        });

        btnCancel = view.findViewById(R.id.cancel_new_game);
        btnCancel.setOnClickListener(v -> dismiss());

        btnStart = view.findViewById(R.id.start_new_game);
        btnStart.setOnClickListener(v -> {
            data.p1Name = editTextP1.getText().toString();
            data.p2Name = editTextP2.getText().toString();
            data.p1Cpu = checkBoxP1.isChecked();
            data.p2Cpu = checkBoxP2.isChecked();
            if (data.p1Flag == data.p2Flag) {
                Toast.makeText(view.getContext(), "Chose different flags!", Toast.LENGTH_SHORT).show();
            } else if ((data.p1Name.equals(getString(R.string.cpu_1_name)) && !data.p1Cpu) || data.p1Name.equals(getString(R.string.cpu_2_name)) ||
                    data.p2Name.equals(getString(R.string.cpu_1_name)) || (data.p2Name.equals(getString(R.string.cpu_2_name)) && !data.p2Cpu)) {
                Toast.makeText(view.getContext(), "Can't use CPU reserved names for players!", Toast.LENGTH_SHORT).show();
            } else if (data.p1Name == data.p2Name) {
                Toast.makeText(view.getContext(), "Players can't have the same name!", Toast.LENGTH_SHORT).show();
            } else {
                callbackListener.onFinishNewGameDialog(data);
                dismiss();
            }
        });

        updateImages();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        checkBoxP2.setChecked(true);
    }

    private void updateImages() {
        GameAssetManager gam = GameAssetManager.getGameAssetManager();
        flag1View.setImageBitmap(gam.getFlag(data.p1Flag));
        flag2View.setImageBitmap(gam.getFlag(data.p2Flag));
    }

}
