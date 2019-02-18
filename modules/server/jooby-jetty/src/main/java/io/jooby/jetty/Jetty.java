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
package io.jooby.jetty;

import io.jooby.ExecutionMode;
import io.jooby.Jooby;
import io.jooby.internal.jetty.JettyHandler;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.MultiPartFormDataCompliance;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.server.handler.gzip.GzipHandler;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import io.jooby.Throwing;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class Jetty extends io.jooby.Server.Base {

  private int port = 8080;

  private Server server;

  private boolean gzip;

  private List<Jooby> applications = new ArrayList<>();

  private long maxRequestSize = _10MB;

  private int bufferSize = _16KB;

  private int workerThreads = 200;

  private boolean defaultHeaders = true;

  static {
    System.setProperty("org.eclipse.jetty.util.log.class", "org.eclipse.jetty.util.log.Slf4jLog");
  }

  @Override public io.jooby.Server port(int port) {
    this.port = port;
    return this;
  }

  @Override public int port() {
    return port;
  }

  @Nonnull @Override public io.jooby.Server maxRequestSize(long maxRequestSize) {
    this.maxRequestSize = maxRequestSize;
    return this;
  }

  @Nonnull @Override public io.jooby.Server defaultHeaders(boolean value) {
    this.defaultHeaders = value;
    return this;
  }

  @Nonnull @Override public io.jooby.Server bufferSize(int bufferSize) {
    this.bufferSize = bufferSize;
    return this;
  }

  public io.jooby.Server gzip(boolean enabled) {
    this.gzip = enabled;
    return this;
  }

  @Override public io.jooby.Server workerThreads(int workerThreads) {
    this.workerThreads = workerThreads;
    return this;
  }

  @Nonnull @Override public io.jooby.Server start(Jooby application) {
    System.setProperty("org.eclipse.jetty.util.UrlEncoded.charset", "utf-8");
    /** Set max request size attribute: */
    System.setProperty("org.eclipse.jetty.server.Request.maxFormContentSize",
        Long.toString(maxRequestSize));

    /** Jetty only support worker executor: */
    application.mode(ExecutionMode.WORKER);
    applications.add(application);

    addShutdownHook();

    QueuedThreadPool executor = new QueuedThreadPool(workerThreads);
    executor.setName("jetty-worker");

    fireStart(applications, executor);

    this.server = new Server(executor);
    server.setStopAtShutdown(false);

    HttpConfiguration httpConf = new HttpConfiguration();
    httpConf.setOutputBufferSize(bufferSize);
    httpConf.setOutputAggregationSize(bufferSize);
    httpConf.setSendXPoweredBy(false);
    httpConf.setSendDateHeader(defaultHeaders);
    httpConf.setSendServerVersion(false);
    httpConf.setMultiPartFormDataCompliance(MultiPartFormDataCompliance.RFC7578);
    ServerConnector connector = new ServerConnector(server);
    connector.addConnectionFactory(new HttpConnectionFactory(httpConf));
    connector.setPort(port);
    connector.setHost("0.0.0.0");

    server.addConnector(connector);

    AbstractHandler handler = new JettyHandler(applications.get(0), bufferSize, maxRequestSize, defaultHeaders);

    if (gzip) {
      GzipHandler gzipHandler = new GzipHandler();
      gzipHandler.setHandler(handler);
      handler = gzipHandler;
    }

    server.setHandler(handler);

    try {
      server.start();
    } catch (Exception x) {
      throw Throwing.sneakyThrow(x);
    }

    fireReady(applications);

    return this;
  }

  @Override public io.jooby.Server stop() {
    fireStop(applications);
    if (server != null) {
      try {
        server.stop();
      } catch (Exception x) {
        throw Throwing.sneakyThrow(x);
      }
      server = null;
    }
    return this;
  }
}
