package com.github.drxaos.mpsync.examples.pong;/*
 *  Copyright (C) 2010  Luca Wehrstedt
 *
 *  This file is released under the GPLv2
 *  Read the file 'COPYING' for more information
 */

import com.github.drxaos.mpsync.bus.ServerInfo;
import com.github.drxaos.mpsync.examples.pong.data.Board;
import com.github.drxaos.mpsync.examples.pong.data.Info;
import com.github.drxaos.mpsync.examples.pong.data.Keys;
import com.github.drxaos.mpsync.sim.Simulation;
import com.github.drxaos.mpsync.sync.SimInput;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Random;

public class Pong extends JPanel implements KeyListener, Simulation<Board, Keys, Info> {
    // Proprietà della palla
    private static final int RADIUS = 10; // Raggio
    private static final int START_SPEED = 9; // Velocità iniziale
    private static final int ACCELERATION = 200; // Ogni quanti frame aumenta di 1 pixel la velocità

    // Proprietà dei carrelli
    private static final int SPEED = 12; // Velocità dei carrelli
    private static final int HEIGHT = 50; // SEMI-altezza del carrello
    private static final int WIDTH = 20;
    private static final int TOLERANCE = 15;
    private static final int PADDING = 10;

    private Player player1;
    private Player player2;

    private boolean new_game = true;

    private int ball_x;
    private int ball_y;
    private double ball_x_speed;
    private double ball_y_speed;

    public boolean acceleration = false;
    private int ball_acceleration_count;

    long frame = 0;

    private boolean key_up1 = false;
    private boolean key_down1 = false;
    private boolean key_up2 = false;
    private boolean key_down2 = false;

    private Keys input = null;
    private int clientId = 0;

    // Constructor
    public Pong(int p1_type, int p2_type) {
        super();
        setBackground(new Color(0, 0, 0));

        player1 = new Player(p1_type);
        player2 = new Player(p2_type);
    }

    // Compute destination of the ball
    private void computeDestination(Player player) {
        int base;
        if (ball_x_speed > 0)
            player.destination = ball_y + (getWidth() - PADDING - WIDTH - RADIUS - ball_x) * (int) (ball_y_speed) / (int) (ball_x_speed);
        else
            player.destination = ball_y - (ball_x - PADDING - WIDTH - RADIUS) * (int) (ball_y_speed) / (int) (ball_x_speed);

        if (player.destination <= RADIUS)
            player.destination = 2 * PADDING - player.destination;

        if (player.destination > getHeight() - 10) {
            player.destination -= RADIUS;
            if ((player.destination / (getHeight() - 2 * RADIUS)) % 2 == 0)
                player.destination = player.destination % (getHeight() - 2 * RADIUS);
            else
                player.destination = getHeight() - 2 * RADIUS - player.destination % (getHeight() - 2 * RADIUS);
            player.destination += RADIUS;
        }
    }

    // Set new position of the player
    private void movePlayer(Player player, int destination) {
        int distance = Math.abs(player.position - destination);

        if (distance != 0) {
            int direction = -(player.position - destination) / distance;

            if (distance > SPEED)
                distance = SPEED;

            player.position += direction * distance;

            if (player.position - HEIGHT < 0)
                player.position = HEIGHT;
            if (player.position + HEIGHT > getHeight())
                player.position = getHeight() - HEIGHT;

            System.out.println("Pos(" + clientId + "): " + player.type + " = " + player.position);
        }
    }

    // Compute player position
    private void computePosition(Player player) {
        // KEYBOARD1
        if (player.getType() == Player.KEYBOARD1) {
            if (key_up1 && !key_down1) {
                movePlayer(player, player.position - SPEED);
            } else if (key_down1 && !key_up1) {
                movePlayer(player, player.position + SPEED);
            }
        }
        // KEYBOARD2
        if (player.getType() == Player.KEYBOARD2) {
            if (key_up2 && !key_down2) {
                movePlayer(player, player.position - SPEED);
            } else if (key_down2 && !key_up2) {
                movePlayer(player, player.position + SPEED);
            }
        }
    }


