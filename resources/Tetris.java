// This program is not well-organized. Someday I will adjust these code.

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.applet.*;
import java.io.*;
import javax.sound.sampled.*;

public class Tetris extends JFrame implements Runnable {
  Thread thread;
  Thread keyThread;
  Field field;

  int formNext[][];

  static int score = 0;
  static int fallSpeed = 400;
  static int nowSpeed = 1;
  boolean isActive = true;

  public static void main(String[] args) {
    Tetris tetris = new Tetris();


    AudioFormat format = null;
    DataLine.Info info = null;
    Clip line = null;
    File audioFile = null;

    String filename = "Music";
    Random r = new Random();
    int filenum = r.nextInt(3);
    switch (filenum) {
      case 0:
        filename += "A";
        break;
      case 1:
        filename += "B";
        break;
      case 2:
        filename += "C";
        break;
      default:
        filename += "A";
        break;
    }
    filename += ".wav";

    try{
        audioFile = new File("./sound/" + filename);
        format = AudioSystem.getAudioFileFormat(audioFile).getFormat();
        info = new DataLine.Info(Clip.class, format);
        line = (Clip)AudioSystem.getLine(info);
        line.open(AudioSystem.getAudioInputStream(audioFile));
        line.loop(Clip.LOOP_CONTINUOUSLY);
    }
    catch(Exception e){
        e.printStackTrace();
    }
  }
  Tetris() {
    field = new Field();
    thread = new Thread(this);
    keyThread = new Thread(new Runnable() {
      public void run() {
        while (isActive) {
          try {
            keyThread.sleep(100);
          } catch(Exception e) {
            e.printStackTrace();
          }
          field.rotateBlock();
          field.moveBlockX();
          if (Key.keyState.get(Key.KEYDOWN)) field.moveBlockY();
          repaint();
        }
      }
    });
    thread.start();
    keyThread.start();
    setTitle("Tetris");
    setSize(Block.WIDTH * (Field.HORIZONTAL + 7), Block.HEIGHT * (Field.VERTICAL + 3));
    setBackground(Color.WHITE);
    setLocationRelativeTo(null);
    setDefaultCloseOperation(EXIT_ON_CLOSE);

    addKeyListener(new Key());
    requestFocus();
    setVisible(true);
  }

  public void run() {
    while (isActive) {
      try {
        thread.sleep(fallSpeed);
      } catch(Exception e) {
        e.printStackTrace();
      }
      field.moveBlockY();
      field.checkField();
      if (!field.checkBlock()) {
        isActive = false;
      };
    }
  }

  public void update(Graphics g) {
    paint(g);
  }

  public void paint(Graphics g) {

    // 仮描画
    Dimension size = getSize();
    Image back = createImage(size.width, size.height);
    Graphics g2 = back.getGraphics();

    field.draw(g2);
    Font font = new Font("Arial", Font.BOLD, 20);
    g2.setFont(font);
    g2.drawString("Score: " + String.valueOf(score), Block.WIDTH * (Field.HORIZONTAL + 2), Block.HEIGHT * 2);
    g2.drawString("Speed: " + String.valueOf(nowSpeed), Block.WIDTH * (Field.HORIZONTAL + 2), Block.HEIGHT * 3);

    if (field.count > 1) {
      g2.setColor(Color.ORANGE);
      g2.drawString(String.valueOf(field.count) + "ComBo!",
      Block.WIDTH * (Field.HORIZONTAL + 2), (Block.HEIGHT * 13));
      g2.setColor(Color.BLACK);
    }
    g2.drawString("Next", Block.WIDTH * (Field.HORIZONTAL + 2), Block.HEIGHT * 4);

    formNext = field.getNextBlock();

    int START_X = 0;
    int START_Y = 0;
    int color = 0;
    for (int i = 0; i < formNext.length; i++) {
      for (int j = 0; j < formNext[0].length; j++) {
        if (formNext[i][j] != 0) {
          START_X = j * Block.WIDTH + Block.WIDTH * (Field.HORIZONTAL + 2);
          START_Y = i * Block.HEIGHT + Block.HEIGHT * 5;
          color = formNext[i][j];
          if (color == 0) {
            g2.setColor(Color.BLACK);
          } else if (color == 1) {
            g2.setColor(Color.WHITE);
          } else if (color == 2) {
            g2.setColor(Color.BLUE);
          } else if (color == 3) {
            g2.setColor(Color.RED);
          } else if (color == 4) {
            g2.setColor(Color.GREEN);
          } else if (color == 5) {
            g2.setColor(Color.YELLOW);
          } else if (color == 6) {
            g2.setColor(Color.MAGENTA);
          } else if (color == 7) {
            g2.setColor(Color.ORANGE);
          } else if (color == 8) {
            g2.setColor(Color.CYAN);
          } else {
            g2.setColor(Color.WHITE);
          }
          g2.fillRect(START_X, START_Y, Block.WIDTH, Block.HEIGHT);
          g2.setColor(Color.BLACK);
          g2.drawRect(START_X, START_Y, Block.WIDTH, Block.HEIGHT);
        }
      }
    }

    // 描画
    g.drawImage(back, 0, this.getInsets().top, this);
  }
}

