package io.stud.mobileapp;

import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.support.constraint.ConstraintLayout;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.nfc.NfcAdapter;
import android.nfc.NfcManager;
import android.content.Intent;
import android.content.IntentFilter;
import android.app.PendingIntent;
import android.nfc.tech.IsoDep;
import android.nfc.tech.MifareClassic;
import android.nfc.tech.MifareUltralight;
import android.nfc.tech.Ndef;
import android.nfc.tech.NfcA;
import android.nfc.tech.NfcB;
import android.nfc.tech.NfcF;
import android.nfc.tech.NfcV;
import android.content.Context;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.time.Instant;
import java.util.Date;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.util.List;

/**
 * Author : Hutinet Maxime & Nagy Livio
 */

public class RFIDActivity extends AppCompatActivity {

    TextView badgeText = null;
    ConstraintLayout layout = null;
    ProgressBar progressBar = null;
    ImageView nfcImage = null;
    String androidID = null;
    String examID = null;
    String backendAddress = null;
    final String jwtKey = "J9tgrFKhj/jspuhd9uflEFe6PlZKduD8RAcO8UAhL2k=";
    String tagID = null;


    //Sample method to validate and read the JWT
    private Claims parseJWT(String jwt) {

        //This line will throw an exception if it is not a signed JWS (as expected)
        Claims claims = Jwts.parser()
                .setSigningKey("J9tgrFKhj/jspuhd9uflEFe6PlZKduD8RAcO8UAhL2k=")
                .parseClaimsJws(jwt).getBody();
        return claims;
    }

    private void displayGreenBackground(){
        nfcImage.setVisibility(View.GONE);
        layout.setBackgroundColor(Color.rgb(32, 216, 4));
        progressBar.setVisibility(View.GONE);
    }

    private void displayRedBackground(){
        nfcImage.setVisibility(View.GONE);
        layout.setBackgroundColor(Color.rgb(235, 29, 29));
        progressBar.setVisibility(View.GONE);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rfid);

        badgeText = (TextView) findViewById(R.id.scanBadge);
        layout = (ConstraintLayout) findViewById(R.id.viewRFID);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        nfcImage = (ImageView) findViewById(R.id.imageNFC);

        progressBar.setVisibility(View.GONE);

        Intent intent = getIntent();
        androidID = intent.getStringExtra("AndroidID");
        examID = intent.getStringExtra("ExamID");
        backendAddress = intent.getStringExtra("URLServer");

        Log.e("AndroidID : ", androidID);
        Log.e("ExamID : ", examID);

    }


    private boolean isActivate(){
        NfcManager manager = (NfcManager) getApplicationContext().getSystemService(Context.NFC_SERVICE);
        NfcAdapter adapter = manager.getDefaultAdapter();
        return (adapter != null && adapter.isEnabled());
    }


    @Override
    protected void onResume() {
        super.onResume();
        if(isActivate()){
            // creating pending intent:
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
            // creating intent receiver for NFC events:
            IntentFilter filter = new IntentFilter();
            filter.addAction(NfcAdapter.ACTION_TAG_DISCOVERED);
            filter.addAction(NfcAdapter.ACTION_NDEF_DISCOVERED);
            filter.addAction(NfcAdapter.ACTION_TECH_DISCOVERED);
            // enabling foreground dispatch for getting intent from NFC event:
            NfcAdapter nfcAdapter = NfcAdapter.getDefaultAdapter(this);
            nfcAdapter.enableForegroundDispatch(this, pendingIntent, new IntentFilter[]{filter}, this.techList);
        }
        else{
            Toast.makeText(getApplicationContext(), "Please activate NFC on your device", Toast.LENGTH_LONG).show();
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        // disabling foreground dispatch:
        NfcAdapter nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        nfcAdapter.disableForegroundDispatch(this);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        if (intent.getAction().equals(NfcAdapter.ACTION_TAG_DISCOVERED)) {
            // We assign here the scanned tag to the attribute
            tagID = ByteArrayToHexString(intent.getByteArrayExtra(NfcAdapter.EXTRA_ID));
            progressBar.getIndeterminateDrawable().setColorFilter(Color.parseColor("#ffffffff"), android.graphics.PorterDuff.Mode.MULTIPLY);
            progressBar.setVisibility(View.VISIBLE);


            String compactJws = Jwts.builder()
                    .setIssuedAt(Date.from(Instant.now()))
                    .signWith(SignatureAlgorithm.HS256, jwtKey)
                    .claim("id_tel", androidID)
                    .claim("tag_id", tagID)
                    .claim("module_id", examID)
                    .compact();

            Log.e("JWT", compactJws);
            Log.e("TagID : ", tagID);

            JSONObject postparams = new JSONObject();
            try {
                postparams.put("token", compactJws);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            Log.e("SENT : ", "AndroidID : " + androidID + " tagID : " + tagID + " examID : " + examID);
            NetworkManager.getInstance().sendPOSTRequest("/api/exam", postparams, new NetworkManagerIdListener<JSONObject>()
            {

                @Override
                public void getResult(JSONObject result) throws JSONException {

                    String jwtToken;
                    Claims claimsJWTToken;

                    if(result.has("token")){
                        jwtToken = result.getString("token"); //
                        claimsJWTToken = parseJWT(jwtToken);

                        Log.e("RECEIVED : ", "Student : " + claimsJWTToken.get("Student") + "URL : "
                                + claimsJWTToken.get("exams") + "Module : " + claimsJWTToken.get("Module"));

                        // If the user can access to the exam
                        if(!claimsJWTToken.get("exams").equals("null")) {
                            String url = (String) claimsJWTToken.get("exams");
                            JSONArray urlJson = new JSONArray(url);
                            StringBuilder stringURL = new StringBuilder();
                            for(int i = 0; i < urlJson.length(); i++){

                                stringURL.append(backendAddress).append("/media/").append(urlJson.getJSONObject(i).getJSONObject("fields").getString("document")).append("\n");
                            }

                            displayGreenBackground();
                           ((TextView)findViewById(R.id.scanBadge)).setText("Student : " +
                                    claimsJWTToken.get("Student") +
                                   "\n\nModule : " + claimsJWTToken.get("Module") + "\n\nURL : \n" + String.valueOf(stringURL));

                        }else{
                            displayRedBackground();
                            ((TextView)findViewById(R.id.scanBadge)).setText("Please enter a new exam id or go to the administration to register");

                        }
                    }

                }

            });


        }
    }

    // Converting byte[] to hex string:
    private String ByteArrayToHexString(byte [] inarray) {
        int i, j, in;
        String [] hex = {"0","1","2","3","4","5","6","7","8","9","A","B","C","D","E","F"};
        String out= "";
        for(j = 0 ; j < inarray.length ; ++j)
        {
            in = (int) inarray[j] & 0xff;
            i = (in >> 4) & 0x0f;
            out += hex[i];
            i = in & 0x0f;
            out += hex[i];
        }
        return out;
    }

    // list of NFC technologies detected:
    private final String[][] techList = new String[][] {
            new String[] {
                    NfcA.class.getName(),
                    NfcB.class.getName(),
                    NfcF.class.getName(),
                    NfcV.class.getName(),
                    IsoDep.class.getName(),
                    MifareClassic.class.getName(),
                    MifareUltralight.class.getName(), Ndef.class.getName()
            }
    };

}
