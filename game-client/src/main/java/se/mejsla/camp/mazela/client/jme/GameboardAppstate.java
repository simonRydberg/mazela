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

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.asset.AssetManager;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Sphere;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Johan Maasing <johan@zoom.nu>
 */
public class GameboardAppstate extends AbstractAppState {

    private final Logger log = LoggerFactory.getLogger(getClass());
    private final HashMap<UUID, Node> entityNodes = new HashMap<>();
    private AssetManager assetManager;
    private List<EntityUpdate> pendingUpdates = null;
    private Node rootNode;
    private Node entityNode;

    @Override
    public void initialize(AppStateManager stateManager, Application app) {
        SimpleApplication sapp = ((SimpleApplication) app);
        this.rootNode = sapp.getRootNode();
        this.assetManager = sapp.getAssetManager();
        this.rootNode.addLight(new DirectionalLight(new Vector3f(0.5f, -1.0f, 0.0f).normalize()));
        this.entityNode = new Node("Entities");
        this.rootNode.attachChild(this.entityNode);
        super.initialize(stateManager, app);
    }

    @Override
    public void update(float tpf) {
        if (this.pendingUpdates != null) {
            // Mutate the scene graph
            ArrayList<UUID> updatedEntities = new ArrayList<>();
            for (EntityUpdate pu : this.pendingUpdates) {
                final UUID entityUUID = pu.getEntityID();
                Node playerNode = this.entityNodes.get(entityUUID);
                if (playerNode == null) {
                    // New player
                    final Geometry geom = new Geometry(
                            entityUUID.toString() + "-geom",
                            new Sphere(32, 32, 0.5f)
                    );
                    final Material sphereMat = new Material(
                            assetManager,
                            "Common/MatDefs/Light/Lighting.j3md"
                    );
                    sphereMat.setBoolean("UseMaterialColors", true);
                    sphereMat.setColor("Diffuse", ColorRGBA.Red);
                    sphereMat.setColor("Specular", ColorRGBA.White);
                    sphereMat.setFloat("Shininess", 64f);  // [0,128]
                    geom.setMaterial(sphereMat);
                    playerNode = new Node(entityUUID.toString());
                    playerNode.attachChild(geom);
                    log.debug("Attaching player {} node", entityUUID);
                    rootNode.attachChild(playerNode);
                    entityNodes.put(entityUUID, playerNode);
                }
                playerNode.setLocalTranslation(transformCoordinatesFromServerToClient(pu));

                updatedEntities.add(entityUUID);
            }

            // remove entities that was not included in the update
            final Set<UUID> entitiesToRemove = entityNodes
                    .keySet()
                    .stream()
                    .filter(knownEntityUUID -> !updatedEntities.contains(knownEntityUUID))
                    .collect(Collectors.toSet());
            entitiesToRemove.forEach(e -> {
                final Node nodeToRemove = entityNodes.remove(e);
                if (nodeToRemove != null) {
                    nodeToRemove.detachAllChildren();
                    this.entityNode.detachChild(nodeToRemove);
                }
            });
            pendingUpdates = null;
        }
    }

    public void setPendingUpdates(final List<EntityUpdate> updates) {
        this.pendingUpdates = updates;
    }

    private Vector3f transformCoordinatesFromServerToClient(EntityUpdate pu) {
        return new Vector3f(
                pu.getX() * 4.0f - 2.0f,
                pu.getY() * 4.0f - 2.0f,
                0.0f
        );
    }

}
