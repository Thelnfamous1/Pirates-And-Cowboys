package com.infamous.pirates_and_cowboys.entity;

import net.minecraft.entity.IJumpingMount;

public interface IServerJumpingMount extends IJumpingMount {

    void setServerSideJumpPower(int jumpPowerIn);
}
