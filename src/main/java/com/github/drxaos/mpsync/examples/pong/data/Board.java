package com.github.drxaos.mpsync.examples.pong.data;

import com.github.drxaos.mpsync.examples.pong.Player;

public class Board {

    public Player player1;
    public Player player2;

    public boolean new_game = true;

    public int ball_x;
    public int ball_y;
    public double ball_x_speed;
    public double ball_y_speed;

    public boolean acceleration = false;
    public int ball_acceleration_count;

    public long frame = 0;

    public boolean key_up1 = false;
    public boolean key_down1 = false;
    public boolean key_up2 = false;
    public boolean key_down2 = false;

    public int player1Id;
    public int player2Id;
    public int countDown;
}