class Field {
  static int field[][];   // フィールドのデータ配列(0→何もなし、1〜 →ブロックあり)

  static final int WIDTH  = Block.WIDTH;
  static final int HEIGHT = Block.HEIGHT;

  static final int START_X = Block.WIDTH;
  static final int START_Y = Block.HEIGHT;

  static final int VERTICAL   = 21;
  static final int HORIZONTAL = 12;

  Block block;

  // ブロック連続消しカウンター
  int count = 0;

  Field() {
    field = new int[VERTICAL][HORIZONTAL];
    // 壁や地面のデータを配列に格納する
    for (int i = 0; i < VERTICAL; i++) {
      field[i][0] = 1;
      if (i == VERTICAL - 1) {
        for (int j = 0; j < HORIZONTAL; j++) {
          field[i][j] = 1;
          if (j == HORIZONTAL - 1) {
            for (int k = 0; k < VERTICAL; k++) {
              field[i - k][j] = 1;
            }
          }
        }
      }
    }
    block = new Block(-1);
  }

  public void moveBlockX() {
    block = block.moveX();
  }
  public void moveBlockY() {
    block = block.moveY();
  }

  public void rotateBlock() {
    block.rotate();
  }

  public int[][] getNextBlock() {
    return block.getNext();
  }

  public boolean checkBlock() {
    return block.checkBlock();
  }

  public void checkField() {
    for (int i = 0; i < VERTICAL - 1; i++) {
      boolean flag = true;
      for (int j = 0; j < field[i].length; j++) {
        if (field[i][j] == 0) {
          flag = false;
        }
      }
      if (flag) {
        // 消す
        for (int k = 0; k < field[i].length - 2; k++) {
          field[i][k + 1] = 0;
        }
        count += 1;
        Tetris.score += 100 * count;
        if (Tetris.fallSpeed >= 200){
          Tetris.fallSpeed -= 10;
          Tetris.nowSpeed += 1;
        }
        // 落とす
        for (int l = i; l > 0; l--) {
          for (int m = 1; m < field[l].length - 1; m++) {
            while (field[l - 1][m] != 0 && field[l][m] == 0) {
              field[l][m] = field[l - 1][m];
              field[l - 1][m] = 0;
            }
          }
        }
      } else {
        count = 0;
      }
    }
  }

  // フィールドの描画処理
  public void draw(Graphics g) {
    int color = 0;
    for (int i = 0; i < VERTICAL; i++) {
      for (int j = 0; j < HORIZONTAL; j++) {
        color = field[i][j];
        if (color == 0) {
          g.setColor(Color.BLACK);
        } else if (color == 1) {
          g.setColor(Color.WHITE);
        } else if (color == 2) {
          g.setColor(Color.BLUE);
        } else if (color == 3) {
          g.setColor(Color.RED);
        } else if (color == 4) {
          g.setColor(Color.GREEN);
        } else if (color == 5) {
          g.setColor(Color.YELLOW);
        } else if (color == 6) {
          g.setColor(Color.MAGENTA);
        } else if (color == 7) {
          g.setColor(Color.ORANGE);
        } else if (color == 8) {
          g.setColor(Color.CYAN);
        } else {
          g.setColor(Color.WHITE);
        }
        g.fillRect(START_X + j * WIDTH, START_Y + i * HEIGHT, WIDTH, HEIGHT);
        g.setColor(Color.BLACK);
        g.drawRect(START_X + j * WIDTH, START_Y + i * HEIGHT, WIDTH, HEIGHT);
      }
    }
    block.draw(g);
  }
}

class Block {

  static final int WIDTH  = 35;
  static final int HEIGHT = 35;

