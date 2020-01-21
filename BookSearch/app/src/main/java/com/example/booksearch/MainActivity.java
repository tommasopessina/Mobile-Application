package com.example.booksearch;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    private EditText name;
    private EditText pswd;
    private Button login;
    private Button sign;

    private FirebaseAuth mAuth;
    private FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        name = findViewById(R.id.nome);
        pswd = findViewById(R.id.password);
        login = findViewById(R.id.login);
        sign = findViewById(R.id.signup);

        mAuth = FirebaseAuth.getInstance();

        final Intent i = new Intent(MainActivity.this, listActivity.class);

        //Controlla se l'utente è già loggato
        if (mAuth.getCurrentUser() != null) {
            //se è già loggato lancia l'activityChoice
            startActivity(i);
            finish();
        }

        name.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    hideKeyboard(v);
                }
            }
        });

        pswd.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    hideKeyboard(v);
                }
            }
        });

        sign.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = name.getText().toString();
                String password = pswd.getText().toString();

                if (!email.equals("") && !password.equals("")) { //Controlla che i campi mail e password non siano vuoti

                    final ProgressDialog dialog = ProgressDialog.show(MainActivity.this, getString(R.string.signing),
                            getString(R.string.wait), true); //ProgressBar in AlertDialog

                    mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(MainActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                            dialog.dismiss();

                            if (task.isSuccessful()) {
                                // Sign in success, update UI with the signed-in user's information
                                user = mAuth.getCurrentUser();
                                makeToast(getString(R.string.signed));

                                startActivity(i); //accesso autorizzato
                                finish(); //termina l'activity corrente

                            } else {
                                // If sign in fails, display a message to the user.
                                try {
                                    //Cattura l'eccezzione che identifica l'errore
                                    throw task.getException();
                                } catch (FirebaseAuthWeakPasswordException e) {
                                    //Password troppo debole, deve essere lunga almeno 6 caratteri
                                    pswd.setError(getString(R.string.errorWeakPassword)); //Mostra l'errore im un riqaudro vicino all'editText
                                    pswd.requestFocus();  //mostra il simbolo di errore vicino al'editText
                                } catch (FirebaseAuthInvalidCredentialsException e) {
                                    //Formato dell'email scorretto
                                    name.setError(getString(R.string.errorInvalidEmail));
                                    name.requestFocus();
                                } catch (FirebaseAuthUserCollisionException e) {
                                    //Email già registrata
                                    name.setError(getString(R.string.errorUserExists));
                                    name.requestFocus();
                                } catch (Exception e) {
                                    makeToast(getString(R.string.connerror));
                                }
                            }

                            }
                        });
                    }
                }
            });

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = name.getText().toString();
                String password = pswd.getText().toString();

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
                                            // Sign in success, update UI with the signed-in user's information
                                            user = mAuth.getCurrentUser();
                                            makeToast(getString(R.string.login));

                                            startActivity(i); //accesso autorizzato
                                            finish(); //termina l'activity corrente

                                        } else {
                                            // If sign in fails, display a message to the user.
                                            if (((FirebaseAuthException) task.getException()).getErrorCode().equals("ERROR_WRONG_PASSWORD")) {
                                                //Password errata
                                                pswd.setError(getString(R.string.errorWrongPassword));
                                                pswd.requestFocus();
                                            }
                                        }
                                    }catch(Exception e){
                                        makeToast(getString(R.string.connerror));
                                    }
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            if(e.getMessage().equals("ERROR_WRONG_PASSWORD")){
                                pswd.setError(getString(R.string.errorWrongPassword));
                                pswd.requestFocus();
                            }
                        }
                    });
                }
            }
        });

    }

    public void hideKeyboard(View view) {
        InputMethodManager inputMethodManager =(InputMethodManager)getSystemService(MainActivity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }


    //Funzione per la generica stampa di un ToastMessage
    private void makeToast(String mex){
        Toast.makeText(getApplicationContext(),mex,Toast.LENGTH_SHORT).show();
    }

}
