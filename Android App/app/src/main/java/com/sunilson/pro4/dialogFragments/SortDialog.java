package com.sunilson.pro4.dialogFragments;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.sunilson.pro4.R;
import com.sunilson.pro4.adapters.SimpleDialogAdapter;
import com.sunilson.pro4.utilities.Constants;

/**
 * @author Linus Weiss
 *
 * Dialog used for sorting the "Now Live" Feed
 */

public class SortDialog extends BaseDialog {

    private SimpleDialogAdapter simpleDialogAdapter;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);

        View view = inflater.inflate(R.layout.pick_image_dialog, null);
        builder.setView(view);

        //Setup the Listview with the available options
        final ListView listView = (ListView) view.findViewById(R.id.pick_image_dialog_listView);
        simpleDialogAdapter = new SimpleDialogAdapter(getContext(), R.layout.simple_dialog_row);
        listView.setAdapter(simpleDialogAdapter);
        simpleDialogAdapter.add(getActivity().getString(R.string.liveticker_sort_dialog_live), R.drawable.ic_whatshot_black_24dp);
        simpleDialogAdapter.add(getActivity().getString(R.string.liveticker_sort_dialog_not_live), R.drawable.ic_archive_black_24dp);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String string = simpleDialogAdapter.getStringAtPosition(i);
                Intent intent = new Intent();

                //Return result depending on chosen list item
                if (string.equals(getString(R.string.liveticker_sort_dialog_live))) {
                    getTargetFragment().onActivityResult(getTargetRequestCode(), Constants.SORT_DIALOG_RESULT_LIVE, intent);
                } else {
                    getTargetFragment().onActivityResult(getTargetRequestCode(), Constants.SORT_DIALOG_RESULT_NOT_LIVE, intent);
                }
                getDialog().dismiss();
            }
        });

        return builder.create();
    }

    /**
     * Get instance of Sorting dialog
     *
     * @return Instance of SortDialog
     */
    public static SortDialog newInstance() {
        SortDialog sortDialog = new SortDialog();
        return sortDialog;
    }
}
