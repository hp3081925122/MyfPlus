package com.hp.myfplus.core;

/** 只有用强化召唤物召唤的 Boss 才会挂上本模组的技能大脑。 */
public interface EnhanceableBoss {
    void myfplus$enhance();

    boolean myfplus$enhanced();
}
