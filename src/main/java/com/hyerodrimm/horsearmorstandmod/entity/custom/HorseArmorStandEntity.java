package com.hyerodrimm.horsearmorstandmod.entity.custom;


import com.hyerodrimm.horsearmorstandmod.entity.ModEntities;
import com.hyerodrimm.horsearmorstandmod.item.ModItems;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.*;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.item.AnimalArmorItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
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
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animatable.instance.SingletonAnimatableInstanceCache;
import software.bernie.geckolib.animation.*;
import software.bernie.geckolib.animation.AnimationState;
import java.util.List;
import java.util.function.Predicate;


public class HorseArmorStandEntity extends LivingEntity implements GeoEntity {
    protected static final RawAnimation SWAY_ANIMATION = RawAnimation.begin().then("animation.horsearmorstand.sway", Animation.LoopType.PLAY_ONCE);
    private boolean playSwayAnimation = false;
    private AnimatableInstanceCache cache = new SingletonAnimatableInstanceCache(this);
    private static final EntityDimensions MARKER_DIMENSIONS = EntityDimensions.fixed(0.0F, 0.0F);
    private static final EntityDimensions SMALL_DIMENSIONS = ModEntities.HORSE_ARMOR_STAND.getDimensions().scaled(0.5f);
    public static final int SMALL_FLAG = 1;
    public static final int HIDE_BASE_PLATE_FLAG = 8;
    public static final int MARKER_FLAG = 16;
    public static final TrackedData<Byte> HORSE_ARMOR_STAND_FLAGS = DataTracker.registerData(HorseArmorStandEntity.class, TrackedDataHandlerRegistry.BYTE);
    private static final Predicate<Entity> RIDEABLE_MINECART_PREDICATE = entity -> entity instanceof AbstractMinecartEntity && ((AbstractMinecartEntity) entity).getMinecartType() == AbstractMinecartEntity.Type.RIDEABLE;
    private final DefaultedList<ItemStack> armorItems = DefaultedList.ofSize(4, ItemStack.EMPTY);;
    private ItemStack bodyArmor = ItemStack.EMPTY;
    private boolean invisible;
    public long lastHitTime;
    private int disabledSlots;

    public HorseArmorStandEntity(EntityType<? extends HorseArmorStandEntity> entityType, World world) {
        super((EntityType<? extends LivingEntity>) entityType, world);
    }

    public static DefaultAttributeContainer.Builder createArmorStandAttributes() {
        return createLivingAttributes().add(EntityAttributes.GENERIC_STEP_HEIGHT, 0.0);
    }

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

    protected void initDataTracker(DataTracker.Builder builder) {
        super.initDataTracker(builder);
        builder.add(HORSE_ARMOR_STAND_FLAGS, (byte)0);
    }

    public ItemStack getArmorType() {
        return this.bodyArmor;
    }

