/*
 * Copyright 2017 Johan Maasing <johan@zoom.nu>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package se.mejsla.camp.mazela.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.mejsla.camp.mazela.network.server.grizzly.GrizzlyNetworkServer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 *
 * @author Johan Maasing <johan@zoom.nu>
 */
public class Server {

    private final Logger log = LoggerFactory.getLogger(getClass());
    private final ThreadFactory threadFactory = new ThreadFactory() {
        private long threadNumber = 0;
        private final ThreadGroup threadGroup = new ThreadGroup("ServerServiceGroup");

        @Override
        public Thread newThread(Runnable r) {
            final String threadName = "ServerService-" + threadNumber++;
            log.debug("Allocating new thread: " + threadName);
            Thread thread = new Thread(threadGroup, r, threadName);
            thread.setDaemon(true);
            thread.setUncaughtExceptionHandler((t, e) -> System.err.println("Uncaught exception in thread " + t + ": " + e));
            return thread;
        }
    };
    private GrizzlyNetworkServer grizzlyNetworkServer;

    private ServerService server;

    public static void main(final String[] args) {
        Server server = new Server();
        server.parseCommandLine(args);
        server.initialize();
        server.run();
    }
    private int serverPort;

    private void run() {
        final BufferedReader consoleReader
                = new BufferedReader(new InputStreamReader(System.in));
        try {
            try {
                System.out.print("Press enter to stop the server >>");
                while (!consoleReader.ready()) {
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException ex) {
                        log.error("Console reader thread was interrupted");
                        Thread.currentThread().interrupt();
                    }
                }
                final String line = consoleReader.readLine();
                stopAndWait();
                log.debug("'" + line + "' - to you too");
            } catch (IOException iOException) {
                log.error("Unable to read stdin, exiting...", iOException);
                stopAndWait();
            }
        } catch (TimeoutException e) {
            log.warn("Server did not shut down in a timely fashion.");
        }
    }

    private void parseCommandLine(String[] args) {
        log.debug("Parsing command line");
        this.serverPort = 1666;
    }

    private void initialize() {
        this.grizzlyNetworkServer = new GrizzlyNetworkServer(
                100,
                this.threadFactory,
                this.serverPort
        );
        log.debug("Starting grizzly network service");
        this.grizzlyNetworkServer.startAsync();
        this.grizzlyNetworkServer.awaitRunning();

        this.server = new ServerService(
                this.grizzlyNetworkServer,
                this.threadFactory
        );
        log.debug("Starting the server service");
        this.server.startAsync();
    }

    private void stopAndWait() throws TimeoutException {
        if (this.server != null) {
            this.log.info("Stopping server");
            this.server.stopAsync();
            this.log.info("Waiting for server to shut down");
            this.server.awaitTerminated(30, TimeUnit.SECONDS);
        }
        if (this.grizzlyNetworkServer != null) {
            log.debug("Stopping grizzly network service");
            this.grizzlyNetworkServer.stopAsync();
            this.log.info("Waiting for networking to shut down");
            this.grizzlyNetworkServer.awaitTerminated(30, TimeUnit.SECONDS);
        }
    }
}
