package com.example.snakegame;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Random;

public class GameView extends SurfaceView implements SurfaceHolder.Callback, Runnable {

    private Thread gameThread;
    private volatile boolean running = false;
    private final SurfaceHolder holder;

    // velocidad de la serpiente
    private long moveDelayMillis = 150;
    private long lastMoveTime = 0;

    // grilla del mapa
    private final int cellSize = 40;
    private int numColumns;
    private int numRows;

    // serpiente
    private final LinkedList<Point> snake = new LinkedList<>();
    private Direction currentDirection = Direction.RIGHT;
    private Direction nextDirection = Direction.RIGHT;

    // "comida"
    private final ArrayList<Point> foods = new ArrayList<>();
    private final Random random = new Random();
    private static final int MAX_FOODS = 3;

    // estado juego
    private boolean gameOver = false;
    private int score = 0;

    // pintura
    private final Paint paint = new Paint();
    private int snakeColor = Color.GREEN;

    // controles con swipe
    private float touchStartX, touchStartY;
    private static final int SWIPE_THRESHOLD = 60;

    private enum Direction {UP, DOWN, LEFT, RIGHT}

    public GameView(Context context) {
        super(context);
        holder = getHolder();
        holder.addCallback(this);
        setFocusable(true);

        //  velocidad y color configurado de opciones
        SharedPreferences prefs = context.getSharedPreferences("SnakePrefs", Context.MODE_PRIVATE);
        moveDelayMillis = prefs.getLong("speed_delay", 150);
        snakeColor = prefs.getInt("snake_color", Color.GREEN);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        numColumns = getWidth() / cellSize;
        numRows = getHeight() / cellSize;
        initGame();
        resume();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        pause();
    }

    // pause y resume

    public void resume() {
        running = true;
        gameThread = new Thread(this);
        gameThread.start();
    }

    public void pause() {
        running = false;
        try {
            if (gameThread != null) {
                gameThread.join();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    // loop juego
    @Override
    public void run() {
        while (running) {
            long now = System.currentTimeMillis();

            if (!gameOver && now - lastMoveTime >= moveDelayMillis) {
                update();
                lastMoveTime = now;
            }

            draw();
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    // iniciar y reiniciar juego
    private void initGame() {
        snake.clear();
        currentDirection = Direction.RIGHT;
        nextDirection = Direction.RIGHT;
        gameOver = false;
        score = 0;

        int startCol = numColumns / 2;
        int startRow = numRows / 2;
        snake.add(new Point(startCol, startRow));
        snake.add(new Point(startCol - 1, startRow));
        snake.add(new Point(startCol - 2, startRow));

        foods.clear();
        spawnFood();
    }

    private void spawnFood() {
        if (numColumns <= 0 || numRows <= 0) return;

        while (foods.size() < MAX_FOODS) {
            Point newFood;
            do {
                int col = random.nextInt(numColumns);
                int row = random.nextInt(numRows);
                newFood = new Point(col, row);
            } while (snake.contains(newFood) || foods.contains(newFood));

            foods.add(newFood);
        }
    }

    // tick del juego
    private void update() {
        currentDirection = nextDirection;

        Point head = snake.getFirst();
        Point newHead;

        switch (currentDirection) {
            case UP:
                newHead = new Point(head.x, head.y - 1);
                break;
            case DOWN:
                newHead = new Point(head.x, head.y + 1);
                break;
            case LEFT:
                newHead = new Point(head.x - 1, head.y);
                break;
            case RIGHT:
            default:
                newHead = new Point(head.x + 1, head.y);
                break;
        }

        // Colisión con los bordes
        if (newHead.x < 0 || newHead.x >= numColumns || newHead.y < 0 || newHead.y >= numRows) {
            gameOver = true;
            return;
        }

        // Colisión con el propio cuerpo
        if (snake.contains(newHead)) {
            gameOver = true;
            return;
        }

        snake.addFirst(newHead);

        boolean ateFood = false;
        for (int i = 0; i < foods.size(); i++) {
            if (newHead.equals(foods.get(i))) {
                foods.remove(i);
                ateFood = true;
                break;
            }
        }

        if (ateFood) {
            score++;
            spawnFood();
        } else {
            snake.removeLast();
        }
    }

    // Dibujo

    private void draw() {
        if (!holder.getSurface().isValid()) return;

        Canvas canvas = holder.lockCanvas();
        if (canvas == null) return;

        try {
            canvas.drawColor(Color.BLACK);
            paint.setColor(Color.RED);
            for (Point f : foods) {
                canvas.drawRect(
                        f.x * cellSize,
                        f.y * cellSize,
                        f.x * cellSize + cellSize,
                        f.y * cellSize + cellSize,
                        paint
                );
            }

            // Serpiente
            paint.setColor(snakeColor);
            for (Point p : snake) {
                canvas.drawRect(
                        p.x * cellSize,
                        p.y * cellSize,
                        p.x * cellSize + cellSize,
                        p.y * cellSize + cellSize,
                        paint
                );
            }

            // Puntaje
            paint.setColor(Color.WHITE);
            paint.setTextSize(50);
            canvas.drawText("Puntaje: " + score, 20, 60, paint);

            // Game Over
            if (gameOver) {
                paint.setTextSize(80);
                paint.setColor(Color.WHITE);
                String msg = "GAME OVER";
                float textWidth = paint.measureText(msg);
                canvas.drawText(msg, (canvas.getWidth() - textWidth) / 2f, canvas.getHeight() / 2f, paint);
            }

        } finally {
            holder.unlockCanvasAndPost(canvas);
        }
    }


    // Control por swipe


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                touchStartX = event.getX();
                touchStartY = event.getY();

                if (gameOver) {
                    initGame();
                }
                return true;

            case MotionEvent.ACTION_UP:
                float deltaX = event.getX() - touchStartX;
                float deltaY = event.getY() - touchStartY;

                if (Math.max(Math.abs(deltaX), Math.abs(deltaY)) < SWIPE_THRESHOLD) {
                    return true;
                }

                if (Math.abs(deltaX) > Math.abs(deltaY)) {
                    if (deltaX > 0 && currentDirection != Direction.LEFT) {
                        nextDirection = Direction.RIGHT;
                    } else if (deltaX < 0 && currentDirection != Direction.RIGHT) {
                        nextDirection = Direction.LEFT;
                    }
                } else {
                    if (deltaY > 0 && currentDirection != Direction.UP) {
                        nextDirection = Direction.DOWN;
                    } else if (deltaY < 0 && currentDirection != Direction.DOWN) {
                        nextDirection = Direction.UP;
                    }
                }
                return true;
        }
        return super.onTouchEvent(event);
    }

    // botones

    public void setDirectionUp() {
        if (currentDirection != Direction.DOWN) nextDirection = Direction.UP;
    }

    public void setDirectionDown() {
        if (currentDirection != Direction.UP) nextDirection = Direction.DOWN;
    }

    public void setDirectionLeft() {
        if (currentDirection != Direction.RIGHT) nextDirection = Direction.LEFT;
    }

    public void setDirectionRight() {
        if (currentDirection != Direction.LEFT) nextDirection = Direction.RIGHT;
    }

    public int getScore() {
        return score;
    }
}