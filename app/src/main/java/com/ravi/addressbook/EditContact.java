package com.ravi.addressbook;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;

public class EditContact extends AppCompatActivity {

    EditText etFirstName, etLastName, etContactNumber, etEmail;
    Button btUpdate;
    String firstName, lastName, contactNumber, email;
    Toolbar myToolbar;
    int id = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_contact);

        myToolbar = (Toolbar) findViewById(R.id.edit_contact_my_toolbar);
        setSupportActionBar(myToolbar);

        etFirstName = findViewById(R.id.edit_contact_first_name);
        etLastName = findViewById(R.id.edit_contact_last_name);
        etEmail = findViewById(R.id.edit_contact_email);
        etContactNumber = findViewById(R.id.edit_contact_contact_number);

        btUpdate = findViewById(R.id.button_update_contact);

        firstName = etFirstName.getText().toString();
        lastName = etLastName.getText().toString();
        email = etEmail.getText().toString();
        contactNumber = etContactNumber.getText().toString();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getIncomingIntent();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    public void getIncomingIntent(){
        if(getIntent().hasExtra("id")) {
            id = getIntent().getIntExtra("id", 0);
            firstName = getIntent().getStringExtra("firstName");
            lastName = getIntent().getStringExtra("lastName");
            email = getIntent().getStringExtra("email");
            contactNumber = getIntent().getStringExtra("contactNumber");

            if(id!=0){
                etFirstName.setText(firstName);
                etLastName.setText(lastName);
                etEmail.setText(email);
                etContactNumber.setText(contactNumber);
            }
        }else{
            btUpdate.setText("Add");
        }
    }

    public void updateContact(View view) {

        firstName = etFirstName.getText().toString();
        lastName = etLastName.getText().toString();
        email = etEmail.getText().toString();
        contactNumber = etContactNumber.getText().toString();

        if(firstName.equalsIgnoreCase("")){
            etFirstName.setError("Please enter first name");
            etFirstName.requestFocus();
            return;
        }
        if(lastName.equalsIgnoreCase("")){
            etLastName.setError("Please enter last name");
            etLastName.requestFocus();
            return;
        }
        if(email.equalsIgnoreCase("") || !email.contains("@")){
            etEmail.setError("Please enter valid email id");
            etEmail.requestFocus();
            return;
        }
        if(contactNumber.length()!=10){
            etContactNumber.setError("Please enter 10 digit mobile number");
            etContactNumber.requestFocus();
            return;
        }


        //Toast.makeText(getApplicationContext(), "moved on", Toast.LENGTH_SHORT).show();

        new addUpdateContact(EditContact.this, EditContact.this).execute();


    }


    private class addUpdateContact extends AsyncTask<Void, Void, String> {
        private ProgressDialog progressBar;
        android.app.AlertDialog.Builder builder;
        Context mContext;

        public addUpdateContact(EditContact activity, Context mContext){
            progressBar = new ProgressDialog(activity);
            builder = new android.app.AlertDialog.Builder(mContext, R.style.MyDialogTheme);
            this.mContext = mContext;
        }

        protected void onPreExecute(){
            progressBar.setMessage("Loading...");
            progressBar.show();
        }

        @Override
        protected String doInBackground(Void... voids) {
            URL url = null;
            BufferedReader reader = null;
            StringBuilder stringBuilder = new StringBuilder();

            String methodName = "addUpdateContacts";
            stringBuilder.append(Constant.PROTOCOL);
            stringBuilder.append(Constant.COLON);
            stringBuilder.append(Constant.FORWARD_SLASH);
            stringBuilder.append(Constant.FORWARD_SLASH);
            stringBuilder.append(Constant.WEB_SERVICE_HOST);
            stringBuilder.append(Constant.COLON);
            stringBuilder.append(Constant.WEB_SERVICE_PORT);
            stringBuilder.append(Constant.FORWARD_SLASH);
            stringBuilder.append(Constant.CONTEXT_PATH);
            stringBuilder.append(Constant.FORWARD_SLASH);
            stringBuilder.append(Constant.APPLICATION_PATH);
            stringBuilder.append(Constant.FORWARD_SLASH);
            stringBuilder.append(Constant.CLASS_PATH);
            stringBuilder.append(Constant.FORWARD_SLASH);
            stringBuilder.append(methodName);


            try {

                String urlParameters  = "id="+id+"&firsName="+firstName+"&lastName="+lastName
                        +"&email="+email+"&contactNumber="+contactNumber;

                byte[] postData       = urlParameters.getBytes();
                int    postDataLength = postData.length;
                url = new URL(stringBuilder.toString());
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                // uncomment this if you want to write output to this url
                connection.setDoOutput(true);
                connection.setDoInput(true);
                connection.setInstanceFollowRedirects( false );
                connection.setRequestProperty( "charset", "utf-8");
                connection.setRequestProperty( "Content-Length", Integer.toString( postDataLength ));
                connection.setRequestProperty( "Content-Type", "application/x-www-form-urlencoded");
                connection.setUseCaches( false );
                // give it 15 seconds to respond
                connection.setReadTimeout(15*1000);
                connection.setConnectTimeout(15*1000);
                try( DataOutputStream wr = new DataOutputStream( connection.getOutputStream())) {
                    wr.write( postData );
                }

                connection.connect();
                // read the output from the server
                stringBuilder = new StringBuilder();
                reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String line = null;
                while ((line = reader.readLine()) != null) {
                    stringBuilder.append(line);
                }

                System.out.println("response: "+stringBuilder.toString());
            }
            catch (Exception e) {
                if(e instanceof ConnectException){
                    progressBar.dismiss();
                    AddressBookAlertDialog alertDialog = new AddressBookAlertDialog(EditContact.this, EditContact.this);
                    alertDialog.alertDialog("Network issue", Constant.NO_INTERNET_ERROR_MESSAGE);

                }else if(e instanceof SocketTimeoutException){
                    progressBar.dismiss();
                    AddressBookAlertDialog alertDialog = new AddressBookAlertDialog(EditContact.this, EditContact.this);
                    alertDialog.alertDialog("Time out", "Please try again.");
                }
                e.printStackTrace();
                try {
                    throw e;
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
            finally {
                if (reader != null) {
                    try{
                        reader.close();
                    }
                    catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            return stringBuilder.toString();
        }
        @Override

        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            System.out.println("update contact: "+result);
            try {
                JSONObject allLanguageList = new JSONObject(result);
                if(allLanguageList.get("status").toString().equalsIgnoreCase("ok")) {

                    Toast toast = Toast.makeText(getApplicationContext(),"Contact Saved successfully...",Toast.LENGTH_LONG);
                    progressBar.dismiss();
                    Intent intent = new Intent(EditContact.this, ContactList.class);
                    startActivity(intent);
                }
                else{
                    Toast toast = Toast.makeText(getApplicationContext(),"Contact Not Saved, Please try agian...",Toast.LENGTH_LONG);
                    progressBar.dismiss();
                    toast.show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
