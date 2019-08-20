package com.obcomdeveloper.realmusic.Utils;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatDialogFragment;

import com.obcomdeveloper.realmusic.R;

public class TrackDeleteAlertDialog extends AppCompatDialogFragment {

    private TextView title, text;
    private TrackDeleteDialogListener listener;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder  builder=new AlertDialog.Builder(getActivity());

        LayoutInflater inflater=getActivity().getLayoutInflater();
        View view=inflater.inflate(R.layout.alert_dialog_custom_view,null);

        builder.setView(view)
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                }).setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                listener.onConfirmDelete();
            }
        });

        title=view.findViewById(R.id.first_tv);
        text=view.findViewById(R.id.second_tv);


        return builder.create();

    }
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            listener = (TrackDeleteDialogListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() +
                    "must implement ExampleDialogListener");
        }
    }

    public interface TrackDeleteDialogListener {
        void onConfirmDelete();
    }
}
