package org.keycloak.keycloakaccountdemo;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.ActionBar;
import android.app.Fragment;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.os.Build;
import android.widget.TextView;
import android.widget.Toast;

//import org.jboss.aerogear.android.http.HeaderAndBody;
//import org.jboss.aerogear.android.impl.http.HttpRestProvider;
import org.json.JSONObject;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;


public class KeyCloakAccount extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_key_cloak_account);
        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.key_cloak_account, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_key_cloak_account, container, false);
            return rootView;
        }

        @Override
        public void onResume() {
            super.onResume();
            final AccountManager am = AccountManager.get(getActivity());
            final Account[] accounts = am.getAccountsByType("org.keycloak.Account");

            if (accounts.length == 0) {
                Log.i(this.getClass().getName(), "No account");
                am.addAccount("org.keycloak.Account", "org.keycloak.Account", null, null, getActivity(), null, null);
            } else {
                Account account = accounts[0];
                Log.i(this.getClass().getName(),"Account:"+account.toString());
                fetchAccountInfo(account);
            }
        }

        private void fetchAccountInfo(final Account account) {

            final AccountManager am = AccountManager.get(getActivity());

            new AsyncTask<Void, Void, String>() {
                @Override
                protected String doInBackground(Void... voids) {

                    URL accountUrl = null;
                    try {

                        String origUrl;
                        //origUrl = "http://test.jarrebola.com/WebSocketTest/webresources/authtest";
                        origUrl="https://wattsec.com/auth/realms/test/account";
                        accountUrl = new URL(origUrl);

                        Bundle result = am.getAuthToken(account, "org.keycloak.Account", null, getActivity(), null, null).getResult();
                        if (result.containsKey(AccountManager.KEY_ERROR_MESSAGE)) {
                            throw new RuntimeException("Herf derf");
                        } else {
                            String token = result.getString(AccountManager.KEY_AUTHTOKEN);

                            //HttpRestProvider provider = new HttpRestProvider(accountUrl);
                            //provider.setDefaultHeader("Authorization", "bearer " + token);
                            //HeaderAndBody accountData = provider.get();
                            //String accountBody = new String(accountData.getBody());

                            //return accountBody;

                            HttpURLConnection conn = (HttpURLConnection) accountUrl.openConnection();
                            conn.setReadTimeout(10000);
                            conn.setConnectTimeout(15000);
                            conn.setRequestProperty("Content-Type", "application/json");
                            conn.setRequestProperty("Accept", "application/json");
                            conn.setRequestProperty("Authorization", "bearer " + token);
                            conn.setRequestMethod("GET");
                            conn.setDoInput(true);
                            conn.connect();

                            int responseCode = conn.getResponseCode();

                            Log.i(this.getClass().getName(), "ResponseCode:"+responseCode);
                            StringBuilder salida = new StringBuilder();
                            if (responseCode != 200) {
                                salida.append("ResponseCode : ").append(responseCode);
                            } else {
                                InputStream stream = conn.getInputStream();

                                Reader reader = null;
                                reader = new InputStreamReader(stream, "UTF-8");

                                char[] buffer = new char[1];
                                while (reader.read(buffer, 0, buffer.length) >= 0) {
                                    salida.append(buffer);
                                }

                                reader.close();
                            }

                            return salida.toString();
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                        throw new RuntimeException(e);
                    }

                }

                @Override
                protected void onPostExecute(String s) {
                    super.onPostExecute(s);
                    ((TextView)getActivity().findViewById(R.id.outputText)).setText(s);
                    Toast.makeText(getActivity(), s, Toast.LENGTH_LONG).show();
                }
            }.execute((Void[]) null);
        }

    }
}
