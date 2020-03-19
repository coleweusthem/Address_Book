package com.ravi.addressbook;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;


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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ContactList extends AppCompatActivity {

    SwipeRefreshLayout refreshLayout;
    RecyclerView recyclerView;
    List<Contact> contactList;
    ContactListAdapter adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_list);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.contact_list_my_toolbar);
        setSupportActionBar(myToolbar);

        //getting the recyclerview from xml
        recyclerView = (RecyclerView) findViewById(R.id.contact_list_recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        refreshLayout = findViewById(R.id.contact_list_refresh_layout);
        //initializing the contactList
        contactList = new ArrayList<>();

        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                contactList = new ArrayList<>();
                new GetContacts(ContactList.this, ContactList.this).execute();
            }
        });
        //to get contactList
        new GetContacts(this, this).execute();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //return super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu, menu);
        MenuItem mSearch = menu.findItem(R.id.action_search);
        MenuItem addContact = menu.findItem(R.id.add_contact);
        MenuItem firstNameAscendingSort = menu.findItem(R.id.action_sort_ascending_fn);
        MenuItem firstNameDescendingSort = menu.findItem(R.id.action_sort_descending_fn);
        MenuItem lastNameAscendingSort = menu.findItem(R.id.action_sort_ascending_ln);
        MenuItem lastNameDescendingSort = menu.findItem(R.id.action_sort_descending_ln);
        MenuItem contactNumberAscendingSort = menu.findItem(R.id.action_sort_ascending_cn);
        MenuItem contactNumberDescendingSort = menu.findItem(R.id.action_sort_descending_cn);

        //sorting code start
        firstNameAscendingSort.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener(){

            @Override
            public boolean onMenuItemClick(MenuItem item) {
                //Collections.sort(contactList, Contact.BY_FIRSTNAME_DESCENDING);
                Collections.sort(contactList, (Contact p1, Contact p2) -> p1.getFirstName().compareTo(p2.getFirstName()));
                adapter.notifyDataSetChanged();
                return false;
            }
        });

        firstNameDescendingSort.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener(){

            @Override
            public boolean onMenuItemClick(MenuItem item) {
                Collections.sort(contactList, Contact.BY_FIRSTNAME_ASCENDING);
                Collections.sort(contactList, (Contact p1, Contact p2) -> p2.getFirstName().compareTo(p1.getFirstName()));
                adapter.notifyDataSetChanged();
                return false;
            }
        });

        lastNameAscendingSort.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener(){

            @Override
            public boolean onMenuItemClick(MenuItem item) {
                Collections.sort(contactList, (Contact p1, Contact p2) -> p1.getLastName().compareTo(p2.getLastName()));
                adapter.notifyDataSetChanged();
                return false;
            }
        });

        lastNameDescendingSort.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener(){

            @Override
            public boolean onMenuItemClick(MenuItem item) {
                Collections.sort(contactList, (Contact p1, Contact p2) -> p2.getLastName().compareTo(p1.getLastName()));
                adapter.notifyDataSetChanged();
                return false;
            }
        });

        contactNumberAscendingSort.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener(){

            @Override
            public boolean onMenuItemClick(MenuItem item) {
                Collections.sort(contactList, (Contact p1, Contact p2) -> p1.getContactNumber().compareTo(p2.getContactNumber()));
                adapter.notifyDataSetChanged();
                return false;
            }
        });

        contactNumberDescendingSort.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener(){

            @Override
            public boolean onMenuItemClick(MenuItem item) {
                Collections.sort(contactList, (Contact p1, Contact p2) -> p2.getContactNumber().compareTo(p1.getContactNumber()));
                adapter.notifyDataSetChanged();
                return false;
            }
        });
        //sorting code end
        addContact.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener(){

            @Override
            public boolean onMenuItemClick(MenuItem item) {
                Intent intent = new Intent(ContactList.this, EditContact.class);
                startActivity(intent);
                return false;
            }
        });

        SearchView mSearchView = (SearchView) mSearch.getActionView();
        mSearchView.setQueryHint("Search");

        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if(!query.equalsIgnoreCase("")) {
                    adapter.getFilter().filter(query);
                }else{
                    contactList = new ArrayList<>();
                    new GetContacts(ContactList.this, ContactList.this).execute();
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if(!newText.equalsIgnoreCase("")) {
                    adapter.getFilter().filter(newText);
                }else{
                    contactList = new ArrayList<>();
                    new GetContacts(ContactList.this, ContactList.this).execute();
                }
                return true;
            }
        });

        return super.onCreateOptionsMenu(menu);
    }

    private class GetContacts extends AsyncTask<Void, Void, String> {
        private ProgressDialog progressBar;
        android.app.AlertDialog.Builder builder;
        Context mContext;

        public GetContacts(ContactList activity, Context mContext){
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

            String methodName = "featchContacts";
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

                String urlParameters  = "contact=1";

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
                    AddressBookAlertDialog alertDialog = new AddressBookAlertDialog(ContactList.this, ContactList.this);
                    alertDialog.alertDialog("Network issue", Constant.NO_INTERNET_ERROR_MESSAGE);

                }else if(e instanceof SocketTimeoutException){
                    progressBar.dismiss();
                    AddressBookAlertDialog alertDialog = new AddressBookAlertDialog(ContactList.this, ContactList.this);
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
            System.out.println("featchContacts: "+result);
            try {
                JSONObject allLanguageList = new JSONObject(result);
                if(allLanguageList.get("status").toString().equalsIgnoreCase("ok")) {

                    JSONArray contacts = allLanguageList.getJSONArray("contacts");
                    contacts = contacts.getJSONArray(0);
                    JSONObject contact;
                    for(int i = 0; i<contacts.length();i++){
                        contact = contacts.getJSONObject(i);


                        contactList.add(new Contact(contact.getInt("id"),
                                contact.getString("firstName"),
                                contact.getString("lastName"),
                                contact.getString("email"),
                                contact.getString("contactNumber")));
                    }
                    adapter = new ContactListAdapter(ContactList.this, contactList);
                    recyclerView.setAdapter(adapter);
                    progressBar.dismiss();
                    refreshLayout.setRefreshing(false);
                }
                else{
                    Toast toast = Toast.makeText(getApplicationContext(),"Not available any contact.",Toast.LENGTH_LONG);
                    progressBar.dismiss();
                    toast.show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
