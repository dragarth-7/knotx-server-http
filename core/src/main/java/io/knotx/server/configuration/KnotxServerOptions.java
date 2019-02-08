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
package io.knotx.server.configuration;

import static io.knotx.server.KnotxServerVerticle.KNOTX_PORT_PROP_NAME;

import io.knotx.server.api.header.CustomHttpHeader;
import io.knotx.server.handler.logger.AccessLogOptions;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.reactivex.BackpressureOverflowStrategy;
import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.core.Vertx;
import io.vertx.reactivex.ext.web.api.contract.openapi3.OpenAPI3RouterFactory;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Describes a Knot.x HTTP Server configuration
 */
@DataObject(generateConverter = true, publicConverter = false)
public class KnotxServerOptions {

  /**
   * Default flag if to show the exceptions on error pages = false
   */
  private static final boolean DEFAULT_DISPLAY_EXCEPTIONS = false;

  /**
   * Default flag whether a request dropping on heavy load (backpressure) is enabled or not = false
   */
  private static final boolean DEFAULT_DROP_REQUESTS = false;

  /**
   * Default response status code send for dropped requests = 429 (Too Many Requests)
   */
  private static final int DEFAULT_DROP_REQUESTS_RESPONSE_CODE = HttpResponseStatus.TOO_MANY_REQUESTS
      .code();

  /**
   * Defaukt backpressure buffer size = 1000
   */
  private static final long DEFAULT_BACKPRESSURE_BUFFER_SIZE = 1000L;

  /**
   * Default backpressure strategy = DROP_LATEST
   */
  private static final BackpressureOverflowStrategy DEFAULT_BACKPRESSURE_STRATEGY = BackpressureOverflowStrategy.DROP_LATEST;

  private boolean displayExceptionDetails;
  private HttpServerOptions serverOptions;
  private DeliveryOptions deliveryOptions;
  private String routingSpecificationLocation;
  private List<RoutingOperationOptions> routingOperations;
  private Set<String> allowedResponseHeaders;
  private CustomHttpHeader customResponseHeader;
  private AccessLogOptions accessLog;
  private boolean dropRequests;
  private int dropRequestResponseCode;
  private long backpressureBufferCapacity;
  private BackpressureOverflowStrategy backpressureStrategy;

  /**
   * Default constructor
   */
  public KnotxServerOptions() {
    init();
  }


  /**
   * Copy constructor
   *
   * @param other the instance to copy
   */
  public KnotxServerOptions(KnotxServerOptions other) {
    this.displayExceptionDetails = other.displayExceptionDetails;
    this.allowedResponseHeaders = new HashSet<>(other.allowedResponseHeaders);
    this.serverOptions = new HttpServerOptions(other.serverOptions);
    this.deliveryOptions = new DeliveryOptions(other.deliveryOptions);
    this.routingSpecificationLocation = other.routingSpecificationLocation;
    this.routingOperations = new ArrayList<>(other.routingOperations);
    this.customResponseHeader = new CustomHttpHeader(other.customResponseHeader);
    this.accessLog = new AccessLogOptions(other.accessLog);
    this.dropRequests = other.dropRequests;
    this.dropRequestResponseCode = other.dropRequestResponseCode;
    this.backpressureBufferCapacity = other.backpressureBufferCapacity;
    this.backpressureStrategy = other.backpressureStrategy;
  }

  /**
   * Create an settings from JSON
   *
   * @param json the JSON
   */
  public KnotxServerOptions(JsonObject json) {
    init();
    KnotxServerOptionsConverter.fromJson(json, this);

    allowedResponseHeaders = allowedResponseHeaders.stream().map(String::toLowerCase)
        .collect(Collectors.toSet());

    //port was specified in config, try to overwrite with system props if defined
    serverOptions.setPort(Integer.getInteger(KNOTX_PORT_PROP_NAME, serverOptions.getPort()));
  }

  /**
   * Convert to JSON
   *
   * @return the JSON
   */
  public JsonObject toJson() {
    JsonObject json = new JsonObject();
    KnotxServerOptionsConverter.toJson(this, json);
    return json;
  }

