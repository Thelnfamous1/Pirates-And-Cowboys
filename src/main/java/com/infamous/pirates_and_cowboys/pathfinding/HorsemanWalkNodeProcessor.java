package com.infamous.pirates_and_cowboys.pathfinding;

import com.infamous.pirates_and_cowboys.entity.IHorseUser;
import com.infamous.pirates_and_cowboys.entity.IServerJumpingMount;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import net.minecraft.entity.MobEntity;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.pathfinding.WalkNodeProcessor;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.Region;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

/**
 * Note that typically, this node processor will be created
 * with a horse user's mount as the entity field value rather
 * than the horse user themselves if they are riding a mount
 */
public class HorsemanWalkNodeProcessor extends WalkNodeProcessor implements IWalkNodeProcessorHelper {

    public HorsemanWalkNodeProcessor() {
    }


    /**
     * This method is called by WalkNodeProcess#getPathNodeType
     * to convert a PathNodeType if certain conditions are met.
     * For example, the Ravager overrides this in its custom node processor
     * to convert a LEAF PathNodeType to OPEN since it can break leaf blocks.
     * @param blockReader The blockReader used for processing the local world region
     * @param canOpenDoors Whether or not the mob can open doors
     * @param canEnterDoors Whether or not the mob can enter doors
     * @param blockPos The block position for the current PathNode
     * @param originalPathNodeType The original PathNodeType passed in
     * @return A different PathNodeType than what was passed in, or the original one
     */
    @Override
    protected PathNodeType evaluateBlockPathType(IBlockReader blockReader, boolean canOpenDoors, boolean canEnterDoors, BlockPos blockPos, PathNodeType originalPathNodeType) {
        return originalPathNodeType == PathNodeType.FENCE
                && this.canHopFences() ?
                PathNodeType.OPEN : super.evaluateBlockPathType(blockReader, canOpenDoors, canEnterDoors, blockPos, originalPathNodeType);
    }

    private boolean canHopFences() {
        return this.mob instanceof IServerJumpingMount
                && this.mob.getControllingPassenger() instanceof IHorseUser
        ;
    }

    /**
     * This function is called by PathFinder#func_227479_a_
     * using its PathPoint[] "pathOptions" field and a dequeued PathPoint from its PathHeap "path" field
     * as arguments for pathOptionsIn and pathPointIn, respectively.
     * @param pathOptionsIn An array of PathPoints representing point options
     * @param pathPointIn A supplied path point for calculating new path options
     * @return The number of paths added to the path option array
     */
    @Override
    public int getNeighbors(PathPoint[] pathOptionsIn, PathPoint pathPointIn) {
        return this.updatePathOptions(pathOptionsIn, pathPointIn);
    }

    @Override
    public Region getBlockAccess(){
        return this.level;
    }

    @Override
    public MobEntity getEntity(){
        return this.mob;
    }

    @Override
    public PathPoint getOpenPointForPos(int x, int y, int z) {
        return this.getNode(x, y, z);
    }

    @Override
    public boolean isAllowedToSwim() {
        return this.canFloat();
    }

    @Override
    public int getEntitySizeX(){
        return this.entityWidth;
    }

    @Override
    public int getEntitySizeY(){
        return this.entityHeight;
    }

    @Override
    public int getEntitySizeZ(){
        return this.entityDepth;
    }

    @Override
    public Object2BooleanMap<AxisAlignedBB> getAxisAlignedBBObject2BooleanMap() {
        return ObfuscationReflectionHelper.getPrivateValue(WalkNodeProcessor.class, this, IWalkNodeProcessorHelper.BOUNDING_BOX_TO_BOOLEAN_MAP_FIELD_NAME);
    }


    @Override
    public Long2ObjectMap<PathNodeType> getPathNodeTypeLong2ObjectMap() {
        return ObfuscationReflectionHelper.getPrivateValue(WalkNodeProcessor.class, this, IWalkNodeProcessorHelper.LONG_TO_PATH_NODE_TYPE_MAP_FIELD_NAME);
    }

    @Override
    public boolean isAllowedToOpenDoors() {
        return this.canOpenDoors();
    }

    @Override
    public boolean isAllowedToEnterDoors() {
        return this.canPassDoors();
    }

    @Override
    public PathNodeType accessPathNodeType(Region blockAccess, int xIn, int yIn, int zIn, MobEntity mobEntity, int entitySizeX, int entitySizeY, int entitySizeZ, boolean allowedToOpenDoors, boolean allowedToEnterDoors) {
        return this.getBlockPathType(blockAccess, xIn, yIn, zIn, mobEntity, entitySizeX, entitySizeY, entitySizeZ, allowedToOpenDoors, allowedToEnterDoors);
    }
}