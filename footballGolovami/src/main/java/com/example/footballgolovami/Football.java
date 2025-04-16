package com.example.footballgolovami;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

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

    private static double PLAYER_SPEED = 8; // Скорость движения
    private static double BALL_RADIUS = 27; // Радиус мяча
    private static double PLAYER_RADIUS = 40; // Радиус игроков
    private static double BALL_SPEED_X = 14; // Скорость мяча по X
    private static double BALL_SPEED_Y = 10;// Скорость мяча по Y
    private double BALL_MAXENERGY= 450;
    private double blueJumpVelocity = 0;
    private double redJumpVelocity = 0;
    private double GRAVITY = 0.5;
    private double PlayerGRAVITY=1;
    private double JumpVelocity = -22;

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

    private final double WINDOW_WIDTH = 1200;
    private final double WINDOW_HEIGHT = 600;

    private double WallBounceRatio=0.95;
    private double PlayerBounceRatio=1.2;
    private double KickingPowerRatio=1.4;

    private double GoalsHight=WINDOW_HEIGHT*4/9;
    private static final double KICK_DISTANCE = 20; // Расстояние для удара по мячу

    private Image[] gameBackgrounds = new Image[4];
    private Image[] menuBackgrounds = new Image[2];
    private Image settingsBackground;
    private Image[] playerImages = new Image[10];
    private Image ballImage;
    private ImageView bluePlayerImage;
    private ImageView redPlayerImage;
    private ImageView ballImageView;
    private int currentGameBackgroundIndex = -1;

    private Media[] soundtracks = new Media[3];
    private MediaPlayer mediaPlayer;
    private int currentSoundtrackIndex = -1;
    private boolean isMusicEnabled = true;

    @Override
    public void start(Stage primaryStage) {
        // Загрузка фоновых изображений
        loadBackgroundImages();
        loadPlayerImages();
        loadSoundtracks(); // Загружаем саундтреки
        primaryStage.setTitle("Football Game");
        primaryStage.setScene(createCutScene(primaryStage));
        primaryStage.show();
    }

    private void loadBackgroundImages() {
        // Загрузка фонов для игры
        for (int i = 0; i < 3; i++) {
            try {
                gameBackgrounds[i] = new Image("file:Pictures/BACKGROUND_" + (i + 1) + ".jpg");
            } catch (Exception e) {
                System.out.println("Не удалось загрузить фоновое изображение игры " + (i + 1));
            }
        }

        // Загрузка фонов для меню
        try {
            menuBackgrounds[0] = new Image("file:Pictures/BACKGROUND_MENU_1.jpg");
        } catch (Exception e) {
            System.out.println("Не удалось загрузить фоновое изображение меню 1");
        }

        // Загрузка фона для настроек
        try {
            settingsBackground = new Image("file:Pictures/BACKGROUND_SETTINGS_1.jpg");
        } catch (Exception e) {
            System.out.println("Не удалось загрузить фоновое изображение настроек");
        }

        // Загрузка изображения мяча
        try {
            ballImage = new Image("file:Pictures/BALL_1.png");
        } catch (Exception e) {
            System.out.println("Не удалось загрузить изображение мяча");
        }
    }

    private void loadPlayerImages() {
        for (int i = 0; i < 10; i++) {
            try {
                playerImages[i] = new Image("file:Pictures/PLAYER_" + (i + 1) + ".png");
                System.out.println("Загружено изображение PLAYER_" + (i + 1) + ".png"); // Добавляем отладочный вывод
            } catch (Exception e) {
                System.out.println("Не удалось загрузить изображение игрока " + (i + 1) + ": " + e.getMessage());
            }
        }
    }

    private void loadSoundtracks() {
        for (int i = 0; i < 3; i++) {
            try {
                String path = new File("Sounds/SOUNDTRACK_" + (i + 1) + ".mp3").toURI().toString();
                soundtracks[i] = new Media(path);
            } catch (Exception e) {
                System.out.println("Не удалось загрузить саундтрек " + (i + 1));
            }
        }
    }

    private void playRandomSoundtrack() {
        if (!isMusicEnabled) return; // Не воспроизводим музыку, если она отключена
        
        if (mediaPlayer != null) {
            mediaPlayer.stop();
        }
        // Если это продолжение игры, используем сохраненный индекс
        int index = currentSoundtrackIndex != -1 ? currentSoundtrackIndex : (int)(Math.random() * 3);
        if (soundtracks[index] != null) {
            mediaPlayer = new MediaPlayer(soundtracks[index]);
            mediaPlayer.setCycleCount(MediaPlayer.INDEFINITE);
            mediaPlayer.setVolume(0.5);
            mediaPlayer.play();
            currentSoundtrackIndex = index; // Сохраняем индекс текущего саундтрека
        }
    }

    private void setRandomPlayerImages() {
        // Создаем список доступных индексов изображений
        List<Integer> availableIndices = new ArrayList<>();
        for (int i = 0; i < playerImages.length; i++) {
            if (playerImages[i] != null) {
                availableIndices.add(i);
            }
        }

        // Проверяем, что у нас есть хотя бы два разных изображения
        if (availableIndices.size() < 2) {
            System.out.println("Недостаточно изображений для игроков");
            return;
        }

        // Выбираем случайный индекс для синего игрока
        int blueIndex = availableIndices.remove((int)(Math.random() * availableIndices.size()));
        
        // Выбираем случайный индекс для красного игрока из оставшихся
        int redIndex = availableIndices.get((int)(Math.random() * availableIndices.size()));

        System.out.println("Выбраны изображения: синий - " + (blueIndex + 1) + ", красный - " + (redIndex + 1));

        // Создаем новые ImageView для игроков
        bluePlayerImage = new ImageView(playerImages[blueIndex]);
        redPlayerImage = new ImageView(playerImages[redIndex]);

        // Устанавливаем размеры изображений
        bluePlayerImage.setFitWidth(PLAYER_RADIUS * 2);
        bluePlayerImage.setFitHeight(PLAYER_RADIUS * 2);
        redPlayerImage.setFitWidth(PLAYER_RADIUS * 2);
        redPlayerImage.setFitHeight(PLAYER_RADIUS * 2);

        // Устанавливаем позиции изображений
        bluePlayerImage.setX(blueCircle.getCenterX() - PLAYER_RADIUS);
        bluePlayerImage.setY(blueCircle.getCenterY() - PLAYER_RADIUS);
        redPlayerImage.setX(redCircle.getCenterX() - PLAYER_RADIUS);
        redPlayerImage.setY(redCircle.getCenterY() - PLAYER_RADIUS);

        // Делаем круги игроков прозрачными
        blueCircle.setFill(Color.TRANSPARENT);
        redCircle.setFill(Color.TRANSPARENT);
    }

    private Scene createGameScene(Stage primaryStage) {
        StopTimelines();
        
        // Воспроизводим музыку только в GameScene
        if (isMusicEnabled) {
            playRandomSoundtrack();
        }

        Pane root = new Pane();

        // Выбираем фон только если это новая игра
        if (currentGameBackgroundIndex == -1) {
            currentGameBackgroundIndex = (int)(Math.random() * 3);
        }
        ImageView background = new ImageView(gameBackgrounds[currentGameBackgroundIndex]);
        background.setFitWidth(WINDOW_WIDTH);
        background.setFitHeight(WINDOW_HEIGHT);
        root.getChildren().add(background);

        // Создаем элементы игры (круги, мяч, ворота, текст)
        blueCircle = new Circle(PLAYER_RADIUS, WINDOW_HEIGHT-PLAYER_RADIUS, PLAYER_RADIUS);
        blueCircle.setFill(Color.TRANSPARENT);

        redCircle = new Circle(WINDOW_WIDTH-PLAYER_RADIUS, WINDOW_HEIGHT-PLAYER_RADIUS, PLAYER_RADIUS);
        redCircle.setFill(Color.TRANSPARENT);

        // Устанавливаем изображения для игроков только если это новая игра
        if (bluePlayerImage == null || redPlayerImage == null) {
            setRandomPlayerImages();
        } else {
            // Обновляем позиции существующих изображений
            bluePlayerImage.setX(blueCircle.getCenterX() - PLAYER_RADIUS);
            bluePlayerImage.setY(blueCircle.getCenterY() - PLAYER_RADIUS);
            redPlayerImage.setX(redCircle.getCenterX() - PLAYER_RADIUS);
            redPlayerImage.setY(redCircle.getCenterY() - PLAYER_RADIUS);
        }

        ball = new Circle(WINDOW_WIDTH/2, WINDOW_HEIGHT*6/9, BALL_RADIUS);
        ball.setFill(Color.TRANSPARENT); // Делаем круг мяча прозрачным

        // Создаем и настраиваем ImageView для мяча
        ballImageView = new ImageView(ballImage);
        ballImageView.setFitWidth(BALL_RADIUS * 2);
        ballImageView.setFitHeight(BALL_RADIUS * 2);
        ballImageView.setX(ball.getCenterX() - BALL_RADIUS);
        ballImageView.setY(ball.getCenterY() - BALL_RADIUS);

        leftGoal = new Rectangle(0, WINDOW_HEIGHT-GoalsHight, WINDOW_WIDTH/75, GoalsHight);
        leftGoal.setFill(Color.GRAY);

        rightGoal = new Rectangle(WINDOW_WIDTH*74/75, WINDOW_HEIGHT-GoalsHight, WINDOW_WIDTH/75, GoalsHight);
        rightGoal.setFill(Color.GRAY);

        blueScoreText = new Text(WINDOW_WIDTH/30, WINDOW_HEIGHT/18, "Blue: "+blueScore);
        blueScoreText.setFont(Font.font(30));
        blueScoreText.setFill(Color.BLUE);

        redScoreText = new Text(WINDOW_WIDTH*27/30, WINDOW_HEIGHT/18, "Red: "+redScore);
        redScoreText.setFont(Font.font(30));
        redScoreText.setFill(Color.RED);

        timerText = new Text(WINDOW_WIDTH*7/15, WINDOW_HEIGHT/18, "Time: " + gameTime);
        timerText.setFont(Font.font(30));
        timerText.setFill(Color.BLACK);

        // Добавляем элементы в правильном порядке
        root.getChildren().addAll(
            blueCircle, redCircle, 
            bluePlayerImage, redPlayerImage,
            ball, ballImageView, leftGoal, rightGoal, 
            blueScoreText, redScoreText, timerText
        );

        Scene scene = new Scene(root, WINDOW_WIDTH, WINDOW_HEIGHT);

        // Обработка нажатия клавиш
        scene.setOnKeyPressed(event -> {
            // Обработка движения и прыжков
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

            // Обработка прыжков
            if (event.getCode() == KeyCode.W && !isBlueJumping) {
                jumpCircle(blueCircle, true);
            }
            if (event.getCode() == KeyCode.UP && !isRedJumping) {
                jumpCircle(redCircle, false);
            }

            if(event.getCode() == KeyCode.ESCAPE || event.getCode() == KeyCode.SPACE){
                PauseTimelines();
                primaryStage.setScene(createMenuScene(primaryStage));
            }
        });

        scene.setOnKeyReleased(event -> {
            // Обработка отпускания клавиш
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
        PauseTimelines();
        if (mediaPlayer != null) {
            mediaPlayer.stop();
        }
        Pane menuRoot = new Pane();

        ImageView background = new ImageView(menuBackgrounds[0]);
        background.setFitWidth(WINDOW_WIDTH);
        background.setFitHeight(WINDOW_HEIGHT);
        menuRoot.getChildren().add(background);

        Button MenuButton = new Button("Menu");
        MenuButton.setPrefSize(WINDOW_WIDTH*4/15, WINDOW_HEIGHT*1.5/9);
        MenuButton.setLayoutX(WINDOW_WIDTH*5.5/15);
        MenuButton.setLayoutY(WINDOW_HEIGHT*7/9);
        setButtonStyle(MenuButton);
        MenuButton.setOnAction(event -> {
            currentSoundtrackIndex = -1;
            primaryStage.setScene(createCutScene(primaryStage));
        });

        Button ContinueButton = new Button("Continue");
        ContinueButton.setLayoutX(WINDOW_WIDTH*5.5/15);
        ContinueButton.setLayoutY(WINDOW_HEIGHT*2/9);
        ContinueButton.setPrefSize(WINDOW_WIDTH*4/15, WINDOW_HEIGHT*1.5/9);
        setButtonStyle(ContinueButton);
        ContinueButton.setOnAction(event -> {
            PlayTimelines();
            primaryStage.setScene(createGameScene(primaryStage));
            FladsReset();
        });

        Button RestartButton = new Button("Restart");
        RestartButton.setLayoutX(WINDOW_WIDTH*5.5/15);
        RestartButton.setLayoutY(WINDOW_HEIGHT*4.5/9);
        RestartButton.setPrefSize(WINDOW_WIDTH*4/15, WINDOW_HEIGHT*1.5/9);
        setButtonStyle(RestartButton);
        RestartButton.setOnAction(event -> {
            setRestartGameSetting();
            primaryStage.setScene(createGameScene(primaryStage));
        });

        Text scoreText = new Text(WINDOW_WIDTH*6.25/15, WINDOW_HEIGHT*1.5/9, "");
        scoreText.setFont(Font.font(40));

        if (gameTime > 0) {
            scoreText.setText("Blue " + blueScore + " : " + redScore + " Red");
            scoreText.setFill(Color.BLACK);
            scoreText.setX(WINDOW_WIDTH*6/15);
        } else {
            if (blueScore > redScore) {
                scoreText.setText("BLUE WIN");
                scoreText.setFill(Color.BLUE);
                scoreText.setX(WINDOW_WIDTH*6.4/15);
            } else if (blueScore < redScore) {
                scoreText.setText("RED WIN");
                scoreText.setFill(Color.RED);
                scoreText.setX(WINDOW_WIDTH*6.4/15);
            } else {
                scoreText.setText("DRAW");
                scoreText.setFill(Color.BLACK);
                scoreText.setX(WINDOW_WIDTH*6.5/15);
            }
        }

        menuRoot.getChildren().addAll(scoreText, MenuButton);
        if(gameTime >= 1) menuRoot.getChildren().add(ContinueButton);
        menuRoot.getChildren().add(RestartButton);

        Scene scene = new Scene(menuRoot, WINDOW_WIDTH, WINDOW_HEIGHT);

        scene.setOnKeyPressed(event -> {
            KeyCode code = event.getCode();
            
            if (code == KeyCode.ESCAPE) {
                PlayTimelines();
                primaryStage.setScene(createGameScene(primaryStage));
                FladsReset();
                return;
            }

            if (code == KeyCode.UP) {
                if (RestartButton.isFocused()) {
                    if (gameTime >= 1) ContinueButton.requestFocus();
                } else if (MenuButton.isFocused()) {
                    RestartButton.requestFocus();
                }
            } else if (code == KeyCode.DOWN) {
                if (ContinueButton.isFocused()) {
                    RestartButton.requestFocus();
                } else if (RestartButton.isFocused()) {
                    MenuButton.requestFocus();
                }
            } else if (code == KeyCode.ENTER || code == KeyCode.SPACE) {
                if (ContinueButton.isFocused()) {
                    PlayTimelines();
                    primaryStage.setScene(createGameScene(primaryStage));
                    FladsReset();
                } else if (RestartButton.isFocused()) {
                    setRestartGameSetting();
                    primaryStage.setScene(createGameScene(primaryStage));
                } else if (MenuButton.isFocused()) {
                    primaryStage.setScene(createCutScene(primaryStage));
                }
            }
        });

        if (gameTime >= 1) {
            ContinueButton.requestFocus();
        } else {
            RestartButton.requestFocus();
        }

        return scene;
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

    private void startMovementLoop() {
        movementTimeline = new Timeline(new KeyFrame(Duration.millis(16), event -> {
            // Обновляем позицию синего круга
            if (isBlueMovingLeft) {
                moveCircle(blueCircle, -PLAYER_SPEED, 0, redCircle);
            }
            if (isBlueMovingRight) {
                moveCircle(blueCircle, PLAYER_SPEED, 0, redCircle);
            }

            // Обновляем позицию красного круга
            if (isRedMovingLeft) {
                moveCircle(redCircle, -PLAYER_SPEED, 0, blueCircle);
            }
            if (isRedMovingRight) {
                moveCircle(redCircle, PLAYER_SPEED, 0, blueCircle);
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
            checkBallEnergy();
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
        if (mediaPlayer != null) {
            mediaPlayer.stop();
        }
    }

    private void PauseTimelines(){
        if(movementTimeline!=null) {
            movementTimeline.pause();
        }
        if(timerTimeline!=null){
            timerTimeline.pause();
        }
        if (mediaPlayer != null) {
            mediaPlayer.pause();
        }
    }

    private void PlayTimelines(){
        if(movementTimeline!=null) {
            movementTimeline.play();
        }
        if(timerTimeline!=null){
            timerTimeline.play();
        }
        if (mediaPlayer != null && isMusicEnabled) {
            mediaPlayer.play();
        }
    }

    private Scene createCutScene(Stage primaryStage) {
        StopTimelines();
        if (mediaPlayer != null) {
            mediaPlayer.stop();
        }
        Pane CutSceneRoot = new Pane();

        // Используем BACKGROUND_MENU_1
        ImageView background = new ImageView(menuBackgrounds[0]);
        background.setFitWidth(WINDOW_WIDTH);
        background.setFitHeight(WINDOW_HEIGHT);
        CutSceneRoot.getChildren().add(background);

        Text WelcomeText = new Text("SOUTH FOOTBALL");
        WelcomeText.setX(WINDOW_WIDTH*5.5/15);
        WelcomeText.setY(WINDOW_HEIGHT*2/10);
        WelcomeText.setFont(Font.font(40));

        Button PlayButton = new Button("Play");
        PlayButton.setLayoutX(WINDOW_WIDTH*5.5/15);
        PlayButton.setLayoutY(WINDOW_HEIGHT*3/10);
        PlayButton.setPrefSize(WINDOW_WIDTH*4/15, WINDOW_HEIGHT*1.5/9);
        setButtonStyle(PlayButton);
        PlayButton.setOnAction(event -> primaryStage.setScene(createGameModeScene(primaryStage)));

        Button SettingButton = new Button("Settings");
        SettingButton.setLayoutX(WINDOW_WIDTH*5.5/15);
        SettingButton.setLayoutY(WINDOW_HEIGHT*5/10);
        SettingButton.setPrefSize(WINDOW_WIDTH*4/15, WINDOW_HEIGHT*1.5/9);
        setButtonStyle(SettingButton);
        SettingButton.setOnAction(event -> primaryStage.setScene(createSettingScene(primaryStage)));

        Button CloseButton = new Button("Close");
        CloseButton.setLayoutX(WINDOW_WIDTH*5.5/15);
        CloseButton.setLayoutY(WINDOW_HEIGHT*7/10);
        CloseButton.setPrefSize(WINDOW_WIDTH*4/15, WINDOW_HEIGHT*1.5/9);
        setButtonStyle(CloseButton);
        CloseButton.setOnAction(event -> System.exit(0));

        CutSceneRoot.getChildren().addAll(PlayButton, WelcomeText, SettingButton, CloseButton);

        Scene scene = new Scene(CutSceneRoot, WINDOW_WIDTH, WINDOW_HEIGHT);

        // Добавляем обработку клавиш для навигации между кнопками
        scene.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.UP) {
                if (SettingButton.isFocused()) {
                    PlayButton.requestFocus();
                } else if (CloseButton.isFocused()) {
                    SettingButton.requestFocus();
                }
            } else if (event.getCode() == KeyCode.DOWN) {
                if (PlayButton.isFocused()) {
                    SettingButton.requestFocus();
                } else if (SettingButton.isFocused()) {
                    CloseButton.requestFocus();
                }
            } else if (event.getCode() == KeyCode.ENTER || event.getCode() == KeyCode.SPACE) {
                if (PlayButton.isFocused()) {
                    primaryStage.setScene(createGameModeScene(primaryStage));
                } else if (SettingButton.isFocused()) {
                    primaryStage.setScene(createSettingScene(primaryStage));
                } else if (CloseButton.isFocused()) {
            System.exit(0);
                }
            }
        });

        // Восстанавливаем обработчики событий для кнопок
        PlayButton.setOnAction(event -> primaryStage.setScene(createGameModeScene(primaryStage)));
        SettingButton.setOnAction(event -> primaryStage.setScene(createSettingScene(primaryStage)));
        CloseButton.setOnAction(event -> System.exit(0));

        // Устанавливаем начальный фокус на кнопку Play
        PlayButton.requestFocus();

        return scene;
    }

    private void setButtonStyle(Button button) {
        button.setStyle("-fx-background-color: transparent; -fx-text-fill: black; -fx-border-color: black; -fx-border-width: 2px; -fx-font-size: 24px;");

        button.setOnMouseEntered(e -> button.setStyle("-fx-background-color: rgba(255, 255, 255, 0.3); -fx-text-fill: black; -fx-border-color: black; -fx-border-width: 2px; -fx-font-size: 24px;"));

        button.setOnMouseExited(e -> {
            if (!button.isFocused()) {
                button.setStyle("-fx-background-color: transparent; -fx-text-fill: black; -fx-border-color: black; -fx-border-width: 2px; -fx-font-size: 24px;");
            } else {
                button.setStyle("-fx-background-color: rgba(255, 255, 255, 0.5); -fx-text-fill: black; -fx-border-color: black; -fx-border-width: 3px; -fx-font-size: 24px;");
            }
        });

        button.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                button.setStyle("-fx-background-color: rgba(255, 255, 255, 0.5); -fx-text-fill: black; -fx-border-color: black; -fx-border-width: 3px; -fx-font-size: 24px;");
            } else {
                button.setStyle("-fx-background-color: transparent; -fx-text-fill: black; -fx-border-color: black; -fx-border-width: 2px; -fx-font-size: 24px;");
            }
        });
    }

    private Scene createSettingScene(Stage primaryStage) {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
        }
        Pane settingRoot = new Pane();

        // Используем BACKGROUND_SETTINGS_1
        ImageView background = new ImageView(settingsBackground);
        background.setFitWidth(WINDOW_WIDTH);
        background.setFitHeight(WINDOW_HEIGHT);
        settingRoot.getChildren().add(background);

        // Кнопка возврата
        Button backButton = new Button("Back");
        backButton.setPrefSize(WINDOW_WIDTH/10, WINDOW_HEIGHT/15);
        backButton.setLayoutX(WINDOW_WIDTH/30);
        backButton.setLayoutY(WINDOW_HEIGHT/30);
        setButtonStyle(backButton);
        backButton.setOnAction(event -> primaryStage.setScene(createCutScene(primaryStage)));

        // Заголовок настроек
        Text settingsTitle = new Text("Game Settings");
        settingsTitle.setFont(Font.font(40));
        settingsTitle.setX(WINDOW_WIDTH/4);
        settingsTitle.setY(WINDOW_HEIGHT/6);

        // Кнопка включения/выключения музыки
        Button musicToggleButton = new Button(isMusicEnabled ? "Music: ON" : "Music: OFF");
        musicToggleButton.setPrefSize(WINDOW_WIDTH/4, WINDOW_HEIGHT/8);
        musicToggleButton.setLayoutX(WINDOW_WIDTH*2/3);
        musicToggleButton.setLayoutY(WINDOW_HEIGHT*1.3/6);
        setButtonStyle(musicToggleButton);
        musicToggleButton.setOnAction(event -> {
            isMusicEnabled = !isMusicEnabled;
            musicToggleButton.setText(isMusicEnabled ? "Music: ON" : "Music: OFF");
            if (isMusicEnabled) {
                playRandomSoundtrack();
            } else if (mediaPlayer != null) {
                mediaPlayer.stop();
            }
        });

        // Управление синим игроком
        Text bluePlayerTitle = new Text("Blue Player (WASD):");
        bluePlayerTitle.setFont(Font.font(30));
        bluePlayerTitle.setFill(Color.BLUE);
        bluePlayerTitle.setX(WINDOW_WIDTH/4);
        bluePlayerTitle.setY(WINDOW_HEIGHT*2.5/9);

        Text blueControls = new Text(
                "W - Прыжок\n" +
                        "A - Движение влево\n" +
                        "D - Движение вправо\n" +
                        "S - Удар по мячу");
        blueControls.setFont(Font.font(25));
        blueControls.setX(WINDOW_WIDTH/4);
        blueControls.setY(WINDOW_HEIGHT*3/9);

        // Управление красным игроком
        Text redPlayerTitle = new Text("Red Player (Arrow Keys):");
        redPlayerTitle.setFont(Font.font(30));
        redPlayerTitle.setFill(Color.RED);
        redPlayerTitle.setX(WINDOW_WIDTH/4);
        redPlayerTitle.setY(WINDOW_HEIGHT*5/9);

        Text redControls = new Text(
                "↑ - Прыжок\n" +
                        "← - Движение влево\n" +
                        "→ - Движение вправо\n" +
                        "↓ - Удар по мячу");
        redControls.setFont(Font.font(25));
        redControls.setX(WINDOW_WIDTH/4);
        redControls.setY(WINDOW_HEIGHT*5.5/9);

        // Общая информация
        Text generalInfo = new Text("ESC/Пробел - Пауза\n");
        generalInfo.setFont(Font.font(25));
        generalInfo.setX(WINDOW_WIDTH/4);
        generalInfo.setY(WINDOW_HEIGHT*7.5/9);

        settingRoot.getChildren().addAll(
                backButton, settingsTitle, musicToggleButton,
                bluePlayerTitle, blueControls,
                redPlayerTitle, redControls,
                generalInfo
        );

        Scene scene = new Scene(settingRoot, WINDOW_WIDTH, WINDOW_HEIGHT);

        // Добавляем обработку клавиш
        scene.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER || event.getCode() == KeyCode.SPACE ||
                event.getCode() == KeyCode.ESCAPE) {
                primaryStage.setScene(createCutScene(primaryStage));
            }
        });

        // Устанавливаем фокус на кнопку Back при создании сцены
        backButton.requestFocus();

        return scene;
    }

    // Метод для проверки попадания мяча в ворота
    private void checkGoal() {
        double ballX = ball.getCenterX();
        double ballY = ball.getCenterY();

        // Проверка левых ворот
        if (ballX - BALL_RADIUS - 3  <= leftGoal.getWidth()) {
            // Проверяем, находится ли мяч в пределах высоты ворот
            if (ballY >= leftGoal.getY() && ballY <= leftGoal.getY() + leftGoal.getHeight()) {
                redScore++; // Красный игрок забил гол
                updateScore();
                resetBallRed();
            }
        }

        // Проверка правых ворот
        if (ballX + BALL_RADIUS +  3 >= rightGoal.getX()) {
            // Проверяем, находится ли мяч в пределах высоты ворот
            if (ballY >= rightGoal.getY() && ballY <= rightGoal.getY() + rightGoal.getHeight()) {
                blueScore++; // Синий игрок забил гол
                updateScore();
                resetBallBlue();
            }
        }
    }

    private int BallRandomSpeedX(int Random){
        return Random % 2 == 0 ? 1 : -1;
    }

    // Метод для обновления счета на экране
    private void updateScore() {
        blueScoreText.setText("Blue: " + blueScore);
        redScoreText.setText("Red: " + redScore);
    }

    private void resetBall(){
        if (blueCircle != null && redCircle != null) {
            double safeDistanceFromEdge = PLAYER_RADIUS + 10;

            blueCircle.setCenterX(safeDistanceFromEdge);
            blueCircle.setCenterY(WINDOW_HEIGHT - PLAYER_RADIUS);
            if (bluePlayerImage != null) {
                bluePlayerImage.setX(safeDistanceFromEdge - PLAYER_RADIUS);
                bluePlayerImage.setY(WINDOW_HEIGHT - PLAYER_RADIUS * 2);
            }

            redCircle.setCenterX(WINDOW_WIDTH - safeDistanceFromEdge);
            redCircle.setCenterY(WINDOW_HEIGHT - PLAYER_RADIUS);
            if (redPlayerImage != null) {
                redPlayerImage.setX(WINDOW_WIDTH - safeDistanceFromEdge - PLAYER_RADIUS);
                redPlayerImage.setY(WINDOW_HEIGHT - PLAYER_RADIUS * 2);
            }

            isRedJumping = false;
            isBlueJumping = false;
        }
    }

    private void resetBallToCenter() {
        if (ball != null) {
            ball.setCenterX(WINDOW_WIDTH/2);
            ball.setCenterY(WINDOW_HEIGHT*6/9);
            ballVelocityX = BALL_SPEED_X * (Math.random() < 0.5 ? 1 : -1);
            ballVelocityY = BALL_SPEED_Y;
        }
    }

    private void resetBallRed() {
        resetBall();
        resetBallToCenter();
    }

    private void resetBallBlue() {
        resetBall();
        resetBallToCenter();
    }

    private void moveCircle(Circle circle, double deltaX, double deltaY, Circle otherCircle) {
        double newX = circle.getCenterX() + deltaX;
        double newY = circle.getCenterY() + deltaY;

        // Проверяем границы экрана с учетом радиуса игрока
        if (newX - PLAYER_RADIUS < 0 || newX + PLAYER_RADIUS > WINDOW_WIDTH) {
            return;
        }

        // Проверяем коллизию с другим кругом
        if (!checkCircleCollision(circle, newX, newY, otherCircle)) {
            circle.setCenterX(newX);
            circle.setCenterY(newY);

            // Обновляем позицию изображения игрока
            if (circle == blueCircle) {
                bluePlayerImage.setX(newX - PLAYER_RADIUS);
                bluePlayerImage.setY(newY - PLAYER_RADIUS);
            } else {
                redPlayerImage.setX(newX - PLAYER_RADIUS);
                redPlayerImage.setY(newY - PLAYER_RADIUS);
            }
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
        if (newX - BALL_RADIUS <= 0 || newX + BALL_RADIUS >= WINDOW_WIDTH) {
            ballVelocityX = -ballVelocityX;
            BallBouncedWall();
            newX = ball.getCenterX() + ballVelocityX; // Корректируем позицию
        }
        if (newY - BALL_RADIUS <= 0 || newY + BALL_RADIUS >= WINDOW_HEIGHT) {
            ballVelocityY = -ballVelocityY;
            BallBouncedWall();
            newY = ball.getCenterY() + ballVelocityY;// Корректируем позицию
        }
        if(newY - BALL_RADIUS <= 0){
            ball.setCenterY(WINDOW_HEIGHT-BALL_RADIUS);
        }

        // Проверка столкновения с игроками
        if (checkBallCollision(ball, blueCircle)) {
            calculateNewBallVelocity(ball, blueCircle);
            BallBouncedPlayer();
            newX = ball.getCenterX() + ballVelocityX; // Корректируем позицию
            newY = ball.getCenterY() + ballVelocityY;
        }
        if (checkBallCollision(ball, redCircle)) {
            calculateNewBallVelocity(ball, redCircle);
            BallBouncedPlayer();
            newX = ball.getCenterX() + ballVelocityX; // Корректируем позицию
            newY = ball.getCenterY() + ballVelocityY;
        }

        // Обновление позиции мяча
        ball.setCenterX(newX);
        ball.setCenterY(newY);
        // Обновление позиции изображения мяча
        ballImageView.setX(newX - BALL_RADIUS);
        ballImageView.setY(newY - BALL_RADIUS);
    }

    private void BallBouncedWall(){
        ballVelocityX*=WallBounceRatio;
        ballVelocityY*=WallBounceRatio;
    }

    private void BallBouncedPlayer(){
        ballVelocityY*=PlayerBounceRatio;
        ballVelocityX*=PlayerBounceRatio;
    }

    private void BallKicked(){
        // Увеличиваем скорость мяча после удара
        double currentSpeed = Math.sqrt(ballVelocityX * ballVelocityX + ballVelocityY * ballVelocityY);
        double maxKickSpeed = BALL_SPEED_X * 3; // Максимальная скорость после удара

        if (currentSpeed > maxKickSpeed) {
            // Если скорость превышает максимальную, нормализуем её
            double ratio = maxKickSpeed / currentSpeed;
            ballVelocityX *= ratio;
            ballVelocityY *= ratio;
        }
    }

    private void FladsReset(){
        isBlueMovingLeft=false;
        isBlueMovingRight=false;
        isBlueJumping=false;
        isBlueKicking=false;
        isRedMovingLeft=false;
        isRedMovingRight=false;
        isRedJumping=false;
        isRedKicking=false;
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

        // Вычисляем скалярное произведение скорости мяча на нормаль
        double dotProduct = ballVelocityX * nx + ballVelocityY * ny;

        // Если мяч движется к игроку (dotProduct < 0), отражаем его
        if (dotProduct < 0) {
            // Новая скорость мяча после отскока
            ballVelocityX = ballVelocityX - 2 * dotProduct * nx;
            ballVelocityY = ballVelocityY - 2 * dotProduct * ny;

            // Добавляем небольшой импульс отскока
            double bounceImpulse = 2.0;
            ballVelocityX += nx * bounceImpulse;
            ballVelocityY += ny * bounceImpulse;
        }

        // Корректируем позицию мяча, чтобы он не застрял внутри игрока
        double overlap = (ball.getRadius() + player.getRadius()) - distance;
        if (overlap > 0) {
            // Выталкиваем мяч на безопасное расстояние
            ball.setCenterX(ball.getCenterX() + nx * (overlap + 5)); // Увеличиваем отступ
            ball.setCenterY(ball.getCenterY() + ny * (overlap + 5));
        }

        // Ограничиваем максимальную скорость мяча
        double speed = Math.sqrt(ballVelocityX * ballVelocityX + ballVelocityY * ballVelocityY);
        double maxSpeed = BALL_SPEED_X * 2;
        if (speed > maxSpeed) {
            ballVelocityX = (ballVelocityX / speed) * maxSpeed;
            ballVelocityY = (ballVelocityY / speed) * maxSpeed;
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

    private void checkBallEnergy(){
        double ballEnergy = ballVelocityX * ballVelocityX + ballVelocityY * ballVelocityY;
        double maxEnergy = BALL_MAXENERGY * 1.2; // Увеличиваем максимальную энергию мяча

        if (ballEnergy >= maxEnergy) {
            double ratio = Math.sqrt(maxEnergy / ballEnergy);
            ballVelocityX *= ratio;
            ballVelocityY *= ratio;
        }
    }

    private void jumpCircle(Circle circle, boolean isBlue) {
        if (isBlue) {
            if (!isBlueJumping) {
                isBlueJumping = true;
                blueJumpVelocity = JumpVelocity; // Начальная скорость прыжка
            }
        } else {
            if (!isRedJumping) {
                isRedJumping = true;
                redJumpVelocity = JumpVelocity; // Начальная скорость прыжка
            }
        }
    }

    private void Jump(Circle circle, boolean isBlue) {
        if (isBlue) {
            if (isBlueJumping) {
                double newY = circle.getCenterY() + blueJumpVelocity;
                blueJumpVelocity += PlayerGRAVITY;

                // Проверяем, достиг ли игрок земли
                if (newY >= WINDOW_HEIGHT - PLAYER_RADIUS) {
                    newY = WINDOW_HEIGHT - PLAYER_RADIUS;
                    blueJumpVelocity = 0;
                    isBlueJumping = false;
                }

                circle.setCenterY(newY);
                bluePlayerImage.setY(newY - PLAYER_RADIUS);
            }
        } else {
            if (isRedJumping) {
                double newY = circle.getCenterY() + redJumpVelocity;
                redJumpVelocity += PlayerGRAVITY;

                // Проверяем, достиг ли игрок земли
                if (newY >= WINDOW_HEIGHT - PLAYER_RADIUS) {
                    newY = WINDOW_HEIGHT - PLAYER_RADIUS;
                    redJumpVelocity = 0;
                    isRedJumping = false;
                }

                circle.setCenterY(newY);
                redPlayerImage.setY(newY - PLAYER_RADIUS);
            }
        }
    }

    private void kickBall(boolean isBlueKick) {
        double kickPower = PLAYER_SPEED * 2.0; // Увеличиваем мощность удара

        if (isBlueKick) {
            // Проверяем, находится ли мяч в зоне удара
            double distance = Math.sqrt(
                    Math.pow(ball.getCenterX() - blueCircle.getCenterX(), 2) +
                            Math.pow(ball.getCenterY() - blueCircle.getCenterY(), 2)
            );

            if (distance <= PLAYER_RADIUS + BALL_RADIUS + KICK_DISTANCE) {
                // Мяч получает сильный горизонтальный импульс
                ballVelocityX = Math.abs(ballVelocityX) + kickPower;
                ballVelocityY = 0; // Мяч летит только горизонтально
                BallKicked();
            }
        } else {
            // Проверяем, находится ли мяч в зоне удара
            double distance = Math.sqrt(
                    Math.pow(ball.getCenterX() - redCircle.getCenterX(), 2) +
                            Math.pow(ball.getCenterY() - redCircle.getCenterY(), 2)
            );

            if (distance <= PLAYER_RADIUS + BALL_RADIUS + KICK_DISTANCE) {
                // Мяч получает сильный горизонтальный импульс
                ballVelocityX = -(Math.abs(ballVelocityX) + kickPower);
                ballVelocityY = 0; // Мяч летит только горизонтально
                BallKicked();
            }
        }
    }

    private Scene createGameModeScene(Stage primaryStage){
        if (mediaPlayer != null) {
            mediaPlayer.stop();
        }
        Pane GameModeRoot = new Pane();

        // Используем BACKGROUND_MENU_1
        ImageView background = new ImageView(menuBackgrounds[0]);
        background.setFitWidth(WINDOW_WIDTH);
        background.setFitHeight(WINDOW_HEIGHT);
        GameModeRoot.getChildren().add(background);

        Button ClassicModeButton = new Button("Classic Mode");
        ClassicModeButton.setLayoutX(WINDOW_WIDTH*4/12);
        ClassicModeButton.setLayoutY(WINDOW_HEIGHT*2/9);
        ClassicModeButton.setPrefSize(WINDOW_WIDTH*4/12, WINDOW_HEIGHT*2.5/9);
        setButtonStyle(ClassicModeButton);
        ClassicModeButton.setOnAction(event -> {
            setClassicModeSettings();
            setRestartGameSetting();
            primaryStage.setScene(createGameScene(primaryStage));
        });

        Button RandomModeButton = new Button("Random Mode");
        RandomModeButton.setLayoutX(WINDOW_WIDTH*4/12);
        RandomModeButton.setLayoutY(WINDOW_HEIGHT*5/9);
        RandomModeButton.setPrefSize(WINDOW_WIDTH*4/12, WINDOW_HEIGHT*2.5/9);
        setButtonStyle(RandomModeButton);
        RandomModeButton.setOnAction(event -> {
            setRandomModeSettings();
            setRestartGameSetting();
            bluePlayerImage = null; // Сбрасываем изображения игроков
            redPlayerImage = null;
            primaryStage.setScene(createGameScene(primaryStage));
        });

        GameModeRoot.getChildren().addAll(ClassicModeButton, RandomModeButton);

        Scene scene = new Scene(GameModeRoot, WINDOW_WIDTH, WINDOW_HEIGHT);

        // Добавляем обработку клавиш для навигации между кнопками
        scene.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.UP) {
                if (RandomModeButton.isFocused()) {
                    ClassicModeButton.requestFocus();
                }
            } else if (event.getCode() == KeyCode.DOWN) {
                if (ClassicModeButton.isFocused()) {
                    RandomModeButton.requestFocus();
                }
            } else if (event.getCode() == KeyCode.ENTER || event.getCode() == KeyCode.SPACE) {
                if (ClassicModeButton.isFocused()) {
                    setClassicModeSettings();
            setRestartGameSetting();
            primaryStage.setScene(createGameScene(primaryStage));
                } else if (RandomModeButton.isFocused()) {
                    setRandomModeSettings();
                    setRestartGameSetting();
                    primaryStage.setScene(createGameScene(primaryStage));
                }
            }
        });

        // Устанавливаем начальный фокус на кнопку Classic Mode
        ClassicModeButton.requestFocus();

        return scene;
    }

    private void setClassicModeSettings(){
        GoalsHight=WINDOW_HEIGHT*5/9;
        BALL_RADIUS=25;
        PLAYER_RADIUS=50;
        BALL_MAXENERGY=450;
        WallBounceRatio=0.95;
        PlayerBounceRatio=1.2;
        KickingPowerRatio=1.4;
        GRAVITY = 0.5;
        PlayerGRAVITY=1;
        JumpVelocity = -22;
    }

    private void setRandomModeSettings(){
        int randomMode = (int)(Math.random() * 5);

        switch (randomMode) {
            case 0:
                GRAVITY = 0.3;
                PlayerGRAVITY = 0.6;
                GoalsHight = WINDOW_HEIGHT * 2/3;
                BALL_RADIUS = 20;
                PLAYER_RADIUS = 50;
                break;
            case 1:
                GRAVITY = 0.8;
                PlayerGRAVITY = 1.5;
                GoalsHight = WINDOW_HEIGHT * 1/3;
                BALL_RADIUS = 20;
                PLAYER_RADIUS = 40;
                break;
            case 2:
                GRAVITY = 0.5;
                PlayerGRAVITY = 1;
                GoalsHight = WINDOW_HEIGHT * 2/3;
                BALL_RADIUS = 30;
                PLAYER_RADIUS = 50;
                break;
            case 3:
                GRAVITY = 0.5;
                PlayerGRAVITY = 1;
                GoalsHight = WINDOW_HEIGHT * 1/3;
                BALL_RADIUS = 15;
                PLAYER_RADIUS = 40;
                break;
            case 4:
                GRAVITY = 0.5;
                PlayerGRAVITY = 1;
                GoalsHight = WINDOW_HEIGHT * 4/9;
                BALL_RADIUS = 20;
                PLAYER_RADIUS = 50;
                break;
        }

        BALL_MAXENERGY = 450;
        WallBounceRatio = 0.95;
        PlayerBounceRatio = 1.2;
        KickingPowerRatio = 1.4;
        JumpVelocity = -22;
    }

    private void setRestartGameSetting(){
        gameTime = 60;
        redScore = 0;
        blueScore = 0;
        FladsReset();
        currentGameBackgroundIndex = (int)(Math.random() * 3);
        bluePlayerImage = null;
        redPlayerImage = null;
        currentSoundtrackIndex = -1;
    }

    public static void main(String[] args) {
        launch(args);
    }
}