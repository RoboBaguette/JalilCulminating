package jalil.sayeed;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.physics.box2d.*;

import static jalil.sayeed.Utils.Constants.PPM;

/**
 * Fllename: Skeleton.java
 * Author: Jalil, S
 * Date Created: January 16th
 * Description: This class is responsible for all things regarding the Skeleton enemy
 */
public class Skeleton extends Enemies{
    private int health;
    private boolean canBeHit;
    private boolean isAttacked;
    private boolean dead;
    private enum State {DEAD, IDLE, WALKING, ATTACKING, HURT}
    private State currentState;
    private State previousState;
    private boolean lookRight;
    private Animation<TextureRegion> run;
    private Animation<TextureRegion> death;
    private Animation<TextureRegion> standing;
    private Animation<TextureRegion> walk;
    private Animation<TextureRegion> hurt;
    private Animation<TextureRegion> attack;
    private Animation<TextureRegion> idle;


    private float stateTimer;
    TextureRegion currentFrame;
    private World world;
    private boolean isAttacking;
    private Body body;
    private int x;
    private int y;
    private SpriteBatch batch;
    private float timer;
    private float attackTimer;
    private boolean runningRight;
    private boolean isHitting;
    private boolean currentlyAttacking;

    /**
     * Constructor for Skeleton Class
     * @param x
     * @param y
     * @param world
     * @param attack
     * @param death
     * @param hurt
     * @param walk
     * @param batch
     */
    Skeleton(int x, int y, World world, Animation<TextureRegion> attack, Animation<TextureRegion> death, Animation<TextureRegion> hurt, Animation<TextureRegion> walk, Animation<TextureRegion> idle, SpriteBatch batch){
        this.x = x;
        this.y = y;
        this.world = world;
        this.death = death;
        this.hurt = hurt;
        this.attack = attack;
        this.walk = walk;
        this.idle = idle;
        this.batch = batch;
        dead = false;
        isAttacked = false;
        canBeHit = false;
        health = 4;
        isAttacking = false;
        canBeHit = true;
        health = 6;
        stateTimer = 0;
        timer = 0;
        lookRight = true;
        timer = 0;
        isHitting = false;
        currentlyAttacking = false;
    }

    /**
     * Updates the Skeleton
     * @param delta
     * @param isPlayerAttacking
     * @param playerBody
     * @param attackTime
     */
    @Override
    public void update(float delta, boolean isPlayerAttacking, Body playerBody, float attackTime) {
        timer += delta;

        int velocity = 0;;

        // Basic movement for the Skeleton if it isn't dead
        if(!dead) {
            if (playerBody.getPosition().x > body.getPosition().x && playerBody.getPosition().x < x + 20 && playerBody.getPosition().x > x - 20  && playerBody.getPosition().y < body.getPosition().y + 8 && playerBody.getPosition().y > body.getPosition().y - 8) {
                velocity += 1;
                lookRight = true;
            } else if (playerBody.getPosition().x < body.getPosition().x && playerBody.getPosition().x < x + 20 && playerBody.getPosition().x > x - 20  && playerBody.getPosition().y < body.getPosition().y + 8 && playerBody.getPosition().y > body.getPosition().y - 8) {
                velocity -= 1;
                lookRight = false;
            }
        }

        // If the Skeleton isAttacked or if the skeleton isAttacking stop the skeleton from moving
        if(isAttacked){
            body.setLinearVelocity(0, 0);
        } else if(isAttacking){
            body.setLinearVelocity(0, 0);
        }
        else{
            body.setLinearVelocity(velocity * 2, body.getLinearVelocity().y);
        }

        draw(delta);
        hit(isPlayerAttacking, playerBody, attackTime);
        attack(playerBody, delta);
    }

    /**
     * Draws the Skeleton on screen
     * @param delta
     */
    @Override
    public void draw(float delta){
        getFrame(delta);
        batch.begin();
        batch.draw(currentFrame, body.getPosition().x * PPM - 80, body.getPosition().y * PPM -70, currentFrame.getRegionWidth(), currentFrame.getRegionHeight());
        batch.end();
    }

