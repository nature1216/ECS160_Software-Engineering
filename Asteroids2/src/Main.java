import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.scene.text.Font;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Translate;
import javafx.stage.Stage;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class Main extends Application {

    Point2D size = new Point2D(1000, 800); // size of window
    Set<KeyCode> keysDown = new HashSet<>();

    int key(KeyCode k) {
        return keysDown.contains(k) ? 1 : 0;
    }

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        Group gRoot = new Group();
        Scene scene = new Scene(gRoot, size.getX(), size.getY());

        stage.setScene(scene); // display the window
        stage.setTitle("Asteroids");
        scene.setFill(Color.BLACK); // background color of the scene

        Label fpsLabel = new Label();
        fpsLabel.setTranslateX(2); // 2픽셀 x축쪽으로 움직임
        fpsLabel.setTextFill(Color.WHITE);

        Group gGame = new Group();
        Group gAsteroids = new Group();
        Group gBullets = new Group();
        gGame.getChildren().addAll(gAsteroids, gBullets);

        Ship ship = new Ship(gGame, size.multiply(0.5));

        Label loseLabel = new Label();
        loseLabel.setTranslateX(size.getX() / 2 - 200);
        loseLabel.setTranslateY(size.getY() / 2 - 100);
        loseLabel.setTextFill(Color.WHITE);
        loseLabel.setFont(Font.font(30));
        loseLabel.setVisible(false);

        List<Asteroid> asteroids = new LinkedList<>();
        List<Bullet> bullets = new LinkedList<>();

        gRoot.getChildren().addAll(gGame, loseLabel, fpsLabel); // getChildren: list

        /* SETUP stuff in the game */
        scene.setOnKeyPressed(new EventHandler<KeyEvent>() {
            public void handle(KeyEvent event) {
                keysDown.add(event.getCode());
            }
        });

        scene.setOnKeyReleased(new EventHandler<KeyEvent>() {
            public void handle(KeyEvent event) {
                keysDown.remove(event.getCode());
            }
        });

        Rectangle bounds = new Rectangle(0, 0, size.getX(), size.getY());
        bounds.setVisible(false);
        gRoot.getChildren().add(bounds);

        AnimationTimer loop = new AnimationTimer() {
            double old = -1;
            double elapsedTime = 0; // 프레임간 시간이 얼마나 지났는지 알기 위함

            double bulletWait = 0.3;
            double bulletTimer = 0;

            int score = 0;
            boolean died = false;
            int astroidCount = 15;// 초기 소행성 개수, 프레임 안에 남은 소행성 개수
            double asteroidWait = 3;
            double asteroidTimer = 0;


            public void handle(long nano) { // invoked at the start of every frame
                if (old < 0) old = nano;
                double delta = (nano - old) / 1e9; // 1초에 60프레임 1e9로 나누면 1초 됨, 지난 프레임보다 몇 초 지났는지

                old = nano;
                elapsedTime += delta;

                fpsLabel.setText(String.format("%.2f  %.2f", 1 / delta, elapsedTime));

                /* GAME LOOP */

                /* 첫 번째 프레임이 15개의 소행성을 만들고 그것들을 파괴하면 다시 채움 */
                for (Asteroid aster : asteroids) {
                    for (Bullet bullet : bullets) {
                        if (aster.intersects(bullet)) {
                            aster.destroy(gAsteroids);
                            bullet.destroy(gBullets);
                            score++;
                            break;
                        }
                    }

                    if (!aster.alive) continue;
                    if (aster.intersects(ship)) {
                        aster.destroy(gAsteroids);
                        loseLabel.setText(String.format(
                                "You blew up!\nYou survived %.2f seconds\nand destroyed %d asteroids.",
                                elapsedTime, score
                        ));
                        loseLabel.setVisible(true);
                        died = true;
                    }
                    if (aster.leavingBounds(bounds)) aster.destroy(gAsteroids);
                    aster.update(delta);
                }

                for (Bullet b : bullets) {
                    if (b.leavingBounds(bounds)) b.destroy(gBullets);
                    b.update(delta);
                }

                asteroids.removeIf(a -> !a.alive);
                bullets.removeIf(b -> !b.alive);

                while (asteroids.size() < astroidCount) {
                    asteroids.add(Asteroid.make(gAsteroids, size));
                }

                if (!died) {
                    asteroidTimer += delta;
                    if(asteroidTimer > asteroidWait) {
                        astroidCount++;
                        asteroidTimer = 0; // every 3 seconds we'll get another asteroid and this while loop will handle
                    }

                    if (key(KeyCode.SPACE) == 1 && bulletTimer <= 0) {
                        Bullet b = new Bullet(gBullets, ship.p, ship.v, ship.theta);
                        bullets.add(b);
                        bulletTimer = bulletWait;
                    }

                    bulletTimer -= delta;

                    if(ship.leavingBounds(bounds)) {
                        ship.destroy(gGame);
                        loseLabel.setText(String.format(
                                "You flew away!\nYou survived %.2f seconds\nand destroyed %d asteroids.",
                                elapsedTime, score
                        ));
                        loseLabel.setVisible(true);
                        died = true;
                    } else {
                        double rot = 4 * (key(KeyCode.D) - key(KeyCode.A));
                        ship.update(delta, rot, key(KeyCode.W));
                    }


                }
            }
        };

        loop.start();

        stage.show();
    }
}