  int type = 0;
  int nextType = 0;  // -1は未定義の意
  int pos[];  // formの左上の座標
  int form[][];  // form(ブロックの形状を保存)

  int moveCounter = 0;

  Block(int next) {
    pos = new int[2];
    Random r = new Random();
    if (next == -1) {
      type = r.nextInt(7);
    } else {
      type = next;
    }
    nextType = r.nextInt(7);
    pos[0] = 0;
    switch(type) {
      case 0:
        // ■
        // ■
        // ■
        // ■
        form = new int[4][1];
        form[0][0] = 2;
        form[1][0] = 2;
        form[2][0] = 2;
        form[3][0] = 2;
        pos[1] = 5;
        break;
      case 1:
        // ■ ■
        // ■ ■
        form = new int[2][2];
        form[0][0] = 3;
        form[0][1] = 3;
        form[1][0] = 3;
        form[1][1] = 3;
        pos[1] = 5;
        break;
      case 2:
        // ■
        // ■
        // ■ ■
        form = new int[3][2];
        form[0][0] = 4;
        form[1][0] = 4;
        form[2][0] = 4;
        form[2][1] = 4;
        pos[1] = 5;
        break;
      case 3:
        //   ■
        //   ■
        // ■ ■
        form = new int[3][2];
        form[0][1] = 4;
        form[1][1] = 4;
        form[2][0] = 4;
        form[2][1] = 4;
        pos[1] = 5;
        break;
      case 4:
        // ■
        // ■ ■
        //   ■
        form = new int[3][2];
        form[0][0] = 5;
        form[1][0] = 5;
        form[1][1] = 5;
        form[2][1] = 5;
        pos[1] = 5;
        break;
      case 5:
        //   ■
        // ■ ■
        // ■
        form = new int[3][2];
        form[0][1] = 5;
        form[1][0] = 5;
        form[1][1] = 5;
        form[2][0] = 5;
        pos[1] = 5;
        break;
      case 6:
        // ■
        // ■ ■
        // ■
        form = new int[3][2];
        form[0][0] = 6;
        form[1][0] = 6;
        form[1][1] = 6;
        form[2][0] = 6;
        pos[1] = 5;
        break;
      // case 7:
      //   // ■
      //   // ■ ■
      //   form = new int[2][2];
      //   form[0][0] = 7;
      //   form[1][0] = 7;
      //   form[1][1] = 7;
      //   pos[1] = 5;
      //   break;
      // case 8:
      //   //   ■
      //   // ■ ■ ■
      //   //   ■
      //   form = new int[3][3];
      //   form[0][1] = 8;
      //   form[1][0] = 8;
      //   form[1][1] = 8;
      //   form[1][2] = 8;
      //   form[2][1] = 8;
      //   pos[1] = 5;
      //   break;
    }

  }

  public boolean checkBlock() {
    boolean flag = true;
    for (int i = 0; i < form.length; i++) {
      for (int j = 0; j < form[0].length; j++) {
        if (Field.field[i][pos[1] + j] != 0 && form[i][j] != 0) {
          flag =  false;
        }
      }
    }
    return flag;
  }

  public int[][] getNext() {
    int[][] formNext;
    switch (nextType) {
      case 0:
        // ■
        // ■
        // ■
        // ■
        formNext = new int[4][1];
        formNext[0][0] = 2;
        formNext[1][0] = 2;
        formNext[2][0] = 2;
        formNext[3][0] = 2;
        break;
      case 1:
        // ■ ■
        // ■ ■
        formNext = new int[2][2];
        formNext[0][0] = 3;
        formNext[0][1] = 3;
        formNext[1][0] = 3;
        formNext[1][1] = 3;
        break;
      case 2:
        // ■
        // ■
        // ■ ■
        formNext = new int[3][2];
        formNext[0][0] = 4;
        formNext[1][0] = 4;
        formNext[2][0] = 4;
        formNext[2][1] = 4;
        break;
      case 3:
        //   ■
        //   ■
        // ■ ■
        formNext = new int[3][2];
        formNext[0][1] = 4;
        formNext[1][1] = 4;
        formNext[2][0] = 4;
        formNext[2][1] = 4;
        break;
      case 4:
        // ■
        // ■ ■
        //   ■
        formNext = new int[3][2];
        formNext[0][0] = 5;
        formNext[1][0] = 5;
        formNext[1][1] = 5;
        formNext[2][1] = 5;
        break;
      case 5:
        //   ■
        // ■ ■
        // ■
        formNext = new int[3][2];
        formNext[0][1] = 5;
        formNext[1][0] = 5;
        formNext[1][1] = 5;
        formNext[2][0] = 5;
        break;
      case 6:
        // ■
        // ■ ■
        // ■
        formNext = new int[3][2];
        formNext[0][0] = 6;
        formNext[1][0] = 6;
        formNext[1][1] = 6;
        formNext[2][0] = 6;
        break;
      // case 7:
      //   // ■
      //   // ■ ■
      //   formNext = new int[2][2];
      //   formNext[0][0] = 7;
      //   formNext[1][0] = 7;
      //   formNext[1][1] = 7;
      //   pos[1] = 5;
      //   break;
      // case 8:
      //   //   ■
      //   // ■ ■ ■
      //   //   ■
      //   formNext = new int[3][3];
      //   formNext[0][1] = 8;
      //   formNext[1][0] = 8;
      //   formNext[1][1] = 8;
      //   formNext[1][2] = 8;
      //   formNext[2][1] = 8;
      //   pos[1] = 5;
      //   break;
      default:
        formNext = new int[4][4];
        break;
    }
    return formNext;
  }

