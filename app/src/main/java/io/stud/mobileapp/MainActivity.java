package io.stud.mobileapp;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.animation.ObjectAnimator;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.time.Instant;
import java.util.Date;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.Claims;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Author : Hutinet Maxime & Nagy Livio
 */

public class MainActivity extends AppCompatActivity {

    Button clickSendNumber, clickEnrol = null;
    EditText textField = null;
    TextView result = null;
    ImageView logoHepia = null;
    ImageView logo = null;
    String androidId = null;
    String backendAddress = null;

    // Sample method to validate and read the JWT
    private Claims parseJWT(String jwt) {

        // This line will throw an exception if it is not a signed JWS (as expected)
        Claims claims = Jwts.parser()
                .setSigningKey("J9tgrFKhj/jspuhd9uflEFe6PlZKduD8RAcO8UAhL2k=")
                .parseClaimsJws(jwt).getBody();
        return claims;
    }


    @SuppressLint("HardwareIds")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(this /* Activity context */);
        backendAddress = sharedPreferences.getString("server", "http://127.0.0.1:8000");

        NetworkManager.getInstance(this, backendAddress);

        // Super secret key used to exchange information with the android app
        final String jwtKey = "J9tgrFKhj/jspuhd9uflEFe6PlZKduD8RAcO8UAhL2k=";

        setContentView(R.layout.activity_main);

        clickSendNumber = findViewById(R.id.ok); //Assign the button to a view
        clickEnrol = findViewById(R.id.enrol);
        textField = (EditText) findViewById(R.id.editText);
        result = (TextView) findViewById(R.id.myText);
        logoHepia = (ImageView) findViewById(R.id.logo_hepia);
        logo = (ImageView) findViewById(R.id.logo);

        // Translation of lapie image
        moveImage(logoHepia);

        // Fadein of app name
        Animation myFadeInAnimation = AnimationUtils.loadAnimation(this, R.anim.fadein);
        logo.startAnimation(myFadeInAnimation);


        myFadeInAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                androidId = Settings.Secure.getString(getContentResolver(),
                        Settings.Secure.ANDROID_ID);

                String compactJws = Jwts.builder()
                        .setIssuedAt(Date.from(Instant.now()))
                        .signWith(SignatureAlgorithm.HS256, jwtKey)
                        .claim("id_tel", androidId)
                        .compact();

                Log.d("JWT", compactJws);

                JSONObject postparams = new JSONObject();
                try {
                    postparams.put("token", compactJws);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                // Remove la pie
                NetworkManager.getInstance().sendPOSTRequest("/api/enroled", postparams, new NetworkManagerIdListener<JSONObject>()
                {
                    @Override
                    public void getResult(JSONObject result) throws JSONException {
                        String jwtToken;
                        Claims claimsJWTToken;

                        if(result.has("token")){
                            jwtToken = result.getString("token"); //
                            claimsJWTToken = parseJWT(jwtToken);

                            if(claimsJWTToken.get("enroled").toString().equals("true")) {
                                showFormContent();
                            }else{
                                clickEnrol.setVisibility(View.VISIBLE);
                            }
                        }

                    }

                    });
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        clickSendNumber.setOnClickListener(clickListenerButonEnter); //Add a listener to the button
        textField.addTextChangedListener(idTextWatcher);
        logo.setOnClickListener(clickListenerHepiaLogo);

        hideFormContent(); // Here we hide the button and the texte field

        clickEnrol.setOnClickListener(clickListenerButonRegister);

        String androidId = Settings.Secure.getString(getContentResolver(),
                Settings.Secure.ANDROID_ID);

    }

    private TextWatcher idTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };

    private void moveImage(ImageView image){
        ObjectAnimator animation = ObjectAnimator.ofFloat(image, "translationX", -600f);
        animation.setDuration(2000);
        animation.start();
    }

    private void hideFormContent(){
        clickSendNumber.setVisibility(View.GONE);
        textField.setVisibility(View.GONE);
        clickEnrol.setVisibility(View.GONE);
        result.setVisibility(View.GONE);
    }

    private void showFormContent(){
        textField.setVisibility(View.VISIBLE);
        result.setVisibility(View.VISIBLE);
        clickSendNumber.setVisibility(View.VISIBLE);
    }


    private OnClickListener clickListenerButonEnter = new OnClickListener() {
        @Override
        public void onClick(View v) {

            Intent RFIDIntent = new Intent(MainActivity.this, RFIDActivity.class);

            // If the content of the exam textField field is not null we lunch the activiy
            if (!textField.getText().toString().equals("")){
                RFIDIntent.putExtra("ExamID", textField.getText().toString());
                RFIDIntent.putExtra("AndroidID", androidId);
                RFIDIntent.putExtra("URLServer", backendAddress);

                startActivity(RFIDIntent);
            }
            else if (textField.getText().toString().equals("")){
                Toast.makeText(getApplicationContext(), "Please enter a valid exam ID.", Toast.LENGTH_SHORT).show();
            }
        }
    };


    private OnClickListener clickListenerButonRegister = new OnClickListener() {
        @Override
        public void onClick(View v) {
            startActivity(new Intent(MainActivity.this, RegistrationActivity.class));
        }
    };

    private OnClickListener clickListenerHepiaLogo = new OnClickListener() {
        @Override
        public void onClick(View v) {
            startActivity(new Intent(MainActivity.this, SettingsActivity.class));
        }
    };
}
