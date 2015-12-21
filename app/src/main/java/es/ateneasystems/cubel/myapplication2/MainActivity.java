package es.ateneasystems.cubel.myapplication2;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.Context;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import org.apache.http.params.HttpConnectionParams;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Random;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static final String DEBUG_TAG = "WhatsAPP Emulador";
    private Context cntx_app;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            // fetch data
        } else {
            // display error
        }



        cntx_app = getApplicationContext(); // get application context
        //Now call below function to do the real task for you.




        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ejecutar_proceso_completo();
                /*
                AccountManager manager = (AccountManager) getSystemService(ACCOUNT_SERVICE);
                Account[] list = manager.getAccounts();
                Log.d("INFO", String.valueOf(list));
                */
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);


    }

    /**
     * Funciones
     */
    public void ejecutar_proceso_completo(){
        CargandoElementosSegundoPlano tarea = new CargandoElementosSegundoPlano();
        tarea.execute();
    }
    public boolean guardar_textos_db(JSONArray mensajes_usuarios) throws JSONException{

        ArrayList<String> comandos = new ArrayList<String>();
        int cantidad = mensajes_usuarios.length();
        JSONArray datos = mensajes_usuarios;
        for (int i=0; i<cantidad;i++ ){
            JSONObject json_data = null;
            json_data = datos.getJSONObject(i);
            long l1 = System.currentTimeMillis();
            long l2 = l1 / 1000L;
            String texto = json_data.getString("mensaje");
            String movil = json_data.getString("numero")+"@s.whatsapp.net";
            Random localRandom = new Random(20L);
            int k = localRandom.nextInt();
            String str1 = "sqlite3 /data/data/com.whatsapp/databases/msgstore.db \"INSERT INTO messages (key_remote_jid, key_from_me, key_id, status, needs_push, data, timestamp, MEDIA_URL, media_mime_type, media_wa_type, MEDIA_SIZE, media_name , latitude, longitude, thumb_image, remote_resource, received_timestamp, send_timestamp, receipt_server_timestamp, receipt_device_timestamp, raw_data, media_hash, recipient_count, media_duration, origin)VALUES ('"
                    + movil
                    + "', 1,'"
                    + l2
                    + "-"
                    + k
                    + "', 0,0, '"
                    + texto
                    + "',"
                    + l1
                    + ",'','', '0', 0,'', 0.0,0.0,'','',"
                    + l1
                    + ", -1, -1, -1,0 ,'',0,0,0); \"";

            String str2 = "sqlite3 /data/data/com.whatsapp/databases/msgstore.db \"insert into chat_list (key_remote_jid) select '"
                    + movil
                    + "' where not exists (select 1 from chat_list where key_remote_jid='"
                    + movil + "');\"";

            String str3 = "sqlite3 /data/data/com.whatsapp/databases/msgstore.db \"update chat_list set message_table_id = (select max(messages._id) from messages) where chat_list.key_remote_jid='"
                    + movil + "';\"";
            comandos.add(str1);
            comandos.add(str2);
            comandos.add(str3);
            Log.d(DEBUG_TAG, "Mensaje enviado a : " + json_data.getString("nombre"));
        }









        String str4 = "am force-stop com.whatsapp";
        String str5 = "monkey -p com.whatsapp -c android.intent.category.LAUNCHER 1";

        comandos.add(str4);
        comandos.add(str5);

       // String[] commands = {str1, str2, str3};
        Process p = null;
        try {
            p = Runtime.getRuntime().exec("su");
            DataOutputStream os = new DataOutputStream(p.getOutputStream());
            for (String tmpCmd : comandos) {
                os.writeBytes(tmpCmd + "\n");
            }
            os.writeBytes("exit\n");
            os.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }


        return true;
    }

    public boolean guardar_contacto_agenda(JSONObject info_usuario) throws JSONException {
        JSONObject datos = info_usuario;
        int startIndex = 2;
        int endIndex = info_usuario.getString("numero").length();
        String numero= info_usuario.getString("numero").substring(startIndex, endIndex);
        String nombre = info_usuario.getString("nombre");

        WritePhoneContact(nombre, numero,cntx_app);
        return true;
    }

    public JSONArray comprobar_mensajes_nuevos() throws IOException {
        InputStream is = null;
        try {
            //URL url = new URL("http://laura.ateneasystems.es/private/tabla.php");
            URL url = new URL("http://testandprobe.com/pedido_24h/webapp/get_mensajes_pendientes.php");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(10000 /* milliseconds */);
            conn.setConnectTimeout(15000 /* milliseconds */);
            conn.setRequestMethod("GET");

            conn.setDoInput(true);
            // Starts the query
            conn.connect();
            int response = conn.getResponseCode();
            String respuesta = conn.getResponseMessage();
            Log.d(DEBUG_TAG, "The response code is: " + response);
            Log.d(DEBUG_TAG, "The response text is: " + respuesta);
            is = conn.getInputStream();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder stringBuilder = new StringBuilder();

            String line;
            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line + '\n');
            }

            String jsonString = stringBuilder.toString();
            Log.d(DEBUG_TAG, "The response message is: " + jsonString);
            try {
                JSONArray jsonArray = new JSONArray(jsonString);
                // int cantidad = jsonArray.length();
                return jsonArray;
                /*Log.d(DEBUG_TAG, "The response cantidad is: " + cantidad);

                for (int i=0; i<cantidad;i++ ){
                    Log.d(DEBUG_TAG, "The response read is: " + jsonArray);
                    JSONObject json_data = jsonArray.getJSONObject(i);
                    Log.d(DEBUG_TAG, "The response read is: " + json_data.getString("numero"));
                    Log.d(DEBUG_TAG, "The response read is: " + json_data.getString("mensaje"));
                }
                */
            } catch (JSONException e) {
                e.printStackTrace();
            }


            // Convert the InputStream into a string
            //String contentAsString = readIt(is, len);
            //return contentAsString;

            // Makes sure that the InputStream is closed after the app is
            // finished using it.


        } finally {
            if (is != null) {
                is.close();
            }
        }

        return null;
    }

    public void WritePhoneContact(String displayName, String number,Context cntx /*App or Activity Ctx*/)
    {
        Context contetx 	= cntx; //Application's context or Activity's context
        String strDisplayName 	=  displayName; // Name of the Person to add
        String strNumber 	=  number; //number of the person to add with the Contact

        ArrayList<ContentProviderOperation> cntProOper = new ArrayList<ContentProviderOperation>();
        int contactIndex = cntProOper.size();//ContactSize

        //Newly Inserted contact
        // A raw contact will be inserted ContactsContract.RawContacts table in contacts database.
        cntProOper.add(ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI)//Step1
                .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
                .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null).build());

        //Display name will be inserted in ContactsContract.Data table
        cntProOper.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)//Step2
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID,contactIndex)
                .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                .withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, strDisplayName) // Name of the contact
                .build());
        //Mobile number will be inserted in ContactsContract.Data table
        cntProOper.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)//Step 3
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID,contactIndex)
                .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, strNumber) // Number to be added
                .withValue(ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE).build()); //Type like HOME, MOBILE etc
        try
        {
            // We will do batch operation to insert all above data
            //Contains the output of the app of a ContentProviderOperation.
            //It is sure to have exactly one of uri or count set
            ContentProviderResult[] contentProresult = null;
            contentProresult = contetx.getContentResolver().applyBatch(ContactsContract.AUTHORITY, cntProOper); //apply above data insertion into contacts list
        }
        catch (RemoteException exp)
        {
            //logs;
        }
        catch (OperationApplicationException exp)
        {
            //logs
        }
    }

    public void cerrar_abrir_wtapp(){
        String str4 = "am force-stop com.whatsapp";
        String str5 = "monkey -p com.whatsapp -c android.intent.category.LAUNCHER 1";
        String[] commands = {str4, str5};
        Process p = null;
        try {
            p = Runtime.getRuntime().exec("su");
            DataOutputStream os = new DataOutputStream(p.getOutputStream());
            for (String tmpCmd : commands) {
                os.writeBytes(tmpCmd + "\n");
            }
            os.writeBytes("exit\n");
            os.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Asintask
     * Para cargar el contenido del servidor mientras se carga la interfaz en primer plano
     */
    private class CargandoElementosSegundoPlano extends AsyncTask<String, Integer, JSONArray> {
        @Override
        protected JSONArray doInBackground(String... params) {
            JSONArray respuesta = null;
            try {
                respuesta = comprobar_mensajes_nuevos();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return respuesta;
        }

        /**
         * Se ejecuta después de terminar "doInBackground".
         * <p/>
         * Se ejecuta en el hilo: PRINCIPAL
         * <p/>
         * //@param String con los valores pasados por el return de "doInBackground".
         */
        @Override
        protected void onPostExecute(JSONArray mensajes_con_numeros) {
            int cantidad = mensajes_con_numeros.length();
            JSONArray datos = mensajes_con_numeros;
            for (int i=0; i<cantidad;i++ ){
                JSONObject json_data = null;
                try {
                    json_data = datos.getJSONObject(i);
                    String numero = json_data.getString("numero");
                    String mensaje = json_data.getString("mensaje");
                    String nombre = json_data.getString("nombre");
                    Log.d(DEBUG_TAG, "The response read is: " + json_data.getString("numero"));
                    Log.d(DEBUG_TAG, "The response read is: " + json_data.getString("mensaje"));
                    Log.d(DEBUG_TAG, "The response read is: " + json_data.getString("nombre"));

                    //TODO: Agregamos el numero a la agenda telefonica
                    if (guardar_contacto_agenda(json_data)){
                        //TODO: Agregamos el mensaje a la base de datos del wtapp
                        Log.w(DEBUG_TAG, "Contacto " + nombre + " agregado");

                    };


                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }

            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            try {
                guardar_textos_db(datos);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            //Log.w(DEBUG_TAG, "Mensaje a " + nombre + " enviado");



            //TODO: Cerramos el wtapp
            //cerrar_abrir_wtapp();
            //TODO: Abrimos wtapp de nuevo para que se manden


        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
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

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
            Log.i("Menu", "Camara");
        } else if (id == R.id.nav_gallery) {
            Log.i("Menu", "Galeria");
        } else if (id == R.id.nav_slideshow) {
            Log.i("Menu", "Camara");
        } else if (id == R.id.nav_manage) {
            Log.i("Menu", "Camara");
        } else if (id == R.id.nav_share) {
            Log.i("Menu", "Camara");
        } else if (id == R.id.nav_send) {
            Log.i("Menu", "Camara");
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

}