  // 描画処理
  public void draw(Graphics g) {
    int START_X;
    int START_Y;
    int color = 0;

    for (int i = 0; i < form.length; i++) {
      for (int j = 0; j < form[i].length; j++) {
        if (form[i][j] != 0) {
          START_X = (pos[1] + j) * this.WIDTH + Field.START_X;
          START_Y = (pos[0] + i) * this.HEIGHT + Field.START_Y;
          color = form[i][j];
          if (color == 0) {
            g.setColor(Color.BLACK);
          } else if (color == 1) {
            g.setColor(Color.WHITE);
          } else if (color == 2) {
            g.setColor(Color.BLUE);
          } else if (color == 3) {
            g.setColor(Color.RED);
          } else if (color == 4) {
            g.setColor(Color.GREEN);
          } else if (color == 5) {
            g.setColor(Color.YELLOW);
          } else if (color == 6) {
            g.setColor(Color.MAGENTA);
          } else if (color == 7) {
            g.setColor(Color.ORANGE);
          } else if (color == 8) {
            g.setColor(Color.CYAN);
          } else {
            g.setColor(Color.WHITE);
          }
          g.fillRect(START_X, START_Y, this.WIDTH, this.HEIGHT);
          g.setColor(Color.BLACK);
          g.drawRect(START_X, START_Y, this.WIDTH, this.HEIGHT);
        }
      }
    }
  }

  // 移動処理
  public Block moveY() {
    // ループ変数の宣言
    int i = 0;
    int j = 0;
    // 移動フラグ
    boolean yMoveFlag = true;
    // form配列内の値を入れる
    int formVal = 0;
    // 縦移動
    int underVal = 0;
    formVal = 0;
    for (i = 0; i < form.length; i++) {
      for (j = 0; j < form[i].length; j++) {
        formVal = form[i][j];
        underVal = Field.field[pos[0] + i + 1][pos[1] + j];
        if (formVal != 0 && underVal != 0) {
          yMoveFlag = false;
          break;
        }
      }
    }
    if (yMoveFlag) {
      pos[0] += 1;
      return this;
    } else {
      writeToField();
      return new Block(nextType);
    }
  }

  public Block moveX() {
    // ループ変数の宣言
    int i = 0;
    int j = 0;
    // 移動フラグ
    boolean left = true;
    boolean right = true;
    boolean xMoveFlag = true;
    // form配列内の値を入れる
    int formVal = 0;
    // 横移動
    int leftVal = 0;
    int rightVal = 0;
    for (i = 0; i < form.length; i++) {
      for (j = 0; j < form[i].length; j++) {
        formVal = form[i][j];
        leftVal = Field.field[pos[0] + i][pos[1] + j - 1];
        rightVal = Field.field[pos[0] + i][pos[1] + j + 1];
        if (formVal != 0) {
          if (leftVal != 0) {
            left = false;
          }
          if (rightVal != 0) {
            right = false;
            break;
          }
        }
      }
    }
    if (left && Key.keyState.get(Key.KEYLEFT)) {
      pos[1] -= 1;
    } else if (right && Key.keyState.get(Key.KEYRIGHT)) {
      pos[1] += 1;
    }
    return this;
  }

