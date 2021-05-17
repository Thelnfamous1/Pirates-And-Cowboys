package com.infamous.pirates_and_cowboys.pathfinding;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import net.minecraft.entity.MobEntity;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.pathfinding.WalkNodeProcessor;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.Region;

import javax.annotation.Nullable;

/**
 * No changes from vanilla logic apart from refactoring for organization/renaming.
 * This is just meant as a reference for how the walk node processor works.
 */
public interface IWalkNodeProcessorHelper {

    String BOUNDING_BOX_TO_BOOLEAN_MAP_FIELD_NAME = "field_237227_l_";
    String LONG_TO_PATH_NODE_TYPE_MAP_FIELD_NAME = "field_237226_k_";

    default int updatePathOptions(PathPoint[] pathOptionsIn, PathPoint pathPointIn) {
        int pathPointIndex = 0;
        int flooredStepHeight = 0;
        PathNodeType pathNodeTypeForPos = this.getPathNodeTypeForPosition(this.getEntity(), pathPointIn.x, pathPointIn.y + 1, pathPointIn.z);
        PathNodeType pathNodeTypeForPos1 = this.getPathNodeTypeForPosition(this.getEntity(), pathPointIn.x, pathPointIn.y, pathPointIn.z);
        if (this.getEntity().getPathfindingMalus(pathNodeTypeForPos) >= 0.0F && pathNodeTypeForPos1 != PathNodeType.STICKY_HONEY) {
            flooredStepHeight = MathHelper.floor(Math.max(1.0F, this.getEntity().maxUpStep));
        }

        double groundY = WalkNodeProcessor.getFloorLevel(this.getBlockAccess(), new BlockPos(pathPointIn.x, pathPointIn.y, pathPointIn.z));
        PathPoint safePoint0 = this.getNextSafePoint(pathPointIn.x, pathPointIn.y, pathPointIn.z + 1, flooredStepHeight, groundY, Direction.SOUTH, pathNodeTypeForPos1);
        if (this.shouldAddLateralSafePoint(safePoint0, pathPointIn)) {
            pathOptionsIn[pathPointIndex++] = safePoint0;
        }

        PathPoint safePoint1 = this.getNextSafePoint(pathPointIn.x - 1, pathPointIn.y, pathPointIn.z, flooredStepHeight, groundY, Direction.WEST, pathNodeTypeForPos1);
        if (this.shouldAddLateralSafePoint(safePoint1, pathPointIn)) {
            pathOptionsIn[pathPointIndex++] = safePoint1;
        }

        PathPoint safePoint2 = this.getNextSafePoint(pathPointIn.x + 1, pathPointIn.y, pathPointIn.z, flooredStepHeight, groundY, Direction.EAST, pathNodeTypeForPos1);
        if (this.shouldAddLateralSafePoint(safePoint2, pathPointIn)) {
            pathOptionsIn[pathPointIndex++] = safePoint2;
        }

        PathPoint safePoint3 = this.getNextSafePoint(pathPointIn.x, pathPointIn.y, pathPointIn.z - 1, flooredStepHeight, groundY, Direction.NORTH, pathNodeTypeForPos1);
        if (this.shouldAddLateralSafePoint(safePoint3, pathPointIn)) {
            pathOptionsIn[pathPointIndex++] = safePoint3;
        }

        PathPoint safePoint4 = this.getNextSafePoint(pathPointIn.x - 1, pathPointIn.y, pathPointIn.z - 1, flooredStepHeight, groundY, Direction.NORTH, pathNodeTypeForPos1);
        if (this.shouldAddDiagonalSafePoint(pathPointIn, safePoint1, safePoint3, safePoint4)) {
            pathOptionsIn[pathPointIndex++] = safePoint4;
        }

        PathPoint safePoint5 = this.getNextSafePoint(pathPointIn.x + 1, pathPointIn.y, pathPointIn.z - 1, flooredStepHeight, groundY, Direction.NORTH, pathNodeTypeForPos1);
        if (this.shouldAddDiagonalSafePoint(pathPointIn, safePoint2, safePoint3, safePoint5)) {
            pathOptionsIn[pathPointIndex++] = safePoint5;
        }

        PathPoint safePoint6 = this.getNextSafePoint(pathPointIn.x - 1, pathPointIn.y, pathPointIn.z + 1, flooredStepHeight, groundY, Direction.SOUTH, pathNodeTypeForPos1);
        if (this.shouldAddDiagonalSafePoint(pathPointIn, safePoint1, safePoint0, safePoint6)) {
            pathOptionsIn[pathPointIndex++] = safePoint6;
        }

        PathPoint safePoint7 = this.getNextSafePoint(pathPointIn.x + 1, pathPointIn.y, pathPointIn.z + 1, flooredStepHeight, groundY, Direction.SOUTH, pathNodeTypeForPos1);
        if (this.shouldAddDiagonalSafePoint(pathPointIn, safePoint2, safePoint0, safePoint7)) {
            pathOptionsIn[pathPointIndex++] = safePoint7;
        }

        return pathPointIndex;
    }

