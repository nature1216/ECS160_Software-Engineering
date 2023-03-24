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
import javafx.scene.transform.Translate;
import javafx.stage.Stage;

import javax.sound.midi.*;

public class PongApp extends Application {

    Point2D size = new Point2D(580, 750);

    Point2D bp = new Point2D(300, 0);
    Point2D bv = new Point2D(0,12);
    Point2D tv = new Point2D(0,0);

    BinkBonkSound sound = new BinkBonkSound();

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        Group gRoot = new Group();
        Scene scene = new Scene(gRoot, size.getX(), size.getY());

        stage.setScene(scene);
        stage.setTitle("PONG!");

        Label infoLabel = new Label();
        infoLabel.setTranslateX(2);
        infoLabel.setTextFill(Color.ORANGERED);

        Label scoreDisplay = new Label();
        scoreDisplay.setTranslateX(size.getX() / 2);
        scoreDisplay.setTranslateY(size.getY() / 2 - 200);
        scoreDisplay.setTextFill(Color.BLUE);
        scoreDisplay.setFont(Font.font(40));

        Group gGame = new Group();
        Group gBall = new Group();
        Group gBallTransform = new Group();

        Rectangle ball = new Rectangle(25, 25, Color.BLUE);

        gBallTransform.getChildren().add(ball);
        gBall.getChildren().add(gBallTransform);

        Group gBat = new Group();
        Group gBatTransform = new Group();

        Rectangle bat = new Rectangle(180, 20, Color.BLUE);
        gBatTransform.getTransforms().add(new Translate(600, 730));

        gBatTransform.getChildren().add(bat);
        gBat.getChildren().add(gBatTransform);

        gGame.getChildren().addAll(gBall, gBat);

        gRoot.getChildren().addAll(gGame, infoLabel, scoreDisplay);

        Rectangle bounds = new Rectangle(0,0, size.getX(), size.getY());
        bounds.setVisible(false);
        gRoot.getChildren().add(bounds);

        AnimationTimer loop = new AnimationTimer() {
            double old = -1;
            double GT = 0;
            double FT = 0;
            Point2D oldBat = Point2D.ZERO;

            int score = 0;
            boolean died = false;
            boolean soundOn = true;
            boolean inCollision = false;

            @Override
            public void handle(long nano) {
                if(old < 0) old = nano;
                double delta = (nano - old) / 1e9;

                FT = (nano-old) / 1e6;
                old = nano;
                GT += delta;

                scoreDisplay.setText(String.format("%d",score));
                infoLabel.setText(String.format("%.2f FPS (avg) FT=%.2f (ms avg), GT=%.2f (s)", 1/delta, FT , GT));

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

                scene.setOnKeyPressed(new EventHandler<KeyEvent>() {
                    @Override
                    public void handle(KeyEvent event) {
                        if(event.getCode().equals(KeyCode.I)) { // infoLabel visible or not
                            if(infoLabel.isVisible()) {
                                infoLabel.setVisible(false);
                            } else {
                                infoLabel.setVisible(true);
                            }
                        } else if(event.getCode().equals(KeyCode.S)) { // sound on/off
                            if(soundOn) {
                                soundOn = false;
                            } else {
                                soundOn = true;
                            }
                        }
                    }
                });

                scene.addEventFilter(MouseEvent.MOUSE_MOVED, e-> {
                    tv = new Point2D(e.getX(),0);
                    gBatTransform.getTransforms().clear();
                    gBatTransform.getTransforms().addAll(
                            new Translate(e.getX() - bat.getWidth() / 2,size.getY() - bat.getHeight())
                    );
                });

                if(isCollideWithBat()) { // when the ball is colliding with bat
                    if(!inCollision) {
                        bv = bv.multiply(-1);
                        if(tv.subtract(oldBat).getX() == 0) {
                            bv = new Point2D(0, bv.getY());
                        } else {
                            bv = bv.add(tv.subtract(oldBat).multiply(delta));
                        }
                        score++;
                        if(soundOn) {
                            sound.play(true);
                        }
                        inCollision = true;
                    }
                } else if (isCollideWithWall(bounds)) { // when the ball is colliding with the wall on both sides
                    if (!inCollision) {
                        bv = new Point2D(bv.getX() * -1, bv.getY());
                        score++;
                        if(soundOn) {
                            sound.play(false);
                            inCollision = true;
                        }
                    }
                } else if (isCollideWithCeiling(bounds)) { // when the ball is colliding with the ceiling
                    if(!inCollision) {
                        bv = new Point2D(bv.getX(), bv.getY() * -1);
                        score++;
                        if(soundOn) {
                            sound.play(false);
                        }
                        inCollision = true;
                    }
                } else if (isDied(bounds)) {
                    bp = new Point2D(rand(0,580),0);
                    bv = new Point2D(0,12);

                    gBallTransform.getTransforms().clear();
                    gBallTransform.getTransforms().addAll(
                            new Translate(rand(0,580), 0)
                    );
                    died = false;
                    score = 0;
                    inCollision = false;
                }
                else {
                    inCollision = false;
                }

                bp = bp.add(bv);
                gBallTransform.getTransforms().clear();
                gBallTransform.getTransforms().addAll(
                        new Translate(bp.getX(), bp.getY())
                );

                oldBat = tv;

            }

            public boolean isDied(Rectangle bounds) {
                if(!Shape.intersect(ball, bounds).getBoundsInLocal().isEmpty()) {
                    return false;
                }

                double y = bp.getY();
                double by = bounds.getHeight();

                return y >= by && bv.getY() >= 0;
            }

            public boolean isCollideWithBat() {
                return !Shape.intersect(ball, bat).getBoundsInLocal().isEmpty();
            }

            public boolean isCollideWithWall(Rectangle bounds) {
                if(!Shape.intersect(ball, bounds).getBoundsInLocal().isEmpty()) {
                    return false;
                }

                double x = bp.getX();
                double bx = bounds.getWidth();

                return x <= 0 && bv.getX() <= 0 ||
                        x >= bx && bv.getX() >=0;
            }

            public boolean isCollideWithCeiling(Rectangle bounds) {
                if(!Shape.intersect(ball, bounds).getBoundsInLocal().isEmpty()) {
                    return false;
                }

                double y = bp.getY();

                return y <= 0 && bv.getY() <= 0;
            }

        };

        loop.start();

        stage.show();
    }
    public double rand(double min, double max) {
        return Math.random() * (max - min) + min;
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