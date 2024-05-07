package com.zergatul.freecam;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.Camera;
import net.minecraft.client.CameraType;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.Input;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class FreeCam {

    public static final FreeCam instance = new FreeCam();

    private final static int REMEMBER_STATE_DELAY_MS = 400;

    private final Minecraft mc = Minecraft.getInstance();
    private final Quaternionf rotation = new Quaternionf(0.0F, 0.0F, 0.0F, 1.0F);
    private final Vector3f forwards = new Vector3f(0.0F, 0.0F, 1.0F);
    private final Vector3f up = new Vector3f(0.0F, 1.0F, 0.0F);
    private final Vector3f left = new Vector3f(1.0F, 0.0F, 0.0F);
    private final FreeCamPath path = new FreeCamPath(this);
    private final FreeCamConfig config = ConfigRepository.instance.load();
    private boolean active;
    private CameraType oldCameraType;
    private Input playerInput;
    private Input freecamInput;
    private double x, y, z;
    private float yRot, xRot;
    private double forwardVelocity;
    private double leftVelocity;
    private double upVelocity;
    private long lastTime;
    private boolean freecamHitResultPicking;
    private boolean cameraLock;
    private boolean eyeLock;
    private boolean followCamera;
    private double followDeltaX, followDeltaY, followDeltaZ;
    private boolean gameRendererPicking;
    private boolean moveAlongPath;
    private long pathStartTime;
    private long dontMoveFreeCamBefore;

    private FreeCam() {

    }

    public boolean isActive() {
        return active;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getZ() {
        return z;
    }

    public float getXRot() {
        return xRot;
    }

    public float getYRot() {
        return yRot;
    }

    public FreeCamConfig getConfig() {
        return config;
    }

    public FreeCamPath getPath() {
        return path;
    }

    public void toggle() {
        if (active) {
            disable();
        } else {
            enable();
        }
    }

    public void toggleCameraLock() {
        assert mc.player != null;

        if (active && !followCamera) {
            cameraLock = !cameraLock;
            if (cameraLock) {
                mc.player.input = playerInput;
            } else {
                mc.player.input = freecamInput;
            }
        }
    }

    public void toggleEyeLock() {
        if (active && !followCamera) {
            eyeLock = !eyeLock;
        }
    }

    public void toggleFollowCamera() {
        assert mc.player != null;

        if (active) {
            followCamera = !followCamera;
            if (followCamera) {
                mc.player.input = playerInput;
                cameraLock = false;
                eyeLock = false;

                Entity entity = mc.getCameraEntity();
                if (entity == null) {
                    return;
                }

                Vec3 pos = entity.getEyePosition();
                followDeltaX = x - pos.x;
                followDeltaY = y - pos.y;
                followDeltaZ = z - pos.z;
            } else {
                mc.player.input = freecamInput;
            }
        }
    }

    public void enable() {
        if (active) {
            return;
        }

        Entity entity = mc.getCameraEntity();
        if (entity == null) {
            return;
        }

        active = true;
        cameraLock = false;
        eyeLock = false;
        followCamera = false;
        oldCameraType = mc.options.getCameraType();
        playerInput = mc.player.input;
        mc.player.input = freecamInput = createFreeCamInput(playerInput);
        mc.options.setCameraType(CameraType.THIRD_PERSON_BACK);
        if (oldCameraType.isFirstPerson() != mc.options.getCameraType().isFirstPerson()) {
            mc.gameRenderer.checkEntityPostEffect(mc.options.getCameraType().isFirstPerson() ? mc.getCameraEntity() : null);
        }

        if (config.rememberInputState) {
            dontMoveFreeCamBefore = System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(REMEMBER_STATE_DELAY_MS);
        }

        float frameTime = mc.getFrameTime();
        Vec3 pos = entity.getEyePosition(frameTime);
        x = pos.x;
        y = pos.y;
        z = pos.z;
        yRot = entity.getViewYRot(frameTime);
        xRot = entity.getViewXRot(frameTime);

        calculateVectors();

        double distance = -2;
        x += (double)this.forwards.x() * distance;
        y += (double)this.forwards.y() * distance;
        z += (double)this.forwards.z() * distance;

        forwardVelocity = 0;
        leftVelocity = 0;
        upVelocity = 0;
        lastTime = 0;
    }

    public void disable() {
        assert mc.player != null;

        if (!active) {
            return;
        }

        active = false;
        CameraType cameraType = mc.options.getCameraType();
        mc.options.setCameraType(oldCameraType);
        mc.player.input = playerInput;
        if (cameraType.isFirstPerson() != mc.options.getCameraType().isFirstPerson()) {
            mc.gameRenderer.checkEntityPostEffect(mc.options.getCameraType().isFirstPerson() ? mc.getCameraEntity() : null);
        }
        oldCameraType = null;
    }

    public void onHandleKeyBindings() {
        if (mc.player == null) {
            return;
        }
        if (mc.screen != null) {
            return;
        }
        while (KeyBindings.toggleFreeCam.consumeClick()) {
            toggle();
        }
        while (KeyBindings.toggleCameraLock.consumeClick()) {
            toggleCameraLock();
        }
        while (KeyBindings.toggleEyeLock.consumeClick()) {
            toggleEyeLock();
        }
        while (KeyBindings.toggleFollowCam.consumeClick()) {
            toggleFollowCamera();
        }
        while (KeyBindings.startPath.consumeClick()) {
            startPath();
        }
    }

    public void onPlayerTurn(LocalPlayer player, double yRot, double xRot) {
        if (active && !cameraLock && !followCamera) {
            if (!eyeLock && !moveAlongPath) {
                this.xRot += (float) xRot * 0.15F;
                this.yRot += (float) yRot * 0.15F;
                this.xRot = Mth.clamp(this.xRot, -90, 90);
                calculateVectors();
            }
        } else {
            player.turn(yRot, xRot);
        }
    }

    public boolean onRenderCrosshairModifyIsFirstPerson(boolean value) {
        if (active) {
            return !cameraLock && !eyeLock && !followCamera && config.target;
        } else {
            return value;
        }
    }

    public boolean onRenderItemInHandIsFirstPerson(CameraType cameraType) {
        if (active && config.renderHands && !cameraLock && !eyeLock && !followCamera) {
            return true;
        } else {
            return cameraType.isFirstPerson();
        }
    }

    public void onRenderTickStart(float partialTicks) {
        if (!active) {
            return;
        }

        if (lastTime == 0) {
            lastTime = System.nanoTime();
            return;
        }

        long currTime = System.nanoTime();
        float frameTime = (currTime - lastTime) / 1e9f;
        lastTime = currTime;

        if (moveAlongPath) {
            FreeCamPath.Entry entry = path.interpolate((currTime - pathStartTime) / 1e6);
            if (entry == null) {
                moveAlongPath = false;
            } else {
                x = entry.position().x;
                y = entry.position().y;
                z = entry.position().z;
                xRot = (float) entry.xRot();
                yRot = (float) entry.yRot();
            }
        } else if (followCamera) {
            Entity entity = mc.getCameraEntity();
            if (entity != null) {
                Vec3 pos = entity.getEyePosition(partialTicks);
                x = pos.x + followDeltaX;
                y = pos.y + followDeltaY;
                z = pos.z + followDeltaZ;
            }
        } else {
            Input input = playerInput;
            float forwardImpulse = !cameraLock ? (input.up ? 1 : 0) + (input.down ? -1 : 0) : 0;
            float leftImpulse = !cameraLock ? (input.left ? 1 : 0) + (input.right ? -1 : 0) : 0;
            float upImpulse = !cameraLock ? ((input.jumping ? 1 : 0) + (input.shiftKeyDown ? -1 : 0)) : 0;
            double slowdown = Math.pow(config.slowdownFactor, frameTime);
            forwardVelocity = combineMovement(forwardVelocity, forwardImpulse, frameTime, config.acceleration, slowdown);
            leftVelocity = combineMovement(leftVelocity, leftImpulse, frameTime, config.acceleration, slowdown);
            upVelocity = combineMovement(upVelocity, upImpulse, frameTime, config.acceleration, slowdown);

            double dx = (double) this.forwards.x() * forwardVelocity + (double) this.left.x() * leftVelocity;
            double dy = (double) this.forwards.y() * forwardVelocity + upVelocity + (double) this.left.y() * leftVelocity;
            double dz = (double) this.forwards.z() * forwardVelocity + (double) this.left.z() * leftVelocity;
            dx *= frameTime;
            dy *= frameTime;
            dz *= frameTime;
            double speed = new Vec3(dx, dy, dz).length() / frameTime;
            if (speed > config.maxSpeed) {
                double factor = config.maxSpeed / speed;
                forwardVelocity *= factor;
                leftVelocity *= factor;
                upVelocity *= factor;
                dx *= factor;
                dy *= factor;
                dz *= factor;
            }
            if (!config.rememberInputState || dontMoveFreeCamBefore < currTime) {
                x += dx;
                y += dy;
                z += dz;
            }
        }

        applyEyeLock(partialTicks);
    }

    public void onClientTickStart() {
        if (active) {
            disableKey(mc.options.keyTogglePerspective);
            playerInput.tick(false, 0);
        }
    }

    public void onWorldUnload() {
        disable();
    }

    public void onRenderWorldLast(Matrix4f pose, Matrix4f projectionMatrix, Camera camera) {
        if (!active || moveAlongPath) {
            return;
        }

        List<FreeCamPath.Entry> path = getPath().get();
        if (path.size() < 2) {
            return;
        }

        BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();
        bufferBuilder.begin(VertexFormat.Mode.DEBUG_LINES, DefaultVertexFormat.POSITION_COLOR);
        RenderSystem.setShaderColor(1f, 1.0f, 1f, 1f);

        Vec3 view = camera.getPosition();
        for (int i = 1; i < path.size(); i++) {
            FreeCamPath.Entry e1 = path.get(i - 1);
            FreeCamPath.Entry e2 = path.get(i);

            bufferBuilder.vertex(
                            e1.position().x - view.x,
                            e1.position().y - view.y,
                            e1.position().z - view.z)
                    .color(1, 1, 1, 1f).endVertex();
            bufferBuilder.vertex(
                            e2.position().x - view.x,
                            e2.position().y - view.y,
                            e2.position().z - view.z)
                    .color(1, 1, 1, 1f).endVertex();
        }

        renderLines(bufferBuilder, pose, projectionMatrix);
    }

    private void startPath() {
        if (!active) {
            return;
        }

        moveAlongPath = true;
        pathStartTime = System.nanoTime();
    }

    public boolean shouldOverrideCameraEntityPosition(Entity entity) {
        if (active && !cameraLock && !eyeLock && !followCamera && config.target) {
            return entity == mc.getCameraEntity() && gameRendererPicking || freecamHitResultPicking;
        } else {
            return false;
        }
    }

    public void onRenderDebugScreenLeft(List<String> list) {
        if (active) {
            list.add("");
            String coordinates = String.format(Locale.ROOT, "Free Cam XYZ: %.3f / %.5f / %.3f", x, y, z);
            list.add(coordinates);
        }
    }

    public void onRenderDebugScreenRight(List<String> list) {
        if (!active) {
            return;
        }
        if (cameraLock || eyeLock || followCamera) {
            return;
        }
        if (!config.target) {
            return;
        }

        // TODO: remove and just use free cam target as normal?
        freecamHitResultPicking = true;
        try {
            HitResult hit = mc.player.pick(20.0D, 0.0F, false);
            if (hit.getType() == HitResult.Type.BLOCK) {
                BlockPos pos = ((BlockHitResult)hit).getBlockPos();
                BlockState state = mc.level.getBlockState(pos);
                list.add("");
                list.add(ChatFormatting.UNDERLINE + "Free Cam Targeted Block: " + pos.getX() + ", " + pos.getY() + ", " + pos.getZ());
                list.add(String.valueOf(ModApiWrapper.instance.BLOCKS.getKey(state.getBlock())));

                for (var entry: state.getValues().entrySet()) {
                    list.add(getPropertyValueString(entry));
                }

                state.getTags().map(tag -> "#" + tag.location()).forEach(list::add);
            }
        }
        finally {
            freecamHitResultPicking = false;
        }
    }

    public void onBeforeGameRendererPick() {
        gameRendererPicking = true;
    }

    public void onAfterGameRendererPick() {
        gameRendererPicking = false;
    }

    private Input createFreeCamInput(Input playerInput) {
        if (config.rememberInputState) {
            Input input = new Input();
            input.up = playerInput.up;
            input.down = playerInput.down;
            input.left = playerInput.left;
            input.right = playerInput.right;
            input.jumping = playerInput.jumping;
            input.shiftKeyDown = playerInput.shiftKeyDown;
            input.forwardImpulse = playerInput.forwardImpulse;
            input.leftImpulse = playerInput.leftImpulse;
            return input;
        } else {
            return new Input();
        }
    }

    private void applyEyeLock(float partialTicks) {
        if (!eyeLock) {
            return;
        }

        Entity entity = mc.getCameraEntity();
        if (entity == null) {
            return;
        }

        Vec3 pos = entity.getEyePosition(partialTicks);
        double dx = x - pos.x;
        double dy = y - pos.y;
        double dz = z - pos.z;
        this.xRot = (float) (Math.atan2(dy, Math.sqrt(dx * dx + dz * dz)) / Math.PI * 180);
        this.yRot = (float) (Math.atan2(dz, dx) / Math.PI * 180 + 90);
        this.xRot = Mth.clamp(this.xRot, -90, 90);
        calculateVectors();
    }

    private void calculateVectors() {
        rotation.rotationYXZ(-yRot * ((float)Math.PI / 180F), (config.spectatorMovement ? 0 : xRot) * ((float)Math.PI / 180F), 0.0F);
        forwards.set(0.0F, 0.0F, 1.0F).rotate(rotation);
        up.set(0.0F, 1.0F, 0.0F).rotate(rotation);
        left.set(1.0F, 0.0F, 0.0F).rotate(rotation);
    }

    private double combineMovement(double velocity, double impulse, double frameTime, double acceleration, double slowdown) {
        if (impulse != 0) {
            if (impulse > 0 && velocity < 0) {
                velocity = 0;
            }
            if (impulse < 0 && velocity > 0) {
                velocity = 0;
            }
            velocity += acceleration * impulse * frameTime;
        } else {
            velocity *= slowdown;
        }
        return velocity;
    }

    private String getPropertyValueString(Map.Entry<Property<?>, Comparable<?>> p_94072_) {
        Property<?> property = p_94072_.getKey();
        Comparable<?> comparable = p_94072_.getValue();
        String s = Util.getPropertyName(property, comparable);
        if (Boolean.TRUE.equals(comparable)) {
            s = ChatFormatting.GREEN + s;
        } else if (Boolean.FALSE.equals(comparable)) {
            s = ChatFormatting.RED + s;
        }

        return property.getName() + ": " + s;
    }

    private void disableKey(KeyMapping key) {
        while (key.consumeClick()) {}
        key.setDown(false);
    }

    private void renderLines(BufferBuilder bufferBuilder, Matrix4f pose, Matrix4f projection) {
        RenderSystem.disableCull();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest();
        GL11.glEnable(GL11.GL_LINE_SMOOTH);

        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);

        SharedVertexBuffer.instance.bind();
        SharedVertexBuffer.instance.upload(bufferBuilder.end());
        SharedVertexBuffer.instance.drawWithShader(pose, projection, GameRenderer.getPositionColorShader());
        VertexBuffer.unbind();

        RenderSystem.disableBlend();
        RenderSystem.enableCull();
        RenderSystem.enableDepthTest();
    }

    private static class SharedVertexBuffer {
        public static final VertexBuffer instance = new VertexBuffer(VertexBuffer.Usage.DYNAMIC);
    }
}