package com.infamous.pirates_and_cowboys.pathfinding;

import com.infamous.pirates_and_cowboys.entity.IMountUser;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.network.DebugPacketSender;
import net.minecraft.pathfinding.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraft.world.Region;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

import javax.annotation.Nullable;
import java.util.Set;

public class MountingGroundPathNavigator<T extends MobEntity & IMountUser> extends GroundPathNavigator {
    private static final String PATH_FINDER_FIELD_NAME = "field_179681_j";
    private static final String RANGE_MULTIPLIER_FIELD_NAME = "field_226334_s_";
    private static final String TARGET_FIELD_NAME = "field_188564_r";
    private static final String DISTANCE_FIELD_NAME = "field_225468_r";
    private static final String UNKNOWN_FIELD_NAME = "field_244431_t";
    private boolean shouldRiderAvoidSun;

    public MountingGroundPathNavigator(T mountUser, World levelIn) {
        super(mountUser, levelIn);
    }

    @Nullable
    @Override
    protected Path createPath(Set<BlockPos> positions, int regionOffset, boolean offsetUpward, int distance) {
        if (positions.isEmpty()) {
            return null;
        } else if (this.getActualMovingMob().getY() < 0.0D) {
            return null;
        } else if (!this.canUpdatePath()) {
            return null;
        } else if (this.path != null && !this.path.isDone() && positions.contains(this.getTargetPos())) {
            return this.path;
        } else {
            this.level.getProfiler().push("pathfind");
            float followRange = (float)this.getNavigatingMob().getAttributeValue(Attributes.FOLLOW_RANGE);
            BlockPos blockpos = offsetUpward ? this.getActualMovingMob().blockPosition().above() : this.getActualMovingMob().blockPosition();
            int adjustedFollowRange = (int)(followRange + (float)regionOffset);
            Region region = new Region(this.level, blockpos.offset(-adjustedFollowRange, -adjustedFollowRange, -adjustedFollowRange), blockpos.offset(adjustedFollowRange, adjustedFollowRange, adjustedFollowRange));

            PathFinder pathFinder = ObfuscationReflectionHelper.getPrivateValue(PathNavigator.class, this, PATH_FINDER_FIELD_NAME);
            float rangeMultiplier = ObfuscationReflectionHelper.getPrivateValue(PathNavigator.class, this, RANGE_MULTIPLIER_FIELD_NAME);

            // creates a new path finder for the current pathfinding calculation, including setting the entity value
            Path path = pathFinder.findPath(region, this.getActualMovingMob(), positions, followRange, distance, rangeMultiplier);
            this.level.getProfiler().pop();
            if (path != null && path.getTarget() != null) {
                ObfuscationReflectionHelper.setPrivateValue(PathNavigator.class, this, path.getTarget(), TARGET_FIELD_NAME);
                ObfuscationReflectionHelper.setPrivateValue(PathNavigator.class, this, distance, DISTANCE_FIELD_NAME);
                this.resetTimeOut();
            }

            return path;
        }
    }

    private void resetTimeOut() {
        this.timeoutCachedNode = Vector3i.ZERO;
        this.timeoutTimer = 0L;
        this.timeoutLimit = 0.0D;
        ObfuscationReflectionHelper.setPrivateValue(PathNavigator.class, this, false, UNKNOWN_FIELD_NAME);
    }

