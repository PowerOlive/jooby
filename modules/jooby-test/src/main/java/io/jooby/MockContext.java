/**
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
 *
 * Copyright 2014 Edgar Espina
 */
package io.jooby;

import io.netty.buffer.ByteBuf;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;

/**
 * Unit test friendly context implementation. Allows to set context properties.
 */
public class MockContext implements Context {

  private String method = Router.GET;

  private Route route;

  private String pathString;

  private Map<String, String> pathMap;

  private String queryString;

  private Map<String, Collection<String>> headers = new HashMap<>();

  private Formdata formdata = Formdata.create();

  private Multipart multipart = Multipart.create();

  private Body body;

  private Map<String, Parser> parsers = new HashMap<>();

  private Map<String, Object> responseHeaders = new HashMap<>();

  private Map<String, Object> attributes = new HashMap<>();

  private MockResponse response = new MockResponse();

  private Map<String, String> cookies = new LinkedHashMap<>();

  private Router router;

  @Nonnull @Override public String getMethod() {
    return method;
  }

  MockContext setMethod(@Nonnull String method) {
    this.method = method;
    return this;
  }

  @Nonnull @Override public Map<String, String> cookieMap() {
    return cookies;
  }

  @Nonnull public MockContext setCookieMap(@Nonnull Map<String, String> cookies) {
    this.cookies = cookies;
    return this;
  }

  @Nonnull @Override public Route getRoute() {
    return route;
  }

  @Nonnull @Override public MockContext setRoute(@Nonnull Route route) {
    this.route = route;
    return this;
  }

  @Nonnull @Override public String pathString() {
    return pathString;
  }

  /**
   * Set pathString and queryString (if any).
   *
   * @param pathString Path string.
   * @return This context.
   */
  MockContext setPathString(@Nonnull String pathString) {
    int q = pathString.indexOf("?");
    if (q > 0) {
      this.pathString = pathString.substring(0, q);
      this.queryString = pathString.substring(q + 1);
    } else {
      this.pathString = pathString;
      this.queryString = null;
    }
    return this;
  }

  @Nonnull @Override public Map<String, String> pathMap() {
    return pathMap;
  }

  @Nonnull @Override public MockContext setPathMap(@Nonnull Map<String, String> pathMap) {
    this.pathMap = pathMap;
    return this;
  }

  @Nonnull @Override public QueryString query() {
    return QueryString.create(queryString);
  }

  @Nonnull @Override public String queryString() {
    return queryString;
  }

  @Nonnull @Override public Value headers() {
    return Value.hash(headers);
  }

  /**
   * Set request headers.
   *
   * @param headers Request headers.
   * @return This context.
   */
  @Nonnull public MockContext setHeaders(@Nonnull Map<String, Collection<String>> headers) {
    this.headers = headers;
    return this;
  }

  @Nonnull @Override public Formdata form() {
    return formdata;
  }

  /**
   * Set formdata.
   *
   * @param formdata Formdata.
   * @return This context.
   */
  @Nonnull public MockContext setForm(@Nonnull Formdata formdata) {
    this.formdata = formdata;
    return this;
  }

  @Nonnull @Override public Multipart multipart() {
    return multipart;
  }

  /**
   * Set multipart.
   *
   * @param multipart Multipart.
   * @return This context.
   */
  @Nonnull public MockContext setMultipart(@Nonnull Multipart multipart) {
    this.multipart = multipart;
    return this;
  }

  @Nonnull @Override public Body body() {
    return body;
  }

  /**
   * Set request body.
   *
   * @param body Request body.
   * @return This context.
   */
  @Nonnull public MockContext setBody(@Nonnull Body body) {
    this.body = body;
    return this;
  }

  /**
   * Set request body.
   *
   * @param body Request body.
   * @return This context.
   */
  @Nonnull public MockContext setBody(@Nonnull String body) {
    byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
    return setBody(bytes);
  }

  /**
   * Set request body.
   *
   * @param body Request body.
   * @return This context.
   */
  @Nonnull public MockContext setBody(@Nonnull byte[] body) {
    this.body = Body.of(new ByteArrayInputStream(body), body.length);
    return this;
  }

  @Nonnull @Override public Parser parser(@Nonnull MediaType contentType) {
    return parsers.getOrDefault(contentType, Parser.UNSUPPORTED_MEDIA_TYPE);
  }

  @Override public boolean isInIoThread() {
    return false;
  }

  @Nonnull @Override public MockContext dispatch(@Nonnull Runnable action) {
    action.run();
    return this;
  }

  @Nonnull @Override
  public MockContext dispatch(@Nonnull Executor executor, @Nonnull Runnable action) {
    action.run();
    return this;
  }

  @Nonnull @Override public MockContext detach(@Nonnull Runnable action) {
    action.run();
    return this;
  }

  @Nonnull @Override public Map<String, Object> getAttributes() {
    return attributes;
  }

