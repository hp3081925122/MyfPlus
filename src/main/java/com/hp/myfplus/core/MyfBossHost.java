package com.hp.myfplus.core;

import javax.annotation.Nullable;

public interface MyfBossHost {
    int myfplus$skill();

    int myfplus$skillAge();

    void myfplus$setSkill(int skill);

    int myfplus$phase();

    void myfplus$setPhase(int phase);

    @Nullable
    BossBrain<?> myfplus$brain();
}