  public void rotate() {
    int i = 0;
    int j = 0;
    if (Key.keyState.get(Key.KEYSPACE)) {
      // 回転可能なスペースがあるかどうかをチェックする
      int v = 0;
      int h = 0;
      int right = 0;
      int left = 0;
      int top = 0;
      int bottom = 0;
      int spaceV = 0;
      int spaceH = 0;
      // 右チェック
      for (i = 0; i < form.length; i++) {
        for (j = 0; j < Field.HORIZONTAL - pos[1]; j++) {
          v = pos[0] + i;
          h = pos[1] + j;
          if (Field.field[v][h] != 0) {
            right = h;
            break;
          }
        }
      }
      // 下チェック
      int bottoms[] = new int[form.length];
      for (i = 0; i < Field.VERTICAL - pos[0]; i++) {
        for (j = 0; j < form[0].length; j++) {
          v = pos[0] + i;
          h = pos[1] + j;
          if (Field.field[v][h] != 0) {
            bottom = v;
            break;
          }
        }
      }
      // 上チェック
      for (i = pos[0]; i >= 0; i--) {
        for (j = 0; j < form[0].length; j++) {
          v = i;
          h = pos[1] + j;
          if (Field.field[v][h] != 0) {
            top = v;
            break;
          }
        }
      }
      // 左横チェック
      for (i = 0; i < form.length; i++) {
        for (j = pos[1]; j >= 0; j--) {
          v = pos[0] + i;
          h = j;
          if (Field.field[v][h] != 0) {
            left = h;
            break;
          }
        }
      }
      spaceV = bottom - pos[0] - form.length + 1;
      spaceH = right - left - form[0].length;
      System.out.println("left:" + left);
      System.out.println("right:" + right);
      System.out.println("top:" + top);
      System.out.println("bottom:" + bottom);
      System.out.println("spaceV:" + spaceV);
      System.out.println("spaceH:" + spaceH);
      if (spaceV >= form[0].length && spaceH >= form.length) {
        // 回転処理
        int formRotated[][] = new int[form[0].length][form.length];
        for (i = 0; i < form[0].length; i++) {
          for (j = 0; j < form.length; j++) {
            formRotated[i][j] = form[form.length - 1 - j][i];
          }
        }
        if (pos[1] >= Field.HORIZONTAL / 2) {
          pos[1] += form[0].length - formRotated[0].length;
        }
        form = formRotated;
      }
    }
  }

  // フィールドへの書き込み
  private void writeToField() {
    for (int i = 0; i < form.length; i++) {
      for (int j = 0; j < form[i].length; j++) {
        if (form[i][j] != 0) {
          Field.field[pos[0] + i][pos[1] + j] = form[i][j];
        }
      }
    }
  }
}

class Key implements KeyListener {

  static Map<Integer, Boolean> keyState = new HashMap<Integer, Boolean>();

  static final int KEYUP    = 0x00;
  static final int KEYDOWN  = 0x01;
  static final int KEYLEFT  = 0x02;
  static final int KEYRIGHT = 0x03;
  static final int KEYSPACE = 0x04;

  static {
    keyState.put(KEYUP, false);
    keyState.put(KEYDOWN, false);
    keyState.put(KEYLEFT, false);
    keyState.put(KEYRIGHT, false);
    keyState.put(KEYSPACE, false);
  }

  public void keyPressed(KeyEvent e) {
    switch (e.getKeyCode()) {
      case KeyEvent.VK_UP   : keyState.put(KEYUP, true); break;
      case KeyEvent.VK_DOWN : keyState.put(KEYDOWN, true); break;
      case KeyEvent.VK_LEFT : keyState.put(KEYLEFT, true); break;
      case KeyEvent.VK_RIGHT: keyState.put(KEYRIGHT, true); break;
      case KeyEvent.VK_SPACE: keyState.put(KEYSPACE, true); break;
    }
  }
  public void keyReleased(KeyEvent e) {
    switch (e.getKeyCode()) {
      case KeyEvent.VK_UP   : keyState.put(KEYUP, false); break;
      case KeyEvent.VK_DOWN : keyState.put(KEYDOWN, false); break;
      case KeyEvent.VK_LEFT : keyState.put(KEYLEFT, false); break;
      case KeyEvent.VK_RIGHT: keyState.put(KEYRIGHT, false); break;
      case KeyEvent.VK_SPACE: keyState.put(KEYSPACE, false); break;
    }
  }
  public void keyTyped(KeyEvent e) {

  }
}