    /**
     * Gets the currentFrame of the Skeleton depending on what state its in
     * @param dt
     */
    @Override
    public void getFrame(float dt){
        currentState = getState();
        switch(currentState){
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
            case WALKING:
                currentFrame = walk.getKeyFrame(stateTimer, true);
                break;
            case IDLE:
            default:
                currentFrame = idle.getKeyFrame(stateTimer, true);

        }

        // Depending on if the Skeleton is looking right or not, flip the Skeleton
        if (!lookRight && !currentFrame.isFlipX()) {
            currentFrame.flip(true, false);
        } else if (lookRight && currentFrame.isFlipX()) {
            currentFrame.flip(true, false);
        }
        stateTimer = currentState == previousState ? stateTimer + dt : 0;
        previousState = currentState;
    }

    /**
     * Create the Skeleton's body
     * @param x
     * @param y
     */
    @Override
    public void createBody(int x, int y){
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.position.set(x, y);
        bodyDef.fixedRotation = false;

        body = world.createBody(bodyDef);

        PolygonShape shape = new PolygonShape();
        shape.setAsBox(32 / 2 / PPM, 32 / 1.4f /PPM);

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.density = 0;
        fixtureDef.friction = 0.8f;
        fixtureDef.restitution = 0;
        fixtureDef.filter.categoryBits = (short) 1;
        fixtureDef.filter.maskBits = (short) 1;

        body.createFixture(fixtureDef).setUserData(this);

        shape.dispose();
    }

    /**
     * Get the Skeleton's body
     * @return
     */
    @Override
    public Body getBody() {
        createBody(x, y);
        return body;
    }

    /**
     * Gets the currentState of the Skeleton
     * @return
     */

    public State getState(){
        // Depending on what the Skeleton is currently doing, set the state to the corresponding action
        if(dead){
            return State.DEAD;
        }else if(isAttacked){
            return State.HURT;
        } else if (isAttacking) {
            return State.ATTACKING;
        } else if(body.getLinearVelocity().x == 0){
            return State.IDLE;
        }
        else{
            return State.WALKING;
        }
    }

    /**
     * Checks if the Skeleton has been hit
     * @param isPlayerAttacking
     * @param playerBody
     * @param attackTime
     */
    @Override
    public void hit(boolean isPlayerAttacking, Body playerBody, float attackTime){
        if(playerBody.getPosition().x - body.getPosition().x < 2 && playerBody.getPosition().x - body.getPosition().x > -1  && playerBody.getPosition().y < body.getPosition().y + 1.4 && playerBody.getPosition().y > body.getPosition().y - 2  && isPlayerAttacking && attackTime > 0.2 && canBeHit ){
            isAttacked = true;
            health -= 1;

        } else if(body.getPosition().x - playerBody.getPosition().x < 2 && body.getPosition().x - playerBody.getPosition().x > -1 && isPlayerAttacking  && playerBody.getPosition().y < body.getPosition().y + 1.4 && playerBody.getPosition().y > body.getPosition().y - 2 && attackTime > 0.2 && canBeHit){
            isAttacked = true;
            health -= 1;
        }
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
     * Checks if the Skeleton is attacking
     * @param playerBody
     * @param delta
     */
    @Override
    public void attack(Body playerBody, float delta){
        if(!(attackTimer > 0)){
            isAttacking = false;
            isHitting = false;
        }

        if(playerBody.getPosition().x - body.getPosition().x < 4 && playerBody.getPosition().x - body.getPosition().x > -1 && playerBody.getPosition().y < body.getPosition().y + 1.4 && playerBody.getPosition().y > body.getPosition().y - 1){

            isAttacking = true;
            if(attackTimer > 1.5 && playerBody.getPosition().x - body.getPosition().x < 4 && playerBody.getPosition().x - body.getPosition().x > -1){
                isHitting = true;
            }
        } else if(body.getPosition().x - playerBody.getPosition().x < 4 && body.getPosition().x - playerBody.getPosition().x > -1 && playerBody.getPosition().y < body.getPosition().y + 1.4 && playerBody.getPosition().y > body.getPosition().y - 1){
            isAttacking = true;
            if(attackTimer > 1.5 && body.getPosition().x - playerBody.getPosition().x < 4 && body.getPosition().x - playerBody.getPosition().x > -1){
                isHitting = true;
            }
        }
        if(dead){
            isAttacking = false;
            isHitting = false;
        }

        if(attackTimer > 2.5){
            attackTimer = 0;
            isAttacking = false;
            isHitting = false;
        }
    }

    /**
     * Get a boolean value of whether the Skeleton is hitting or not
     * @return
     */
    @Override
    public boolean getIsHitting(){
        return isHitting;
    }

    /**
     * Gets if the Skeleton isAttacking
     * @return
     */
    @Override
    public boolean getIsAttacking(){
        return isAttacking;
    }

}
