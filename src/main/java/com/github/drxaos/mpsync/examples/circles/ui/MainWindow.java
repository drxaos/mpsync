package com.github.drxaos.mpsync.examples.circles.ui;

import com.github.drxaos.mpsync.examples.circles.CirclesEngine;
import com.github.drxaos.mpsync.examples.circles.sim.Spawn;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

public class MainWindow extends Thread implements MouseListener, MouseMotionListener {
    public final int MAX_SPAWN = 30;
    public static final int X = 640;
    public static final int Y = 480;
    public final String TITLE = "2D Physics Engine";
    private JFrame f;
    private Canvas c;
    public BufferStrategy b;
    private GraphicsEnvironment ge;
    private GraphicsDevice gd;
    private GraphicsConfiguration gc;
    private BufferedImage buffer;
    private Graphics graphics;
    private Graphics2D g2d;

    CirclesEngine engine;

    MouseEvent click;

    public MainWindow() {
        super("MainWindow");
    }

    public MouseEvent getClick() {
        try {
            return click;
        } finally {
            click = null;
        }
    }

    public void setEngine(CirclesEngine engine) {
        this.engine = engine;
        engine.setController(this);
    }

    @Override
    public synchronized void start() {
        // Initialize some things.
        initializeJFrame();

        super.start();
    }

    @Override
    public void run() {
        // Run the animation loop.
        runAnimation();
    }

    public void runAnimation() {
        // Set up some variables.
        int fps = 0;
        int frames = 0;
        long totalTime = 0;
        long curTime = System.currentTimeMillis();
        long lastTime = curTime;
        // Start the loop.
        while (true) {
            try {
                // Calculations for FPS.
                lastTime = curTime;
                curTime = System.currentTimeMillis();
                totalTime += curTime - lastTime;
                if (totalTime > 1000) {
                    totalTime -= 1000;
                    fps = frames;
                    frames = 0;
                }
                ++frames;
                // clear back buffer...
                g2d = buffer.createGraphics();
                g2d.setColor(Color.WHITE);
                g2d.fillRect(0, 0, X, Y);
                // Draw entities
                ArrayList<Spawn> living = new ArrayList<Spawn>();
                if (engine != null) {
                    living.addAll(engine.getFullState().living);
                }
                for (int i = 0; i < living.size(); i++) {
                    g2d.setColor(Color.BLACK);
                    Spawn s = living.get(i);
                    g2d.fill(new Ellipse2D.Double(s.getX(), s.getY(), s.getRadius() * 2, s.getRadius() * 2));
                }
                for (int i = 0; i < living.size(); i++) {
                    g2d.setColor(Color.RED);
                    Spawn s = living.get(i);
                    g2d.drawLine((int) (s.getCenterX()), (int) (s.getCenterY()), (int) (s.getCenterX() + s.vx()), (int) (s.getCenterY() + s.vy()));
                }
                // display frames per second...
                g2d.setFont(new Font("Courier New", Font.PLAIN, 12));
                g2d.setColor(Color.GREEN);
                g2d.drawString(String.format("FPS: %s", fps), 20, 20);
                // Blit image and flip...
                graphics = b.getDrawGraphics();
                graphics.drawImage(buffer, 0, 0, null);
                if (!b.contentsLost())
                    b.show();
                // Let the OS have a little time...
                Thread.sleep(15);
            } catch (InterruptedException e) {
            } finally {
                // release resources
                if (graphics != null)
                    graphics.dispose();
                if (g2d != null)
                    g2d.dispose();
            }
        }
    }

    private void initializeJFrame() {
        // Create the frame...
        f = new JFrame(TITLE);
        f.setIgnoreRepaint(true);
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        // Create canvas for painting...
        c = new Canvas();
        c.setIgnoreRepaint(true);
        c.setSize(X, Y);
        // Add the canvas, and display.
        f.add(c);
        f.pack();
        // The following line centers the window on the screen.
        f.setLocationRelativeTo(null);
        f.setVisible(true);
        // Set up the BufferStrategy for double buffering.
        c.createBufferStrategy(2);
        b = c.getBufferStrategy();
        // Get graphics configuration...
        ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        gd = ge.getDefaultScreenDevice();
        gc = gd.getDefaultConfiguration();
        // Create off-screen drawing surface
        buffer = gc.createCompatibleImage(X, Y);
        // Objects needed for rendering...
        graphics = null;
        g2d = null;

        c.addMouseListener(this);
        c.addMouseMotionListener(this);
    }

    public void setPosition(int x, int y) {
        f.setLocation(x, y);
    }

    public void mouseClicked(MouseEvent e) {
        click = e;
    }

    public void mousePressed(MouseEvent e) {

    }

    public void mouseReleased(MouseEvent e) {

    }

    public void mouseEntered(MouseEvent e) {

    }

    public void mouseExited(MouseEvent e) {

    }


    public void mouseDragged(MouseEvent e) {
        click = e;
    }

    public void mouseMoved(MouseEvent e) {
    }
}
