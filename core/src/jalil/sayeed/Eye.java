package jalil.sayeed;

import com.badlogic.gdx.ai.steer.Steerable;
import com.badlogic.gdx.ai.utils.Location;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import jalil.sayeed.Utils.Box2dSteeringUtils;

import static jalil.sayeed.Utils.Constants.PPM;

public class Eye extends Enemies implements Steerable<Vector2> {

    float maxLinearSpeed;
    float maxLinearAcceleration;
    float maxAngularSpeed;
    float maxAngularAcceleration;
    float boundingRadius;
    boolean tagged;
    private enum State {STANDING, RUNNING, ATTACKING}

    private boolean lookRight;
    public Animation<TextureRegion> fly;
    TextureRegion currentFrame;
    private World world;
    private State currentState;
    private State previousState;
    private Animation<TextureRegion> run;
    private Animation<TextureRegion> attack;
    private float stateTimer;
    private boolean runningRight;
    private  boolean isAttacking;
    private Body body;
    private  int x;
    private  int y;
    private  SpriteBatch batch;
    private float timer;
    Eye(int x, int y, World world, Animation<TextureRegion> run, Animation<TextureRegion> fly, Animation<TextureRegion> attack, SpriteBatch batch) {
        this.x = x;
        this.y = y;
        this.world = world;
        this.run = run;
        this.fly = fly;
        this.attack = attack;
        this.batch = batch;
        isAttacking = false;
        stateTimer = 0;
        timer = 0;
        lookRight = true;
    }

    /**
     * Moves the eye
     * @param delta
     */
    public void move(float delta){
        timer += delta;
        int velocity = 0;

        if(timer < 2){
            velocity += 1;
            lookRight = true;
        } else if(timer < 4){
            velocity -= 1;
            lookRight = false;
        } else{
            timer = 0;
        }
        body.setLinearVelocity(velocity * 2, body.getLinearVelocity().y);
    }

    /**
     * Draws the eye on screen
     * @param delta
     */
    public void draw(float delta) {
        getFrame(delta);

        batch.begin();
        batch.draw(currentFrame, body.getPosition().x * PPM - 80, body.getPosition().y * PPM - 70, currentFrame.getRegionWidth(), currentFrame.getRegionHeight());
        batch.end();
    }

    /**
     * Gets animation frame
     * @param dt
     */
    @Override
    public void getFrame(float dt) {
        currentState = getState();

        // Checks which state Eye is in and sets the frame accordingly
        switch (currentState) {
            case ATTACKING:
                currentFrame = attack.getKeyFrame(stateTimer, false);
                break;
            case RUNNING:
                currentFrame = fly.getKeyFrame(stateTimer, true);
                break;
            case STANDING:
            default:
                currentFrame = fly.getKeyFrame(stateTimer, true);
        }

        // Flips the frame depending on where the Eye is facing
        if (!lookRight && !currentFrame.isFlipX()) {
            currentFrame.flip(true, false);
            runningRight = false;
        } else if (lookRight && currentFrame.isFlipX()) {
            currentFrame.flip(true, false);
            runningRight = true;
        }
        stateTimer = currentState == previousState ? stateTimer + dt : 0;
        previousState = currentState;
    }

    /**
     * Create the body
     * @param x
     * @param y
     */
    @Override
    public void createBody(int x, int y) {
        // Initialize body
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.KinematicBody;
        bodyDef.position.set(x, y);
        bodyDef.fixedRotation = false;

        body = world.createBody(bodyDef);

        PolygonShape shape = new PolygonShape();
        shape.setAsBox(32 / 2 / PPM, 32 / 2 / PPM / 1.2f);

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.density = 0;
        fixtureDef.friction = 0.8f;
        fixtureDef.restitution = 0;


        body.createFixture(fixtureDef).setUserData(this);

        shape.dispose();



    }

    @Override
    public Body getBody() {
        createBody(x, y);
        return body;
    }

    public State getState() {

        if (isAttacking) {
            return State.ATTACKING;
        } else if (body.getLinearVelocity().x != 0) {
            return State.RUNNING;
        } else {
            return State.STANDING;
        }
    }

    public TextureRegion getCurrentFrame() {
        return currentFrame;
    }

    public void hit(){
        System.out.println("Enemy");
    }
    @Override
    public Vector2 getLinearVelocity() {
        return body.getLinearVelocity();
    }

    @Override
    public float getAngularVelocity() {
        return body.getAngularVelocity();
    }

    @Override
    public float getBoundingRadius() {
        return boundingRadius;
    }

    @Override
    public boolean isTagged() {
        return tagged;
    }

    @Override
    public void setTagged(boolean tagged) {
        this.tagged = tagged;
    }

    @Override
    public float getZeroLinearSpeedThreshold() {
        return 0.001f;
    }

    @Override
    public void setZeroLinearSpeedThreshold(float value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public float getMaxLinearSpeed() {
        return maxLinearSpeed;
    }

    @Override
    public void setMaxLinearSpeed(float maxLinearSpeed) {
        this.maxLinearSpeed = maxLinearSpeed;
    }

    @Override
    public float getMaxLinearAcceleration() {
        return maxLinearAcceleration;
    }

    @Override
    public void setMaxLinearAcceleration(float maxLinearAcceleration) {
        this.maxLinearAcceleration = maxLinearAcceleration;
    }

    @Override
    public float getMaxAngularSpeed() {
        return maxAngularSpeed;
    }

    @Override
    public void setMaxAngularSpeed(float maxAngularSpeed) {
        this.maxAngularSpeed = maxAngularSpeed;
    }

    @Override
    public float getMaxAngularAcceleration() {
        return maxAngularAcceleration;
    }

    @Override
    public void setMaxAngularAcceleration(float maxAngularAcceleration) {
        this.maxAngularAcceleration = maxAngularAcceleration;
    }

    @Override
    public Vector2 getPosition() {
        return body.getPosition();
    }

    @Override
    public float getOrientation() {
        return body.getAngle();
    }

    @Override
    public void setOrientation(float orientation) {
        body.setTransform(getPosition(), orientation);
    }
//asdadasd
    @Override
    public float vectorToAngle(Vector2 vector) {
        return Box2dSteeringUtils.vectorToAngle(vector);
    }

    @Override
    public Vector2 angleToVector(Vector2 outVector, float angle) {
        return Box2dSteeringUtils.angleToVector(outVector, angle);
    }

    @Override
    public Location<Vector2> newLocation() {
        return new Box2dLocation();
    }
}
