package com.example.demo1;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;

public class Football extends Application {
    private Circle blueCircle;    // Синий круг (A, D, W)
    private Circle redCircle;    // Красный круг (стрелочки)
    private Circle ball;         // Мяч
    private Rectangle leftGoal;  // Ворота слева
    private Rectangle rightGoal; // Ворота справа
    private Text blueScoreText;  // Текст для отображения счета синего
    private Text redScoreText;   // Текст для отображения счета красного
    private Text timerText;      // Текст для отображения времени
    private int gameTime = 60;   // Начальное значение таймера (60 секунд)

    private static final double MOVE_SPEED = 8; // Скорость движения

    private static final double BALL_RADIUS = 25; // Радиус мяча
    private static final double PLAYER_RADIUS = 50; // Радиус игроков
    private static final double BALL_SPEED_X = 16; // Скорость мяча по X
    private static final double BALL_SPEED_Y = 12; // Скорость мяча по Y
    private double blueJumpVelocity = 0;
    private double redJumpVelocity = 0;
    private double GRAVITY = 0.5;
    private double PlayerGRAVITY=1;

    private int blueScore = 0; // Счет синего игрока
    private int redScore = 0;  // Счет красного игрока

    // Флаги для синего круга
    private boolean isBlueMovingLeft = false;
    private boolean isBlueMovingRight = false;
    private boolean isBlueJumping = false;

    // Флаги для красного круга
    private boolean isRedMovingLeft = false;
    private boolean isRedMovingRight = false;
    private boolean isRedJumping = false;

    private double ballVelocityX = BALL_SPEED_X; // Направление движения мяча по X
    private double ballVelocityY = BALL_SPEED_Y; // Направление движения мяча по Y

    private boolean isBlueKicking=false;
    private boolean isRedKicking=false;



