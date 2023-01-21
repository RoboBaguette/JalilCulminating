package jalil.sayeed;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.*;
import jalil.sayeed.Utils.TiledUtil;

import static jalil.sayeed.Utils.Constants.PPM;

public class Main extends ApplicationAdapter {
    private SpriteBatch batch;
    private World world;
    private Box2DDebugRenderer debugRenderer;
    private OrthographicCamera camera;
    private Body playerBody;
    private TiledMap map;
    private OrthogonalTiledMapRenderer mapRenderer;
    private Texture background1;
    private Texture background2;
    private Texture background3;
    private Texture playerSprite;
    private TextureAtlas atlas;
    private TextureAtlas enemyAtlas;
    private Body eyeBody1;
    private Eye eye1;
    private Animation<TextureRegion> playerRun;
    private Animation<TextureRegion> playerJump;
    private Animation<TextureRegion> playerIdle;
    private Animation<TextureRegion> playerFalling;
    private Animation<TextureRegion> playerAttack;
    private Animation<TextureRegion> playerSlide;
    private Animation<TextureRegion> playerHurt;
    private Animation<TextureRegion> playerDead;

    private Animation<TextureRegion> eyeFly;
    private Animation<TextureRegion> eyeAttack;
    private Animation<TextureRegion> eyeHurt;
    private Animation<TextureRegion> eyeDeath;
    private Body skeletonBody1;
    private Skeleton skeleton1;
    private Animation<TextureRegion> skeletonAttack;
    private Animation<TextureRegion> skeletonWalk;
    private Animation<TextureRegion> skeletonDeath;
    private Animation<TextureRegion> skeletonHurt;
    private Animation<TextureRegion> skeletonIdle;

    private Player player;
    private final float frameTime = 1 / 5f;


    /**
     * Create everything in the game
     */
    @Override
    public void create() {
        // Initialize Box2D
        Box2D.init();

        float height = Gdx.graphics.getHeight();
        float width = Gdx.graphics.getWidth();

        // Setup camera
        camera = new OrthographicCamera();
        camera.setToOrtho(false, width / 2, height / 2);


        // Initial Setup
        world = new World(new Vector2(0, -19.6f), false);
        debugRenderer = new Box2DDebugRenderer();

        batch = new SpriteBatch();

        // Setup map
        map = new TmxMapLoader().load("MainMap.tmx");
        TiledUtil.parseTiledObjectLayer(world, map.getLayers().get("CollisionLayer").getObjects());

        mapRenderer = new OrthogonalTiledMapRenderer(map);


        // Setup background
        background1 = new Texture(Gdx.files.internal("Tile Sets/FPSET1_v2.0/SET1/_PNG/SET1_bakcground_day1.png"));
        background2 = new Texture(Gdx.files.internal("Tile Sets/FPSET1_v2.0/SET1/_PNG/SET1_bakcground_day2.png"));
        background3 = new Texture(Gdx.files.internal("Tile Sets/FPSET1_v2.0/SET1/_PNG/SET1_bakcground_day3.png"));


        // Create player animations
        atlas = new TextureAtlas(Gdx.files.internal("SpriteSheets/PlayerSprites.atlas"));

        playerDead = new Animation(frameTime, atlas.findRegions("dead"));
        playerDead.setFrameDuration(frameTime);

        playerRun = new Animation(frameTime, atlas.findRegions("run"));
        playerRun.setFrameDuration(frameTime);

        playerJump = new Animation(frameTime, atlas.findRegions("jump"));
        playerJump.setFrameDuration(frameTime);

        playerIdle = new Animation(frameTime, atlas.findRegions("idle"));
        playerIdle.setFrameDuration(frameTime);

        playerFalling = new Animation(frameTime, atlas.findRegions("falling"));
        playerFalling.setFrameDuration(frameTime);

        playerAttack = new Animation(frameTime, atlas.findRegions("attack"));
        playerAttack.setFrameDuration(frameTime);

        playerSlide = new Animation(frameTime, atlas.findRegions("slide"));
        playerSlide.setFrameDuration(frameTime);

        playerHurt = new Animation(frameTime, atlas.findRegions("hurt"));
        playerHurt.setFrameDuration(frameTime);

        // Create Enemy Animations
        enemyAtlas = new TextureAtlas(Gdx.files.internal("SpriteSheets/Enemies.atlas"));

        // Fly animations
        eyeFly = new Animation(frameTime, enemyAtlas.findRegions("EyeFly"));
        eyeFly.setFrameDuration(frameTime);

        eyeHurt = new Animation(frameTime, enemyAtlas.findRegions("EyeHurt"));
        eyeHurt.setFrameDuration(frameTime);

        eyeDeath = new Animation(frameTime, enemyAtlas.findRegions("EyeDeath"));
        eyeDeath.setFrameDuration(frameTime);

        // Skeleton Animations
        skeletonWalk = new Animation(frameTime, enemyAtlas.findRegions("SkeletonWalk"));
        skeletonWalk.setFrameDuration(frameTime);

        skeletonHurt = new Animation(frameTime, enemyAtlas.findRegions("SkeletonHurt"));
        skeletonHurt.setFrameDuration(frameTime);

        skeletonAttack = new Animation(frameTime, enemyAtlas.findRegions("SkeletonAttack"));
        skeletonAttack.setFrameDuration(frameTime);

        skeletonDeath = new Animation(frameTime, enemyAtlas.findRegions("SkeletonDeath"));
        skeletonDeath.setFrameDuration(frameTime);

        skeletonIdle = new Animation(frameTime, enemyAtlas.findRegions("SkeletonIdle"));
        skeletonIdle.setFrameDuration(frameTime);

        // Setup contact listener
        this.world.setContactListener(new ContactListener());

        // Setup players and enemies
        int x = 10;
        int y = 60;
        eye1 = new Eye(x, y, world, eyeDeath, eyeFly, eyeHurt, eyeFly, batch);
        eyeBody1 = eye1.getBody();

        player = new Player(x, y, world, playerRun, playerJump, playerIdle, playerFalling, playerAttack, playerSlide, playerHurt, playerDead);
        player.createBody(x, y);

        playerBody = player.getBody();
        playerSprite = player.getPlayerSprite();

        x = 40;
        y = 62;
        skeleton1 = new Skeleton(x, y, world, skeletonAttack, skeletonDeath, skeletonHurt, skeletonWalk, skeletonIdle, batch);
        skeletonBody1 = skeleton1.getBody();
    }

