package com.github.drxaos.mpsync.examples.pong.engine;/*
 *  Copyright (C) 2010  Luca Wehrstedt
 *
 *  This file is released under the GPLv2
 *  Read the file 'COPYING' for more information
 */

import javax.swing.*;

public class PongWindow extends JFrame {
    Pong content;

    public PongWindow() {
        super();
        setTitle("Pong");
        setSize(640, 480);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    }

    public void setContent(Pong content) {
        this.content = content;
        content.acceleration = true;
        getContentPane().add(content);
        addKeyListener(content);
    }
}
