package mygame;

import com.jme3.app.SimpleApplication;
import com.jme3.asset.TextureKey;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.collision.shapes.CapsuleCollisionShape;
import com.jme3.bullet.control.CharacterControl;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.font.BitmapText;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.AnalogListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.scene.CameraNode;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.CameraControl.ControlDirection;
import com.jme3.scene.shape.Box;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture.WrapMode;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This is the Main Class of your Game. You should only do initialization here.
 * Move your Logic into AppStates or Controls
 *
 * @author normenhansen
 */
public class Main extends SimpleApplication {

    protected CharacterControl player_character_control;
    private BulletAppState bulletAppState;
    Boolean isRunning = true;

    private RigidBodyControl brick_phy;
    private RigidBodyControl floor_phy;

    private Material player_mat;
    private Material brick_mat;
    private Material floor_mat;

    private CameraNode camNode;
    
    private static final Box floor;
    private static final Box fake_player_box;
    private Geometry player_geometry;
    private Vector3f walkDirection = new Vector3f();

    private boolean left;
    private boolean right;
    private boolean jump;

    static {
        floor = new Box(10f, 0.1f, 5f);
        floor.scaleTextureCoordinates(new Vector2f(3, 6));
        fake_player_box = new Box(1f, 1f, 1f);
    }

    public static void main(String[] args) {
        Main app = new Main();
        app.start();
    }

    @Override
    public void simpleInitApp() {
        bulletAppState = new BulletAppState();
        stateManager.attach(bulletAppState);

        initMaterials();
        initFloor();
        cam.setLocation(new Vector3f(0, 4f, 6f));
        cam.lookAt(new Vector3f(2, 2, 0), Vector3f.UNIT_Y);

        // Load a model from test_data (OgreXML + material + texture)
        //Spatial ninja = assetManager.loadModel("Models/Ninja/Ninja.mesh.xml");
        player_geometry = new Geometry("Player", fake_player_box);
        player_geometry.setLocalScale(new Vector3f(20f, 20f, 20f));
        player_geometry.setMaterial(brick_mat);
        player_geometry.scale(0.01f, 0.01f, 0.01f);
        player_geometry.setLocalTranslation(0.0f, 0.0f, 00f);
        rootNode.attachChild(player_geometry);

        CapsuleCollisionShape capsuleShape = new CapsuleCollisionShape(0.1f, 0.1f, 1);
        player_character_control = new CharacterControl(capsuleShape, 0.05f);
        player_character_control.setJumpSpeed(5);
        player_character_control.setFallSpeed(5);
        player_character_control.setGravity(30);
        player_geometry.addControl(player_character_control);

        player_character_control.setPhysicsLocation(new Vector3f(0, 10, 0));
        bulletAppState.getPhysicsSpace().add(player_character_control);
        
        // You must add a light to make the model visible
        DirectionalLight sun = new DirectionalLight();
        sun.setDirection(new Vector3f(-0.1f, -0.7f, -1.0f));
        rootNode.addLight(sun);

        initKeys();
    }

    /**
     * Make a solid floor and add it to the scene.
     */
    public void initFloor() {
        Geometry floor_geo = new Geometry("Floor", floor);
        floor_geo.setMaterial(floor_mat);

        floor_geo.setLocalTranslation(0f, 0f, 0f);
        this.rootNode.attachChild(floor_geo);

        /* Make the floor physical with mass 0.0f! */
        floor_phy = new RigidBodyControl(0.0f);
        floor_geo.addControl(floor_phy);
        bulletAppState.getPhysicsSpace().add(floor_phy);
    }

    private void initKeys() {
        // You can map one or several inputs to one named action
        inputManager.addMapping("Pause", new KeyTrigger(KeyInput.KEY_P));
        inputManager.addMapping("Left", new KeyTrigger(KeyInput.KEY_J));
        inputManager.addMapping("Right", new KeyTrigger(KeyInput.KEY_K));
        inputManager.addMapping("Jump", new KeyTrigger(KeyInput.KEY_SPACE));

        // Add the names to the action listener.
        inputManager.addListener(actionListener, "Left", "Right", "Jump");

    }

    private ActionListener actionListener = new ActionListener() {

        @Override
        public void onAction(String binding, boolean isPressed, float tpf) {
            switch (binding) {
                case "Left":
                    if (isPressed) {
                        left = true;
                    } else {
                        left = false;
                    }   
                    break;
                case "Right":
                    if (isPressed) {
                        right = true;
                    } else {
                        right = false;
                    }   
                    break;
                case "Jump":
                    /** Located here only one jump allowed ber frame*/
                    player_character_control.jump();
                    break;
                default:
                    break;
            }

        }

    };

    @Override
    public void simpleUpdate(float tpf) {
        walkDirection.set(0, 0, 0);
        if (left) {
            walkDirection.setX(0.1f);
        } else {
            if (right) {
                walkDirection.setX(-0.1f);
            }
        }
        player_character_control.setWalkDirection(walkDirection);
        cam.lookAt(player_geometry.getLocalTranslation(), Vector3f.UNIT_Y);

     
    }

    @Override
    public void simpleRender(RenderManager rm) {
        //TODO: add render code
    }

    public void createWall(int lenghtOfWall) {
        // Create a wall with a simple texture from test_data
        for (int index = 0; index < lenghtOfWall; index++) {
            float coeficient_x = (index) * 2;
            /**
             * Create a box shape and then create Geometry
             */
            Box box = new Box(1f, 1.0f, 3.0f);
            Spatial wall = new Geometry("Box", box);

            /**
             * Material and texture
             */
            wall.setMaterial(brick_mat);
            wall.setLocalTranslation(coeficient_x, -2f, +7f);

            /**
             * Make brick physical with a mass > 0.0f.
             */
            brick_phy = new RigidBodyControl(2f);

            /**
             * Add physical brick to physics space.
             */
            wall.addControl(brick_phy);
            bulletAppState.getPhysicsSpace().add(brick_phy);

            rootNode.attachChild(wall);
        }
    }

    private void initMaterials() {
        player_mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        player_mat.setColor("Color", ColorRGBA.Blue);

        brick_mat = new Material(
                assetManager, "Common/MatDefs/Misc/Unshaded.j3md");

        brick_mat.setTexture("ColorMap",
                assetManager.loadTexture("Textures/Terrain/BrickWall/BrickWall.jpg"));

        floor_mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");

        TextureKey key3 = new TextureKey("Textures/Terrain/Pond/Pond.jpg");
        key3.setGenerateMips(true);

        Texture tex3 = assetManager.loadTexture(key3);
        tex3.setWrap(WrapMode.Repeat);
        floor_mat.setTexture("ColorMap", tex3);
    }
}
