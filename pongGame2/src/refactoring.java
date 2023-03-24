import javafx.event.EventHandler;
import javafx.geometry.Point2D;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

public class refactoring {

//    1. inCollision if문 앞으로 뻄
//    2. soundOn boolean -> isSoundOn() function
//    3. offSound(), onSound() function
    public void update(double delta) {
        if (isCollideWithBat()) { // when the ball is colliding with bat
            if(inCollision) {
                return;
            }

            scoreDisplay.addScore();
            ball.setV(ball.getV().multiply(-1));

            ball.changeVelocityWithBat(oldBat, bat);
            if (isSoundOn()) {
                binkBonkSound.play(true);
            }
            inCollision = true;

        } else if (isCollideWithWall(bounds)) { // when the ball is colliding with the wal on both sides
            if (inCollision) {
                return;
            }
            ball.changeVelocityWithWall();
            scoreDisplay.addScore();
            if (isSoundOn()) {
                binkBonkSound.play(false);
            }
            inCollision = true;

        } else if (isCollideWithCeiling(bounds)) { // when the ball is colliding with the ceiling
            if (inCollision) {
                return;
            }
            ball.changeAngleWithCeiling();
            scoreDisplay.addScore();
            if (isSoundOn()) {
                binkBonkSound.play(false);
            }
            inCollision = true;
        } else if (isDied()) { // when the ball fall down
            ball.reset();
            scoreDisplay.resetScore();
            inCollision = false;
        } else {
            inCollision = false;
        }

        scene.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                if (event.getCode().equals(KeyCode.S)) {
                    if (isSoundOn()) {
                        offSound();
                    } else {
                        onSound();
                    }
                } else if (event.getCode().equals(KeyCode.I)) {
                    gameTimer.showLabel();
                }
            }
        });
        oldBat = bat.v;
    }
}
