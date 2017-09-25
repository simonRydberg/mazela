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

import com.google.common.base.Preconditions;
import com.jme3.app.Application;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.input.InputManager;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.AnalogListener;
import com.jme3.input.controls.KeyTrigger;
import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicBoolean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.mejsla.camp.mazela.network.client.NetworkClient;
import se.mejsla.camp.mazela.network.common.NotConnectedException;
import se.mejsla.camp.mazela.network.common.OutgoingQueueFullException;
import se.mejsla.camp.mazela.network.common.protos.MazelaProtocol;

/**
 *
 * @author Johan Maasing <johan@zoom.nu>
 */
public class KeyboardInputAppState extends AbstractAppState {

    private final Logger log = LoggerFactory.getLogger(getClass());
    private final InputManager inputManager;
    private final NetworkClient networkClient;
    private final AtomicBoolean up = new AtomicBoolean(false);
    private final AtomicBoolean down = new AtomicBoolean(false);
    private final AtomicBoolean left = new AtomicBoolean(false);
    private final AtomicBoolean right = new AtomicBoolean(false);
    private final AtomicBoolean needsUpdate = new AtomicBoolean(false);
    private KeyboardListener keyboardListener;

    public KeyboardInputAppState(InputManager inputManager, NetworkClient networkClient) {
        this.inputManager = Preconditions.checkNotNull(inputManager);
        this.networkClient = Preconditions.checkNotNull(networkClient);
    }

    @Override
    public void initialize(AppStateManager stateManager, Application app) {
        inputManager.addMapping("Left", new KeyTrigger(KeyInput.KEY_A));
        inputManager.addMapping("Right", new KeyTrigger(KeyInput.KEY_D));
        inputManager.addMapping("Up", new KeyTrigger(KeyInput.KEY_W));
        inputManager.addMapping("Down", new KeyTrigger(KeyInput.KEY_S));
        keyboardListener = new KeyboardListener();
        inputManager.addListener(keyboardListener, "Left", "Right", "Up", "Down");
        super.initialize(stateManager, app);
    }

    @Override
    public void cleanup() {
        inputManager.deleteMapping("Left");
        inputManager.deleteMapping("Right");
        inputManager.deleteMapping("Up");
        inputManager.deleteMapping("Down");
        inputManager.removeListener(keyboardListener);
        super.cleanup();
    }

    @Override
    public void update(float tpf) {
        if (needsUpdate.get()) {
            final ByteBuffer message = ByteBuffer.wrap(
                    MazelaProtocol.ClientInput
                            .newBuilder()
                            .setDown(this.down.get())
                            .setUp(this.up.get())
                            .setLeft(this.left.get())
                            .setRight(this.right.get())
                            .build().toByteArray()
            );
            /*
            try {
                this.networkClient.sendMessage(message);
                this.needsUpdate.set(false);
            } catch (OutgoingQueueFullException | NotConnectedException ex) {
                log.error("Unable to send keyboard message", ex);
            }
*/
        }
        super.update(tpf);

    }

    private class KeyboardListener implements AnalogListener {

        @Override
        public void onAnalog(final String name, final float value, final float tpf) {
            log.debug("Analog input: {}, {}, {}", name, value, tpf);
            switch (name) {
                case "Left":
                    left.set(true);
                    needsUpdate.set(true);
                    break;
                case "Right":
                    right.set(true);
                    needsUpdate.set(true);
                    break;
                case "Up":
                    up.set(true);
                    needsUpdate.set(true);
                    break;
                case "Down":
                    down.set(true);
                    needsUpdate.set(true);
                    break;
            }
        }

    }
}
