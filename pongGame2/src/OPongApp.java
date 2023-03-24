import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.scene.text.Font;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;
import javafx.stage.Stage;

import javax.sound.midi.*;

public class OPongApp extends Application {

    Point2D size = new Point2D(600, 750);

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {

        Group gRoot = new Group();
        Scene scene = new Scene(gRoot, size.getX(), size.getY());

        stage.setScene(scene);
        stage.setTitle("PONG!");

        Group gGame = new Group();

        GameTimer gameTimer = new GameTimer(gRoot);
        ScoreDisplay scoreDisplay = new ScoreDisplay(gRoot, size);

        Ball ball = new Ball(gGame, 25, 25, new Point2D(size.getX() / 2, 0), new Point2D(0,12));
        Bat bat = new Bat(gGame, 180, 25, new Point2D(600, 730));

        gRoot.getChildren().addAll(gGame);

        Pong pong = new Pong(ball, bat, gameTimer, scoreDisplay, scene);

        AnimationTimer loop = new AnimationTimer() {

            double old = -1;
            double GT = 0;
            double FT = 0;

            public void handle(long nano) {
                if(old < 0) old = nano;
                double delta = (nano - old) / 1e9;

                FT = (nano-old) / 1e6;
                old = nano;
                GT += delta;

                gameTimer.update(1/delta, GT, FT, scene);

                ball.update(delta);
                bat.update(scene);
                pong.update(delta);
                scoreDisplay.update();

            }
        };

        loop.start();

        stage.show();
    }
}

abstract class PhysicsObject {
    Point2D p, v; // position, velocity(pixels per second)
    Group root, transform;

    final static double TAU = Math.PI * 2;

    public PhysicsObject(Group parent, Point2D p0, Point2D v0) {
        root = new Group();
        transform = new Group();
        root.getChildren().add(transform);
        parent.getChildren().add(root);

        p = p0;
        v = v0;
    }

    abstract Shape getShapeBounds();

    static double rand(double min, double max) {
        return Math.random() * (max - min) + min;
    }
}

class Ball extends PhysicsObject {

    Rectangle ball;

    Point2D p, v;

    boolean died;

    public Ball(Group parent, double width, double height, Point2D p0, Point2D v0) {
        super(parent, p0, v0);

        ball = new Rectangle(width, height, Color.BLUE);
        transform.getChildren().add(ball);

        this.p = p0;
        this.v = v0;

        died = false;
    }

    public void update(double delta) {
        p = p.add(v);

        transform.getTransforms().clear();
        transform.getTransforms().addAll(
                new Translate(p.getX(), p.getY())
        );
    }

    Shape getShapeBounds() {
        return ball;
    }

    public Point2D getV() {
        return v;
    }

    public void setV(Point2D v0) {
        this.v = v0;
        super.v = v0;
    }

    public Point2D getP() {
        return p;
    }

    public void setP(Point2D p0) {
        this.p = p0;
        super.p = p0;
    }
}

class Bat extends PhysicsObject {

    Rectangle bat;

    Point2D p;
    Point2D v;
    Point2D old;

    public Bat(Group parent, double width, double height, Point2D p0) {
        super(parent, p0, Point2D.ZERO);

        bat = new Rectangle(width, height, Color.BLUE);

        this.p = p0;
        this.v = Point2D.ZERO;
        this.old = Point2D.ZERO;
        transform.getTransforms().add(
                new Translate(p0.getX(), p0.getY())
        );

        transform.getChildren().add(bat);

    }

    public void update(Scene scene) {

        scene.setOnMouseExited(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                bat.setFill(Color.RED);
            }
        });

        scene.setOnMouseEntered(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                bat.setFill(Color.BLUE);
            }
        });

        scene.addEventFilter(MouseEvent.MOUSE_MOVED, e-> {
            v = new Point2D(e.getX(), 0);
            transform.getTransforms().clear();
            transform.getTransforms().addAll(
                    new Translate(e.getX() - bat.getWidth() / 2, scene.getHeight() - bat.getHeight())
            );
        });
    }

    Shape getShapeBounds() {
        return bat;
    }
}

class Pong extends BinkBonkSound {

