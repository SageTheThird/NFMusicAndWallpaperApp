package com.obcomdeveloper.realmusic.Utils;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.obcomdeveloper.realmusic.R;

import java.io.Serializable;
import java.util.Objects;

public class MaterialBottomSheet extends BottomSheetDialogFragment {

    private BottomSheetListener mListener;

    public interface BottomSheetListener extends Serializable {
        void onConfirmClicked();
        void onCancelClicked();
    }


    private int resourceLay;
    private Button confirmBtn,cancelbtn;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view=inflater.inflate(resourceLay,container,false);

        confirmBtn=view.findViewById(R.id.confirm_bottomSheet);
        cancelbtn=view.findViewById(R.id.cancel_bottomSheet);

        try {
            mListener = (BottomSheetListener) Objects.requireNonNull(getArguments()).getSerializable("dialogInterface");
        }catch (NullPointerException e){
            e.printStackTrace();
        }


        confirmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mListener.onConfirmClicked();
                dismiss();
            }
        });

        cancelbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onCancelClicked();
                dismiss();
            }
        });
        return view;
    }

    public void setLayout(int resourceLayout){
        this.resourceLay=resourceLayout;
    }

    public static MaterialBottomSheet getInstance(BottomSheetListener dialogInterface,int position) {
        MaterialBottomSheet fragmentDialog = new MaterialBottomSheet();

        // set fragment arguments
        Bundle args = new Bundle();
        args.putSerializable("dialogInterface", dialogInterface);
        args.putInt("position",position);
        fragmentDialog.setArguments(args);

        return fragmentDialog;
    }
}