    @Override
    public void tick() {
        ++this.tick;
        if (this.hasDelayedRecomputation) {
            this.recomputePath();
        }

        if (!this.isDone()) {
            if (this.canUpdatePath()) {
                this.followThePath();
            } else if (this.path != null && !this.path.isDone()) {
                Vector3d actualMovingMobPositionVec = this.getTempMobPos();
                Vector3d pathPointPositionVec = this.path.getNextEntityPos(this.getActualMovingMob());
                if (actualMovingMobPositionVec.y > pathPointPositionVec.y
                        && !this.getActualMovingMob().isOnGround()
                        && MathHelper.floor(actualMovingMobPositionVec.x) == MathHelper.floor(pathPointPositionVec.x)
                        && MathHelper.floor(actualMovingMobPositionVec.z) == MathHelper.floor(pathPointPositionVec.z)) {
                    this.path.advance();
                }
            }

            DebugPacketSender.sendPathFindingPacket(this.level, this.getNavigatingMob(), this.path, this.maxDistanceToWaypoint);
            if (!this.isDone()) {
                Vector3d actualMovingMobPathPosition = this.path.getNextEntityPos(this.getActualMovingMob());
                BlockPos pathPosition = new BlockPos(actualMovingMobPathPosition);
                this.getNavigatingMob().getMoveControl().setWantedPosition(
                        actualMovingMobPathPosition.x,
                        this.level.getBlockState(pathPosition.below()).isAir() ? actualMovingMobPathPosition.y : WalkNodeProcessor.getFloorLevel(this.level, pathPosition),
                        actualMovingMobPathPosition.z, this.speedModifier);
            }
        }
    }

    @Override
    protected void followThePath() {
        Vector3d actualMovingMobPosition = this.getTempMobPos();
        this.maxDistanceToWaypoint = this.getActualMovingMob().getBbWidth() > 0.75F ? this.getActualMovingMob().getBbWidth() / 2.0F : 0.75F - this.getActualMovingMob().getBbWidth() / 2.0F;
        Vector3i pathPointPosition = this.path.getNextNodePos();
        double xDistanceToWaypoint = Math.abs(this.getActualMovingMob().getX() - ((double)pathPointPosition.getX() + (this.getActualMovingMob().getBbWidth() + 1) / 2D)); //Forge: Fix MC-94054
        double yDistanceToWaypoint = Math.abs(this.getActualMovingMob().getY() - (double)pathPointPosition.getY());
        double zDistanceToWaypoint = Math.abs(this.getActualMovingMob().getZ() - ((double)pathPointPosition.getZ() + (this.getActualMovingMob().getBbWidth() + 1) / 2D)); //Forge: Fix MC-94054
        boolean flag = xDistanceToWaypoint < (double)this.maxDistanceToWaypoint && zDistanceToWaypoint < (double)this.maxDistanceToWaypoint && yDistanceToWaypoint < 1.0D;
        if (flag || this.getActualMovingMob().canCutCorner(this.path.getNextNode().type) && this.shouldTargetNextNodeInDirection(actualMovingMobPosition)) {
            this.path.advance();
        }

        this.doStuckDetection(actualMovingMobPosition);
    }

    private boolean shouldTargetNextNodeInDirection(Vector3d p_234112_1_) {
        if (this.path.getNextNodeIndex() + 1 >= this.path.getNodeCount()) {
            return false;
        } else {
            Vector3d vector3d = Vector3d.atBottomCenterOf(this.path.getNextNodePos());
            if (!p_234112_1_.closerThan(vector3d, 2.0D)) {
                return false;
            } else {
                Vector3d vector3d1 = Vector3d.atBottomCenterOf(this.path.getNodePos(this.path.getNextNodeIndex() + 1));
                Vector3d vector3d2 = vector3d1.subtract(vector3d);
                Vector3d vector3d3 = p_234112_1_.subtract(vector3d);
                return vector3d2.dot(vector3d3) > 0.0D;
            }
        }
    }

    @Override
    protected boolean isInLiquid() {
        return this.getNavigatingMob().isInWaterOrBubble() || this.getNavigatingMob().isInLava() || this.getActualMovingMob().isInWaterOrBubble() || this.getActualMovingMob().isInLava();
    }

    @Override
    public void recomputePath(BlockPos pos) {
        if (this.path != null && !this.path.isDone() && this.path.getNodeCount() != 0) {
            PathPoint pathpoint = this.path.getEndNode();
            Vector3d vector3d = new Vector3d(((double)pathpoint.x + this.getActualMovingMob().getX()) / 2.0D, ((double)pathpoint.y + this.getActualMovingMob().getY()) / 2.0D, ((double)pathpoint.z + this.getActualMovingMob().getZ()) / 2.0D);
            if (pos.closerThan(vector3d, (double)(this.path.getNodeCount() - this.path.getNextNodeIndex()))) {
                this.recomputePath();
            }

        }
    }