    Ball ball;
    Bat bat;
    GameTimer gameTimer;
    ScoreDisplay scoreDisplay;
    Rectangle bounds;
    Scene scene;
    BinkBonkSound binkBonkSound = new BinkBonkSound();
    boolean inCollision = false;
    boolean soundOn = true;
    Point2D oldBat = new Point2D(0,0);

    public Pong(Ball ball, Bat bat, GameTimer gameTimer, ScoreDisplay scoreDisplay, Scene scene) {
        this.ball = ball;
        this.bat = bat;
        this.gameTimer = gameTimer;
        this.scoreDisplay = scoreDisplay;
        this.bounds = new Rectangle(0,0, scene.getWidth(), scene.getHeight());
        this.scene = scene;
    }

    public void update(double delta) {
        if(isCollideWithBat()) { // when the ball is colliding with bat
            if(!inCollision) {
                scoreDisplay.addScore();
                ball.setV(ball.getV().multiply(-1));

                if(bat.v.subtract(oldBat).getX() == 0) {
                    ball.setV(new Point2D(0, ball.getV().getY()));
                } else {
                    ball.setV(ball.getV().add(bat.v.subtract(oldBat).multiply(delta)));
                }
                if(soundOn) {
                    binkBonkSound.play(true);
                }
                inCollision = true;
            }
        } else if(isCollideWithWall(bounds)) { // when the ball is colliding with the wal on both sides
            if(!inCollision) {
                ball.setV(new Point2D(ball.getV().getX() * -1, ball.getV().getY()));
                scoreDisplay.addScore();
                if(soundOn) {
                    binkBonkSound.play(false);
                }
                inCollision = true;
            }
        } else if(isCollideWithCeiling(bounds)) { // when the ball is colliding with the ceiling
            if(!inCollision) {
                ball.setV(new Point2D(ball.getV().getX(), ball.getV().getY() * -1));
                scoreDisplay.addScore();
                if(soundOn) {
                    binkBonkSound.play(false);
                }
                inCollision = true;
            }
        } else if(isDied()) { // when the ball fall down
            ball.setP(new Point2D(PhysicsObject.rand(0,500),0));
            ball.setV(new Point2D(0,12));
            scoreDisplay.resetScore();
            inCollision = false;
        } else {
            inCollision = false;
        }

        scene.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                if(event.getCode().equals(KeyCode.S)) {
                    if(soundOn) {
                        soundOn = false;
                    } else {
                        soundOn = true;
                    }
                } else if(event.getCode().equals(KeyCode.I)) {
                    gameTimer.showLabel();
                }
            }
        });
        oldBat = bat.v;
    }

    public boolean isCollideWithBat() {
        return !Shape.intersect(ball.getShapeBounds(), bat.getShapeBounds()).getBoundsInLocal().isEmpty();
    }

    public boolean isCollideWithWall(Rectangle bounds) {
        if(!Shape.intersect(ball.getShapeBounds(), bounds).getBoundsInLocal().isEmpty()) {
            return false;
        }

        double x = ball.getP().getX();
        double bx = bounds.getWidth();

        return x <= 0 && ball.v.getX() <= 0 ||
                x >= bx && ball.v.getX() >=0;
    }

    public boolean isCollideWithCeiling(Rectangle bounds) {
        if(!Shape.intersect(ball.getShapeBounds(), bounds).getBoundsInLocal().isEmpty()) {
            return false;
        }

        double y = ball.getP().getY();

        return y <= 0 && ball.v.getY() <= 0;
    }

    public boolean isDied() {
        if(!Shape.intersect(ball.getShapeBounds(), bounds).getBoundsInLocal().isEmpty()) {
            return false;
        }

        double y = ball.getP().getY();
        double by = bounds.getHeight();

        return y >= by && ball.getV().getY() >= 0;
    }

}

class ScoreDisplay extends Label {

    int score = 0;
    Label scoreLabel;

    public ScoreDisplay(Group parent, Point2D size) {
        scoreLabel = new Label();
        scoreLabel.setTextFill(Color.BLUE);
        scoreLabel.setFont(Font.font(40));
        scoreLabel.setText(String.format("%d", score));
        scoreLabel.setTranslateX(size.getX() / 2);
        scoreLabel.setTranslateY(size.getY() / 2 - 200);

        parent.getChildren().add(scoreLabel);
    }

