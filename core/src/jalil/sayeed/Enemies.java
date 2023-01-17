package jalil.sayeed;

import com.badlogic.gdx.physics.box2d.Body;

public abstract class Enemies {
    public abstract void getFrame(float dt);


    public abstract void createBody(int x, int y);

    public abstract Body getBody();

}