abstract class PhysicsObject {
    Point2D p, v; // position, velocity(pixels per second)
    double theta, omega; // theta: angle that the object is, omega: angular velocity of the object
    Group root, transform;
    boolean alive;

    final static double TAU = Math.PI * 2;

    public PhysicsObject(Group parent, Point2D p0, Point2D v0, double theta0, double omega0) {
        root = new Group();
        transform = new Group();
        root.getChildren().add(transform);
        parent.getChildren().add(root);

        p = p0;
        v = v0;
        theta = theta0;
        omega = omega0;
        alive = true;
    }

    public void update(double delta) {
        p = p.add(v.multiply(delta)); // pixels per frame
        theta = (theta + omega * delta) % TAU; // degree or radians per second

        transform.getTransforms().clear();
        transform.getTransforms().addAll(
                new Translate(p.getX(), p.getY()), //two-dimensional translation
                new Rotate(Math.toDegrees(theta))
        );
    }

    public void destroy(Group parent) {
        parent.getChildren().remove(root);
        alive = false;
    }

    abstract Shape getShapeBounds();

    public boolean intersects(PhysicsObject po) {
        return alive && po.alive &&
                !Shape.intersect(getShapeBounds(), po.getShapeBounds())
                        .getBoundsInLocal().isEmpty();
    }

    public boolean leavingBounds(Rectangle bounds) { // tell us if something is out of bounds and is moving away
        if(!Shape.intersect(getShapeBounds(), bounds).getBoundsInLocal().isEmpty()) {
            return false;
        }

        double x = p.getX();
        double y = p.getY();
        double bx = bounds.getWidth();
        double by = bounds.getHeight();

        return x <= 0 && v.getX() <= 0 ||
                x >= bx && v.getY() >=0 ||
                y <= 0 && v.getY() <= 0 ||
                y >= by && v.getY() >= 0;
    }

    static Point2D vecAngle(double angle, double mag) {
        return new Point2D(Math.cos(angle), Math.sin(angle)).multiply(mag);
    }

    static double rand(double min, double max) {
        return Math.random() * (max - min) + min;
    }
}

class Ship extends PhysicsObject {
    double thrust = 150;
    Polygon pgon;

    public Ship(Group parent, Point2D p) {
        super(parent, p, Point2D.ZERO, 0, 0);

        pgon = new Polygon(
                0.7, 0, -0.7,  -0.4, -0.7, 0.4
        );
        transform.getChildren().add(pgon);

        pgon.setStroke(Color.rgb(196, 237, 253));
        pgon.setStrokeWidth(0.1);
        pgon.getTransforms().add(new Scale(30, 30));

        update(0, 0, 0); // set up everything but not move it
    }

    public void update(double delta, double omega, double throttle) {
        if(throttle != 0) {
            Point2D acc = vecAngle(theta, thrust * throttle);
            v = v.add(acc.multiply(delta));
        } else { // 키 안 누르는 중 : ship의 속도를 slow down
            v = v.multiply(1 - 0.2 * delta);
        }

        this.omega = omega;
        super.update(delta);
    }

    public Shape getShapeBounds() { return pgon; }
}

class Asteroid extends PhysicsObject {
    Polygon pgon;

    public Asteroid(Group parent, double radius, Point2D p0, Point2D v0, double omega) {
        super(parent, p0, v0, 0, omega);

        pgon = new Polygon();
        pgon.setStroke(Color.hsb(rand(20, 50), rand(0,0.2), rand(0.8,1)));
        pgon.setStrokeWidth(3);
        pgon.setFill(Color.TRANSPARENT);

        int points = 20;
        for(int i=0;i<points; i++) {
            double a = (i * TAU) / points;
            double r = radius * rand(0.9,1.1);
            pgon.getPoints().addAll(Math.cos(a) * r, Math.sin(a) * r);
        }
        transform.getChildren().add(pgon);

        update(0);
    }

    public Shape getShapeBounds() { return pgon; }

    static Asteroid make(Group parent, Point2D size) {
        double angle = Math.random() * TAU;
        double radius = rand(20, 30);
        double omega = rand(-2, 2);
        double distCenter = size.magnitude() / 2 * rand(1,2); // 스크린에서 두 배 정도 떨어진 곳에서 시작
        Point2D p = vecAngle(angle, distCenter).add(size.multiply(0.5));
        Point2D v = vecAngle(Math.PI + angle + rand(-0.2, 0.2), rand(50, 100));
        return new Asteroid(parent, radius, p, v, omega);
    }
}

class Bullet extends PhysicsObject {
    Circle circle;
    static double muzzle = 400;

    public Bullet(Group parent, Point2D p /* 총알이 나가는 ship의 위치 */, Point2D vShip /* velocity of the ship to offset the velocity projectile */, double angle) {
        super(parent, p, vecAngle(angle, muzzle).add(vShip), angle, 0);
        circle = new Circle(3, Color.rgb(252, 255, 219));
        transform.getChildren().add(circle);
    }

    Shape getShapeBounds() { return circle; }
}