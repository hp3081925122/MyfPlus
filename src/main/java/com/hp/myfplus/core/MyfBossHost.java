package com.hp.myfplus.core;

import javax.annotation.Nullable;

public interface MyfBossHost {
    int myfplus$skill();

    int myfplus$skillAge();

    void myfplus$setSkill(int skill);

    @Nullable
    BossBrain<?> myfplus$brain();
}