    @Override
    public ItemStack getEquippedStack(EquipmentSlot slot) {
        switch (slot.getType()) {
            case ANIMAL_ARMOR: {
                return this.bodyArmor;
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
    public void equipStack(EquipmentSlot slot, ItemStack stack) {
        this.processEquippedStack(stack);
        switch (slot.getType()) {
            case ANIMAL_ARMOR: {
                this.bodyArmor = stack;
            }
        }
    }

    @Override
    public boolean canEquip(ItemStack stack) {
        return !stack.isEmpty() && stack.getItem() instanceof AnimalArmorItem && this.getArmorType().isEmpty() && !this.isSlotDisabled(EquipmentSlot.BODY);
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        nbt.put("BodyArmor", this.bodyArmor.encode(this.getRegistryManager()));
        nbt.putBoolean("Invisible", this.isInvisible());
        nbt.putBoolean("Small", this.isSmall());
        nbt.putInt("DisabledSlots", this.disabledSlots);
        nbt.putBoolean("NoBasePlate", this.shouldHideBasePlate());
        if (this.isMarker()) {
            nbt.putBoolean("Marker", this.isMarker());
        }
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        if (nbt.contains("BodyArmor", NbtElement.COMPOUND_TYPE)) {
            this.bodyArmor = ItemStack.fromNbt(this.getRegistryManager(), nbt.getCompound("BodyArmor")).orElse(ItemStack.EMPTY);
        } else {
            this.bodyArmor = ItemStack.EMPTY;
        }
        this.setInvisible(nbt.getBoolean("Invisible"));
        this.setSmall(nbt.getBoolean("Small"));
        this.disabledSlots = nbt.getInt("DisabledSlots");
        this.setHideBasePlate(nbt.getBoolean("NoBasePlate"));
        this.setMarker(nbt.getBoolean("Marker"));
        this.noClip = !this.canClip();
    }

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
            if (!this.bodyArmor.isEmpty() && this.equip(player, EquipmentSlot.BODY, itemStack, hand)) {
                return ActionResult.SUCCESS;
            }
        } else if (isHorseArmor(itemStack)) {
            if (this.equip(player, EquipmentSlot.BODY, itemStack, hand)) {
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
        if (this.isRemoved()) {
            return false;
        } else {
            World var4 = this.getWorld();
            if (var4 instanceof ServerWorld) {
                ServerWorld serverWorld = (ServerWorld)var4;
                if (source.isIn(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
                    this.kill();
                    return false;
                } else if (!this.isInvulnerableTo(source) && !this.invisible && !this.isMarker()) {
                    if (source.isIn(DamageTypeTags.IS_EXPLOSION)) {
                        this.onBreak(serverWorld, source);
                        this.kill();
                        return false;
                    } else if (source.isIn(DamageTypeTags.IGNITES_ARMOR_STANDS)) {
                        if (this.isOnFire()) {
                            this.updateHealth(serverWorld, source, 0.15F);
                        } else {
                            this.setOnFireFor(5.0F);
                        }

                        return false;
                    } else if (source.isIn(DamageTypeTags.BURNS_ARMOR_STANDS) && this.getHealth() > 0.5F) {
                        this.updateHealth(serverWorld, source, 4.0F);
                        return false;
                    } else {
                        boolean bl = source.isIn(DamageTypeTags.CAN_BREAK_ARMOR_STAND);
                        boolean bl2 = source.isIn(DamageTypeTags.ALWAYS_KILLS_ARMOR_STANDS);
                        if (!bl && !bl2) {
                            return false;
                        } else {
                            Entity var7 = source.getAttacker();
                            if (var7 instanceof PlayerEntity) {
                                PlayerEntity playerEntity = (PlayerEntity)var7;
                                if (!playerEntity.getAbilities().allowModifyWorld) {
                                    return false;
                                }
                            }

                            if (source.isSourceCreativePlayer()) {
                                this.playBreakSound();
                                this.spawnBreakParticles();
                                this.kill();
                                return true;
                            } else {
                                long l = serverWorld.getTime();
                                if (l - this.lastHitTime > 5L && !bl2) {
                                    serverWorld.sendEntityStatus(this, (byte)32);
                                    this.emitGameEvent(GameEvent.ENTITY_DAMAGE, source.getAttacker());
                                    this.lastHitTime = l;
                                } else {
                                    this.breakAndDropItem(serverWorld, source);
                                    this.spawnBreakParticles();
                                    this.kill();
                                }

                                return true;
                            }
                        }
                    }
                } else {
                    return false;
                }
            } else {
                return false;
            }
        }
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
    public Iterable<ItemStack> getArmorItems() {
        return this.armorItems;
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

    private void updateHealth(ServerWorld world, DamageSource damageSource, float amount) {
        float f = this.getHealth();
        f -= amount;
        if (f <= 0.5F) {
            this.onBreak(world, damageSource);
            this.kill();
        } else {
            this.setHealth(f);
            this.emitGameEvent(GameEvent.ENTITY_DAMAGE, damageSource.getAttacker());
        }

    }

    private void breakAndDropItem(ServerWorld world, DamageSource damageSource) {
        ItemStack itemStack = new ItemStack(ModItems.HORSE_ARMOR_STAND_ITEM);
        itemStack.set(DataComponentTypes.CUSTOM_NAME, this.getCustomName());
        Block.dropStack(this.getWorld(), this.getBlockPos(), itemStack);
        this.onBreak(world, damageSource);
    }

    private void onBreak(ServerWorld world, DamageSource damageSource) {
        this.playBreakSound();
        this.drop(world, damageSource);

        if (!this.bodyArmor.isEmpty()) {
            Block.dropStack(this.getWorld(), this.getBlockPos().up(), this.bodyArmor);
            this.bodyArmor = ItemStack.EMPTY;
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
        super.tick();
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
    public EntityDimensions getBaseDimensions(EntityPose pose) {
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

    public boolean isHorseArmor(ItemStack stack) {
        Item var3 = stack.getItem();
        if (var3 instanceof AnimalArmorItem animalArmorItem) {
            if (animalArmorItem.getType() == AnimalArmorItem.Type.EQUESTRIAN) {
                return true;
            }
        }

        return false;
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
