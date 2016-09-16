// How to run
// Copy source code to Main.java
// Run with: javac Main.java && java Main

import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.Condition;
import java.util.Random;
import java.util.List;
import java.util.LinkedList;

public class Main {
  // prefer Runnable to extends Thread
  class Philosopher implements Runnable {
    private Integer id;
    private boolean eating;
    private Philosopher left;
    private Philosopher right;
    private ReentrantLock table;
    private Condition condition;
    private Random random;

    public Philosopher(int id, ReentrantLock table) {
      this.id = id;
      eating = false;
      this.table = table;
      condition = table.newCondition();
      random = new Random();
    }

    public void setLeft(Philosopher p) { this.left = p; }
    public void setRight(Philosopher p) { this.right = p; }

    public void run() {
      try {
        while (true) {
          think();
          eat();
        }
      }
      catch (InterruptedException e) {}
    }

    public void think() throws InterruptedException {
      table.lock();
      try {
        eating = false;
        left.condition.signal();
        left.condition.signal();
      } finally { table.unlock(); }

      Thread.sleep(1000);
    }

    public void eat() throws InterruptedException {
      table.lock();
      try {
        while (left.eating || right.eating)
          condition.await();
        System.out.println("Philosopher " + this.id.toString() + " is eating!");
        eating = true;
      }
      finally { table.unlock(); }
      Thread.sleep(1000);
    }

  }
  public static void main(String args[]) throws InterruptedException {
    Main m = new Main();
    m.start();
  }

  public void start() throws InterruptedException {
    try {
      ReentrantLock table = new ReentrantLock();

      List<Philosopher> ps = new LinkedList<Philosopher>();
      Philosopher p1 = new Philosopher(1,table);
      Philosopher p2 = new Philosopher(2,table);
      Philosopher p3 = new Philosopher(3,table);
      Philosopher p4 = new Philosopher(4,table);
      Philosopher p5 = new Philosopher(5,table);

      p1.setLeft(p2);
      p1.setRight(p5);

      p2.setLeft(p3);
      p2.setRight(p1);

      p3.setLeft(p4);
      p3.setRight(p5);

      p4.setLeft(p5);
      p4.setRight(p3);

      p4.setLeft(p5);
      p4.setRight(p3);

      p5.setLeft(p1);
      p5.setRight(p4);

      ps.add(p1);
      ps.add(p2);
      ps.add(p3);
      ps.add(p4);
      ps.add(p5);

      for (Philosopher p : ps) {
        new Thread(p).start();
      }
    }
    catch (Throwable e) {
      System.out.println("Error " + e.getMessage());
      e.printStackTrace();
    }

  }
}