    public void update() {
        scoreLabel.setText(String.format("%d", score));
    }

    public void addScore() {
        score++;
    }

    public void resetScore() {
        score = 0;
        scoreLabel.setText(String.format("%d", score));
    }

}
class GameTimer extends Label {

    Label gameTimer;
    double FPS = 0;
    double GT = 0;
    double FT = 0;

    public GameTimer(Group parent) {
        gameTimer = new Label();

        gameTimer.setTranslateX(2);
        gameTimer.setTextFill(Color.ORANGERED);
        parent.getChildren().add(gameTimer);
    }

    public void update(double FPS, double GT, double FT, Scene scene) {
        this.FPS = FPS;
        this.GT = GT;
        this.FT = FT;
        gameTimer.setText(String.format("%.2f FPS (avg) FT=%.2f (ms avg), GT=%.2f (s)", FPS, FT , GT));
    }

    public void showLabel() {
        if(gameTimer.isVisible()) {
            gameTimer.setVisible(false);
        } else {
            gameTimer.setVisible(true);
        }
    }

    public void hideLabel() {
        gameTimer.setVisible(false);
    }

}

class BinkBonkSound {

    // magic numbers that are not common knowledge unless one
    // has studied the GM2 standard and the midi sound system
    //
    // The initials GM mean General Midi. This GM standard
    // provides for a set of common sounds that respond
    // to midi messages in a common way.
    //
    // MIDI is a standard for the encoding and transmission
    // of musical sound meta-information, e.g., play this
    // note on this instrument at this level and this pitch
    // for this long.
    //
    private static final int MAX_PITCH_BEND = 16383;
    private static final int MIN_PITCH_BEND = 0;
    private static final int REVERB_LEVEL_CONTROLLER = 91;
    private static final int MIN_REVERB_LEVEL = 0;
    private static final int MAX_REVERB_LEVEL = 127;
    private static final int DRUM_MIDI_CHANNEL = 9;
    private static final int CLAVES_NOTE = 76;
    private static final int NORMAL_VELOCITY = 100;
    private static final int MAX_VELOCITY = 127;

    Instrument[] instrument;
    MidiChannel[] midiChannels;
    boolean playSound;

    public BinkBonkSound(){
        playSound=true;
        try{
            Synthesizer gmSynthesizer = MidiSystem.getSynthesizer();
            gmSynthesizer.open();
            instrument = gmSynthesizer.getDefaultSoundbank().getInstruments();
            midiChannels = gmSynthesizer.getChannels();

        } catch (MidiUnavailableException e) {
            e.printStackTrace();
        }
    }

    // This method has more comments than would typically be needed for
    // programmers using the Java sound system libraries. This is because
    // most students will not have exposure to the specifics of midi and
    // the general midi sound system. For example, drums are on channel
    // 10 and this cannot be changed. The GM2 standard defines much of
    // the detail that I have chosen to use static constants to encode.
    //
    // The use of midi to play sounds allows us to avoid using external
    // media, e.g., wav files, to play sounds in the game.
    //
    void play(boolean hiPitch){
        if(playSound) {

            // Midi pitch bend is required to play a single drum note
            // at different pitches. The high and low pongs are two
            // octaves apart. As you recall from high school physics,
            // each additional octave doubles the frequency.
            //
            midiChannels[DRUM_MIDI_CHANNEL]
                    .setPitchBend(hiPitch ? MAX_PITCH_BEND : MIN_PITCH_BEND);

            // Turn the reverb send fully off. Drum sounds play until they
            // decay completely. Reverb extends the audible decay and,
            // from a gameplay point of view, is distracting.
            //
            midiChannels[DRUM_MIDI_CHANNEL]
                    .controlChange(REVERB_LEVEL_CONTROLLER, MIN_REVERB_LEVEL);

            // Play the claves on the drum channel at a "normal" volume
            //
            midiChannels[DRUM_MIDI_CHANNEL]
                    .noteOn(CLAVES_NOTE, NORMAL_VELOCITY);
        }
    }

    public void toggleSound() {
        playSound = !playSound;
    }
}