    @Override
    public void setAvoidSun(boolean avoidSun) {
        super.setAvoidSun(avoidSun);
        this.shouldRiderAvoidSun = avoidSun;
    }

    @Override
    protected Vector3d getTempMobPos() {
        return new Vector3d(this.getActualMovingMob().getX(), (double)this.getPathablePosY(), this.getActualMovingMob().getZ());
    }


    @Override
    protected void trimPath() {
        if (this.path != null) {
            for(int i = 0; i < this.path.getNodeCount(); ++i) {
                PathPoint pathpoint = this.path.getNode(i);
                PathPoint pathpoint1 = i + 1 < this.path.getNodeCount() ? this.path.getNode(i + 1) : null;
                BlockState blockstate = this.level.getBlockState(new BlockPos(pathpoint.x, pathpoint.y, pathpoint.z));
                if (blockstate.is(Blocks.CAULDRON)) {
                    this.path.replaceNode(i, pathpoint.cloneAndMove(pathpoint.x, pathpoint.y + 1, pathpoint.z));
                    if (pathpoint1 != null && pathpoint.y >= pathpoint1.y) {
                        this.path.replaceNode(i + 1, pathpoint.cloneAndMove(pathpoint1.x, pathpoint.y + 1, pathpoint1.z));
                    }
                }
            }

        }
        if (this.shouldRiderAvoidSun) {
            if (this.level.canSeeSky(new BlockPos(this.getNavigatingMob().getX(), this.getNavigatingMob().getY() + 0.5D, this.getNavigatingMob().getZ()))) {
                return;
            }

            for(int i = 0; i < this.path.getNodeCount(); ++i) {
                PathPoint pathpoint = this.path.getNode(i);
                if (this.level.canSeeSky(new BlockPos(pathpoint.x, pathpoint.y, pathpoint.z))) {
                    this.path.truncateNodes(i);
                    return;
                }
            }
        }
    }

    @Override
    protected boolean canMoveDirectly(Vector3d posVec31, Vector3d posVec32, int sizeX, int sizeY, int sizeZ) {
        int floorVec31X = MathHelper.floor(posVec31.x);
        int floorVec31Z = MathHelper.floor(posVec31.z);
        double xDifference = posVec32.x - posVec31.x;
        double zDifference = posVec32.z - posVec31.z;
        double horizontalDifferenceSq = xDifference * xDifference + zDifference * zDifference;
        if (horizontalDifferenceSq < 1.0E-8D) {
            return false;
        } else {
            double d3 = 1.0D / Math.sqrt(horizontalDifferenceSq);
            xDifference = xDifference * d3;
            zDifference = zDifference * d3;
            sizeX = sizeX + 2;
            sizeZ = sizeZ + 2;
            if (!this.isSafeToStandAt(floorVec31X, MathHelper.floor(posVec31.y), floorVec31Z, sizeX, sizeY, sizeZ, posVec31, xDifference, zDifference)) {
                return false;
            } else {
                sizeX = sizeX - 2;
                sizeZ = sizeZ - 2;
                double d4 = 1.0D / Math.abs(xDifference);
                double d5 = 1.0D / Math.abs(zDifference);
                double floorXDifference = (double)floorVec31X - posVec31.x;
                double floorZDifference = (double)floorVec31Z - posVec31.z;
                if (xDifference >= 0.0D) {
                    ++floorXDifference;
                }

                if (zDifference >= 0.0D) {
                    ++floorZDifference;
                }

                floorXDifference = floorXDifference / xDifference;
                floorZDifference = floorZDifference / zDifference;
                int k = xDifference < 0.0D ? -1 : 1;
                int l = zDifference < 0.0D ? -1 : 1;
                int i1 = MathHelper.floor(posVec32.x);
                int j1 = MathHelper.floor(posVec32.z);
                int k1 = i1 - floorVec31X;
                int l1 = j1 - floorVec31Z;

                while(k1 * k > 0 || l1 * l > 0) {
                    if (floorXDifference < floorZDifference) {
                        floorXDifference += d4;
                        floorVec31X += k;
                        k1 = i1 - floorVec31X;
                    } else {
                        floorZDifference += d5;
                        floorVec31Z += l;
                        l1 = j1 - floorVec31Z;
                    }

                    if (!this.isSafeToStandAt(floorVec31X, MathHelper.floor(posVec31.y), floorVec31Z, sizeX, sizeY, sizeZ, posVec31, xDifference, zDifference)) {
                        return false;
                    }
                }

                return true;
            }
        }
    }

