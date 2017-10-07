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
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Sphere;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.mejsla.camp.mazela.network.common.protos.MazelaProtocol;

import java.util.*;
import java.util.stream.Collectors;

/**
 *
 * @author Johan Maasing <johan@zoom.nu>
 */
public class GameboardAppstate extends AbstractAppState {

    private final float Z_AXIS_OFFSET = -40.0f;
    private final Logger log = LoggerFactory.getLogger(getClass());
    private final Map<UUID, Node> entityNodes = new HashMap<>();
    private final Map<UUID, Node> scoreNodes = new HashMap<>();
    private AssetManager assetManager;
    private List<EntityUpdate> pendingUpdates = null;
    private Node rootNode;
    private Node entityNode;
    private KeyboardInputAppState keyboardInputAppState ;
    private UUID playerUUID;

    @Override
    public void initialize(AppStateManager stateManager, Application app) {
        SimpleApplication sapp = ((SimpleApplication) app);
        this.rootNode = sapp.getRootNode();
        this.assetManager = sapp.getAssetManager();
        this.rootNode.addLight(new DirectionalLight(new Vector3f(0.5f, -1.0f, 1.0f).normalize()));
        this.rootNode.addLight(new AmbientLight(new ColorRGBA(0.5f, 0.5f, 0.5f, 1.0f)));
        this.entityNode = new Node("Entities");
        this.rootNode.attachChild(this.entityNode);
        addHorizontalEdge("top", 9f);
        addHorizontalEdge("bottom", -9f);
        addVerticalEdge("left", -9f);
        addVerticalEdge("right", 9f);
        super.initialize(stateManager, app);
    }

    private void addHorizontalEdge(final String name, final float y) {
        Geometry geom = new Geometry(name, new Box(10, 1, 1));
        final Material mat = new Material(
                assetManager,
                "Common/MatDefs/Light/Lighting.j3md"
        );
        mat.setBoolean("UseMaterialColors", true);
        mat.setColor("Diffuse", ColorRGBA.Green);
        mat.setColor("Ambient", ColorRGBA.Green);
        mat.setColor("Specular", ColorRGBA.White);
        mat.setFloat("Shininess", 8f);  // [0,128]
        geom.setMaterial(mat);
        geom.setLocalTranslation(0, y, Z_AXIS_OFFSET);
        rootNode.attachChild(geom);
    }

    private void addVerticalEdge(final String name, final float x) {
        Geometry geom = new Geometry(name, new Box(1, 10, 1));
        final Material mat = new Material(
                assetManager,
                "Common/MatDefs/Light/Lighting.j3md"
        );
        mat.setBoolean("UseMaterialColors", true);
        mat.setColor("Diffuse", ColorRGBA.Green);
        mat.setColor("Ambient", ColorRGBA.Green);
        mat.setColor("Specular", ColorRGBA.White);
        mat.setFloat("Shininess", 8f);  // [0,128]
        geom.setMaterial(mat);
        geom.setLocalTranslation(x, 0, Z_AXIS_OFFSET);
        rootNode.attachChild(geom);
    }

