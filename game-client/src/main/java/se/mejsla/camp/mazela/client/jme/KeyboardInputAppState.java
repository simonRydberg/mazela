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
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.input.InputManager;
import com.jme3.light.DirectionalLight;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.mejsla.camp.mazela.network.client.NetworkClient;

/**
 *
 * @author Johan Maasing <johan@zoom.nu>
 */
public class KeyboardInputAppState extends AbstractAppState {

    private final Logger log = LoggerFactory.getLogger(getClass());
    private final InputManager inputManager;
    private final NetworkClient networkClient;

    public KeyboardInputAppState(InputManager inputManager, NetworkClient networkClient) {
        this.inputManager = Preconditions.checkNotNull(inputManager);
        this.networkClient = Preconditions.checkNotNull(networkClient);
    }

    @Override
    public void initialize(AppStateManager stateManager, Application app) {
        //inputManager.addMapping("PosX",);
        super.initialize(stateManager, app);
    }

    @Override
    public void cleanup() {
        super.cleanup();
    }

}