    private boolean isSafeToStandAt(int x, int y, int z, int sizeX, int sizeY, int sizeZ, Vector3d vec31, double xDifference, double zDifference) {
        int i = x - sizeX / 2;
        int j = z - sizeZ / 2;
        if (!this.isPositionClear(i, y, j, sizeX, sizeY, sizeZ, vec31, xDifference, zDifference)) {
            return false;
        } else {
            for(int k = i; k < i + sizeX; ++k) {
                for(int l = j; l < j + sizeZ; ++l) {
                    double d0 = (double)k + 0.5D - vec31.x;
                    double d1 = (double)l + 0.5D - vec31.z;
                    if (!(d0 * xDifference + d1 * zDifference < 0.0D)) {
                        PathNodeType pathnodetype = this.nodeEvaluator.getBlockPathType(this.level, k, y - 1, l, this.getActualMovingMob(), sizeX, sizeY, sizeZ, true, true);
                        if (!this.hasValidPathType(pathnodetype)) {
                            return false;
                        }

                        pathnodetype = this.nodeEvaluator.getBlockPathType(this.level, k, y, l, this.getActualMovingMob(), sizeX, sizeY, sizeZ, true, true);
                        float pathPriority = this.getActualMovingMob().getPathfindingMalus(pathnodetype);
                        if (pathPriority < 0.0F || pathPriority >= 8.0F) {
                            return false;
                        }

                        if (pathnodetype == PathNodeType.DAMAGE_FIRE || pathnodetype == PathNodeType.DANGER_FIRE || pathnodetype == PathNodeType.DAMAGE_OTHER) {
                            return false;
                        }
                    }
                }
            }

            return true;
        }
    }

    private boolean isPositionClear(int x, int y, int z, int sizeX, int sizeY, int sizeZ, Vector3d vector3d, double xDifference, double zDifference) {
        for(BlockPos blockpos : BlockPos.betweenClosed(new BlockPos(x, y, z), new BlockPos(x + sizeX - 1, y + sizeY - 1, z + sizeZ - 1))) {
            double blockPosToVecXDifference = (double)blockpos.getX() + 0.5D - vector3d.x;
            double blockPosToVecZDifference = (double)blockpos.getZ() + 0.5D - vector3d.z;
            if (!(blockPosToVecXDifference * xDifference + blockPosToVecZDifference * zDifference < 0.0D) && !this.level.getBlockState(blockpos).isPathfindable(this.level, blockpos, PathType.LAND)) {
                return false;
            }
        }

        return true;
    }

    private MobEntity getNavigatingMob(){
        return this.mob;
    }

    private MobEntity getActualMovingMob(){
        if(this.mob.getVehicle() instanceof MobEntity){
            return (MobEntity) this.mob.getVehicle();
        }
        else return this.mob;
    }

    private int getPathablePosY() {
        if (this.getActualMovingMob().isInWater() && this.canFloat()) {
            int i = MathHelper.floor(this.getActualMovingMob().getY());
            Block block = this.level.getBlockState(new BlockPos(this.getActualMovingMob().getX(), (double)i, this.getActualMovingMob().getZ())).getBlock();
            int j = 0;

            while(block == Blocks.WATER) {
                ++i;
                block = this.level.getBlockState(new BlockPos(this.getActualMovingMob().getX(), (double)i, this.getActualMovingMob().getZ())).getBlock();
                ++j;
                if (j > 16) {
                    return MathHelper.floor(this.getActualMovingMob().getY());
                }
            }

            return i;
        } else {
            return MathHelper.floor(this.getActualMovingMob().getY() + 0.5D);
        }
    }
}
