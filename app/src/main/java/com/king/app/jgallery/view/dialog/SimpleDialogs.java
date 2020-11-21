package com.king.app.jgallery.view.dialog;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.king.app.jgallery.R;

/**
 * @desc
 * @auth 景阳
 * @time 2018/3/23 0023 20:20
 */

public class SimpleDialogs {

    public interface OnDialogActionListener {
        void onOk(String name);
    }

    public void openInputDialog(Context context, String msg, final OnDialogActionListener listener) {
        openInputDialog(context, msg, null, listener);
    }

    public void openInputDialog(Context context, String msg, String initialText, final OnDialogActionListener listener) {
        LinearLayout layout = new LinearLayout(context);
        layout.setPadding(40, 10, 40, 10);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        EditText edit = new EditText(context);
        edit.setLayoutParams(params);
        if (!TextUtils.isEmpty(initialText)) {
            edit.setText(initialText);
        }
        layout.addView(edit);
        AlertDialog.Builder dialog = new AlertDialog.Builder(context);
        if (msg == null) {
            dialog.setMessage("input content");
        }
        else {
            dialog.setMessage(msg);
        }
        dialog.setView(layout);

        final EditText folderEdit = edit;
        dialog.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                String folderName = folderEdit.getText().toString();
                listener.onOk(folderName);
            }
        });
        dialog.setNegativeButton(R.string.cancel, null);
        dialog.show();
    }

    public void showConfirmDialog(Context context, String msg, DialogInterface.OnClickListener listener) {
        showWarningActionDialog(context, msg
                , context.getResources().getString(R.string.ok), listener
                , null, null
                , null, null);
    }

    public void showConfirmCancelDialog(Context context, String msg, DialogInterface.OnClickListener okListener, DialogInterface.OnClickListener cancelListener) {
        showWarningActionDialog(context, msg
                , context.getResources().getString(R.string.ok), okListener
                , context.getResources().getString(R.string.cancel), okListener
                , null, null);
    }

    public void showWarningActionDialog(Context context, String msg
            , String positiveText, DialogInterface.OnClickListener positiveListener
            , String neutralText, DialogInterface.OnClickListener neutralListener
            , String negativeText , DialogInterface.OnClickListener negativeListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Warning");
        builder.setMessage(msg);
        builder.setPositiveButton(positiveText, positiveListener);
        if (neutralText != null) {
            builder.setNeutralButton(neutralText, neutralListener);
        }
        if (negativeText != null) {
            builder.setNegativeButton(negativeText, negativeListener);
        }
        builder.show();
    }

}
