package me.niccorder.instagram.model;

import com.parse.ParseFile;
import com.parse.ParseQuery;
import com.parse.ParseUser;

/**
 * A class used to wrap a {@link ParseUser} to encapsulate the data accessors and mutators.
 */
public final class UserWrapper {

    private static final String KEY_AVATAR = "avatar";

    private final ParseUser user;

    public UserWrapper(ParseUser user) {
        this.user = user;
    }

    public static UserWrapper wrap(ParseUser user) {
        return new UserWrapper(user);
    }

    public String getId() {
        return user.getObjectId();
    }

    public ParseUser unwrap() {
        return user;
    }

    public String getUsername() {
        return user.getUsername();
    }

    public void setUsername(String username) {
        user.setUsername(username);
    }

    public String getEmail() {
        return user.getEmail();
    }

    public void setEmail(String email) {
        user.setEmail(email);
    }

    public ParseFile getAvatar() {
        return user.getParseFile(KEY_AVATAR);
    }

    public void setAvatar(final ParseFile avatar) {
        user.put(KEY_AVATAR, avatar);
    }
}
