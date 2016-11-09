package za.co.retrorabbit.piecommander.fragments;

/**
 * Created by wsche on 2016/11/09.
 */
public class MoveData {
    float moveX, moveY, scaledX, scaledY, angle;

    public float getMoveX() {
        return moveX;
    }

    public void setMoveX(float moveX) {
        this.moveX = moveX;
    }

    public float getMoveY() {
        return moveY;
    }

    public void setMoveY(float moveY) {
        this.moveY = moveY;
    }

    public float getScaledX() {
        return scaledX;
    }

    public void setScaledX(float scaledX) {
        this.scaledX = scaledX;
    }

    public float getScaledY() {
        return scaledY;
    }

    public void setScaledY(float scaledY) {
        this.scaledY = scaledY;
    }

    public float getAngle() {
        return angle;
    }

    public void setAngle(float angle) {
        this.angle = angle;
    }

    @Override
    public String toString() {
        String value =
                "MOVE X : " + moveX + "\n" +
                        "MOVE Y : " + moveY + "\n" +
                        "SCALED X : " + scaledX + "\n" +
                        "SCALED Y : " + scaledY + "\n" +
                        "ANGLE : " + angle + "\n";
        return value;
    }
}