  private void init() {
    displayExceptionDetails = DEFAULT_DISPLAY_EXCEPTIONS;
    allowedResponseHeaders = new HashSet<>();
    allowedResponseHeaders = allowedResponseHeaders.stream().map(String::toLowerCase)
        .collect(Collectors.toSet());
    deliveryOptions = new DeliveryOptions();
    serverOptions = new HttpServerOptions();
    customResponseHeader = null;
    accessLog = new AccessLogOptions();
    dropRequests = DEFAULT_DROP_REQUESTS;
    dropRequestResponseCode = DEFAULT_DROP_REQUESTS_RESPONSE_CODE;
    backpressureBufferCapacity = DEFAULT_BACKPRESSURE_BUFFER_SIZE;
    backpressureStrategy = DEFAULT_BACKPRESSURE_STRATEGY;
  }

  /**
   * @return whether to display or not exceptions on error pages
   */
  public boolean isDisplayExceptionDetails() {
    return displayExceptionDetails;
  }

  /**
   * Set whether to display or not the exception on error pages
   *
   * @param displayExceptionDetails displays exceptions on error pages if true
   * @return reference to this, so the API can be used fluently
   */
  public KnotxServerOptions setDisplayExceptionDetails(boolean displayExceptionDetails) {
    this.displayExceptionDetails = displayExceptionDetails;
    return this;
  }

  /**
   * @return {@link io.vertx.core.http.HttpServerOptions}
   */
  public HttpServerOptions getServerOptions() {
    return serverOptions;
  }

  /**
   * Set the HTTP Server options
   *
   * @param serverOptions {@link io.vertx.core.http.HttpServerOptions} object
   * @return reference to this, so the API can be used fluently
   */
  public KnotxServerOptions setServerOptions(HttpServerOptions serverOptions) {
    this.serverOptions = serverOptions;
    return this;
  }

  /**
   * @return a {@link io.vertx.core.eventbus.DeliveryOptions}
   */
  public DeliveryOptions getDeliveryOptions() {
    return deliveryOptions;
  }

  /**
   * Set the Event Bus Delivery options used to communicate with Knot's
   *
   * @param deliveryOptions {@link io.vertx.core.eventbus.DeliveryOptions} object
   * @return reference to this, so the API can be used fluently
   */
  public KnotxServerOptions setDeliveryOptions(DeliveryOptions deliveryOptions) {
    this.deliveryOptions = deliveryOptions;
    return this;
  }

  /**
   * @return location of your spec. It can be an absolute path, a local path or remote url (with
   * HTTP protocol)
   */
  public String getRoutingSpecificationLocation() {
    return routingSpecificationLocation;
  }

  /**
   * Location of your spec. It can be an absolute path, a local path or remote url (with HTTP
   * protocol).
   *
   * @param routingSpecificationLocation routing specification location
   * @return reference to this, so the API can be used fluently
   * @see OpenAPI3RouterFactory#rxCreate(Vertx, String)
   */
  public KnotxServerOptions setRoutingSpecificationLocation(
      String routingSpecificationLocation) {
    this.routingSpecificationLocation = routingSpecificationLocation;
    return this;
  }

  /**
   * List of {@link RoutingOperationOptions} containing handlers configurations which are initiated
   * (loaded from classpath via {@link java.util.ServiceLoader}) during server setup and joined with
   * Open API operations based on operationId.
   *
   * @return list of routing operations options
   */
  public List<RoutingOperationOptions> getRoutingOperations() {
    return routingOperations;
  }

  /**
   * Set list of {@link RoutingOperationOptions}.
   *
   * @param routingOperations routing operations options
   * @return reference to this, so the API can be used fluently
   */
  public KnotxServerOptions setRoutingOperations(
      List<RoutingOperationOptions> routingOperations) {
    this.routingOperations = routingOperations;
    return this;
  }

