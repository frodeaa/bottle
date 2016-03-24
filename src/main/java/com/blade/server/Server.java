/**
 * Copyright (c) 2015, biezhi 王爵 (biezhi.me@gmail.com)
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.blade.server;

import blade.kit.logging.Logger;
import blade.kit.logging.LoggerFactory;
import com.blade.web.DispatcherServlet;
import github.frodeaa.bottle.App;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import java.net.URL;

/**
 * Jetty Server
 *
 * @author <a href="mailto:biezhi.me@gmail.com" target="_blank">biezhi</a>
 * @since 1.0
 */
public class Server {

    private static final Logger LOGGER = LoggerFactory.getLogger(Server.class);

    private int port = 9000;

    private org.eclipse.jetty.server.Server server;

    private ServletContextHandler context;

    public Server(int port, boolean async) {
        this.port = port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void start(String contextPath) throws Exception {

        server = new org.eclipse.jetty.server.Server(this.port);
        // 设置在JVM退出时关闭Jetty的钩子。
        server.setStopAtShutdown(true);

        context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath(contextPath);
        System.out.println(">>>>>");
        if (Thread.currentThread().getContextClassLoader() != null) {
            System.out.println("2344");
            URL resourceBase = Thread.currentThread().getContextClassLoader()
                    .getResource("");
            if (resourceBase != null) {
                context.setResourceBase(resourceBase.getPath());
            } else {
                System.out.println(App.class.getResource("").getPath());
                context.setResourceBase(App.class.getResource("").getPath());
            }
        }

        ServletHolder servletHolder = new ServletHolder(DispatcherServlet.class);
        servletHolder.setAsyncSupported(false);
        servletHolder.setInitOrder(1);

        context.addServlet(servletHolder, "/");
        server.setHandler(this.context);
        server.start();

//	    server.dump(System.err);
        LOGGER.info("Blade Server Listen on http://127.0.0.1:{}", this.port);
    }

    public void join() throws InterruptedException {
        server.join();
    }

    public void stop() throws Exception {
        context.stop();
        server.stop();
    }
}
