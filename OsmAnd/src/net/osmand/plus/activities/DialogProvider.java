package net.osmand.plus.activities;

import android.app.Dialog;

interface DialogProvider {

    Dialog onCreateDialog(int id);

    void onPrepareDialog(int id, Dialog dialog);

}