  /**
   * @return Set of response headers that Knot.x can return
   */
  public Set<String> getAllowedResponseHeaders() {
    return allowedResponseHeaders;
  }

  /**
   * Set the set of response headers that can be returned by the Knot.x server
   *
   * @param allowedResponseHeaders a set of response headers
   * @return reference to this, so the API can be used fluently
   */
  public KnotxServerOptions setAllowedResponseHeaders(
      Set<String> allowedResponseHeaders) {
    this.allowedResponseHeaders = allowedResponseHeaders.stream().map(String::toLowerCase)
        .collect(Collectors.toSet());
    return this;
  }

  /**
   * @return a configuration of custom response header returned by the Knot.x server
   */
  public CustomHttpHeader getCustomResponseHeader() {
    return customResponseHeader;
  }

  /**
   * Set the custom response header returned by the Knot.x
   *
   * @param customResponseHeader a {@link KnotxServerOptions} object
   * @return reference to this, so the API can be used fluently
   */
  public KnotxServerOptions setCustomResponseHeader(
      CustomHttpHeader customResponseHeader) {
    this.customResponseHeader = customResponseHeader;
    return this;
  }

  /**
   * @return access log configuration options
   */
  public AccessLogOptions getAccessLog() {
    return accessLog;
  }

  /**
   * Set the access log options
   *
   * @param accessLog a {@link AccessLogOptions} object
   * @return reference to this, so the API can be used fluently
   */
  public KnotxServerOptions setAccessLog(AccessLogOptions accessLog) {
    this.accessLog = accessLog;
    return this;
  }

  /**
   * @return if request dropping (backpressure) is enabled
   */
  public boolean isDropRequests() {
    return dropRequests;
  }

  /**
   * Enabled/disables request dropping (backpressure) on heavy load. Default is false - disabled.
   *
   * @param dropRequests true - request drop enabled, false if disabled
   * @return reference to this, so the API can be used fluently
   */
  public KnotxServerOptions setDropRequests(boolean dropRequests) {
    this.dropRequests = dropRequests;
    return this;
  }

  /**
   * @return HTTP response code used when request dropped
   */
  public int getDropRequestResponseCode() {
    return dropRequestResponseCode;
  }

  /**
   * Sets the HTTP response code returned wheb request is dropped. Default is
   * TOO_MANY_REQUESTS(429)
   *
   * @param dropRequestResponseCode status code integer
   * @return reference to this, so the API can be used fluently
   */
  public KnotxServerOptions setDropRequestResponseCode(int dropRequestResponseCode) {
    this.dropRequestResponseCode = dropRequestResponseCode;
    return this;
  }

  /**
   * @return Capacity of the backpressure buffer
   */
  public long getBackpressureBufferCapacity() {
    return backpressureBufferCapacity;
  }

  /**
   * Sets the backpressure buffer capacity. Default value = 1000
   *
   * @param backpressureBufferCapacity long - capacity of the buffer
   * @return reference to this, so the API can be used fluently
   */
  public KnotxServerOptions setBackpressureBufferCapacity(long backpressureBufferCapacity) {
    this.backpressureBufferCapacity = backpressureBufferCapacity;
    return this;
  }

  /**
   * @return a backpressure overflow strategy.
   */
  public BackpressureOverflowStrategy getBackpressureStrategy() {
    return backpressureStrategy;
  }

  /**
   * Sets the strategy how to deal with backpressure buffer overflow. Default is DROP_LATEST.
   *
   * Available values:
   * <ul>
   * <li>ERROR - terminates the whole sequence</li>
   * <li>DROP_OLDEST - drops the oldest value from the buffer</li>
   * <li>DROP_LATEST - drops the latest value from the buffer</li>
   * </ul>
   *
   * @param backpressureStrategy a BackpressureOverflowStrategy value
   * @return reference to this, so the API can be used fluently
   */
  public KnotxServerOptions setBackpressureStrategy(
      BackpressureOverflowStrategy backpressureStrategy) {
    this.backpressureStrategy = backpressureStrategy;
    return this;
  }
}
