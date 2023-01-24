package jalil.sayeed;

import com.badlogic.gdx.physics.box2d.Body;
/**
 * Fllename: Enemies.java
 * Author: Jalil, S
 * Date Created: January 22th
 * Description: This class creates a baseline for any enemies
 */
public abstract class Enemies {
    public abstract void update(float delta, boolean isPlayerAttacking, Body playerBody, float attackTIme);
    public abstract void draw(float delta);
    public abstract void getFrame(float dt);
    public abstract void createBody(int x, int y);
    public abstract Body getBody();
    public abstract void hit(boolean isPlayerAttacking, Body playerBody, float attackTime);
    public abstract void attack(Body playerBody, float delta);
    public abstract boolean getIsHitting();
    public abstract boolean getIsAttacking();

}
