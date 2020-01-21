package com.example.tommy.todolist;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "TEST";
    private EditText nEditText;
    private EditText pEditText;
    private Button sign;
    private Button login;
    private Button anonymus;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        nEditText = findViewById(R.id.nome); //EditText per l'email
        pEditText = findViewById(R.id.password); //EditText per la password
        sign = findViewById(R.id.signup); //Bottone relativo alla registrazione
        login = findViewById(R.id.login); //Bottone relativo al login
        anonymus = findViewById(R.id.anonymus); //Bottone per entrare senza loggarsi

        final Intent i = new Intent(MainActivity.this,listActivity.class); //Activity target (comune a login e signup)

        final SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
        final SharedPreferences.Editor editor = sharedPref.edit();

        mAuth = FirebaseAuth.getInstance();

        //Controlla se l'utente è già loggato
        if (mAuth.getCurrentUser() != null) {
            //se è già loggato lancia la listActivity
            startActivity(i);
            finish();//termina l'activity corrente
        }

        nEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    hideKeyboard(v);
                }
            }
        });

        pEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    hideKeyboard(v);
                }
            }
        });

        //Codice per la registrazione
        sign.setOnClickListener(new View.OnClickListener() {
            //Codice per la registrazione di un nuovo utente
            @Override
            public void onClick(View v) {

            final String email = nEditText.getText().toString();
            final String password = pEditText.getText().toString();

            if (!email.equals("") && !password.equals("")) { //Controlla che i campi mail e password non siano vuoti

                final ProgressDialog dialog = ProgressDialog.show(MainActivity.this, getString(R.string.signing),
                       getString(R.string.wait), true); //Pop-up per mostrare il progresso

                mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(MainActivity.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                       dialog.dismiss();

                        if (task.isSuccessful()) {
                            // La registrazione ha avuto successo
                            Log.d(TAG, "createUserWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            makeToast(getString(R.string.signed) + " " + user.getEmail() + "!");

                            startActivity(i); //accesso autorizzato
                            finish(); //termina l'activity corrente

                        } else {
                            // If sign in fails, display a message to the user.
                            try {
                                //Cattura l'eccezzione che identifica l'errore
                                throw task.getException();
                            } catch (FirebaseAuthWeakPasswordException e) {
                                //Password troppo debole, deve essere lunga almeno 6 caratteri
                                pEditText.setError(getString(R.string.errorWeakPassword)); //Mostra l'errore im un riqaudro vicino all'editText
                                pEditText.requestFocus();  //mostra il simbolo di errore vicino al'editText
                            } catch (FirebaseAuthInvalidCredentialsException e) {
                                //Formato dell'email scorretto
                                nEditText.setError(getString(R.string.errorInvalidEmail));
                                nEditText.requestFocus();
                            } catch (FirebaseAuthUserCollisionException e) {
                                //Email già registrata
                                nEditText.setError(getString(R.string.errorUserExists));
                                nEditText.requestFocus();
                            } catch (Exception e) {
                                //errore generico
                                makeToast(getString(R.string.connerror));
                                Log.e(TAG, e.getMessage());
                            }
                        }

                        }
                    });
            }
            }
        });

        login.setOnClickListener(new View.OnClickListener() {
            //Codice per effettuare il login di un utente già registrato
            @Override
            public void onClick(View v) {

            final String email = nEditText.getText().toString();
            final String password = pEditText.getText().toString();

            if (!email.equals("") && !password.equals("")) {//Controlla che i campi mail e password non siano vuoti

                final ProgressDialog dialog = ProgressDialog.show(MainActivity.this, getString(R.string.login),
                       getString(R.string.wait), true);//ProgressBar in AlertDialog

                mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(MainActivity.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            dialog.dismiss();
                            try {
                                if (task.isSuccessful()) {
                                    // Login avvenuto con successo
                                    Log.d(TAG, "signInWithEmail:success");
                                    FirebaseUser user = mAuth.getCurrentUser();
                                    makeToast(getString(R.string.logedIn) + " " + user.getEmail() + "!");

                                    startActivity(i); //accesso autorizzato
                                    finish(); //termina l'activity corrente

                                } else {
                                    // If sign in fails, display a message to the user.
                                    if (((FirebaseAuthException) task.getException()).getErrorCode().equals("ERROR_WRONG_PASSWORD")) {
                                        //Password errata
                                        pEditText.setError(getString(R.string.errorWrongPassword));
                                        pEditText.requestFocus();
                                    }
                                }
                            }catch(Exception e){
                                makeToast(getString(R.string.connerror));
                            }
                        }
                    });
            }
            }
        });

        //accesso locale
        anonymus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                i.putExtra(listActivity.KEY_EXTRA,"anonimo");
                startActivity(i); //Accesso autorizzato come anonimo
                finish();
            }
        });

    }


    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
    }

    //Funzione per la generica stampa di un ToastMessage
    private void makeToast(String mex){
        Toast.makeText(getApplicationContext(),mex,Toast.LENGTH_SHORT).show();
    }

    public void hideKeyboard(View view) {
        InputMethodManager inputMethodManager =(InputMethodManager)getSystemService(MainActivity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

}