    default boolean shouldAddLateralSafePoint(PathPoint pathPointIn0, PathPoint pathPointIn1) {
        return pathPointIn0 != null
                && !pathPointIn0.closed
                && (pathPointIn0.costMalus >= 0.0F || pathPointIn1.costMalus < 0.0F);
    }

    default boolean shouldAddDiagonalSafePoint(PathPoint pathPointIn0, @Nullable PathPoint pathPointIn1, @Nullable PathPoint pathPointIn2, @Nullable PathPoint pathPointIn3) {
        if (pathPointIn3 != null && pathPointIn2 != null && pathPointIn1 != null) {
            if (pathPointIn3.closed) {
                return false;
            } else if (pathPointIn2.y <= pathPointIn0.y && pathPointIn1.y <= pathPointIn0.y) {
                if (pathPointIn1.type != PathNodeType.WALKABLE_DOOR && pathPointIn2.type != PathNodeType.WALKABLE_DOOR && pathPointIn3.type != PathNodeType.WALKABLE_DOOR) {
                    boolean fenceFlag =
                            (pathPointIn2.type == PathNodeType.FENCE)
                                    && (pathPointIn1.type == PathNodeType.FENCE)
                                    && (double) this.getEntity().getBbWidth() < 0.5D;
                    return pathPointIn3.costMalus >= 0.0F
                            && (pathPointIn2.y < pathPointIn0.y || pathPointIn2.costMalus >= 0.0F || fenceFlag)
                            && (pathPointIn1.y < pathPointIn0.y || pathPointIn1.costMalus >= 0.0F || fenceFlag);
                } else {
                    return false;
                }
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    /**
     * Returns a point that the entity can safely move to
     * NOTE: Had to rename this because having the same name as WalkNodeProcessor#getSafePoint
     * caused an unintentional IllegalAccessError
     */
    @Nullable
    default PathPoint getNextSafePoint(int x, int y, int z, int stepHeight, double groundYIn, Direction facing, PathNodeType pathNodeTypeIn) {
        PathPoint safePoint = null;
        BlockPos.Mutable blockpos$mutable = new BlockPos.Mutable();
        double groundY = WalkNodeProcessor.getFloorLevel(this.getBlockAccess(), blockpos$mutable.set(x, y, z));
        if (groundY - groundYIn > 1.125D) {
            return null;
        } else {
            PathNodeType pathnodetype = this.getPathNodeTypeForPosition(this.getEntity(), x, y, z);
            float pathPriority = this.getEntity().getPathfindingMalus(pathnodetype);
            double halfEntityWidth = (double)this.getEntity().getBbWidth() / 2.0D;
            if (pathPriority >= 0.0F) {
                safePoint = this.getOpenPointForPos(x, y, z);
                safePoint.type = pathnodetype;
                safePoint.costMalus = Math.max(safePoint.costMalus, pathPriority);
            }

            if (pathNodeTypeIn == PathNodeType.FENCE
                    && safePoint != null
                    && safePoint.costMalus >= 0.0F
                    && !this.pathPointHasNoCollisions(safePoint)) {
                safePoint = null;
            }

            if (pathnodetype == PathNodeType.WALKABLE) {
                return safePoint;
            } else {
                if ((safePoint == null || safePoint.costMalus < 0.0F)
                        && stepHeight > 0
                        && (pathnodetype != PathNodeType.FENCE)
                        && pathnodetype != PathNodeType.UNPASSABLE_RAIL
                        && pathnodetype != PathNodeType.TRAPDOOR) {
                    safePoint = this.getNextSafePoint(x, y + 1, z, stepHeight - 1, groundYIn, facing, pathNodeTypeIn);
                    if (safePoint != null && (safePoint.type == PathNodeType.OPEN || safePoint.type == PathNodeType.WALKABLE) && this.getEntity().getBbWidth() < 1.0F) {
                        double facingAdjustedX = (double)(x - facing.getStepX()) + 0.5D;
                        double facingAdjustedZ = (double)(z - facing.getStepZ()) + 0.5D;
                        AxisAlignedBB safePointBoundingBox = new AxisAlignedBB(
                                facingAdjustedX - halfEntityWidth,
                                WalkNodeProcessor.getFloorLevel(this.getBlockAccess(), blockpos$mutable.set(facingAdjustedX, (double)(y + 1), facingAdjustedZ)) + 0.001D,
                                facingAdjustedZ - halfEntityWidth,
                                facingAdjustedX + halfEntityWidth,
                                (double)this.getEntity().getBbHeight() + WalkNodeProcessor.getFloorLevel(this.getBlockAccess(), blockpos$mutable.set((double)safePoint.x, (double)safePoint.y, (double)safePoint.z)) - 0.002D,
                                facingAdjustedZ + halfEntityWidth);
                        if (this.hasCollisionsWith(safePointBoundingBox)) {
                            safePoint = null;
                        }
                    }
                }

                if (pathnodetype == PathNodeType.WATER && !this.isAllowedToSwim()) {
                    if (this.getPathNodeTypeForPosition(this.getEntity(), x, y - 1, z) != PathNodeType.WATER) {
                        return safePoint;
                    }

                    while(y > 0) {
                        --y;
                        pathnodetype = this.getPathNodeTypeForPosition(this.getEntity(), x, y, z);
                        if (pathnodetype != PathNodeType.WATER) {
                            return safePoint;
                        }

                        safePoint = this.getOpenPointForPos(x, y, z);
                        safePoint.type = pathnodetype;
                        safePoint.costMalus = Math.max(safePoint.costMalus, this.getEntity().getPathfindingMalus(pathnodetype));
                    }
                }

                if (pathnodetype == PathNodeType.OPEN) {
                    int j = 0;
                    int i = y;

                    while(pathnodetype == PathNodeType.OPEN) {
                        --y;
                        if (y < 0) {
                            PathPoint openPoint0 = this.getOpenPointForPos(x, i, z);
                            openPoint0.type = PathNodeType.BLOCKED;
                            openPoint0.costMalus = -1.0F;
                            return openPoint0;
                        }

                        if (j++ >= this.getEntity().getMaxFallDistance()) {
                            PathPoint openPoint1 = this.getOpenPointForPos(x, y, z);
                            openPoint1.type = PathNodeType.BLOCKED;
                            openPoint1.costMalus = -1.0F;
                            return openPoint1;
                        }

                        pathnodetype = this.getPathNodeTypeForPosition(this.getEntity(), x, y, z);
                        pathPriority = this.getEntity().getPathfindingMalus(pathnodetype);
                        if (pathnodetype != PathNodeType.OPEN && pathPriority >= 0.0F) {
                            safePoint = this.getOpenPointForPos(x, y, z); // openPoint2
                            safePoint.type = pathnodetype;
                            safePoint.costMalus = Math.max(safePoint.costMalus, pathPriority);
                            break;
                        }

                        if (pathPriority < 0.0F) {
                            PathPoint openPoint3 = this.getOpenPointForPos(x, y, z);
                            openPoint3.type = PathNodeType.BLOCKED;
                            openPoint3.costMalus = -1.0F;
                            return openPoint3;
                        }
                    }
                }

                if (pathnodetype == PathNodeType.FENCE) {
                    safePoint = this.getOpenPointForPos(x, y, z); // openPoint4
                    safePoint.closed = true;
                    safePoint.type = pathnodetype;
                    safePoint.costMalus = pathnodetype.getMalus();
                }

                return safePoint;
            }
        }
    }

    default boolean pathPointHasNoCollisions(PathPoint pathPointIn) {
        Vector3d positionDifferenceVector = new Vector3d((double)pathPointIn.x - this.getEntity().getX(), (double)pathPointIn.y - this.getEntity().getY(), (double)pathPointIn.z - this.getEntity().getZ());
        AxisAlignedBB entityBoundingBox = this.getEntity().getBoundingBox();
        int posDiffVecLengthOverEntityBBAvgEdgeLength = MathHelper.ceil(positionDifferenceVector.length() / entityBoundingBox.getSize());
        positionDifferenceVector = positionDifferenceVector.scale((double)(1.0F / (float)posDiffVecLengthOverEntityBBAvgEdgeLength));

        for(int j = 1; j <= posDiffVecLengthOverEntityBBAvgEdgeLength; ++j) {
            entityBoundingBox = entityBoundingBox.move(positionDifferenceVector);
            if (this.hasCollisionsWith(entityBoundingBox)) {
                return false;
            }
        }

        return true;
    }

    Region getBlockAccess();

    MobEntity getEntity();

    PathPoint getOpenPointForPos(int x, int y, int z);

    boolean isAllowedToSwim();

    int getEntitySizeX();

    int getEntitySizeY();

    int getEntitySizeZ();

    default boolean hasCollisionsWith(AxisAlignedBB axisAlignedBBIn) {

        Object2BooleanMap<AxisAlignedBB> object2BooleanMap = getAxisAlignedBBObject2BooleanMap();

        return object2BooleanMap.computeIfAbsent(axisAlignedBBIn,
                (axisAlignedBB) -> !this.getBlockAccess().noCollision(this.getEntity(), axisAlignedBBIn));
    }

    Object2BooleanMap<AxisAlignedBB> getAxisAlignedBBObject2BooleanMap();

    default PathNodeType getPathNodeTypeForPosition(MobEntity mobEntity, int xIn, int yIn, int zIn) {

        Long2ObjectMap<PathNodeType> long2ObjectMap = getPathNodeTypeLong2ObjectMap();

        return long2ObjectMap.computeIfAbsent(BlockPos.asLong(xIn, yIn, zIn),
                (longKey) -> this.accessPathNodeType(this.getBlockAccess(), xIn, yIn, zIn, mobEntity, this.getEntitySizeX(), this.getEntitySizeY(), this.getEntitySizeZ(), this.isAllowedToOpenDoors(), this.isAllowedToEnterDoors()));
    }

    Long2ObjectMap<PathNodeType> getPathNodeTypeLong2ObjectMap();

    boolean isAllowedToOpenDoors();

    boolean isAllowedToEnterDoors();

    PathNodeType accessPathNodeType(Region blockAccess, int xIn, int yIn, int zIn, MobEntity mobEntity, int entitySizeX, int entitySizeY, int entitySizeZ, boolean allowedToOpenDoors, boolean allowedToEnterDoors);
}
