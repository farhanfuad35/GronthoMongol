package com.example.gronthomongol;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.fragment.app.DialogFragment;

public class SortByDialogFragment extends DialogFragment {
    String[] sortByArray = new String[2];

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        sortByArray[0] = getString(R.string.book_name);
        sortByArray[1] = getString(R.string.writer_name);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Sort By")
                .setItems(sortByArray, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // The 'which' argument contains the index position
                        // of the selected item
                    }
                });
        return builder.create();
}

}
