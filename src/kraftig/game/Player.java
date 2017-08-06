package kraftig.game;

import com.samrj.devil.game.Keyboard;
import com.samrj.devil.graphics.Camera3D;
import com.samrj.devil.graphics.Camera3DController;
import com.samrj.devil.math.Util;
import com.samrj.devil.math.Vec3;
import org.lwjgl.glfw.GLFW;

public class Player
{
    private static final float SENSITIVTY = Util.toRadians(1.0f/8.0f);
    private static final float HEIGHT = 1.75f;
    private static final float SPEED = 0.75f, SPEED_SPRINT = 2.5f, ACC = 8.0f;
    
    private final Keyboard keyboard;
    private final Vec3 pos, vel = new Vec3();
    private final Camera3DController cameraController;
    
    Player(Keyboard keyboard, Camera3D camera)
    {
        this.keyboard = keyboard;
        
        cameraController = new Camera3DController(camera);
        cameraController.sensitivity = SENSITIVTY;
        cameraController.height = HEIGHT;
        pos = cameraController.target;
    }
    
    void setCamera(Camera3D camera)
    {
        cameraController.setCamera(camera);
        cameraController.update();
    }
    
    void onMouseMoved(float x, float y, float dx, float dy)
    {
        cameraController.onMouseMoved(x, y, dx, dy);
    }
    
    float getYaw()
    {
        return cameraController.getYaw();
    }
    
    void step(float dt)
    {
        float yaw = cameraController.getYaw();
        float cosYaw = (float)Math.cos(yaw);
        float sinYaw = (float)Math.sin(yaw);
        
        Vec3 groundFwd = new Vec3(-sinYaw, 0.0f, -cosYaw);
        Vec3 groundRight = new Vec3(cosYaw, 0.0f, -sinYaw);
        Vec3 desiredVel = new Vec3();
        
        if (keyboard.isKeyDown(GLFW.GLFW_KEY_W)) desiredVel.add(groundFwd);
        if (keyboard.isKeyDown(GLFW.GLFW_KEY_S)) desiredVel.sub(groundFwd);
        if (keyboard.isKeyDown(GLFW.GLFW_KEY_D)) desiredVel.add(groundRight);
        if (keyboard.isKeyDown(GLFW.GLFW_KEY_A)) desiredVel.sub(groundRight);
        
        boolean sprinting = keyboard.isKeyDown(GLFW.GLFW_KEY_LEFT_SHIFT);
        float speed = sprinting ? SPEED_SPRINT : SPEED;
        
        if (!desiredVel.isZero(0.0f)) desiredVel.normalize().mult(speed);
        vel.move(desiredVel, dt*ACC);
        
        pos.madd(vel, dt);
        cameraController.update();
    }
}