    /**
     * Render everything in the game
     */
    @Override
    public void render() {
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        float x = playerBody.getPosition().x * PPM - (playerSprite.getWidth() * 5 + 20);
        float y = playerBody.getPosition().y * PPM - (playerSprite.getHeight() * 5);
        // Draw background
        batch.begin();
        batch.draw(background1, x, y);
        batch.draw(background2, x, y);
        batch.draw(background3, x, y);
        batch.end();

        // Render tile map
        mapRenderer.render();
       // debugRenderer.render(world, camera.combined.scl(PPM));

        update(Gdx.graphics.getDeltaTime());
    }

    /**
     * Update camera according to player position
     */
    public void cameraUpdate() {
        Vector3 position = camera.position;
        position.x = playerBody.getPosition().x * PPM;
        position.y = playerBody.getPosition().y * PPM;
        camera.position.set(position);

        camera.update();

    }

    /**
     * Update Everything in the game
     * @param delta
     */
    public void update(float delta) {
        world.step(1 / 60f, 6, 2);
        cameraUpdate();

        skeleton1.update(delta,player.isAttacking(), playerBody, player.getAttackTime());
        eye1.update(delta,player.isAttacking(), playerBody, player.getAttackTime());
        batch.setProjectionMatrix(camera.combined);
        player.input(batch, delta);
        player.hit(skeleton1.isHitting());
        mapRenderer.setView(camera);
    }

    /**
     * Dispose
     */
    @Override
    public void dispose() {
        world.dispose();
        debugRenderer.dispose();
        batch.dispose();
        map.dispose();
        mapRenderer.dispose();
        playerSprite.dispose();
        background1.dispose();
        background2.dispose();
        background3.dispose();
        enemyAtlas.dispose();
        atlas.dispose();
    }
}