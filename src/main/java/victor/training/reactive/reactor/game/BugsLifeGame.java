package victor.training.reactive.reactor.game;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.media.AudioClip;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;
import victor.training.reactive.reactor.game.SillyRxScheduler;

import java.io.File;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class BugsLifeGame extends Application {

    public static final int BUG_SPEED = 2;
    private final Image tileImage = new Image(getResourceUri("GrassBlock.png"));
    private static final int screenWidth = 800;
    private static final int screenHeight = 600;
    private final Text scoreText = new Text();

    public static void main(String[] args) {
        Application.launch(BugsLifeGame.class);
    }

    private final ImageView sun = new ImageView();
    private final ImageView bug = new ImageView();


    @Override
    public void start(Stage stage) throws Exception {
        //  ^
        //  |
        // -dy
        //  |
        // (0,0) -----dx--->
        StackPane root = new StackPane();
        root.setAlignment(Pos.BOTTOM_LEFT);
        Scene scene = new Scene(root);

        root.getChildren().add(createSky());


        // TODO create an observer that fires every 10ms on the computation() thread,
        //  but its subscribers (+operators) run in the GUI event loop thread

//        Scheduler testScheduler = createTestScheduler(scene);
//        Observable<Long> time = Observable.interval(10, TimeUnit.MILLISECONDS, testScheduler)
//                .observeOn(new FxPlatformScheduler());
        Flux<Long> time = Flux.interval(Duration.ofMillis(10)).onBackpressureDrop()
                .publishOn(new SillyRxScheduler());





        // ====== TILES =======
        List<ImageView> tiles = createTiles();
        tiles.forEach(root.getChildren()::add);

        // TODO make every tile move left with BUG_SPEED, and then return from right when out (translateX <= -width)
//                    tile.setTranslateX(screenWidth - BUG_SPEED);

        time.subscribe(tick -> {
            for (ImageView tile : tiles) {
                if (tile.getTranslateX() <= -tileImage.getWidth()) {
                    tile.setTranslateX(screenWidth);
                } else {
                    tile.setTranslateX(tile.getTranslateX() - BUG_SPEED);
                }
            }
        });

        // ========== SUN ===========
        showHeart(false);
        sun.setTranslateY(-(screenHeight - 200));
        root.getChildren().add(sun);

        // TODO make sun move left with BUG_SPEED, and then return from right when out (translateX <= -width)

        time.subscribe(tick -> {
            if (sun.getTranslateX() <= -sun.getImage().getWidth()) {
                sun.setTranslateX(screenWidth);
            } else {
                sun.setTranslateX(sun.getTranslateX() - BUG_SPEED);
            }
        });

        // ========== BUG ===========
        double groundY = (-tileImage.getHeight() / 2) - 5;
        double gravity = 0.1;

        bug.setImage(new Image(getResourceUri("Bug.png")));
        bug.setTranslateY(groundY);
        bug.setTranslateX(screenHeight / 2);
        root.getChildren().add(bug);

        // ========== JUMPS ===========

        Sinks.Many<Double> sink = Sinks.many().multicast().onBackpressureBuffer();


        // TODO jump dynamics with gravity: remember Y decreases UPWARDS
        double jumpSpeed = 8;
        Flux<Double> jumpsFlux = sink.asFlux();
        jumpsFlux.flatMap(v0 -> time
                        .scan(jumpSpeed, (v, tick) -> v - gravity)
                        .takeUntilOther(jumpsFlux)
//                    .takeWhile(v -> v == jumpSpeed || bug.getTranslateY() != groundY)
        )
                .subscribe(dy -> {
//                    System.out.println(dy);
                    if (bug.getTranslateY() <= groundY + dy) {
                        bug.setTranslateY(bug.getTranslateY() - dy);
                    } else {
                        bug.setTranslateY(groundY);
                    }
                });

        // TODO on SPACE jump up with this speed
        // OPT play sound: new AudioClip(getResourceUri("smb3_jump.wav")).play()

        keyPresses(scene)
                .filter(e -> e.getCode() == KeyCode.SPACE)
                .filter(e -> bug.getTranslateY() == groundY)
                .subscribe(e -> sink.tryEmitNext(jumpSpeed));


        // TODO observable of positions: sun.localToScene(sun.getLayoutBounds());

        Flux<Bounds> sunPosition = time.map(tick -> sun.localToScene(sun.getLayoutBounds()));

        Flux<Bounds> bugPosition = time.map(tick -> bug.localToScene(bug.getLayoutBounds()));

        Flux<Boolean> enterExit = Flux.combineLatest(sunPosition, bugPosition, Bounds::intersects)
                .distinctUntilChanged()
//                .doOnNext(x -> System.out.println("NU"))
                ;


        enterExit.subscribe(this::showHeart);
        Flux<Boolean> hitObs = enterExit.filter(b -> b);
        hitObs.subscribe(enter -> new AudioClip(getResourceUri("smb3_coin.wav")).play());

        // TODO intersect the observable of positions, and fire only when they intersect. ONLY ONCE.
//        Observable<List<Boolean>> hitsObservable =



        // TODO increment and display a score: scoreText.setText("Score: " + count);

        hitObs
                .scan(0,(s,t)->s+1)
                .subscribe(s -> scoreText.setText("Score: " + s));

        scoreText.setFont(Font.font("Consolas", FontWeight.BOLD, 40));
        root.getChildren().add(scoreText);

        // TODO: on KeyCode.ESCAPE -> System.exit()

        keyPresses(scene)
                .filter(e -> e.getCode() == KeyCode.ESCAPE)
                .subscribe(event -> System.exit(0));

//        Observable.timer(2, TimeUnit.SECONDS)
//            .subscribe(t -> abonament.unsubscribe());

        stage.setOnShown(e -> new AudioClip(getResourceUri("smb3_power-up.wav")).play());
        stage.setTitle("A Bugs Life");
        stage.setScene(scene);
        stage.show();
    }


    private Canvas createSky() {
        Canvas sky = new Canvas(screenWidth, screenHeight);
        GraphicsContext context = sky.getGraphicsContext2D();
        context.setFill(Color.AZURE);
        context.fillRect(0, 0, screenWidth, screenHeight);
        return sky;
    }

    private List<ImageView> createTiles() {
        int nrTiles = 1 + (int) Math.ceil(screenWidth / tileImage.getWidth());
        List<ImageView> tiles = new ArrayList<>();
        for (int i = 0; i < nrTiles; i++) {
            ImageView tile = new ImageView();
            tile.setImage(tileImage);
            tile.setTranslateX(i * tileImage.getWidth());
            tiles.add(tile);
        }
        return tiles;
    }

    public void showHeart(boolean b) {
        if (b) {
            sun.setImage(new Image(getResourceUri("Heart.png")));
        } else {
            sun.setImage(new Image(getResourceUri("Star.png")));
        }
    }

    public static String getResourceUri(String name) {
        File file = new File("src/main/resources/bug/" + name);
        return file.toURI().toString();
    }

    public static Flux<KeyEvent> keyPresses(Scene scene) {
        return Flux.create(sink -> {
            EventHandler<KeyEvent> handler = sink::next;
            scene.addEventHandler(KeyEvent.KEY_PRESSED, handler);
            // TODO unregister
        });
    }


}
