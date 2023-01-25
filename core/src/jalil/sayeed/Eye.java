package jalil.sayeed;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.Array;

import static jalil.sayeed.Utils.Constants.PPM;
/**
 * Fllename: Eye.java
 * Author: Jalil, S
 * Date Created: January 9th
 * Description: This class is responsible for all things regarding the Eye enemy
 */
public class Eye extends Enemies {
    private int health;
    private boolean canBeHit;
    private boolean isAttacked;
    private boolean dead;
    private enum State {DEAD, FLYING, ATTACKING, HURT}
    private boolean lookRight;
    public Animation<TextureRegion> fly;
    TextureRegion currentFrame;
    private World world;
    private State currentState;
    private State previousState;
    private Animation<TextureRegion> move;
    private Animation<TextureRegion> attack;
    private Animation<TextureRegion> hurt;
    private Animation<TextureRegion> death;
    private float stateTimer;
    private boolean runningRight;
    private boolean isAttacking;
    private Body body;
    private int x;
    private int y;
    private SpriteBatch batch;
    private float timer;
    private float timer2;
    public boolean onFloor;
    private float attackTimer;
    private boolean isHitting;
    private boolean runAway;
    private float runAwayTimer;
    private FixtureDef fixtureDef;

    /**
     * Constructor for eye class
     * @param x
     * @param y
     * @param world
     * @param death
     * @param fly
     * @param hurt
     * @param attack
     * @param batch
     */
    Eye(int x, int y, World world, Animation<TextureRegion> death, Animation<TextureRegion> fly, Animation<TextureRegion> hurt, Animation<TextureRegion> attack, SpriteBatch batch) {
        this.x = x;
        this.y = y;
        this.world = world;
        this.death = death;
        this.hurt = hurt;
        this.attack = attack;
        this.batch = batch;
        this.fly = fly;
        isHitting = false;
        dead = false;
        isAttacked = false;
        canBeHit = true;
        health = 4;
        isAttacking = false;
        stateTimer = 0;
        timer = 0;
        lookRight = true;
        timer2 = 0;
        attackTimer = 0;
        onFloor = false;
        runAway = false;
        runAwayTimer = 0;
    }

    /**
     * Updates the eye
     * @param delta
     * @param isPlayerAttacking
     * @param playerBody
     * @param attackTime
     */
    @Override
    public void update(float delta, boolean isPlayerAttacking, Body playerBody, float attackTime) {
        timer += delta;

        int velocityX = 0;
        int velocityY = 0;

        // Has the enemy fly away from the player
        if(runAway && playerBody.getPosition().x > body.getPosition().x){
            velocityX -= 2;
            lookRight = false;
            velocityY += 2;
            runAwayTimer += delta;
        } else if(runAway && playerBody.getPosition().x < body.getPosition().x){
            velocityX += 2;
            velocityY += 2;
            lookRight = true;
            runAwayTimer += delta;
        }
        // Stops the enemy from running away after timer hits 2
        if(runAwayTimer > 1.8){
            runAway = false;
            runAwayTimer = 0;
        }

        // Runs basic movement for the eye that follows the player if it isn't dead
        if(!dead && !runAway) {
            if (playerBody.getPosition().x < x + 30 && playerBody.getPosition().x > x -30 && playerBody.getPosition().y < y + 14 && playerBody.getPosition().y > y - 14) {
                if (playerBody.getPosition().x > body.getPosition().x) {
                    velocityX += 1.2f;
                    lookRight = true;
                }
                if (playerBody.getPosition().x < body.getPosition().x) {
                    velocityX -= 1.2f;
                    lookRight = false;
                }
                if (playerBody.getPosition().y < body.getPosition().y) {
                    velocityY -= 1.2f;
                }
                if (playerBody.getPosition().y > body.getPosition().y) {
                    velocityY += 1.2f;
                }
            }
        }

        // Stops the Eye if its attacked and moves the eye if isAttacked is false
        if(isAttacked || dead){
            body.setLinearVelocity(0, 0);
        } else {
            body.setLinearVelocity(velocityX * 2, velocityY * 2);
        }
        draw(delta);
        hurt(isPlayerAttacking, playerBody, attackTime);
        attack(playerBody);
    }

    /**
     * Draw the Eye's current frame
     * @param delta
     */
    @Override
    public void draw(float delta) {
        getFrame(delta);
        batch.begin();
        batch.draw(currentFrame, body.getPosition().x * PPM - 80, body.getPosition().y * PPM - 70, currentFrame.getRegionWidth(), currentFrame.getRegionHeight());
        batch.end();
    }

