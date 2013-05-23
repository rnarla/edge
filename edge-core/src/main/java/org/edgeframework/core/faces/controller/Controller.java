package org.edgeframework.core.faces.controller;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;

import org.vertx.java.core.Vertx;

import com.jetdrone.vertx.yoke.middleware.YokeRequest;
import com.jetdrone.vertx.yoke.middleware.YokeResponse;

public abstract class Controller {
  private Vertx vertx;
  private YokeRequest request;

  void setRequest(YokeRequest request) {
    this.request = request;
  }

  void setVertx(Vertx vertx) {
    this.vertx = vertx;
  }

  /* Util Classes */
  protected abstract class Result {
    public abstract void render(YokeResponse response);
  }

  /* Properties */
  protected YokeRequest request() {
    return this.request;
  }

  protected String beginSession() {
    String sessionID = request.getSessionId();

    if (sessionID == null) {
      sessionID = UUID.randomUUID().toString();
      request.setSessionId(sessionID);
    }

    return sessionID;
  }

  protected void endSession() {
    String sessionID = request.getSessionId();

    if (sessionID == null) {
      return;
    }

    // TODO: remove persistence backend
    request.setSessionId(null);
  }

  protected boolean isActiveSession() {
    return request.getSessionId() != null;
  }

  protected Map<String, Object> session() {
    // TODO: I'm going to rely heavily on the Yoke sessionID implementation for
    // this
    // If there are any significant security/auth leaking issues then we may
    // relook this.
    String sessionID = request.getSessionId();

    if (sessionID == null) {
      // TODO: provide warning about accessing map
      return Collections.emptyMap();
    }

    // TODO: implement different types of session persistence strategies

    // return the session map for this session
    // if there isn't one, then create one
    return vertx.sharedData().getMap("edge:sessions:" + sessionID);

  }

  /* Result Methods */
  protected Result ok(final String content) {
    return new Result() {
      @Override
      public void render(YokeResponse response) {
        response.setStatusCode(200);
        response.headers().add("content-length", "" + content.length());
        response.write(content);
        response.close();
      }
    };
  }
}
