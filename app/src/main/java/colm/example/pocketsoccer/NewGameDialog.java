package colm.example.pocketsoccer;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.res.AssetManager;
import android.database.CursorJoiner;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;

import java.io.Serializable;

import colm.example.pocketsoccer.game_model.GameViewModel;

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

    private static final int NUMBER_OF_FLAGS = 5;

    private CheckBox checkBoxP1;
    private CheckBox checkBoxP2;
    private EditText editTextP1;
    private EditText editTextP2;
    private ImageButton btnLeftP1;
    private ImageButton btnRightP1;
    private ImageButton btnLeftP2;
    private ImageButton btnRightP2;
    private Button btnCancel;
    private Button btnStart;

    private NewGameDialogData data;

    private NewGameDialogListener callbackListener;

    public NewGameDialog() {
        data = new NewGameDialogData();
        data.p1Flag = 0;
        data.p2Flag = 0;
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
                editTextP1.setText("CPU 1");
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
                editTextP2.setText("CPU 2");
                editTextP2.setEnabled(false);
            } else {
                editTextP2.setText(data.p2Name);
                editTextP2.setEnabled(true);
            }
        });

        editTextP1 = view.findViewById(R.id.player1_name);

        editTextP2 = view.findViewById(R.id.player2_name);

        btnLeftP1 = view.findViewById(R.id.left_flag1_button);
        btnLeftP1.setOnClickListener(v -> {
            data.p1Flag = (data.p1Flag + NUMBER_OF_FLAGS - 1) % NUMBER_OF_FLAGS;
            updateImages();
        });
        btnRightP1 = view.findViewById(R.id.right_flag1_button);
        btnRightP1.setOnClickListener(v -> {
            data.p1Flag = (data.p1Flag + 1) % NUMBER_OF_FLAGS;
            updateImages();
        });

        btnLeftP2 = view.findViewById(R.id.left_flag2_button);
        btnLeftP2.setOnClickListener(v -> {
            data.p2Flag = (data.p2Flag + NUMBER_OF_FLAGS - 1) % NUMBER_OF_FLAGS;
            updateImages();
        });
        btnRightP2 = view.findViewById(R.id.right_flag2_button);
        btnRightP2.setOnClickListener(v -> {
            data.p2Flag = (data.p2Flag + 1) % NUMBER_OF_FLAGS;
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
            callbackListener.onFinishNewGameDialog(data);
            dismiss();
        });

        return view;
    }

    private void updateImages() {
        /*try {
            switch (data.p1Flag) {
                case 0:
                    fieldView.setImageBitmap(model.getGameAssetManager().getParquetBMP());
                    break;
                case 1:
                    fieldView.setImageBitmap(model.getGameAssetManager().getConcreteBMP());
                    break;
                case 2:
                    fieldView.setImageBitmap(model.getGameAssetManager().getGrassBMP());
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }*/
    }

}
