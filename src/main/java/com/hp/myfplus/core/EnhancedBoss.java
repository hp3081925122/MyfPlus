package com.hp.myfplus.core;

import com.hp.myfplus.config.MyfConfig;

public interface EnhancedBoss {
    MyfConfig.BossCfg myfplus$cfg();

    boolean myfplus$phase2();

    void myfplus$enterPhase2();
}