    private Timeline movementTimeline;
    private Timeline timerTimeline;

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Football Game");
        primaryStage.setScene(createGameScene(primaryStage));
        primaryStage.show();
    }

    private Scene createGameScene(Stage primaryStage) {
        StopTimelines();
        Pane root = new Pane();

        // Создаем элементы игры (круги, мяч, ворота, текст)
        blueCircle = new Circle(100, 850, PLAYER_RADIUS);
        blueCircle.setFill(Color.BLUE);

        redCircle = new Circle(1400, 850, PLAYER_RADIUS);
        redCircle.setFill(Color.RED);

        ball = new Circle(750, 400, BALL_RADIUS);
        ball.setFill(Color.GREEN);

        leftGoal = new Rectangle(0, 500, 20, 400);
        leftGoal.setFill(Color.GRAY);

        rightGoal = new Rectangle(1480, 500, 20, 400);
        rightGoal.setFill(Color.GRAY);

        blueScoreText = new Text(50, 50, "Blue: 0");
        blueScoreText.setFont(Font.font(30));
        blueScoreText.setFill(Color.BLUE);

        redScoreText = new Text(1350, 50, "Red: 0");
        redScoreText.setFont(Font.font(30));
        redScoreText.setFill(Color.RED);

        timerText = new Text(700, 50, "Time: " + gameTime);
        timerText.setFont(Font.font(30));
        timerText.setFill(Color.BLACK);


        Text menuText=new Text(900,50,"menu");
        menuText.setFont(Font.font(30));
        menuText.setFill(Color.RED);
        menuText.setOnMouseClicked(event->{
            primaryStage.setScene(createMenuScene(primaryStage)); // Переходим в меню
        });


        root.getChildren().addAll(blueCircle, redCircle, ball, leftGoal, rightGoal, blueScoreText, redScoreText, timerText,menuText);

        Scene scene = new Scene(root, 1500, 900);

        // Обработка нажатия клавиш
        scene.setOnKeyPressed(event -> {
            // Обработка движения и прыжков (существующий код)
            if (event.getCode() == KeyCode.A) {
                isBlueMovingLeft = true;
            } else if (event.getCode() == KeyCode.D) {
                isBlueMovingRight = true;
            } else if (event.getCode() == KeyCode.S) {
                isBlueKicking = true; // Синий игрок бьет
            }

            if (event.getCode() == KeyCode.LEFT) {
                isRedMovingLeft = true;
            } else if (event.getCode() == KeyCode.RIGHT) {
                isRedMovingRight = true;
            } else if (event.getCode() == KeyCode.DOWN) {
                isRedKicking = true; // Красный игрок бьет
            }

            // Обработка прыжков (существующий код)
            if (event.getCode() == KeyCode.W && !isBlueJumping) {
                jumpCircle(blueCircle, true);
            }
            if (event.getCode() == KeyCode.UP && !isRedJumping) {
                jumpCircle(redCircle, false);
            }
        });

        scene.setOnKeyReleased(event -> {
            // Обработка отпускания клавиш (существующий код)
            if (event.getCode() == KeyCode.A) {
                isBlueMovingLeft = false;
            } else if (event.getCode() == KeyCode.D) {
                isBlueMovingRight = false;
            } else if (event.getCode() == KeyCode.S) {
                isBlueKicking = false; // Синий игрок перестал бить
            }

            if (event.getCode() == KeyCode.LEFT) {
                isRedMovingLeft = false;
            } else if (event.getCode() == KeyCode.RIGHT) {
                isRedMovingRight = false;
            } else if (event.getCode() == KeyCode.DOWN) {
                isRedKicking = false; // Красный игрок перестал бить
            }
        });

        // Запускаем цикл обновления
        startMovementLoop();

        // Запускаем таймер
        startTimer(primaryStage);

        return scene;
    }



    private Scene createMenuScene(Stage primaryStage) {
        StopTimelines();
        Pane menuRoot = new Pane();
        Text gameOverText = new Text(600, 300, "Game Over");
        gameOverText.setFont(Font.font(50));
        gameOverText.setFill(Color.RED);
        gameOverText.setOnMouseClicked(event->{
            System.exit(0);
        });


        // Кнопка "Restart"
        Text restartText = new Text(650, 400, "Restart");
        restartText.setFont(Font.font(30));
        restartText.setFill(Color.BLUEVIOLET);
        restartText.setOnMouseClicked(event -> {
            // Перезапуск игры
            gameTime = 60; // Сброс таймера
            ballVelocityY=BALL_SPEED_Y;
            ballVelocityX=BALL_SPEED_X;

            redScore=0;
            blueScore=0;
            primaryStage.setScene(createGameScene(primaryStage)); // Возврат к игровой сцене
        });

        Text scoreText=new Text(625,500,"");
        scoreText.setFont(Font.font(30));
        if(blueScore>redScore) {
            scoreText.setText("BLUE WIN");
            scoreText.setFill(Color.BLUE);
            scoreText.setX(640);
        }else if(blueScore==redScore){
            scoreText.setText("RED WIN");
            scoreText.setFill(Color.RED);
            scoreText.setX(640);
        }else{
            scoreText.setText("DRAW");
            scoreText.setFill(Color.BLACK);
            scoreText.setX(650);
        }

        menuRoot.getChildren().addAll(gameOverText, restartText,scoreText);

        return new Scene(menuRoot, 1500, 900);
    }

    private void startTimer(Stage primaryStage) {
        // Инициализируем Timeline
            timerTimeline = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
            gameTime--; // Уменьшаем время на 1 секунду
            timerText.setText("Time: " + gameTime); // Обновляем текстовый элемент

            // Если время вышло
            if (gameTime <= 0) {
                primaryStage.setScene(createMenuScene(primaryStage)); // Переходим в меню
            }
        }));
        timerTimeline.setCycleCount(Animation.INDEFINITE); // Бесконечный цикл
        timerTimeline.play(); // Запускаем таймер
    }
    // Метод для запуска цикла обновления
    private void startMovementLoop() {
            movementTimeline = new Timeline(new KeyFrame(Duration.millis(16), event -> {
            // Обновляем позицию синего круга
            if (isBlueMovingLeft) {
                moveCircle(blueCircle, -MOVE_SPEED, 0, redCircle);
            }
            if (isBlueMovingRight) {
                moveCircle(blueCircle, MOVE_SPEED, 0, redCircle);
            }

            // Обновляем позицию красного круга
            if (isRedMovingLeft) {
                moveCircle(redCircle, -MOVE_SPEED, 0, blueCircle);
            }
            if (isRedMovingRight) {
                moveCircle(redCircle, MOVE_SPEED, 0, blueCircle);
            }

            // Обновляем прыжок синего круга
            Jump(blueCircle, true);

            // Обновляем прыжок красного круга
            Jump(redCircle, false);

            if (isBlueKicking) {
                kickBall(true); // Удар синего игрока
            }
            if (isRedKicking) {
                kickBall(false); // Удар красного игрока
            }
            // Обновляем позицию мяча
            updateBallPosition();

            // Проверяем, попал ли мяч в ворота
            checkGoal();
            ballVelocityY+=GRAVITY;

        }));
        movementTimeline.setCycleCount(Animation.INDEFINITE);
        movementTimeline.play();

    }

    private void StopTimelines(){
        if(movementTimeline!=null) {
            movementTimeline.stop();
        }
        if(timerTimeline!=null){
            timerTimeline.stop();
        }
    }

    // Метод для проверки попадания мяча в ворота
    private void checkGoal() {
        double ballX = ball.getCenterX();
        double ballY = ball.getCenterY();

        // Проверяем, попал ли мяч в левые ворота
        if (ballX - BALL_RADIUS <= leftGoal.getWidth() && ballY-BALL_RADIUS>= leftGoal.getY()) {
            redScore++; // Красный игрок забил гол
            updateScore();
            resetBallBlue();
        }

        // Проверяем, попал ли мяч в правые ворота
        if (ballX + BALL_RADIUS >= rightGoal.getX() && ballY-BALL_RADIUS>= rightGoal.getY()) {
            blueScore++; // Синий игрок забил гол
            updateScore();
            resetBallRed();
        }
    }

    // Метод для обновления счета на экране
    private void updateScore() {
        blueScoreText.setText("Blue: " + blueScore);
        redScoreText.setText("Red: " + redScore);
    }

    // Метод для сброса мяча в центр
    private void resetBallRed() {
        ball.setCenterX(750);
        ball.setCenterY(600);
        ballVelocityX = BALL_SPEED_X;
        ballVelocityY = BALL_SPEED_Y;
        blueCircle.setCenterY(850);
        blueCircle.setCenterX(50);
        isBlueJumping=false;
        redCircle.setCenterX(1380);
        redCircle.setCenterY(850);
        isRedJumping=false;
    }
    private void resetBallBlue() {
        ball.setCenterX(750);
        ball.setCenterY(600);
        ballVelocityX = -BALL_SPEED_X;
        ballVelocityY = BALL_SPEED_Y;
        blueCircle.setCenterY(850);
        blueCircle.setCenterX(50);
        isBlueJumping=false;
        redCircle.setCenterX(1380);
        redCircle.setCenterY(850);
        isRedJumping=false;
    }

    private void moveCircle(Circle circle, double deltaX, double deltaY, Circle otherCircle) {
        double newX = circle.getCenterX() + deltaX;
        double newY = circle.getCenterY() + deltaY;

        // Проверяем границы экрана
        if (newX - PLAYER_RADIUS < 0 || newX + PLAYER_RADIUS > 1500) {
            return;
        }

        // Проверяем коллизию с другим кругом
        if (!checkCircleCollision(circle, newX, newY, otherCircle)) {
            circle.setCenterX(newX);
            circle.setCenterY(newY);
        }
    }

    // Метод для проверки коллизии между двумя квадратами
    private boolean checkCircleCollision(Circle circle1, double newX, double newY, Circle circle2) {
        double dx = newX - circle2.getCenterX();
        double dy = newY - circle2.getCenterY();
        double distance = Math.sqrt(dx * dx + dy * dy);
        return distance < PLAYER_RADIUS * 2;
    }

    // Метод для обновления позиции мяча
    private void updateBallPosition() {
        // Предварительно вычисляем новую позицию мяча
        double newX = ball.getCenterX() + ballVelocityX;
        double newY = ball.getCenterY() + ballVelocityY;

        // Проверка столкновения с границами экрана
        if (newX - BALL_RADIUS <= 0 || newX + BALL_RADIUS >= 1500) {
            ballVelocityX = -ballVelocityX;
            newX = ball.getCenterX() + ballVelocityX; // Корректируем позицию
        }
        if (newY - BALL_RADIUS <= 0 || newY + BALL_RADIUS >= 900) {
            ballVelocityY = -ballVelocityY;
            newY = ball.getCenterY() + ballVelocityY; // Корректируем позицию
        }

        // Проверка столкновения с игроками
        if (checkBallCollision(ball, blueCircle)) {
            calculateNewBallVelocity(ball, blueCircle);
            newX = ball.getCenterX() + ballVelocityX; // Корректируем позицию
            newY = ball.getCenterY() + ballVelocityY;
        }
        if (checkBallCollision(ball, redCircle)) {
            calculateNewBallVelocity(ball, redCircle);
            newX = ball.getCenterX() + ballVelocityX; // Корректируем позицию
            newY = ball.getCenterY() + ballVelocityY;
        }

        // Обновление позиции мяча
        ball.setCenterX(newX);
        ball.setCenterY(newY);
    }

    private void calculateNewBallVelocity(Circle ball, Circle player) {
        // Вектор от центра игрока к центру мяча
        double dx = ball.getCenterX() - player.getCenterX();
        double dy = ball.getCenterY() - player.getCenterY();

        // Расстояние между центрами
        double distance = Math.sqrt(dx * dx + dy * dy);

        // Нормализуем вектор
        double nx = dx / distance;
        double ny = dy / distance;

        // Вектор касательной (перпендикуляр к нормали)
        double tx = -ny;
        double ty = nx;

        // Разложение скорости мяча на нормальную и касательную компоненты
        double vNormal = ballVelocityX * nx + ballVelocityY * ny;
        double vTangent = ballVelocityX * tx + ballVelocityY * ty;

        // Меняем направление нормальной компоненты (отскок)
        vNormal = -vNormal;

        // Новая скорость мяча
        ballVelocityX = vNormal * nx + vTangent * tx;
        ballVelocityY = vNormal * ny + vTangent * ty;

        // Корректируем позицию мяча, чтобы он не застрял внутри игрока
        double overlap = (ball.getRadius() + player.getRadius()) - distance;
        if (overlap > 0) {
            // Выталкиваем мяч на безопасное расстояние
            ball.setCenterX(ball.getCenterX() + nx * (overlap + 3)); // +1 для гарантии
            ball.setCenterY(ball.getCenterY() + ny * (overlap + 3));
        }
    }

    // Метод для проверки столкновения мяча с кругом (игроком)
    private boolean checkBallCollision(Circle ball, Circle player) {
        double dx = ball.getCenterX() - player.getCenterX();
        double dy = ball.getCenterY() - player.getCenterY();
        double distance = Math.sqrt(dx * dx + dy * dy);
        double sumOfRadii = ball.getRadius() + player.getRadius();

        return distance <= sumOfRadii;
    }


    private void jumpCircle(Circle circle, boolean isBlue) {
        if (isBlue) {
            if (!isBlueJumping) {
                isBlueJumping = true;
                blueJumpVelocity = -25; // Начальная скорость прыжка
            }
        } else {
            if (!isRedJumping) {
                isRedJumping = true;
                redJumpVelocity = -25; // Начальная скорость прыжка
            }
        }
    }

    private void Jump(Circle circle, boolean isBlue) {
        if (isBlue) {
            if (isBlueJumping) {
                double newY = circle.getCenterY() + blueJumpVelocity;
                blueJumpVelocity += PlayerGRAVITY;

                // Проверяем, достиг ли игрок земли
                if (newY >= 900 - PLAYER_RADIUS) {
                    newY = 900 - PLAYER_RADIUS;
                    blueJumpVelocity = 0;
                    isBlueJumping = false;
                }

                circle.setCenterY(newY);
            }
        } else {
            if (isRedJumping) {
                double newY = circle.getCenterY() + redJumpVelocity;
                redJumpVelocity += PlayerGRAVITY;

                // Проверяем, достиг ли игрок земли
                if (newY >= 900 - PLAYER_RADIUS) {
                    newY = 900 - PLAYER_RADIUS;
                    redJumpVelocity = 0;
                    isRedJumping = false;
                }

                circle.setCenterY(newY);
            }
        }
    }

    private void kickBall(boolean isBlueKick) {
        if (isBlueKick) {
            // Удар синего игрока
            if (checkBallCollision(ball, blueCircle)) {
                ballVelocityX = Math.sqrt(ballVelocityX*ballVelocityX+ballVelocityY*ballVelocityY); // Увеличиваем горизонтальную скорость
                ballVelocityY = 0; // Обнуляем вертикальную скорость
            }
        } else {
            // Удар красного игрока
            if (checkBallCollision(ball, redCircle)) {
                ballVelocityX = -Math.sqrt(ballVelocityX*ballVelocityX+ballVelocityY*ballVelocityY);// Увеличиваем горизонтальную скорость
                ballVelocityY = 0; // Обнуляем вертикальную скорость
            }
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}