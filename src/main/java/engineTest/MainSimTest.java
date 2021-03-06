package engineTest;

import entities.Camera;
import entities.Entity;
import entities.Light;
import entities.Player;
import guis.GuiRenderer;
import guis.GuiTexture;
import models.TexturedModel;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.Display;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;
import particles.Particle;
import particles.ParticleMaster;
import particles.ParticleSystem;
import particles.ParticleTexture;
import renderEngine.*;
import models.RawModel;
import terrains.Terrain;
import terrains.TerrainTexture;
import terrains.TerrainTexturePack;
import textures.ModelTexture;
import tools.MousePicker;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

/**
 * Main simulator Simulator
 * Created by karltrout on 6/6/17.
 */
public class MainSimTest {

    private static List<Entity> entities = new ArrayList<>();


    public static void  main (String[] args){

        DisplayManager.createDisplay();

        Loader loader = new Loader();
        RawModel model = null;

        TerrainTexture  backgroundTexture = new TerrainTexture(loader.loadTexture("terrain"));
        TerrainTexturePack pack = new TerrainTexturePack(backgroundTexture);

        try {
            model = OBJLoader.loadObjModel("A380", loader);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        ModelTexture texture = new ModelTexture(loader.loadTexture("a380_AIRBUS"));
        texture.setShineDampner(50);
        texture.setReflectivity(0.15f);

        TexturedModel texturedModel = new TexturedModel(model, texture);

        Player entity = new Player(texturedModel, new Vector3f(0,25.75f,-100),-90,0,0,1.0f);
        entities.add(entity);

        Terrain terrain = new Terrain(0,-1, loader, pack, null);
        Terrain terrain2 = new Terrain(-1,-1, loader, pack, null);
        Terrain terrain3 = new Terrain(0,0, loader, pack, null);
        Terrain terrain4 = new Terrain(-1,0, loader, pack,null);

        Light light = new Light(new Vector3f(1000000,1000000,-1000000), new Vector3f(1,1,1));

        Camera camera = new Camera(entity);

        MasterRenderer masterRenderer = new MasterRenderer(loader, camera);
        ParticleMaster.init(loader, masterRenderer.getProjectionMatrix());

        MousePicker picker = new MousePicker(camera, masterRenderer.getProjectionMatrix());

        List<GuiTexture> guis = new ArrayList<>();
        GuiTexture shadowBoxGui = new GuiTexture(masterRenderer.getShadowMapTexture(), new Vector2f(0.5f, 0.5f), new Vector2f(0.25f, 0.25f));
        //guis.add(shadowBoxGui);
        GuiRenderer guiRenderer = new GuiRenderer(loader);


        ParticleTexture particleTexture = new ParticleTexture(loader.loadTexture("smoke2"),8);
        ParticleSystem particleSystem = new ParticleSystem(particleTexture, 50, 1, -.005f,10);

        /*
         * Game Loop Here.
         */
        while(!Display.isCloseRequested()){

            //entity.increaseRotation(0,0,.20f);

            camera.move();
            entity.move();

            picker.update();

            Vector3f entityPosition = entity.getPosition();
            Vector3f leftParticlePosition = new Vector3f(entityPosition.x+12.5f,entityPosition.y - 3f,entityPosition.z );
            Vector3f rightParticlePosition = new Vector3f(entityPosition.x-12.5f,entityPosition.y - 3f,entityPosition.z );
            if(Keyboard.isKeyDown(Keyboard.KEY_W)) {
                particleSystem.generateParticles(leftParticlePosition);
                particleSystem.generateParticles(rightParticlePosition);
            }
            ParticleMaster.update(camera );


            masterRenderer.renderShadowMap(entities, light);
            //System.out.println(picker.getCurrentRay());

            masterRenderer.processTerrain(terrain);
            masterRenderer.processTerrain(terrain2);
            masterRenderer.processTerrain(terrain3);
            masterRenderer.processTerrain(terrain4);
            masterRenderer.processEntitity(entity);

            masterRenderer.render(light, camera);

                ParticleMaster.renderParticles(camera);



            guiRenderer.render(guis);

            DisplayManager.updateDisplay();

        }

        ParticleMaster.cleanUp();
        guiRenderer.cleanUp();
        masterRenderer.cleanUp();
        loader.cleanUp();
        DisplayManager.closeDisplay();


    }
}
