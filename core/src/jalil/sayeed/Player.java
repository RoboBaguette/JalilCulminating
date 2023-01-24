package jalil.sayeed;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;

import static jalil.sayeed.Utils.Constants.PPM;

public class Player {
    private enum State {FALLING, JUMPING, STANDING, RUNNING, SLIDE, ATTACKING, HURT, DEAD}
    private int health;
    private boolean isHurt;
    TextureRegion currentFrame;
    float attackTime = 0;
    private final World world;
    private Texture playerSprite;
    private Body body;
    private final int x;
    private final int y;
    private boolean isJump;
    private State currentState;
    private State previousState;
    private final Animation<TextureRegion> run;
    private final Animation<TextureRegion> jump;
    private final Animation<TextureRegion> idle;
    private final Animation<TextureRegion> falling;
    private final Animation<TextureRegion> attack;
    private final Animation<TextureRegion> slide;
    private final Animation<TextureRegion> hurt;
    private final Animation<TextureRegion> dead;
    private boolean lookRight = true;
    private boolean isAttacking = false;
    private float stateTimer;


    /**
     * Constructor class for player
     * @param x
     * @param y
     * @param world
     * @param run
     * @param jump
     * @param idle
     * @param falling
     * @param attack
     * @param slide
     * @param hurt
     */
    Player(int x, int y, World world, Animation<TextureRegion> run, Animation<TextureRegion> jump, Animation<TextureRegion> idle, Animation<TextureRegion> falling, Animation<TextureRegion> attack, Animation<TextureRegion> slide, Animation<TextureRegion> hurt, Animation<TextureRegion> dead) {
        this.x = x;
        this.y = y;
        this.world = world;
        playerSprite = new Texture("IndividualSprites/adventurer-idle-00.png");
        isJump = false;
        this.run = run;
        this.jump = jump;
        this.idle = idle;
        this.falling = falling;
        this.attack = attack;
        this.slide = slide;
        this.dead = dead;
        this.hurt = hurt;
        stateTimer = 0;
        health = 10;
        isHurt = false;

    }


    public void createSoundEffects(Sound run, Sound jump, Sound slash){
        run = Gdx.audio.newSound(Gdx.files.internal("Audio/Soundeffects/Run.wav"));


    }
    /**
     * Player input
     * @param batch
     * @param delta
     */
    public void input(SpriteBatch batch, float delta) {
        int velocity = 0;
        boolean canJump;

        getFrame(delta);


        if (isAttacking) {
            attackTime += delta;
        }

        if(health >= 0) {
            // Basic movement and attacks
            if (Gdx.input.isKeyPressed(Input.Keys.A)) {
                velocity -= 1;
                lookRight = false;
                playerSprite = new Texture("RunRightSprites/adventurer-run-00.png");
            }
            if (Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT) && lookRight) {
                body.applyForce(1000.0f, 0f, body.getPosition().x, body.getPosition().y, true);
            } else if (Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT) && !lookRight) {
                body.applyForce(-1000.0f, 0f, body.getPosition().x, body.getPosition().y, true);
            }

            if (Gdx.input.isKeyPressed(Input.Keys.D)) {
                velocity += 1;
                lookRight = true;
            }

            if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE) && !isJump) {
                canJump = false;
                body.applyLinearImpulse(new Vector2(0, 15), body.getPosition(), canJump);
                isJump = true;
            }

            if (body.getLinearVelocity().y < 0.1 && body.getLinearVelocity().y > -0.1) {
                isJump = false;
            }
        }


        // Sets the speed of the body
        if(health > 0) {
            body.setLinearVelocity(velocity * 10, body.getLinearVelocity().y);
        }
        // Draws the player on screen
        batch.begin();
        batch.draw(currentFrame, body.getPosition().x * PPM - 26, body.getPosition().y * PPM - 16, currentFrame.getRegionWidth(), currentFrame.getRegionHeight());
        batch.end();
    }

    /**
     * Create the body for the player
     * @param x
     * @param y
     */
    public void createBody(int x, int y) {
        // Initialize body
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.position.set(x, y);
        bodyDef.fixedRotation = false;

        body = world.createBody(bodyDef);

        PolygonShape shape = new PolygonShape();
        shape.setAsBox(32 / 2 / PPM / 2, 32 / 2 / PPM);

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
     * Get the player's state
     * @return
     */
    public State getState() {
        // Checks what the player is doing and returns the state that represents whatever its doing
        if(health <= 0){
            return State.DEAD;
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.J) || isAttacking) {
            isAttacking = true;
            if (attackTime > 1) {
                isAttacking = false;
                attackTime = 0;
            }
            return State.ATTACKING;
        } else if(isHurt){
            return State.HURT;
        } else if (Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT)) {
            return State.SLIDE;
        } else if (body.getLinearVelocity().y > 0.1) {
            return State.JUMPING;
        } else if (body.getLinearVelocity().y < -0.1) {
            return State.FALLING;
        } else if (body.getLinearVelocity().x != 0) {
            return State.RUNNING;
        } else {
            return State.STANDING;
        }
    }

    /**
     * Get animation frame
     * @param dt
     */
    public void getFrame(float dt) {
        // Get its state
        currentState = getState();

        // Depending on the state, make currentFrame = an animation sprite
        switch (currentState) {
            case DEAD:
                currentFrame = dead.getKeyFrame(stateTimer, false);
                break;
            case ATTACKING:
                currentFrame = attack.getKeyFrame(stateTimer, false);
                break;
            case HURT:
                currentFrame = hurt.getKeyFrame(stateTimer, false);
                break;
            case SLIDE:
                currentFrame = slide.getKeyFrame(stateTimer, true);
                break;
            case JUMPING:
                currentFrame = jump.getKeyFrame(stateTimer, true);
                break;
            case RUNNING:
                currentFrame = run.getKeyFrame(stateTimer, true);
                break;
            case FALLING:
                currentFrame = falling.getKeyFrame(stateTimer, false);
                break;
            case STANDING:
            default:
                currentFrame = idle.getKeyFrame(stateTimer, true);
                break;
        }

        // flips the player depending on where they are facing
        if(health > 0) {
            if (!lookRight && !currentFrame.isFlipX()) {
                currentFrame.flip(true, false);
            } else if (lookRight && currentFrame.isFlipX()) {
                currentFrame.flip(true, false);
            }
        }
        stateTimer = currentState == previousState ? stateTimer + dt : 0;
        previousState = currentState;

    }

    /**
     * Gets the players body
     * @return
     */
    public Body getBody() {
        return body;
    }

    /**
     * Gets a boolean value of whether or not the player is attacking
     * @return
     */
    public boolean isAttacking(){
        return isAttacking;
    }

    /**
     * Gets the player's sprite
     * @return
     */
    public Texture getPlayerSprite() {
        return playerSprite;
    }

    /**
     * Get the time value of the player's attack
     * @return
     */
    public float getAttackTime(){
        return attackTime;
    }

    /**
     * Checks if the player has been hit or not
     * @param isHitting
     */
    public void hit(boolean isHitting){
        if(isHitting && !isHurt) {
            health -= 1;
            isHurt = true;
        }
        else if(!isHitting){
            isHurt = false;
        }
        System.out.println(health);
    }

}