    @Override
    public void update(float tpf) {
        if (this.pendingUpdates != null) {
            // Mutate the scene graph
            ArrayList<UUID> updatedEntities = new ArrayList<>();
            for (EntityUpdate pu : pendingUpdates) {
                final UUID entityUUID = pu.getEntityID();
                Node node = entityNodes.get(entityUUID);
                if (node == null) {
//                    https://jmonkeyengine.github.io/wiki/jme3/advanced/shape.html#3d-shapes
//                    Dome mesh = new Dome(Vector3f.    ZERO, 2, 4, 1f,false); // Pyramid
                    final Geometry geometry;
                    final Material material;
                    if (pu instanceof PlayerEntityUpdate) {
                        log.debug("Creating player node");
                        geometry = new Geometry(
                                entityUUID.toString() + "-geom",
                                new Sphere(32, 32, 1.0f)
                        );
                        material = new Material(assetManager,"Common/MatDefs/Light/Lighting.j3md");
                        addScoreNode((PlayerEntityUpdate) pu);
                    } else {
                        geometry = new Geometry(
                                entityUUID.toString() + "-geom",
                                new Box(0.5f, 0.5f, 1.0f)
                        );
                        material = new Material(assetManager,"Common/MatDefs/Light/Lighting.j3md");
                    }
                    // Get entity color
                    MazelaProtocol.Color color = pu.getColor();
                    //log.debug("Using color: " + color);
                    ColorRGBA colorRGBA = new ColorRGBA(color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f, 1f);
                    material.setBoolean("UseMaterialColors", true);
                    material.setColor("Diffuse", colorRGBA);
                    material.setColor("Ambient", colorRGBA);
                    material.setColor("Specular", ColorRGBA.White);
                    material.setFloat("Shininess", 64f);  // [0,128]
                    geometry.setMaterial(material);
                    node = new Node(entityUUID.toString());
                    node.attachChild(geometry);
                    //log.debug("Attaching entity {} node", entityUUID);
                    rootNode.attachChild(node);
                    entityNodes.put(entityUUID, node);
                }
                node.setLocalTranslation(transformCoordinatesFromServerToClient(pu));

                updatedEntities.add(entityUUID);

                // If player, also update score text
                if (pu instanceof PlayerEntityUpdate) {
                    updateScoreNodeText((PlayerEntityUpdate) pu);
                }
            }

            // remove entities that was not included in the update
            final Set<UUID> entitiesToRemove = entityNodes
                    .keySet()
                    .stream()
                    .filter(knownEntityUUID -> !updatedEntities.contains(knownEntityUUID))
                    .collect(Collectors.toSet());
            entitiesToRemove.forEach(e -> {
                Node nodeToRemove = entityNodes.remove(e);
                if (nodeToRemove != null) {
                    nodeToRemove.detachAllChildren();
                    this.entityNode.detachChild(nodeToRemove);
                }
                // Also remove score node
                nodeToRemove = scoreNodes.remove(e);
                if (nodeToRemove != null) {
                    nodeToRemove.detachAllChildren();
                    this.entityNode.detachChild(nodeToRemove);
                }
            });
            pendingUpdates = null;

            // Now that we know which players are left, update positions for all score nodes
            updateScoreNodePositions();
        }
    }

    private void updateScoreNodePositions() {
        List<Node> nodes = new ArrayList<>(scoreNodes.values());
        // Sort players in name order
        nodes.sort(new BitmapTextComparator());
        for (int i = 0; i < nodes.size(); i++) {
            Node node = nodes.get(i);
            node.setLocalTranslation(-27, 20 - i * 2, Z_AXIS_OFFSET);
        }
    }

    private void updateScoreNodeText(PlayerEntityUpdate entityUpdate) {
        UUID entityID = entityUpdate.getEntityID();

        String name = entityUpdate.getName();
        int score = entityUpdate.getScore();

        BitmapText scoreNode = (BitmapText) scoreNodes.get(entityID);
        scoreNode.setText(name + ": " + score);
    }

    private void addScoreNode(PlayerEntityUpdate entityUpdate) {
        String name = entityUpdate.getName();
        int score = entityUpdate.getScore();

        BitmapFont guiFont = assetManager.loadFont("Interface/Fonts/Default.fnt");
        BitmapText scoreNode = new BitmapText(guiFont, false);
        scoreNode.setSize(1);
        if (entityUpdate.getEntityID().equals(playerUUID)) {
            scoreNode.setColor(ColorRGBA.Cyan);
        }
        scoreNode.setText(name + ": " + score);

        rootNode.attachChild(scoreNode);
        scoreNodes.put(entityUpdate.getEntityID(), scoreNode);
    }

    public void setPendingUpdates(final List<EntityUpdate> updates) {
        this.pendingUpdates = updates;
    }

    private Vector3f transformCoordinatesFromServerToClient(EntityUpdate pu) {
        return new Vector3f(
                pu.getX(),
                pu.getY(),
                Z_AXIS_OFFSET
        );
    }

    public void setPlayerUUID(UUID uuid) {
        this.playerUUID = uuid;
    }

    private static class BitmapTextComparator implements Comparator<Node> {
        @Override
        public int compare(Node node1, Node node2) {
            BitmapText text1 = (BitmapText) node1;
            BitmapText text2 = (BitmapText) node2;
            return text1.getText().compareTo(text2.getText());
        }
    }
}
