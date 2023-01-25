package jalil.sayeed;

import com.badlogic.gdx.physics.box2d.Body;
/**
 * Fllename: Enemies.java
 * Author: Jalil, S
 * Date Created: January 22th
 * Description: This class creates a baseline for any enemies
 * The getFrame method is from this video https://www.youtube.com/watch?v=1fJrhgc0RRw&list=PLZm85UZQLd2SXQzsF-a0-pPF6IWDDdrXt&index=11
 */
public abstract class Enemies {
    public abstract void update(float delta, boolean isPlayerAttacking, Body playerBody, float attackTIme);
    public abstract void draw(float delta);
    public abstract void getFrame(float dt);
    public abstract void createBody(int x, int y);
    public abstract Body getBody();
    public abstract void hurt(boolean isPlayerAttacking, Body playerBody, float attackTime);
    public abstract void attack(Body playerBody);
    public abstract boolean getIsHitting();
    public abstract boolean getIsAttacking();

}
