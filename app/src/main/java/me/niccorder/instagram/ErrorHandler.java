package me.niccorder.instagram;

import android.app.Application;
import android.support.v4.content.LocalBroadcastManager;

import com.parse.LogOutCallback;
import com.parse.ParseException;
import com.parse.ParseUser;

import static com.parse.ParseException.INVALID_SESSION_TOKEN;

/**
 * TODO(niccorder) – setup dependency injection and use ErrorHandler throughout application.
 */
public final class ErrorHandler {

    ErrorHandler(Application application) {
        LocalBroadcastManager.getInstance(application);
    }

    public static void handleParseError(ParseException e) {
        switch (e.getCode()) {
            case INVALID_SESSION_TOKEN:
                handleInvalidSessionToken();
                break;
        }
    }

    private static void handleInvalidSessionToken() {
        ParseUser.logOutInBackground(new LogOutCallback() {
            @Override
            public void done(ParseException e) {

            }
        });
    }
}