    /**
     * Gets the currentFrame of the Eye depending on it's curret state
     * @param dt
     */
    @Override
    public void getFrame(float dt) {
        currentState = getState();

        // Depending on the currentState of the player, currentFrame will equal the frame that corresponds to that state
        switch (currentState) {
            case DEAD:
                currentFrame = death.getKeyFrame(stateTimer, false);
                break;
            case HURT:
                currentFrame = hurt.getKeyFrame(stateTimer, false);
                break;
            case ATTACKING:
                attackTimer += dt;
                currentFrame = attack.getKeyFrame(stateTimer, true);
                break;
            case FLYING:
            default:
                currentFrame = fly.getKeyFrame(stateTimer, true);
        }

        // Flips the Eye depending on which side its facing
        if (!lookRight && !currentFrame.isFlipX()) {
            currentFrame.flip(true, false);
        } else if (lookRight && currentFrame.isFlipX()) {
            currentFrame.flip(true, false);
        }
        stateTimer = currentState == previousState ? stateTimer + dt : 0;
        previousState = currentState;
    }

    /**
     * Creates the Eye's body
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

        // Create body
        body = world.createBody(bodyDef);

        PolygonShape shape = new PolygonShape();
        shape.setAsBox(32 / 2.2f / PPM, 32 / 2 / PPM / 1.2f);

        // Create fixture
        fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.density = 0;
        fixtureDef.friction = 0.8f;
        fixtureDef.restitution = 0;
        fixtureDef.filter.categoryBits = (short) 2;
        fixtureDef.filter.maskBits = (short) 2;


        body.createFixture(fixtureDef).setUserData(this);
        shape.dispose();
    }

    /**
     * Gets the Eye's body
     * @return
     */
    @Override
    public Body getBody() {
        createBody(x, y);
        return body;
    }

    /**
     * Gets the Eyes current state based on what it is doing
     * @return
     */
    public State getState() {
        if (dead) {
            if(body.getFixtureList().size > 0) {
                body.destroyFixture(body.getFixtureList().get(0));
            }
            return State.DEAD;
        } else if (isAttacked) {
            return State.HURT;
        } else if (isAttacking) {
            return State.ATTACKING;
        } else {
            return State.FLYING;
        }
    }

    /**
     * Checks if the Eye has been hit
     * @param isPlayerAttacking
     * @param playerBody
     * @param attackTime
     */
    @Override
    public void hurt(boolean isPlayerAttacking, Body playerBody, float attackTime){
        // Creates an area where the player can hit the Eye if the player is attacking and detects if it is attacked or not
        if(playerBody.getPosition().x - body.getPosition().x < 2 && playerBody.getPosition().x - body.getPosition().x > -1   && playerBody.getPosition().y < body.getPosition().y + 1.4 && playerBody.getPosition().y > body.getPosition().y - 1 && isPlayerAttacking && attackTime > 0.2 && canBeHit){
            isAttacked = true;
            health -= 1;
            runAway = true;

        } else if(body.getPosition().x - playerBody.getPosition().x < 2 && body.getPosition().x - playerBody.getPosition().x > -1 && playerBody.getPosition().y < body.getPosition().y + 1.4 && playerBody.getPosition().y > body.getPosition().y - 1 && isPlayerAttacking && attackTime > 0.2 && canBeHit){
            isAttacked = true;
            health -= 1;
            runAway = true;
        }
        // Resets variables
        if(attackTime > 0 && isAttacked){
            canBeHit = false;
        } else{
            canBeHit = true;
            isAttacked = false;
        }
        if(health <= 0){
            dead = true;
        }
    }

    /**
     * Attack for the eye
     * @param playerBody
     */
    @Override
    public void attack(Body playerBody){
        // Reset variables
        if(!(attackTimer > 0)){
            isAttacking = false;
            isHitting = false;
        }

        // Checks if the player is within a certain radius and attacks the player
        if(playerBody.getPosition().x - body.getPosition().x < 3 && playerBody.getPosition().x - body.getPosition().x > -1 && playerBody.getPosition().y < body.getPosition().y + 1.4 && playerBody.getPosition().y > body.getPosition().y - 1){
            isAttacking = true;
            if(attackTimer > 1.5 && playerBody.getPosition().x - body.getPosition().x < 3 && playerBody.getPosition().x - body.getPosition().x > -1){
                isHitting = true;
            }
        } else if(body.getPosition().x - playerBody.getPosition().x < 3 && body.getPosition().x - playerBody.getPosition().x > -1 && playerBody.getPosition().y < body.getPosition().y + 1.4 && playerBody.getPosition().y > body.getPosition().y - 1){
            isAttacking = true;
            if(attackTimer > 1.5 && body.getPosition().x - playerBody.getPosition().x < 3 && body.getPosition().x - playerBody.getPosition().x > -1){
                isHitting = true;
            }
        }

        // Resets variables if dead
        if(dead){
            isAttacking = false;
            isHitting = false;
        }

        // Times the attack
        if(attackTimer > 2.5){
            attackTimer = 0;
            isAttacking = false;
            isHitting = false;
            runAway = true;
        }

    }

    /**
     * Gets isHitting
     * @return
     */
    @Override
    public boolean getIsHitting(){
        return isHitting;
    }

    /**
     * Get isAttacking
     * @return
     */
    @Override
    public boolean getIsAttacking(){
        return isAttacking;
    }

}
