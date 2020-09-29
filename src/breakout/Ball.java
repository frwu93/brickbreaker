package breakout;

import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;

public class Ball extends Circle {

  private final int startX;
  private final int startY;
  private final int radius;
  private double xVelocity;
  private double yVelocity;

  public Ball(int xPosition, int yPosition, int radius, Paint color) {
    super(xPosition, yPosition, radius, color);
    this.startX = xPosition;
    this.startY = yPosition;
    this.radius = radius;
    this.xVelocity = 0;
    this.yVelocity = 0;
    setId("ball");
  }

  public void reset() {
    setCenterX(startX);
    setCenterY(startY);
    xVelocity = 0;
    yVelocity = 0;
  }

  public void setXVelocity(double value) {
    xVelocity = value;
  }

  public void setYVelocity(double value) {
    yVelocity = value;
  }

  public void updatePosition(double elapsedTime) {
    setCenterX(getCenterX() + xVelocity * elapsedTime);
    setCenterY(getCenterY() + yVelocity * elapsedTime);
  }

  public void updateVelocityUponCollision(Rectangle gamePiece) {
    if (intersectBottom(gamePiece) || intersectTop(gamePiece)) {
      reverseYVelocity();
    } else if (intersectLeft(gamePiece) || intersectRight(gamePiece)) {
      reverseXVelocity();
    }
  }

  public void updateXVelocityUponBorderCollision() {
    reverseXVelocity();
  }

  public void updateYVelocityUponBorderCollision() {
    reverseYVelocity();
  }

  public double getXVelocity() {
    return xVelocity;
  }

  public double getYVelocity() {
    return yVelocity;
  }

  public boolean notInMotion() {
    return xVelocity == 0 || yVelocity == 0;
  }

  public double getLeft() {
    return getCenterX() - radius;
  }

  public double getRight() {
    return getCenterX() + radius;
  }

  public double getTop() {
    return getCenterY() - radius;
  }

  public double getBottom() {
    return getCenterY() + radius;
  }

  public double getStartX() {
    return startX;
  }

  public double getStartY() {
    return startY;
  }

  private void reverseXVelocity() {
    setXVelocity(-1 * xVelocity);
  }

  private void reverseYVelocity() {
    setYVelocity(-1 * yVelocity);
  }

  private boolean intersectBottom(Rectangle b) {
    return getBottom() >= b.getY() + b.getHeight();
  }

  private boolean intersectTop(Rectangle b) {
    return getTop() <= b.getY();
  }

  private boolean intersectLeft(Rectangle b) {
    return getLeft() <= b.getX();
  }

  private boolean intersectRight(Rectangle b) {
    return getRight() >= b.getX() + b.getWidth();
  }
}
