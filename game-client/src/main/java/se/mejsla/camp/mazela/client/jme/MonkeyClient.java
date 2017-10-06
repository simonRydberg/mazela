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
package se.mejsla.camp.mazela.client.jme;

import com.jme3.app.SimpleApplication;
import com.jme3.system.AppSettings;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.mejsla.camp.mazela.network.client.grizzly.GrizzlyNetworkClient;

/**
 *
 * @author Johan Maasing <johan@zoom.nu>
 */
public class MonkeyClient extends SimpleApplication {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private GrizzlyNetworkClient networkClient;
    private ProtobufAppState networkAppstate;
    private GameboardAppstate gameboardAppstate;
    private KeyboardInputAppState keyboardInputAppState;

    private final ThreadFactory threadFactory = new ThreadFactory() {
        private long threadNumber = 0;
        private final ThreadGroup threadGroup = new ThreadGroup("GameClientGroup");

        @Override
        public Thread newThread(Runnable r) {
            final String threadName = "MonkeyClient-" + threadNumber++;
            log.debug("Allocating new thread: " + threadName);
            Thread t = new Thread(threadGroup, r, threadName);
            t.setDaemon(true);
            return t;
        }
    };

    public MonkeyClient() {
    }

    public static void main(String[] args) {
        final MonkeyClient client = new MonkeyClient();

        final AppSettings settings = new AppSettings(true);
        settings.setRenderer(AppSettings.JOGL_OPENGL_BACKWARD_COMPATIBLE);
        settings.setAudioRenderer(AppSettings.JOAL);
        settings.setTitle("Mazela");
        settings.setWidth(1024);
        settings.setHeight(768);
        settings.setFrameRate(60);
        settings.setVSync(true);
        client.setSettings(settings);
        client.setShowSettings(false);
        client.start();
    }

    private void initializeNetwork() {
        this.networkClient = new GrizzlyNetworkClient(100, threadFactory);
        this.networkClient.startAsync();
    }

    private void beginShutdown() {
        networkClient.stopAsync();
        if (networkAppstate != null) {
            this.stateManager.detach(networkAppstate);
        }
        if (gameboardAppstate != null) {
            this.stateManager.detach(gameboardAppstate);
        }
        this.stop();
    }

    @Override
    public void simpleInitApp() {
        initializeNetwork();

        this.flyCam.setEnabled(false);
        this.gameboardAppstate = new GameboardAppstate();
        this.stateManager.attach(gameboardAppstate);
        this.networkAppstate = new ProtobufAppState(networkClient, gameboardAppstate);
        this.keyboardInputAppState = new KeyboardInputAppState(inputManager, networkClient);

        try {
            this.networkClient.awaitRunning(5, TimeUnit.SECONDS);
            this.stateManager.attach(networkAppstate);
            this.stateManager.attach(keyboardInputAppState);
        } catch (TimeoutException ex) {
            log.error("Network client did not start in time", ex);
            beginShutdown();
        }

    }

}
