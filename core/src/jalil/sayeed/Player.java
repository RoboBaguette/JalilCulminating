package jalil.sayeed;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;

import static jalil.sayeed.Utils.Constants.PPM;

public class Player {
    private enum State {FALLING, JUMPING, STANDING, RUNNING, SLIDE, ATTACKING}

    TextureRegion currentFrame;

    boolean jumpLoop = false;
    float attackTime = 0;
    long lastDash = 0;
    long dashCooldown = 3000;
    private final World world;
    private Texture playerSprite;
    private Body body;
    private final int x;
    private final int y;
    private boolean jump;
    private final float frameTime = 1 / 15f;
    private float elapsedTime;
    private State currentState;
    private State previousState;
    private final Animation<TextureRegion> runRight;
    private final Animation<TextureRegion> jumpRight;
    private final Animation<TextureRegion> idleRight;
    private final Animation<TextureRegion> falling;
    private final Animation<TextureRegion> attack;
    private final Animation<TextureRegion> slide;
    private boolean lookRight = true;
    private boolean isAttacking = false;
    private boolean runningRight;
    private float stateTimer;
    Player(int x, int y, World world, Animation<TextureRegion> runRight, Animation<TextureRegion> jumpRight, Animation<TextureRegion> idleRight, Animation<TextureRegion> falling, Animation<TextureRegion> attack, Animation<TextureRegion> slide) {
        this.x = x;
        this.y = y;
        this.world = world;
        playerSprite = new Texture("IndividualSprites/adventurer-idle-00.png");
        jump = false;
        this.runRight = runRight;
        this.jumpRight = jumpRight;
        this.idleRight = idleRight;
        this.falling = falling;
        this.attack = attack;
        this.slide = slide;
        runningRight = true;
        stateTimer = 0;

    }

    public void input(SpriteBatch batch, float delta) {
        int maxFrame = 5;
        boolean playerMove = false;
        int velocity = 0;
        boolean canJump;

        getFrame(delta);
        if (isAttacking) {
            attackTime += delta;
            System.out.println(attackTime);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.A)) {
            velocity -= 1;
            lookRight = false;
            playerSprite = new Texture("RunRightSprites/adventurer-run-00.png");
            playerMove = true;
        }


        if (Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT)) {
            body.applyForce(1000.0f, 0f, body.getPosition().x, body.getPosition().y, true);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.D)) {
            velocity += 1;
            lookRight = true;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE) && !jump) {
            canJump = false;
            body.applyLinearImpulse(new Vector2(0, 15), body.getPosition(), canJump);
            jump = true;
            jumpLoop = false;
        }
        if (body.getLinearVelocity().y == 0) {
            jump = false;
        }
        if (!playerMove) {
            playerSprite = new Texture("IndividualSprites/adventurer-idle-00.png");
        }
        body.setLinearVelocity(velocity * 10, body.getLinearVelocity().y);
        batch.begin();
        batch.draw(currentFrame, body.getPosition().x * PPM - 26, body.getPosition().y * PPM - 16, currentFrame.getRegionWidth(), currentFrame.getRegionHeight());
        batch.end();
    }

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

    public State getState() {

        if (Gdx.input.isKeyJustPressed(Input.Keys.J) || isAttacking) {
            isAttacking = true;
            if (attackTime > 1) {
                isAttacking = false;
                attackTime = 0;
            }
            return State.ATTACKING;
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

    public void getFrame(float dt) {
        currentState = getState();


        switch (currentState) {
            case ATTACKING:
                currentFrame = attack.getKeyFrame(stateTimer, false);
                break;
            case SLIDE:
                currentFrame = slide.getKeyFrame(stateTimer, false);
                System.out.println(currentFrame);
            case JUMPING:
                currentFrame = jumpRight.getKeyFrame(stateTimer, true);
                break;
            case RUNNING:
                currentFrame = runRight.getKeyFrame(stateTimer, true);
                break;
            case FALLING:
                currentFrame = falling.getKeyFrame(stateTimer, false);
                break;
            case STANDING:
            default:
                currentFrame = idleRight.getKeyFrame(stateTimer, true);
        }
        if (!lookRight && !currentFrame.isFlipX()) {
            currentFrame.flip(true, false);
            runningRight = false;
        } else if (lookRight && currentFrame.isFlipX()) {
            currentFrame.flip(true, false);
            runningRight = true;
        }
        //   if((body.getLinearVelocity().x < 0)  && !currentFrame.isFlipX()){
        //   currentFrame.flip(true, false);
        //  runningRight = false;
        //  } else if((body.getLinearVelocity().x > 0 && currentFrame.isFlipX())){
        //    currentFrame.flip(true, false);
        //     runningRight = true;
        //  }
        stateTimer = currentState == previousState ? stateTimer + dt : 0;
        previousState = currentState;

    }

    public void animation(float delta, Animation<TextureRegion> runRight, Animation<TextureRegion> jumpRight, Animation<TextureRegion> idleRight) {
        elapsedTime += delta;
        if (lookRight && !jump) {
            currentFrame = idleRight.getKeyFrame(elapsedTime, true);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.D) && !jump) {
            currentFrame = runRight.getKeyFrame(elapsedTime, true);
        }
        if (jump) {
            currentFrame = jumpRight.getKeyFrame(elapsedTime, true);
            jumpLoop = true;
        }

    }

    public Body getBody() {
        createBody(x, y);
        return body;
    }

    public Texture getPlayerSprite() {
        return playerSprite;
    }

    public void hit(){
        System.out.println("hit");
    }

}
