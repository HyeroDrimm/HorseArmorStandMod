package com.hyerodrimm.horsearmorstandmod.entity.custom;


import com.hyerodrimm.horsearmorstandmod.HorseArmorStandMod;
import com.hyerodrimm.horsearmorstandmod.entity.ModEntities;
import com.hyerodrimm.horsearmorstandmod.item.ModItems;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.entity.*;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.item.ArmorStandItem;
import net.minecraft.item.HorseArmorItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Arm;
import net.minecraft.util.Hand;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.LightType;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import net.minecraft.world.explosion.Explosion;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animatable.instance.SingletonAnimatableInstanceCache;
import software.bernie.geckolib.core.animation.*;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.object.PlayState;

import java.util.List;
import java.util.function.Predicate;

import static org.apache.commons.lang3.Validate.isAssignableFrom;

public class HorseArmorStandEntity extends LivingEntity implements GeoEntity {
    protected static final RawAnimation SWAY_ANIMATION = RawAnimation.begin().then("animation.horsearmorstand.sway", Animation.LoopType.PLAY_ONCE);
    private boolean playSwayAnimation = false;
    private AnimatableInstanceCache cache = new SingletonAnimatableInstanceCache(this);
    // POSES
    /*private static final EulerAngle DEFAULT_HEAD_ROTATION = new EulerAngle(0.0f, 0.0f, 0.0f);
    private static final EulerAngle DEFAULT_BODY_ROTATION = new EulerAngle(0.0f, 0.0f, 0.0f);
    private static final EulerAngle DEFAULT_LEFT_ARM_ROTATION = new EulerAngle(-10.0f, 0.0f, -10.0f);
    private static final EulerAngle DEFAULT_RIGHT_ARM_ROTATION = new EulerAngle(-15.0f, 0.0f, 10.0f);
    private static final EulerAngle DEFAULT_LEFT_LEG_ROTATION = new EulerAngle(-1.0f, 0.0f, -1.0f);
    private static final EulerAngle DEFAULT_RIGHT_LEG_ROTATION = new EulerAngle(1.0f, 0.0f, 1.0f);*/
    private static final EntityDimensions MARKER_DIMENSIONS = new EntityDimensions(0.0f, 0.0f, true);
    private static final EntityDimensions SMALL_DIMENSIONS = ModEntities.HORSE_ARMOR_STAND.getDimensions().scaled(0.5f);
    public static final int SMALL_FLAG = 1;
    public static final int HIDE_BASE_PLATE_FLAG = 8;
    public static final int MARKER_FLAG = 16;
    public static final TrackedData<Byte> HORSE_ARMOR_STAND_FLAGS = DataTracker.registerData(HorseArmorStandEntity.class, TrackedDataHandlerRegistry.BYTE);
    // POSES
/*    public static final TrackedData<EulerAngle> TRACKER_HEAD_ROTATION = DataTracker.registerData(ArmorStandEntity.class, TrackedDataHandlerRegistry.ROTATION);
    public static final TrackedData<EulerAngle> TRACKER_BODY_ROTATION = DataTracker.registerData(ArmorStandEntity.class, TrackedDataHandlerRegistry.ROTATION);
    public static final TrackedData<EulerAngle> TRACKER_LEFT_ARM_ROTATION = DataTracker.registerData(ArmorStandEntity.class, TrackedDataHandlerRegistry.ROTATION);
    public static final TrackedData<EulerAngle> TRACKER_RIGHT_ARM_ROTATION = DataTracker.registerData(ArmorStandEntity.class, TrackedDataHandlerRegistry.ROTATION);
    public static final TrackedData<EulerAngle> TRACKER_LEFT_LEG_ROTATION = DataTracker.registerData(ArmorStandEntity.class, TrackedDataHandlerRegistry.ROTATION);
    public static final TrackedData<EulerAngle> TRACKER_RIGHT_LEG_ROTATION = DataTracker.registerData(ArmorStandEntity.class, TrackedDataHandlerRegistry.ROTATION);*/
    private static final Predicate<Entity> RIDEABLE_MINECART_PREDICATE = entity -> entity instanceof AbstractMinecartEntity && ((AbstractMinecartEntity) entity).getMinecartType() == AbstractMinecartEntity.Type.RIDEABLE;
    private final DefaultedList<ItemStack> armorItems = DefaultedList.ofSize(4, ItemStack.EMPTY);
    private boolean invisible;
    public long lastHitTime;
    private int disabledSlots;
    // POSES
/*    private EulerAngle headRotation = DEFAULT_HEAD_ROTATION;
    private EulerAngle bodyRotation = DEFAULT_BODY_ROTATION;
    private EulerAngle leftArmRotation = DEFAULT_LEFT_ARM_ROTATION;
    private EulerAngle rightArmRotation = DEFAULT_RIGHT_ARM_ROTATION;
    private EulerAngle leftLegRotation = DEFAULT_LEFT_LEG_ROTATION;
    private EulerAngle rightLegRotation = DEFAULT_RIGHT_LEG_ROTATION;*/

