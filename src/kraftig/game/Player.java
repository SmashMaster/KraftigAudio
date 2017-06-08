package kraftig.game;

import com.samrj.devil.game.Keyboard;
import com.samrj.devil.graphics.Camera3D;
import com.samrj.devil.graphics.Camera3DController;
import com.samrj.devil.math.Util;
import com.samrj.devil.math.Vec2i;
import com.samrj.devil.math.Vec3;
import org.lwjgl.glfw.GLFW;

public class Player
{
    private static final float Z_NEAR = 0.125f, Z_FAR = 128.0f;
    private static final float FOV = Util.toRadians(90.0f);
    private static final float SENSITIVTY = Util.toRadians(1.0f/8.0f);
    private static final float HEIGHT = 1.75f;
    private static final float SPEED = 3.0f, ACC = 24.0f;
    private static final float SPEED_SPRINT = 6.0f, ACC_SPRINT = 48.0f;
    
    private final Keyboard keyboard;
    
    private final Vec3 pos, vel = new Vec3();
    private final Camera3D camera;
    private final Camera3DController cameraController;
    
    Player(Keyboard keyboard, Vec2i resolution)
    {
        this.keyboard = keyboard;
        
        camera = new Camera3D(Z_NEAR, Z_FAR, FOV, (float)resolution.y/resolution.x);
        cameraController = new Camera3DController(camera);
        cameraController.sensitivity = SENSITIVTY;
        cameraController.height = HEIGHT;
        pos = cameraController.target;
    }
    
    Camera3D getCamera()
    {
        return camera;
    }
    
    void onMouseMoved(float x, float y, float dx, float dy)
    {
        cameraController.onMouseMoved(x, y, dx, dy);
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
        float acc = sprinting ? ACC_SPRINT : ACC;
        
        if (!desiredVel.isZero(0.0f)) desiredVel.normalize().mult(speed);
        vel.move(desiredVel, dt*acc);
        
        pos.madd(vel, dt);
        cameraController.update();
    }
}
