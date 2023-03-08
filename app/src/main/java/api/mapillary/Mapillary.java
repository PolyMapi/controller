package api.mapillary;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import androidx.annotation.NonNull;

import com.example.polymapi.MainActivity;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

public class Mapillary {
    private final MainActivity mainActivity;
    int clientId;

    public Mapillary (MainActivity mainActivity, int clientId) {
        this.mainActivity = mainActivity;
        this.clientId = clientId;
    }

    public void auth() {

    }

    public boolean authenticate() {
        AccountManager am = AccountManager.get(mainActivity);
        Bundle options = new Bundle();



        Account[] myAccount_ = am.getAccountsByType("com.google");
        int hello = 25;

        am.getAuthToken(
                myAccount_[0],                     // Account retrieved using getAccountsByType()
                "upload",                       // Auth scope
                options,                        // Authenticator-specific options
                mainActivity,                           // Your activity
                new OnTokenAcquired(),          // Callback called when a token is successfully acquired
                new Handler(new OnError()));    // Callback called if an error occurs

        return false;
    }

    private class OnError implements Handler.Callback {

        @Override
        public boolean handleMessage(@NonNull Message message) {
            return false;
        }
    }

    private class OnTokenAcquired implements AccountManagerCallback<Bundle> {
        @Override
        public void run(AccountManagerFuture<Bundle> result) {
            // Get the result of the operation from the AccountManagerFuture.
            Bundle bundle = null;
            try {
                bundle = result.getResult();
            } catch (AuthenticatorException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (OperationCanceledException e) {
                throw new RuntimeException(e);
            }

            // The token is a named value in the bundle. The name of the value
            // is stored in the constant AccountManager.KEY_AUTHTOKEN.
            String token = bundle.getString(AccountManager.KEY_AUTHTOKEN);

            Intent launch = null;
            try {
                launch = (Intent) result.getResult().get(AccountManager.KEY_INTENT);
            } catch (AuthenticatorException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (OperationCanceledException e) {
                throw new RuntimeException(e);
            }
            if (launch != null) {
                try {
                    URL url = new URL("https://www.mapillary.com/connect?client_id=" + clientId + "&state=upload");
                    URLConnection conn = (HttpURLConnection) url.openConnection();

                } catch (MalformedURLException e) {
                    throw new RuntimeException(e);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                return;
            }


        }
    }

}