    public HorseArmorStandEntity(EntityType<? extends HorseArmorStandEntity> entityType, World world) {
        super((EntityType<? extends LivingEntity>) entityType, world);
        this.setStepHeight(0.0f);
    }

/*    public HorseArmorStandEntity(World world, double x, double y, double z) {
        this((EntityType<? extends HorseArmorStandEntity>) ModEntities.HORSE_ARMOR_STAND, world);
        this.setPosition(x, y, z);
    }*/

    @Override
    public void calculateDimensions() {
        double d = this.getX();
        double e = this.getY();
        double f = this.getZ();
        super.calculateDimensions();
        this.setPosition(d, e, f);
    }

    private boolean canClip() {
        return !this.isMarker() && !this.hasNoGravity();
    }

    @Override
    public boolean canMoveVoluntarily() {
        return super.canMoveVoluntarily() && this.canClip();
    }

    @Override
    protected void initDataTracker() {
        super.initDataTracker();
        this.dataTracker.startTracking(HORSE_ARMOR_STAND_FLAGS, (byte) 0);
        // POSES
/*        this.dataTracker.startTracking(TRACKER_HEAD_ROTATION, DEFAULT_HEAD_ROTATION);
        this.dataTracker.startTracking(TRACKER_BODY_ROTATION, DEFAULT_BODY_ROTATION);
        this.dataTracker.startTracking(TRACKER_LEFT_ARM_ROTATION, DEFAULT_LEFT_ARM_ROTATION);
        this.dataTracker.startTracking(TRACKER_RIGHT_ARM_ROTATION, DEFAULT_RIGHT_ARM_ROTATION);
        this.dataTracker.startTracking(TRACKER_LEFT_LEG_ROTATION, DEFAULT_LEFT_LEG_ROTATION);
        this.dataTracker.startTracking(TRACKER_RIGHT_LEG_ROTATION, DEFAULT_RIGHT_LEG_ROTATION);*/
    }

    @Override
    public Iterable<ItemStack> getArmorItems() {
        return this.armorItems;
    }

    public ItemStack getArmorType() {
        return this.getEquippedStack(EquipmentSlot.CHEST);
    }

