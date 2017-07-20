package com.androidkeychain_sample;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.security.KeyPairGeneratorSpec;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.androidkeychain_sample.R;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;
import java.security.interfaces.RSAPublicKey;
import java.util.Calendar;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.NoSuchPaddingException;
import javax.security.auth.x500.X500Principal;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "log";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onClick(View view) {
                getorstoreThePrivateKey();
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void getorstoreThePrivateKey() {
        String plainText = "zLCxR/Rls7IIDCu/SuDlkqTjencr38YC4NZ4B4p3sFcLZzlO2ooiUUht10xhXZSm";
        try {
            KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
            keyStore.load(null);

            //getting alias string as device unique id
            String alias = getUniqueDeviceId(this);

            int nBefore = keyStore.size();

            // Create the keys if necessary
            if (!keyStore.containsAlias(alias)) {

                Calendar notBefore = Calendar.getInstance();
                Calendar notAfter = Calendar.getInstance();
                notAfter.add(Calendar.YEAR, 1);
                KeyPairGeneratorSpec spec = new KeyPairGeneratorSpec.Builder(this)
                        .setAlias(alias)
                        .setKeyType("RSA")
                        .setKeySize(2048)
                        .setSubject(new X500Principal("CN=test"))
                        .setSerialNumber(BigInteger.ONE)
                        .setStartDate(notBefore.getTime())
                        .setEndDate(notAfter.getTime())
                        .build();
                KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA", "AndroidKeyStore");
                generator.initialize(spec);

                KeyPair keyPair = generator.generateKeyPair();
            }
            int nAfter = keyStore.size();
            Log.v("log", "Before = " + nBefore + " After = " + nAfter);

            // Retrieve the keys
            KeyStore.PrivateKeyEntry privateKeyEntry = (KeyStore.PrivateKeyEntry) keyStore.getEntry(alias, null);
            RSAPublicKey publicKey = (RSAPublicKey) privateKeyEntry.getCertificate().getPublicKey();
            Log.v("log", "public key = " + publicKey.toString());

            // Encrypt the text

            String dataDirectory = getApplicationInfo().dataDir;
            String filesDirectory = getFilesDir().getAbsolutePath();
            String encryptedDataFilePath = filesDirectory + File.separator + "secretlocation";

            Log.v(TAG, "plainText = " + plainText);
            Log.v(TAG, "dataDirectory = " + dataDirectory);
            Log.v(TAG, "filesDirectory = " + filesDirectory);
            Log.v(TAG, "encryptedDataFilePath = " + encryptedDataFilePath);

            Cipher inCipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            inCipher.init(Cipher.ENCRYPT_MODE, publicKey);

            Cipher outCipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            outCipher.init(Cipher.DECRYPT_MODE, privateKeyEntry.getPrivateKey());

            CipherOutputStream cipherOutputStream =
                    new CipherOutputStream(
                            new FileOutputStream(encryptedDataFilePath), inCipher);
            cipherOutputStream.write(plainText.getBytes("UTF-8"));
            cipherOutputStream.close();

            CipherInputStream cipherInputStream =
                    new CipherInputStream(new FileInputStream(encryptedDataFilePath),
                            outCipher);
            byte[] roundTrippedBytes = new byte[1000]; // TODO: dynamically resize as we get more data

            int index = 0;
            int nextByte;
            while ((nextByte = cipherInputStream.read()) != -1) {
                roundTrippedBytes[index] = (byte) nextByte;
                index++;
            }
            String roundTrippedString = new String(roundTrippedBytes, 0, index, "UTF-8");
            Log.v(TAG, "round tripped string = " + roundTrippedString);

        } catch (NoSuchAlgorithmException e) {

            Log.e(TAG, Log.getStackTraceString(e));
        } catch (NoSuchProviderException e) {
            Log.e(TAG, Log.getStackTraceString(e));
        } catch (InvalidAlgorithmParameterException e) {
            Log.e(TAG, Log.getStackTraceString(e));
        } catch (KeyStoreException e) {
            Log.e(TAG, Log.getStackTraceString(e));
        } catch (CertificateException e) {
            Log.e(TAG, Log.getStackTraceString(e));
        } catch (IOException e) {
            Log.e(TAG, Log.getStackTraceString(e));
        } catch (UnrecoverableEntryException e) {
            Log.e(TAG, Log.getStackTraceString(e));
        } catch (NoSuchPaddingException e) {
            Log.e(TAG, Log.getStackTraceString(e));
        } catch (InvalidKeyException e) {
            Log.e(TAG, Log.getStackTraceString(e));
        } catch (UnsupportedOperationException e) {
            Log.e(TAG, Log.getStackTraceString(e));
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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

    public String getUniqueDeviceId(Context ctx) {
        String android_id = Settings.Secure.getString(ctx.getContentResolver(),
                Settings.Secure.ANDROID_ID);
        return android_id;
    }
}
