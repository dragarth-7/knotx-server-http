/*
 * Copyright (C) 2019 Knot.x Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.knotx.server.api.context;

import io.knotx.server.api.context.RequestEventLog.Entry;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.codegen.annotations.DataObject;
import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.core.MultiMap;
import java.util.Optional;

/**
 * Describes context of currently processed request.
 */
@DataObject
public class RequestContext {

  public static final String KEY = "requestContext";

  private RequestEvent requestEvent;
  private ClientResponse clientResponse;
  private final RequestEventLog log;

  public RequestContext(RequestEvent requestEvent) {
    this.requestEvent = requestEvent;
    log = new RequestEventLog();
    clientResponse = new ClientResponse().setStatusCode(HttpResponseStatus.OK.code());
  }

  public RequestContext(JsonObject json) {
    this.requestEvent = new RequestEvent(json.getJsonObject("requestEvent"));
    this.log = new RequestEventLog(json.getJsonObject("log"));
    this.clientResponse = new ClientResponse(json.getJsonObject("clientResponse"));
  }

  /**
   * {@code RequestEvent} with detailed event info from this context.
   *
   * @return {@link RequestEvent} from the current context.
   */
  public RequestEvent getRequestEvent() {
    return requestEvent;
  }

  /**
   * Final {@code ClientResponse} object for this request context.
   *
   * @return final {@link ClientResponse} for the current context.
   */
  public ClientResponse getClientResponse() {
    return clientResponse;
  }

  /**
   * Current {@code RequestContext.Status} that says if current context has
   * failed or not. Optionally it delivers cause of the failure.
   *
   * @return current {@link Status}.
   */
  public Status getStatus() {
    final Optional<Entry> failedOperation = log.getOperations().stream()
        .filter(e -> RequestEventLog.Status.SUCCESS != e.getStatus())
        .findAny();
    return failedOperation
        .map(Status::failed)
        .orElse(Status.ok());
  }

  public RequestContext success(String handlerId, RequestEvent requestEvent) {
    this.requestEvent = requestEvent;
    log.append(handlerId, RequestEventLog.Status.SUCCESS);
    return this;
  }

  public RequestContext failure(String handlerId, String errorMessage) {
    log.append(handlerId, RequestEventLog.Status.FAILURE, errorMessage);
    return this;
  }

  public void fatal(String handlerId) {
    log.append(handlerId, RequestEventLog.Status.FATAL);
    this.clientResponse.setStatusCode(HttpResponseStatus.INTERNAL_SERVER_ERROR.code());
  }

  @GenIgnore
  public RequestContext setStatusCode(Integer statusCode) {
    if (statusCode != null) {
      clientResponse.setStatusCode(statusCode);
    }
    return this;
  }

  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  public RequestContext addHeaders(MultiMap headers) {
    if (headers != null) {
      MultiMap newHeaders = clientResponse.getHeaders().addAll(headers);
      this.setHeaders(newHeaders);
    }
    return this;
  }

  @GenIgnore
  public RequestContext setHeaders(MultiMap headers) {
    if (headers != null) {
      clientResponse.setHeaders(headers);
    }
    return this;
  }

  @GenIgnore
  public RequestContext setBody(Buffer body) {
    if (body != null) {
      clientResponse.setBody(body);
    }
    return this;
  }

  public JsonObject toJson() {
    return new JsonObject()
        .put("requestEvent", requestEvent.toJson())
        .put("log", log.toJson());
  }

  @Override
  public String toString() {
    return "RequestContext{" +
        "requestEvent=" + requestEvent +
        ", clientResponse=" + clientResponse +
        ", log=" + log +
        '}';
  }

  public static class Status {

    private final boolean failed;
    private Entry cause;

    private Status(boolean failed) {
      this.failed = failed;
    }

    static Status ok() {
      return new Status(false);
    }

    static Status failed(Entry cause) {
      final Status status = new Status(true);
      status.cause = cause;
      return status;
    }

    public boolean isFailed() {
      return failed;
    }

    public Optional<JsonObject> getCause() {
      return Optional.ofNullable(cause).map(Entry::toJson);
    }

    @Override
    public String toString() {
      return "Status{" +
          "failed=" + failed +
          ", cause=" + cause +
          '}';
    }
  }

}
