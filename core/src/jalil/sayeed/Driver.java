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

public class Driver extends ApplicationAdapter {
    private SpriteBatch batch;
    private World world;
    private Box2DDebugRenderer debugRenderer;
    private OrthographicCamera camera;
    private BodyDef bodyDef;
    private BodyDef groundBodyDef;
    private Body groundBody;
    private Body playerBody;
    private boolean isJump;
    private TiledMap map;
    private OrthogonalTiledMapRenderer mapRenderer;
    private Texture background1;
    private Texture background2;
    private Texture background3;
    private Texture playerSprite;
    // Change name
    private TextureAtlas rightRightAtlas;

    private TextureAtlas atlas;
    private TextureAtlas enemyAtlas;
    private Body eyeBody;
    private Eye eye;

    private Animation<TextureRegion> runRight;
    private Animation<TextureRegion> jumpRight;
    private Animation<TextureRegion> idleRight;
    private Animation<TextureRegion> falling;
    private Animation<TextureRegion> attack;
    private Animation<TextureRegion> slide;

    private Animation<TextureRegion> eyeFly;
    private Animation<TextureRegion> eyeAttack;
    private Animation<TextureRegion> eyeHurt;
    private Animation<TextureRegion> eyeDeath;

    private float elapsedTime;
    private Player player;
    private final float frameTime = 1 / 5f;

    @Override
    public void create() {
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

        // Create player

        // Create player animations
        rightRightAtlas = new TextureAtlas(Gdx.files.internal("SpriteSheets/RunRightSheet.atlas"));
        runRight = new Animation(frameTime, rightRightAtlas.findRegions("runRight"));
        runRight.setFrameDuration(frameTime);

        atlas = new TextureAtlas(Gdx.files.internal("SpriteSheets/SpritesRight.atlas"));
        jumpRight = new Animation(frameTime, atlas.findRegions("jumpRight"));
        jumpRight.setFrameDuration(frameTime);

        idleRight = new Animation(frameTime, atlas.findRegions("idle"));
        idleRight.setFrameDuration(frameTime);

        falling = new Animation(frameTime, atlas.findRegions("falling"));
        falling.setFrameDuration(frameTime);

        attack = new Animation(frameTime, atlas.findRegions("attackRight"));
        attack.setFrameDuration(frameTime);

        slide = new Animation(frameTime, atlas.findRegions("slide"));
        slide.setFrameDuration(frameTime);

        enemyAtlas = new TextureAtlas(Gdx.files.internal("SpriteSheets/Enemies.atlas"));

        eyeFly = new Animation(frameTime, enemyAtlas.findRegions("EyeFly"));
        eyeFly.setFrameDuration(frameTime);

        // Setup contact listener
        this.world.setContactListener(new ContactListener());

        // Setup players and enemies
        int x = 10;
        int y = 60;
        eye = new Eye(x, y, world, eyeFly, eyeFly, eyeFly, batch);
        eyeBody = eye.getBody();

        player = new Player(x, y, world, runRight, jumpRight, idleRight, falling, attack, slide);
        playerBody = player.getBody();
        playerSprite = player.getPlayerSprite();

//		textureAtlas = new TextureAtlas(Gdx.files.internal("SpriteSheets/RunRightSheet.atlas"));
        //	textureRegion = textureAtlas.findRegion("adventurer-run-00");


    }

    @Override
    public void render() {
        // Initial Setup
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

        // Render map
        mapRenderer.render();

        // Setup debugRenderer
        debugRenderer.render(world, camera.combined.scl(PPM));
        update(Gdx.graphics.getDeltaTime());
    }

    /**
     * Updates the camera
     */
    public void cameraUpdate() {
        Vector3 position = camera.position;
        position.x = playerBody.getPosition().x * PPM;
        position.y = playerBody.getPosition().y * PPM;
        camera.position.set(position);

        camera.update();

    }

    /**
     * General update method
     * @param delta
     */
    public void update(float delta) {
        world.step(1 / 60f, 6, 2);
        cameraUpdate();

        eye.draw(delta);
        eye.move(delta);
        batch.setProjectionMatrix(camera.combined);
        player.input(batch, delta);
        mapRenderer.setView(camera);
    }

    /**
     * Disposes anything that can be disposed
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
        rightRightAtlas.dispose();
        enemyAtlas.dispose();
        atlas.dispose();
    }
}