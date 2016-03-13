package com.github.drxaos.mpsync.examples.pong;/*
 *  Copyright (C) 2010  Luca Wehrstedt
 *
 *  This file is released under the GPLv2
 *  Read the file 'COPYING' for more information
 */

public class Player {
    public static final int KEYBOARD1 = 1;
    public static final int KEYBOARD2 = 2;

    public int type;
    public int position = 0;
    public int destination = 0;
    public int points = 0;

    public Player(int type) {
        this.type = type;
    }

    public int getType() {
        return type;
    }
}