    @Override
    public ItemStack getEquippedStack(EquipmentSlot slot) {
        switch (slot.getType()) {
            case ARMOR: {
                return this.armorItems.get(slot.getEntitySlotId());
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
    public void equipStack(EquipmentSlot slot, ItemStack stack) {
        this.processEquippedStack(stack);
        switch (slot.getType()) {
            case ARMOR: {
                this.onEquipStack(slot, this.armorItems.set(slot.getEntitySlotId(), stack), stack);
            }
        }
    }

    @Override
    public boolean canEquip(ItemStack stack) {
        return !stack.isEmpty() && stack.getItem() instanceof HorseArmorItem && this.getArmorType().isEmpty() && !this.isSlotDisabled(EquipmentSlot.CHEST);
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        NbtList nbtList = new NbtList();
        for (ItemStack itemStack : this.armorItems) {
            NbtCompound nbtCompound = new NbtCompound();
            if (!itemStack.isEmpty()) {
                itemStack.writeNbt(nbtCompound);
            }
            nbtList.add(nbtCompound);
        }
        nbt.put("ArmorItems", nbtList);
        nbt.putBoolean("Invisible", this.isInvisible());
        nbt.putBoolean("Small", this.isSmall());
        nbt.putInt("DisabledSlots", this.disabledSlots);
        nbt.putBoolean("NoBasePlate", this.shouldHideBasePlate());
        if (this.isMarker()) {
            nbt.putBoolean("Marker", this.isMarker());
        }
        // POSES
        //nbt.put("Pose", this.poseToNbt());
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        int i;
        NbtList nbtList;
        super.readCustomDataFromNbt(nbt);
        if (nbt.contains("ArmorItems", NbtElement.LIST_TYPE)) {
            nbtList = nbt.getList("ArmorItems", NbtElement.COMPOUND_TYPE);
            for (i = 0; i < this.armorItems.size(); ++i) {
                this.armorItems.set(i, ItemStack.fromNbt(nbtList.getCompound(i)));
            }
        }
        this.setInvisible(nbt.getBoolean("Invisible"));
        this.setSmall(nbt.getBoolean("Small"));
        this.disabledSlots = nbt.getInt("DisabledSlots");
        this.setHideBasePlate(nbt.getBoolean("NoBasePlate"));
        this.setMarker(nbt.getBoolean("Marker"));
        this.noClip = !this.canClip();
        // POSES
/*        NbtCompound nbtCompound = nbt.getCompound("Pose");
        this.readPoseNbt(nbtCompound);*/
    }

    // POSES
    /*private void readPoseNbt(NbtCompound nbt) {
        NbtList nbtList = nbt.getList("Head", NbtElement.FLOAT_TYPE);
        this.setHeadRotation(nbtList.isEmpty() ? DEFAULT_HEAD_ROTATION : new EulerAngle(nbtList));
        NbtList nbtList2 = nbt.getList("Body", NbtElement.FLOAT_TYPE);
        this.setBodyRotation(nbtList2.isEmpty() ? DEFAULT_BODY_ROTATION : new EulerAngle(nbtList2));
        NbtList nbtList3 = nbt.getList("LeftArm", NbtElement.FLOAT_TYPE);
        this.setLeftArmRotation(nbtList3.isEmpty() ? DEFAULT_LEFT_ARM_ROTATION : new EulerAngle(nbtList3));
        NbtList nbtList4 = nbt.getList("RightArm", NbtElement.FLOAT_TYPE);
        this.setRightArmRotation(nbtList4.isEmpty() ? DEFAULT_RIGHT_ARM_ROTATION : new EulerAngle(nbtList4));
        NbtList nbtList5 = nbt.getList("LeftLeg", NbtElement.FLOAT_TYPE);
        this.setLeftLegRotation(nbtList5.isEmpty() ? DEFAULT_LEFT_LEG_ROTATION : new EulerAngle(nbtList5));
        NbtList nbtList6 = nbt.getList("RightLeg", NbtElement.FLOAT_TYPE);
        this.setRightLegRotation(nbtList6.isEmpty() ? DEFAULT_RIGHT_LEG_ROTATION : new EulerAngle(nbtList6));
    }*/

    // POSES
    /*private NbtCompound poseToNbt() {
        NbtCompound nbtCompound = new NbtCompound();
        if (!DEFAULT_HEAD_ROTATION.equals(this.headRotation)) {
            nbtCompound.put("Head", this.headRotation.toNbt());
        }
        if (!DEFAULT_BODY_ROTATION.equals(this.bodyRotation)) {
            nbtCompound.put("Body", this.bodyRotation.toNbt());
        }
        if (!DEFAULT_LEFT_ARM_ROTATION.equals(this.leftArmRotation)) {
            nbtCompound.put("LeftArm", this.leftArmRotation.toNbt());
        }
        if (!DEFAULT_RIGHT_ARM_ROTATION.equals(this.rightArmRotation)) {
            nbtCompound.put("RightArm", this.rightArmRotation.toNbt());
        }
        if (!DEFAULT_LEFT_LEG_ROTATION.equals(this.leftLegRotation)) {
            nbtCompound.put("LeftLeg", this.leftLegRotation.toNbt());
        }
        if (!DEFAULT_RIGHT_LEG_ROTATION.equals(this.rightLegRotation)) {
            nbtCompound.put("RightLeg", this.rightLegRotation.toNbt());
        }
        return nbtCompound;
    }*/

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    protected void pushAway(Entity entity) {
    }

    @Override
    protected void tickCramming() {
        List<Entity> list = this.getWorld().getOtherEntities(this, this.getBoundingBox(), RIDEABLE_MINECART_PREDICATE);
        for (int i = 0; i < list.size(); ++i) {
            Entity entity = list.get(i);
            if (!(this.squaredDistanceTo(entity) <= 0.2)) continue;
            entity.pushAwayFrom(this);
        }
    }

    @Override
    public ActionResult interactAt(PlayerEntity player, Vec3d hitPos, Hand hand) {
        ItemStack itemStack = player.getStackInHand(hand);
        if (this.isMarker() || itemStack.isOf(Items.NAME_TAG)) {
            return ActionResult.PASS;
        }
        if (player.isSpectator()) {
            return ActionResult.SUCCESS;
        }
        if (player.getWorld().isClient) {
            return ActionResult.CONSUME;
        }

        if (itemStack.isEmpty()) {
            if (this.hasStackEquipped(EquipmentSlot.CHEST) && this.equip(player, EquipmentSlot.CHEST, itemStack, hand)) {
                return ActionResult.SUCCESS;
            }
        } else if (itemStack.getItem() instanceof HorseArmorItem && !this.isSlotDisabled(EquipmentSlot.CHEST)) {
            if (this.isSlotDisabled(EquipmentSlot.CHEST)) {
                return ActionResult.FAIL;
            }
            if (this.equip(player, EquipmentSlot.CHEST, itemStack, hand)) {
                return ActionResult.SUCCESS;
            }
        }

        return ActionResult.PASS;
    }

    private boolean isSlotDisabled(EquipmentSlot slot) {
        return (this.disabledSlots & 1 << slot.getArmorStandSlotId()) != 0;
    }

    private boolean equip(PlayerEntity player, EquipmentSlot slot, ItemStack stack, Hand hand) {
        ItemStack itemStack = this.getEquippedStack(slot);
        if (!itemStack.isEmpty() && (this.disabledSlots & 1 << slot.getArmorStandSlotId() + 8) != 0) {
            return false;
        }
        if (itemStack.isEmpty() && (this.disabledSlots & 1 << slot.getArmorStandSlotId() + 16) != 0) {
            return false;
        }
        if (player.getAbilities().creativeMode && itemStack.isEmpty() && !stack.isEmpty()) {
            this.equipStack(slot, stack.copyWithCount(1));
            return true;
        }
        if (!stack.isEmpty() && stack.getCount() > 1) {
            if (!itemStack.isEmpty()) {
                return false;
            }
            this.equipStack(slot, stack.split(1));
            return true;
        }
        this.equipStack(slot, stack);
        player.setStackInHand(hand, itemStack);
        return true;
    }

    @Override
    public boolean damage(DamageSource source, float amount) {
        if (this.getWorld().isClient || this.isRemoved()) {
            return false;
        }
        if (source.isIn(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            this.kill();
            return false;
        }
        if (this.isInvulnerableTo(source) || this.invisible || this.isMarker()) {
            return false;
        }
        if (source.isIn(DamageTypeTags.IS_EXPLOSION)) {
            this.onBreak(source);
            this.kill();
            return false;
        }
        if (source.isIn(DamageTypeTags.IGNITES_ARMOR_STANDS)) {
            if (this.isOnFire()) {
                this.updateHealth(source, 0.15f);
            } else {
                this.setOnFireFor(5);
            }
            return false;
        }
        if (source.isIn(DamageTypeTags.BURNS_ARMOR_STANDS) && this.getHealth() > 0.5f) {
            this.updateHealth(source, 4.0f);
            return false;
        }
        boolean bl = source.getSource() instanceof PersistentProjectileEntity;
        boolean bl2 = bl && ((PersistentProjectileEntity) source.getSource()).getPierceLevel() > 0;
        boolean bl3 = "player".equals(source.getName());
        if (!bl3 && !bl) {
            return false;
        }
        Entity entity = source.getAttacker();
        if (entity instanceof PlayerEntity) {
            PlayerEntity playerEntity = (PlayerEntity) entity;
            if (!playerEntity.getAbilities().allowModifyWorld) {
                return false;
            }
        }
        if (source.isSourceCreativePlayer()) {
            this.playBreakSound();
            this.spawnBreakParticles();
            this.kill();
            return bl2;
        }
        long l = this.getWorld().getTime();
        if (l - this.lastHitTime <= 5L || bl) {
            this.breakAndDropItem(source);
            this.spawnBreakParticles();
            this.kill();
        } else {
            this.getWorld().sendEntityStatus(this, EntityStatuses.HIT_ARMOR_STAND);
            this.emitGameEvent(GameEvent.ENTITY_DAMAGE, source.getAttacker());
            this.lastHitTime = l;
        }
        return true;
    }

    @Override
    public void handleStatus(byte status) {
        if (status == EntityStatuses.HIT_ARMOR_STAND) {
            if (this.getWorld().isClient) {
                this.getWorld().playSound(this.getX(), this.getY(), this.getZ(), SoundEvents.ENTITY_ARMOR_STAND_HIT, this.getSoundCategory(), 0.3f, 1.0f, false);
                this.lastHitTime = this.getWorld().getTime();
            }
        } else {
            super.handleStatus(status);
        }
    }

    @Override
    public boolean shouldRender(double distance) {
        double d = this.getBoundingBox().getAverageSideLength() * 4.0;
        if (Double.isNaN(d) || d == 0.0) {
            d = 4.0;
        }
        return distance < (d *= 64.0) * d;
    }

    private void spawnBreakParticles() {
        if (this.getWorld() instanceof ServerWorld) {
            ((ServerWorld) this.getWorld()).spawnParticles(new BlockStateParticleEffect(ParticleTypes.BLOCK, Blocks.OAK_PLANKS.getDefaultState()), this.getX(), this.getBodyY(0.6666666666666666), this.getZ(), 10, this.getWidth() / 4.0f, this.getHeight() / 4.0f, this.getWidth() / 4.0f, 0.05);
        }
    }

    private void updateHealth(DamageSource damageSource, float amount) {
        float f = this.getHealth();
        if ((f -= amount) <= 0.5f) {
            this.onBreak(damageSource);
            this.kill();
        } else {
            this.setHealth(f);
            this.emitGameEvent(GameEvent.ENTITY_DAMAGE, damageSource.getAttacker());
        }
    }

    private void breakAndDropItem(DamageSource damageSource) {
        ItemStack itemStack = new ItemStack(ModItems.HORSE_ARMOR_STAND_ITEM);
        if (this.hasCustomName()) {
            itemStack.setCustomName(this.getCustomName());
        }
        Block.dropStack(this.getWorld(), this.getBlockPos(), itemStack);
        this.onBreak(damageSource);
    }

    private void onBreak(DamageSource damageSource) {
        ItemStack itemStack;
        int i;
        this.playBreakSound();
        this.drop(damageSource);
        for (i = 0; i < this.armorItems.size(); ++i) {
            itemStack = this.armorItems.get(i);
            if (itemStack.isEmpty()) continue;
            Block.dropStack(this.getWorld(), this.getBlockPos().up(), itemStack);
            this.armorItems.set(i, ItemStack.EMPTY);
        }
    }

    private void playBreakSound() {
        this.getWorld().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.ENTITY_ARMOR_STAND_BREAK, this.getSoundCategory(), 1.0f, 1.0f);
    }

    @Override
    protected float turnHead(float bodyRotation, float headRotation) {
        this.prevBodyYaw = this.prevYaw;
        this.bodyYaw = this.getYaw();
        return 0.0f;
    }

    @Override
    protected float getActiveEyeHeight(EntityPose pose, EntityDimensions dimensions) {
        return dimensions.height * (this.isBaby() ? 0.5f : 0.9f);
    }

    @Override
    public void travel(Vec3d movementInput) {
        if (!this.canClip()) {
            return;
        }
        super.travel(movementInput);
    }

    @Override
    public void setBodyYaw(float bodyYaw) {
        this.prevBodyYaw = this.prevYaw = bodyYaw;
        this.prevHeadYaw = this.headYaw = bodyYaw;
    }

    @Override
    public void setHeadYaw(float headYaw) {
        this.prevBodyYaw = this.prevYaw = headYaw;
        this.prevHeadYaw = this.headYaw = headYaw;
    }

    @Override
    public void tick() {
        // POSES
/*        EulerAngle eulerAngle6;
        EulerAngle eulerAngle5;
        EulerAngle eulerAngle4;
        EulerAngle eulerAngle3;
        EulerAngle eulerAngle2;*/
        super.tick();
        // POSES
        /*EulerAngle eulerAngle = this.dataTracker.get(TRACKER_HEAD_ROTATION);
        if (!this.headRotation.equals(eulerAngle)) {
            this.setHeadRotation(eulerAngle);
        }
        if (!this.bodyRotation.equals(eulerAngle2 = this.dataTracker.get(TRACKER_BODY_ROTATION))) {
            this.setBodyRotation(eulerAngle2);
        }
        if (!this.leftArmRotation.equals(eulerAngle3 = this.dataTracker.get(TRACKER_LEFT_ARM_ROTATION))) {
            this.setLeftArmRotation(eulerAngle3);
        }
        if (!this.rightArmRotation.equals(eulerAngle4 = this.dataTracker.get(TRACKER_RIGHT_ARM_ROTATION))) {
            this.setRightArmRotation(eulerAngle4);
        }
        if (!this.leftLegRotation.equals(eulerAngle5 = this.dataTracker.get(TRACKER_LEFT_LEG_ROTATION))) {
            this.setLeftLegRotation(eulerAngle5);
        }
        if (!this.rightLegRotation.equals(eulerAngle6 = this.dataTracker.get(TRACKER_RIGHT_LEG_ROTATION))) {
            this.setRightLegRotation(eulerAngle6);
        }*/
    }

    @Override
    protected void updatePotionVisibility() {
        this.setInvisible(this.invisible);
    }

    @Override
    public void setInvisible(boolean invisible) {
        this.invisible = invisible;
        super.setInvisible(invisible);
    }

    @Override
    public boolean isBaby() {
        return this.isSmall();
    }

    @Override
    public void kill() {
        this.remove(Entity.RemovalReason.KILLED);
        this.emitGameEvent(GameEvent.ENTITY_DIE);
    }

    @Override
    public boolean isImmuneToExplosion(Explosion explosion) {
        return this.isInvisible();
    }

    @Override
    public PistonBehavior getPistonBehavior() {
        if (this.isMarker()) {
            return PistonBehavior.IGNORE;
        }
        return super.getPistonBehavior();
    }

    @Override
    public boolean canAvoidTraps() {
        return this.isMarker();
    }

    private void setSmall(boolean small) {
        this.dataTracker.set(HORSE_ARMOR_STAND_FLAGS, this.setBitField(this.dataTracker.get(HORSE_ARMOR_STAND_FLAGS), SMALL_FLAG, small));
    }

    public boolean isSmall() {
        return (this.dataTracker.get(HORSE_ARMOR_STAND_FLAGS) & 1) != 0;
    }

    public void setHideBasePlate(boolean hideBasePlate) {
        this.dataTracker.set(HORSE_ARMOR_STAND_FLAGS, this.setBitField(this.dataTracker.get(HORSE_ARMOR_STAND_FLAGS), HIDE_BASE_PLATE_FLAG, hideBasePlate));
    }

    public boolean shouldHideBasePlate() {
        return (this.dataTracker.get(HORSE_ARMOR_STAND_FLAGS) & 8) != 0;
    }

    private void setMarker(boolean marker) {
        this.dataTracker.set(HORSE_ARMOR_STAND_FLAGS, this.setBitField(this.dataTracker.get(HORSE_ARMOR_STAND_FLAGS), MARKER_FLAG, marker));
    }

    public boolean isMarker() {
        return (this.dataTracker.get(HORSE_ARMOR_STAND_FLAGS) & 0x10) != 0;
    }

    private byte setBitField(byte value, int bitField, boolean set) {
        value = set ? (byte) (value | bitField) : (byte) (value & ~bitField);
        return value;
    }

    // POSES
    /*public void setHeadRotation(EulerAngle angle) {
        this.headRotation = angle;
        this.dataTracker.set(TRACKER_HEAD_ROTATION, angle);
    }

    public void setBodyRotation(EulerAngle angle) {
        this.bodyRotation = angle;
        this.dataTracker.set(TRACKER_BODY_ROTATION, angle);
    }

    public void setLeftArmRotation(EulerAngle angle) {
        this.leftArmRotation = angle;
        this.dataTracker.set(TRACKER_LEFT_ARM_ROTATION, angle);
    }

    public void setRightArmRotation(EulerAngle angle) {
        this.rightArmRotation = angle;
        this.dataTracker.set(TRACKER_RIGHT_ARM_ROTATION, angle);
    }

    public void setLeftLegRotation(EulerAngle angle) {
        this.leftLegRotation = angle;
        this.dataTracker.set(TRACKER_LEFT_LEG_ROTATION, angle);
    }

    public void setRightLegRotation(EulerAngle angle) {
        this.rightLegRotation = angle;
        this.dataTracker.set(TRACKER_RIGHT_LEG_ROTATION, angle);
    }

    public EulerAngle getHeadRotation() {
        return this.headRotation;
    }

    public EulerAngle getBodyRotation() {
        return this.bodyRotation;
    }

    public EulerAngle getLeftArmRotation() {
        return this.leftArmRotation;
    }

    public EulerAngle getRightArmRotation() {
        return this.rightArmRotation;
    }

    public EulerAngle getLeftLegRotation() {
        return this.leftLegRotation;
    }

    public EulerAngle getRightLegRotation() {
        return this.rightLegRotation;
    }
*/
    @Override
    public boolean canHit() {
        return super.canHit() && !this.isMarker();
    }

    @Override
    public boolean handleAttack(Entity attacker) {
        return attacker instanceof PlayerEntity && !this.getWorld().canPlayerModifyAt((PlayerEntity) attacker, this.getBlockPos());
    }

    @Override
    public Arm getMainArm() {
        return Arm.RIGHT;
    }

    @Override
    public LivingEntity.FallSounds getFallSounds() {
        return new LivingEntity.FallSounds(SoundEvents.ENTITY_ARMOR_STAND_FALL, SoundEvents.ENTITY_ARMOR_STAND_FALL);
    }

    @Override
    @Nullable
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.ENTITY_ARMOR_STAND_HIT;
    }

    @Override
    @Nullable
    protected SoundEvent getDeathSound() {
        return SoundEvents.ENTITY_ARMOR_STAND_BREAK;
    }

    @Override
    public void onStruckByLightning(ServerWorld world, LightningEntity lightning) {
    }

    @Override
    public boolean isAffectedBySplashPotions() {
        return false;
    }

    @Override
    public void onTrackedDataSet(TrackedData<?> data) {
        if (HORSE_ARMOR_STAND_FLAGS.equals(data)) {
            this.calculateDimensions();
            this.intersectionChecked = !this.isMarker();
        }
        super.onTrackedDataSet(data);
    }

    @Override
    public boolean isMobOrPlayer() {
        return false;
    }

    @Override
    public EntityDimensions getDimensions(EntityPose pose) {
        return this.getDimensions(this.isMarker());
    }

    private EntityDimensions getDimensions(boolean marker) {
        if (marker) {
            return MARKER_DIMENSIONS;
        }
        return this.isBaby() ? SMALL_DIMENSIONS : this.getType().getDimensions();
    }

    @Override
    public Vec3d getClientCameraPosVec(float tickDelta) {
        if (this.isMarker()) {
            Box box = this.getDimensions(false).getBoxAt(this.getPos());
            BlockPos blockPos = this.getBlockPos();
            int i = Integer.MIN_VALUE;
            for (BlockPos blockPos2 : BlockPos.iterate(BlockPos.ofFloored(box.minX, box.minY, box.minZ), BlockPos.ofFloored(box.maxX, box.maxY, box.maxZ))) {
                int j = Math.max(this.getWorld().getLightLevel(LightType.BLOCK, blockPos2), this.getWorld().getLightLevel(LightType.SKY, blockPos2));
                if (j == 15) {
                    return Vec3d.ofCenter(blockPos2);
                }
                if (j <= i) continue;
                i = j;
                blockPos = blockPos2.toImmutable();
            }
            return Vec3d.ofCenter(blockPos);
        }
        return super.getClientCameraPosVec(tickDelta);
    }

    @Override
    public ItemStack getPickBlockStack() {
        return new ItemStack(ModItems.HORSE_ARMOR_STAND_ITEM);
    }

    @Override
    public boolean isPartOfGame() {
        return !this.isInvisible() && !this.isMarker();
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllerRegistrar) {
        controllerRegistrar.add(new AnimationController<>(this, "controller", 0, this::predicate));
    }

    private <T extends GeoAnimatable> PlayState predicate(final AnimationState<T> tAnimationState) {
        if (playSwayAnimation) {
            playSwayAnimation = false;
            return tAnimationState.setAndContinue(SWAY_ANIMATION);
        }
        //tAnimationState.getController().setAnimation(IDLE_ANIMATION);
        tAnimationState.getController().forceAnimationReset();
        return PlayState.CONTINUE;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    public void SetPlaySwayAnimation(boolean b) {
        playSwayAnimation = b;
    }
}
