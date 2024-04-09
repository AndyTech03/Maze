package com.example.maze;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.graphics.Point;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    TextView[][] _map;
    final int _width = 10;
    final int _height = 10;

    enum Direction{
        Left,
        Right,
        Up,
        Down;

        public static Direction getDirection(int i){
            switch (i){
                case 0:
                    return Left;
                case 1:
                    return Right;
                case 2:
                    return Up;
                case 3:
                    return Down;
                default:
                    throw new IllegalArgumentException("i должно быть в диапозоне [0; 3]!");
            }
        }

        public Direction invert(){
            switch (this){
                case Left:
                    return Right;
                case Right:
                    return Left;
                case Up:
                    return Down;
                default: // Down
                    return Up;
            }
        }

        public Point getPointOnDirection(Point current){
            Point result = new Point(current);
            switch (this){
                case Left:
                    result.x--;
                    break;
                case Right:
                    result.x++;
                    break;
                case Up:
                    result.y--;
                    break;
                default: // Down
                    result.y++;
                    break;
            }
            return result;
        }
    }

    boolean[][] _maze;
    boolean[][] _visited;
    Point _location;

    Random _random;

    final Point _finish = new Point(_width - 1, _height - 1);
    final Point _start = new Point(0, 0);
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        _random = new Random();
        List<TextView> cells = new ArrayList<>();
        cells.add(findViewById(R.id.cell11));
        cells.add(findViewById(R.id.cell12));
        cells.add(findViewById(R.id.cell13));
        cells.add(findViewById(R.id.cell21));
        cells.add(findViewById(R.id.cell22));
        cells.add(findViewById(R.id.cell23));
        cells.add(findViewById(R.id.cell31));
        cells.add(findViewById(R.id.cell32));
        cells.add(findViewById(R.id.cell33));
        findViewById(R.id.restartButton).setOnClickListener(v -> restartGame());

        int row = 0;
        int col = 0;
        _map = new TextView[3][3];
        for (TextView cell : cells) {
            _map[row][col] = cell;
            col++;
            if (col == 3){
                col = 0;
                row++;

            }
        }
        _map[0][1].setOnClickListener(v -> {
            makeMove(Direction.Up);
        });
        _map[1][0].setOnClickListener(v -> {
            makeMove(Direction.Left);
        });
        _map[1][2].setOnClickListener(v -> {
            makeMove(Direction.Right);
        });
        _map[2][1].setOnClickListener(v -> {
            makeMove(Direction.Down);
        });
        restartGame();
    }
    private void restartGame(){
        _location = new Point(0, 0);
        generateMaze();
        drawMap();
        showMessage("Удачи!", "Игра началась.");
    }

    private void makeMove(Direction direction) {
        Point nextLocation = direction.getPointOnDirection(_location);
        Log.i("MAZE_LOG", nextLocation + "|" + _location);
        if (
            nextLocation.x < 0 || nextLocation.y < 0 ||
            nextLocation.x > _width - 1 || nextLocation.y > _height - 1
        ){
            Toast toast = new Toast(MainActivity.this);
            toast.setText("Стена!");
            toast.show();
            return;
        }
        Point wallPosition = direction.getPointOnDirection(
                new Point(_location.x * 2 + 1, _location.y * 2 + 1)
        );
        if (!_maze[wallPosition.y][wallPosition.x]){
            Toast toast = new Toast(MainActivity.this);
            toast.setText("Стена!");
            toast.show();
            return;
        }
        _location = nextLocation;

        Runnable animation = new Runnable() {
            @Override
            public void run() {// Вперёд
                Point movePoint = direction.getPointOnDirection(new Point(1, 1));
                _map[movePoint.y][movePoint.x].setText("O");
                _map[1][1].setText(" ");
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ignored) { }
                // Обновляем
                drawMap();
                // Возвращаем
                movePoint = direction.invert().getPointOnDirection(new Point(1, 1));
                _map[movePoint.y][movePoint.x].setText("O");
                _map[1][1].setText(" ");
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ignored) { }
                // Вперёд
                _map[movePoint.y][movePoint.x].setText(" ");
                _map[1][1].setText("O");
            }
        };
        new Thread(animation).start();
        if (nextLocation.equals(_finish)){
            showMessage("Вы прошли лабирит!", "Победа!");
        }
    }

    private void drawMap(){
        final int X = _location.x * 2;
        final int Y = _location.y * 2;
        for (int y = 0; y < 3; y++){
            for (int x = 0; x < 3; x++){
                _map[y][x].setText(_maze[Y + y][X + x] ? " " : "X");
            }
        }
        _map[1][1].setText("O");
        if (_map[0][1].getText().toString().equals(" ")){
            Point temp = Direction.Up.getPointOnDirection(_location);
            if (temp.equals(_start))
                _map[0][1].setText("S");
            else if (temp.equals(_finish)) {
                _map[0][1].setText("F");
            }
        }
        if (_map[1][0].getText().toString() == " "){
            Point temp = Direction.Left.getPointOnDirection(_location);
            if (temp.equals(_start))
                _map[1][0].setText("S");
            else if (temp.equals(_finish)) {
                _map[1][0].setText("F");
            }
        }
        if (_map[1][2].getText().toString() == " "){
            Point temp = Direction.Right.getPointOnDirection(_location);
            if (temp.equals(_start))
                _map[1][2].setText("S");
            else if (temp.equals(_finish)) {
                _map[1][2].setText("F");
            }
        }
        if (_map[2][1].getText().toString() == " "){
            Point temp = Direction.Down.getPointOnDirection(_location);
            if (temp.equals(_start))
                _map[2][1].setText("S");
            else if (temp.equals(_finish)) {
                _map[2][1].setText("F");
            }
        }

        StringBuilder maze = new StringBuilder();
        for (int y = 0; y < _height * 2 + 1; y++) {
            maze.append("\n");
            for (int x = 0; x < _width * 2 + 1; x++) {
                if (x == _location.x * 2 + 1 && y == _location.y * 2 + 1)
                    maze.append(" O ");
                else
                    maze.append(_maze[y][x] ? "   " : "███");
            }
        }
        Log.i("MAZE_MAP", maze.toString());

    }

    private void generateMaze(){
        final int width = _width * 2 + 1;
        final int height = _height * 2 + 1;
        _maze = new boolean[height][width];
        for (int y = 0; y < _height; y++){
            for (int x = 0; x < _width; x++){
                _maze[y * 2 + 1][x * 2 + 1] = true;
            }
        }
        _visited = new boolean[_height][_width];
        _visited[0][0] = true;
        generationStep(new Point(0, 0), Direction.Down);
    }

    private List<Direction> getAllowedDirections(Point current, Direction lastDir){
        List<Direction> result = new ArrayList<>();
        for (int i = 0; i < 4; i++){
            Direction direction = Direction.getDirection(i);
            if (direction == lastDir.invert())
                continue;
            Point testCell = direction.getPointOnDirection(current);
            if (
                    testCell.x < 0 || testCell.y < 0 ||
                            testCell.x > _width - 1 || testCell.y > _height - 1
            )
                continue;
            if (_visited[testCell.y][testCell.x] && _random.nextInt(100) > 5)
                continue;
            result.add(direction);
        }
        return result;
    }
    private void generationStep(Point current, Direction lastDir){
        _visited[current.y][current.x] = true;
        while (true){
            List<Direction> allowedDirections = getAllowedDirections(current, lastDir);
            if (allowedDirections.size() == 0)
                return;
            //Log.i("MAZE_BUILD", current.toString() + " | " + lastDir.toString());
            Direction nextDir = allowedDirections.get(_random.nextInt(allowedDirections.size()));
            Point nextPos = nextDir.getPointOnDirection(current);
            Point wallPos = nextDir.getPointOnDirection(
                    new Point(current.x * 2 + 1, current.y * 2 + 1)
            );
            _maze[wallPos.y][wallPos.x] = true;
            //Log.i("MAZE_BUILD", wallPos.toString());
            generationStep(nextPos, nextDir);
        }
    }

    private void showMessage(String message, String title) {
        AlertDialog.Builder dlgAlert  = new AlertDialog.Builder(MainActivity.this);
        dlgAlert.setMessage(message);
        dlgAlert.setTitle(title);
        dlgAlert.setPositiveButton("OK", null);
        dlgAlert.setCancelable(true);
        dlgAlert.create().show();
    }
}