  @Nonnull @Override
  public MockContext setResponseHeader(@Nonnull String name, @Nonnull Date value) {
    Context.super.setResponseHeader(name, value);
    return this;
  }

  @Nonnull @Override
  public MockContext setResponseHeader(@Nonnull String name, @Nonnull Instant value) {
    Context.super.setResponseHeader(name, value);
    return this;
  }

  @Nonnull @Override
  public MockContext setResponseHeader(@Nonnull String name, @Nonnull Object value) {
    Context.super.setResponseHeader(name, value);
    return this;
  }

  @Nonnull @Override
  public MockContext setResponseHeader(@Nonnull String name, @Nonnull String value) {
    responseHeaders.put(name, value);
    return this;
  }

  @Nonnull @Override public MockContext setResponseLength(long length) {
    response.setContentLength(length);
    return this;
  }

  @Nonnull @Override public MockContext setResponseType(@Nonnull String contentType) {
    response.setContentType(MediaType.valueOf(contentType));
    return this;
  }

  @Nonnull @Override
  public MockContext setResponseType(@Nonnull MediaType contentType, @Nullable Charset charset) {
    response.setContentType(contentType);
    return this;
  }

  @Nonnull @Override public MockContext setResponseCode(int statusCode) {
    response.setStatusCode(StatusCode.valueOf(statusCode));
    return this;
  }

  @Nonnull @Override public StatusCode getResponseCode() {
    return response.getStatusCode();
  }

  @Nonnull @Override public MockContext render(@Nonnull Object result) {
    this.response.setResult(result);
    return this;
  }

  /**
   * Mock response generated from route execution.
   *
   * @return Mock response.
   */
  @Nonnull public MockResponse getResponse() {
    return response;
  }

  @Nonnull @Override public OutputStream responseStream() {
    ByteArrayOutputStream out = new ByteArrayOutputStream(ServerOptions._16KB);
    this.response.setResult(out);
    return out;
  }

  @Nonnull @Override public Sender responseSender() {
    return new Sender() {
      @Override public Sender write(@Nonnull byte[] data, @Nonnull Callback callback) {
        response.setResult(data);
        callback.onComplete(MockContext.this, null);
        return this;
      }

      @Override public void close() {
      }
    };
  }

  @Nonnull @Override public String getRemoteAddress() {
    return "0.0.0.0";
  }

  @Nonnull @Override public String getProtocol() {
    return "HTTP/1.1";
  }

  @Nonnull @Override public String getScheme() {
    return "http";
  }

  @Nonnull @Override public PrintWriter responseWriter(MediaType type, Charset charset) {
    PrintWriter writer = new PrintWriter(new StringWriter());
    this.response.setResult(writer)
        .setContentType(type);
    return writer;
  }

  @Nonnull @Override public MockContext send(@Nonnull String data, @Nonnull Charset charset) {
    this.response.setResult(data)
        .setContentLength(data.length());
    return this;
  }

  @Nonnull @Override public MockContext send(@Nonnull byte[] data) {
    this.response.setResult(data)
        .setContentLength(data.length);
    return this;
  }

  @Nonnull @Override public MockContext send(@Nonnull ByteBuf data) {
    this.response.setResult(data)
        .setContentLength(data.readableBytes());
    return this;
  }

  @Nonnull @Override public MockContext send(@Nonnull ByteBuffer data) {
    this.response.setResult(data)
        .setContentLength(data.remaining());
    return this;
  }

  @Nonnull @Override public MockContext send(InputStream input) {
    this.response.setResult(input);
    return this;
  }

  @Nonnull @Override public MockContext send(@Nonnull ReadableByteChannel channel) {
    this.response.setResult(channel);
    return this;
  }

  @Nonnull @Override public MockContext send(@Nonnull FileChannel file) {
    this.response.setResult(file);
    return this;
  }

  @Nonnull @Override public MockContext send(StatusCode statusCode) {
    this.response
        .setContentLength(0)
        .setStatusCode(statusCode);
    return this;
  }

  @Nonnull @Override public MockContext sendError(@Nonnull Throwable cause) {
    this.response.setResult(cause)
        .setStatusCode(router.errorCode(cause));
    return this;
  }

  @Nonnull @Override public MockContext setDefaultResponseType(@Nonnull MediaType contentType) {
    response.setContentType(contentType);
    return this;
  }

  @Nonnull @Override public MockContext setResponseCookie(@Nonnull Cookie cookie) {
    String setCookie = response.getHeaders().get("Set-Cookie");
    if (setCookie == null) {
      setCookie = cookie.toCookieString();
    } else {
      setCookie += ";" + cookie.toCookieString();
    }
    response.setHeader("Set-Cookie", setCookie);
    return this;
  }

  @Nonnull @Override public MediaType getResponseType() {
    return response.getContentType();
  }

  @Override public boolean isResponseStarted() {
    return response != null;
  }

  @Nonnull @Override public Router getRouter() {
    return router;
  }

  @Nonnull MockContext setRouter(@Nonnull Router router) {
    this.router = router;
    return this;
  }

}