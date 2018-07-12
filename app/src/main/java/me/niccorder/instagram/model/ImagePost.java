package me.niccorder.instagram.model;

import com.parse.CountCallback;
import com.parse.FindCallback;
import com.parse.ParseClassName;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.LinkedList;
import java.util.List;

@ParseClassName("ImagePost")
public class ImagePost extends ParseObject {
  private static final String KEY_TITLE = "title";
  private static final String KEY_DESCRIPTION = "description";
  private static final String KEY_LIKED_BY = "liked_by";
  private static final String KEY_IMAGE = "image";
  private static final String KEY_USER = "user";

  public void setTitle(String title) {
    put(KEY_TITLE, title);
  }

  public String getTitle() {
    return getString(KEY_TITLE);
  }

  public void setDescription(String description) {
    put(KEY_DESCRIPTION, description);
  }

  public String getDescription() {
    return getString(KEY_DESCRIPTION);
  }

  public void setImage(ParseFile image) {
    put(KEY_IMAGE, image);
  }

  public ParseFile getImage() {
    return getParseFile(KEY_IMAGE);
  }

  public void setUser(ParseUser user) {
    put(KEY_USER, user);
  }

  public UserWrapper getUser() {
    return UserWrapper.wrap(getParseUser(KEY_USER));
  }

  public String getUsername() {
    return getUser().getUsername();
  }

  public ParseFile getAvatar() {
    return getUser().getAvatar();
  }

  private final ParseQuery<ParseObject> likeCountQuery = getRelation(KEY_LIKED_BY).getQuery();
  public void likeCount(final CountCallback callback) {
    likeCountQuery
            .setCachePolicy(ParseQuery.CachePolicy.CACHE_THEN_NETWORK)
            .countInBackground(callback);
  }

  public void likePost(ParseUser user, SaveCallback callback) {
    getRelation(KEY_LIKED_BY).add(user);
    saveInBackground(callback);
  }

  public void unlikePost(ParseUser user, SaveCallback callback) {
    getRelation(KEY_LIKED_BY).remove(user);
    saveInBackground(callback);
  }

  public void hasLiked(ParseUser user, CountCallback callback) {
    getRelation(KEY_LIKED_BY)
            .getQuery()
            .whereEqualTo("objectId", user.getObjectId())
            .countInBackground(callback);
  }

  public static Query query() {
    return new Query();
  }

  public static class Query extends ParseQuery<ImagePost> {

    Query() {
      super(ImagePost.class);
      include(KEY_USER);
    }

    public Query newestFirst() {
      orderByDescending("createdAt");
      return this;
    }

    public Query limit20() {
      setLimit(20);
      return this;
    }

    public Query createdBy(ParseUser user) {
      whereEqualTo(KEY_USER, user);
      return this;
    }

    public Query likedBy(ParseUser user) {
      whereEqualTo(KEY_LIKED_BY, user);
      return this;
    }

    public Query likedPost(ParseUser user, String postId) {
      likedBy(user).whereEqualTo("objectId", postId);
      return this;
    }

    public Query createdByCurrentUser() {
      return createdBy(ParseUser.getCurrentUser());
    }
  }
}