    public void doStep() {

        // Prepara il campo di gioco
        if (new_game) {
            ball_x = getWidth() / 2;
            ball_y = getHeight() / 2;

            Random generator = new Random(frame += 1000000);
            double phase = generator.nextDouble() * Math.PI / 2 - Math.PI / 4;
            ball_x_speed = (int) (Math.cos(phase) * START_SPEED * (generator.nextBoolean() ? -1 : 1));
            ball_y_speed = (int) (Math.sin(phase) * START_SPEED);

            ball_acceleration_count = 0;

            new_game = false;
        }

        computePosition(player1);
        computePosition(player2);

        // Calcola la posizione della pallina
        ball_x += ball_x_speed;
        ball_y += ball_y_speed;
        if (ball_y_speed < 0) // Hack to fix double-to-int conversion
            ball_y++;

        if (ball_y_speed > 20) // Hack to fix double-to-int conversion
            ball_y_speed = 20;

        // Accelera la pallina
        if (acceleration) {
            ball_acceleration_count++;
            if (ball_acceleration_count == ACCELERATION) {
                ball_x_speed = (ball_x_speed + ball_x_speed + (int) ball_x_speed / Math.hypot((int) ball_x_speed, (int) ball_y_speed) * 2) / 2;
                ball_y_speed = (ball_y_speed + ball_y_speed + (int) ball_y_speed / Math.hypot((int) ball_x_speed, (int) ball_y_speed) * 2) / 2;
                ball_acceleration_count = 0;
            }
        }

        // Border-collision LEFT
        if (ball_x <= PADDING + WIDTH + RADIUS) {
            int collision_point = ball_y + (int) (ball_y_speed / ball_x_speed * (PADDING + WIDTH + RADIUS - ball_x));
            if (collision_point > player1.position - HEIGHT - TOLERANCE &&
                    collision_point < player1.position + HEIGHT + TOLERANCE) {
                ball_x = 2 * (PADDING + WIDTH + RADIUS) - ball_x;
                ball_x_speed = Math.abs(ball_x_speed);
                ball_y_speed -= Math.sin((double) (player1.position - ball_y) / HEIGHT * Math.PI / 4)
                        * Math.hypot(ball_x_speed, ball_y_speed);
            } else if (ball_x <= PADDING) {
                player2.points++;
                new_game = true;
            }
        }

        // Border-collision RIGHT
        if (ball_x >= getWidth() - PADDING - WIDTH - RADIUS) {
            int collision_point = ball_y - (int) (ball_y_speed / ball_x_speed * (ball_x - getWidth() + PADDING + WIDTH + RADIUS));
            if (collision_point > player2.position - HEIGHT - TOLERANCE &&
                    collision_point < player2.position + HEIGHT + TOLERANCE) {
                ball_x = 2 * (getWidth() - PADDING - WIDTH - RADIUS) - ball_x;
                ball_x_speed = -1 * Math.abs(ball_x_speed);
                ball_y_speed -= Math.sin((double) (player2.position - ball_y) / HEIGHT * Math.PI / 4)
                        * Math.hypot(ball_x_speed, ball_y_speed);
            } else if (ball_x >= getWidth() - PADDING) {
                player1.points++;
                new_game = true;
            }
        }

        // Border-collision TOP
        if (ball_y <= RADIUS) {
            ball_y_speed = Math.abs(ball_y_speed);
            ball_y = 2 * RADIUS - ball_y;
        }

        // Border-collision BOTTOM
        if (ball_y >= getHeight() - RADIUS) {
            ball_y_speed = -1 * Math.abs(ball_y_speed);
            ball_y = 2 * (getHeight() - RADIUS) - ball_y;
        }

    }

    // Draw
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        // Disegna i carrelli
        g.setColor(Color.WHITE);
        g.fillRect(PADDING, player1.position - HEIGHT, WIDTH, HEIGHT * 2);
        g.fillRect(getWidth() - PADDING - WIDTH, player2.position - HEIGHT, WIDTH, HEIGHT * 2);

        // Disegna la palla
        g.fillOval(ball_x - RADIUS, ball_y - RADIUS, RADIUS * 2, RADIUS * 2);

        // Disegna i punti
        g.drawString(player1.points + " ", getWidth() / 2 - 20, 20);
        g.drawString(player2.points + " ", getWidth() / 2 + 20, 20);
    }

    // Key pressed
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_UP) {
            input = new Keys(true, false);
        } else if (e.getKeyCode() == KeyEvent.VK_DOWN) {
            input = new Keys(false, true);
        }
    }

    // Key released
    public void keyReleased(KeyEvent e) {
        input = new Keys(false, false);
    }

    // Key released
    public void keyTyped(KeyEvent e) {
    }


    // Simulation interface


    public Board getFullState() {

        Board state = new Board();
        state.new_game = new_game;
        state.ball_x = ball_x;
        state.ball_y = ball_y;
        state.ball_x_speed = ball_x_speed;
        state.ball_y_speed = ball_y_speed;
        state.acceleration = acceleration;
        state.ball_acceleration_count = ball_acceleration_count;
        state.frame = frame;
        state.key_up1 = key_up1;
        state.key_down1 = key_down1;
        state.key_up2 = key_up2;
        state.key_down2 = key_down2;

        state.player1 = new Player(player1.type);
        state.player1.position = player1.position;
        state.player1.destination = player1.destination;
        state.player1.points = player1.points;

        state.player2 = new Player(player2.type);
        state.player2.position = player2.position;
        state.player2.destination = player2.destination;
        state.player2.points = player2.points;

        return state;
    }

    public void setFullState(Board state) {
        new_game = state.new_game;
        ball_x = state.ball_x;
        ball_y = state.ball_y;
        ball_x_speed = state.ball_x_speed;
        ball_y_speed = state.ball_y_speed;
        acceleration = state.acceleration;
        ball_acceleration_count = state.ball_acceleration_count;
        frame = state.frame;
        key_up1 = state.key_up1;
        key_down1 = state.key_down1;
        key_up2 = state.key_up2;
        key_down2 = state.key_down2;

        player1.type = state.player1.type;
        player1.position = state.player1.position;
        player1.destination = state.player1.destination;
        player1.points = state.player1.points;

        player2.type = state.player2.type;
        player2.position = state.player2.position;
        player2.destination = state.player2.destination;
        player2.points = state.player2.points;
    }

    public void step() {
        doStep();
    }

    public Keys getInput() {
        try {
            return input;
        } finally {
            input = null;
        }
    }

    public Info getInfo() {
        return new Info();
    }

    public void input(SimInput<Keys> simInput) {
        if (simInput.client == 0) {
            key_down1 = simInput.input.key_down;
            key_up1 = simInput.input.key_up;
        } else {
            key_down2 = simInput.input.key_down;
            key_up2 = simInput.input.key_up;
        }
    }

    public void setServerInfo(ServerInfo<Info> info) {
        clientId = info.clientId;
        JFrame frame = (JFrame) SwingUtilities.getRoot(this);
        frame.setTitle("Pong: " + (clientId > 0 ? "client" : "server"));
    }

    public void lockView() {

    }

    public void unlockView() {
        repaint();
    }
}
