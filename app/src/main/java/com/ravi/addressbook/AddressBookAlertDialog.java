package com.ravi.addressbook;



import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;


public class AddressBookAlertDialog {

    private Context mContext;
    android.app.AlertDialog.Builder builder;
    Activity activity;

    public AddressBookAlertDialog(Context mContext, Activity activity){
        this.mContext = mContext;
        this.activity = activity;
        builder = new android.app.AlertDialog.Builder(mContext, R.style.MyDialogTheme);
    }

    public void alertDialog(String title, String message){
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setIconAttribute(android.R.attr.alertDialogIcon);
        builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                //Toast.makeText(AddLanguage.this, "Yaay", Toast.LENGTH_SHORT).show();

            }});
        activity.runOnUiThread(new Runnable() {
            public void run() {
                builder.show();
            }
        });
    }
}
