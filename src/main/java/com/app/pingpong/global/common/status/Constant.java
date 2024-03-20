package com.app.pingpong.global.common.status;

public enum Constant {
    TEAM_THRESHOLD(6);

    private final int num;

    Constant(int num) {
        this.num = num;
    }

    public int getNumber() {
        return num;
    }
}
