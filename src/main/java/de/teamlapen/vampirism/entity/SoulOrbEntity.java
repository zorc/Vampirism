package de.teamlapen.vampirism.entity;

import de.teamlapen.vampirism.core.ModEntities;
import de.teamlapen.vampirism.core.ModItems;
import de.teamlapen.vampirism.util.Helper;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.IRendersAsItem;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityPredicates;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.network.NetworkHooks;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Similar to EntityXPOrb
 */
@OnlyIn(
        value = Dist.CLIENT,
        _interface = IRendersAsItem.class
)
public class SoulOrbEntity extends Entity implements IRendersAsItem {

    public static final DataParameter<String> TYPE_PARAMETER = EntityDataManager.createKey(SoulOrbEntity.class, DataSerializers.STRING);
    private int delayBeforePickup;
    private PlayerEntity player;
    private int age;
    @Nullable
    private ItemStack soulItemStack;

    public SoulOrbEntity(World worldIn, double x, double y, double z, VARIANT type) {
        super(ModEntities.soul_orb, worldIn);
        this.setVariant(type);
        delayBeforePickup = 10;
        this.setPosition(x, y, z);
        this.rotationYaw = (float) (Math.random() * 360.0D);
        this.setMotion((this.rand.nextDouble() * (double) 0.2F - (double) 0.1F) * 2.0D, this.rand.nextDouble() * 0.2D * 2.0D, (this.rand.nextDouble() * (double) 0.2F - (double) 0.1F) * 2.0D);
    }

    public SoulOrbEntity(EntityType<? extends SoulOrbEntity> type, World worldIn) {
        super(type, worldIn);
    }

    @Override
    public boolean attackEntityFrom(DamageSource source, float amount) {
        return false;
    }

    @Override
    public boolean canBeAttackedWithItem() {
        return false;
    }

    @Override
    public IPacket<?> createSpawnPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }


    @Override
    public ItemStack getItem() {
        return getSoulItemStack();
    }

    @Nonnull
    public ItemStack getSoulItemStack() {
        if (soulItemStack == null) {
            soulItemStack = createSoulItemStack();
        }
        return soulItemStack;
    }

    public VARIANT getVariant() {
        return VARIANT.valueOf(getDataManager().get(TYPE_PARAMETER));
    }

    private void setVariant(VARIANT type) {
        getDataManager().set(TYPE_PARAMETER, type.name());
    }

    @Override
    public boolean isInvisibleToPlayer(@Nonnull PlayerEntity player) {
        switch (getVariant()) {
            case VAMPIRE:
                return !Helper.isHunter(player) || player.isSpectator();
            default:
                return true;
        }
    }

    @Override
    public void onCollideWithPlayer(PlayerEntity entityIn) {
        if (!this.world.isRemote) {
            if (delayBeforePickup == 0) {
                if (Helper.isHunter(entityIn)) {
                    if (entityIn.inventory.addItemStackToInventory(getSoulItemStack())) {
                        entityIn.onItemPickup(this, 1);
                        this.remove();
                    }
                }
            }
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (delayBeforePickup > 0) {
            delayBeforePickup--;
        }

        this.prevPosX = this.getPosX();
        this.prevPosY = this.getPosY();
        this.prevPosZ = this.getPosZ();

        if (this.areEyesInFluid(FluidTags.WATER)) {
            Vector3d vec3d = this.getMotion();
            this.setMotion(vec3d.x * (double) 0.99F, Math.min(vec3d.y + (double) 5.0E-4F, 0.06F), vec3d.z * (double) 0.99F);
        } else if (!this.hasNoGravity()) {
            this.setMotion(this.getMotion().add(0.0D, -0.03D, 0.0D));
        }

        if (this.world.getFluidState(getPosition()).isTagged(FluidTags.LAVA)) {
            this.setMotion((this.rand.nextFloat() - this.rand.nextFloat()) * 0.2F, 0.2F, (this.rand.nextFloat() - this.rand.nextFloat()) * 0.2F);
            this.playSound(SoundEvents.ENTITY_GENERIC_BURN, 0.4F, 2.0F + this.rand.nextFloat() * 0.4F);
        }
        if (!this.world.hasNoCollisions(this.getBoundingBox())) { //areCollisionShapesEmpty
            this.pushOutOfBlocks(this.getPosX(), (this.getBoundingBox().minY + this.getBoundingBox().maxY) / 2.0D, this.getPosZ());
        }


        if (this.age % 10 == 5 & (this.player == null || !this.player.isAlive() || this.player.getDistanceSq(this) > 64)) {
            this.player = this.world.getClosestPlayer(this.getPosX(), this.getPosY(), this.getPosZ(), 8, EntityPredicates.NOT_SPECTATING.and(Helper::isHunter));
        }

        if (this.player != null) {
            Vector3d vec3d = new Vector3d(this.player.getPosX() - this.getPosX(), this.player.getPosY() + (double) this.player.getEyeHeight() / 2.0D - this.getPosY(), this.player.getPosZ() - this.getPosZ());
            double d1 = vec3d.lengthSquared();
            if (d1 < 64.0D) {
                double d2 = 1.0D - Math.sqrt(d1) / 8.0D;
                this.setMotion(this.getMotion().add(vec3d.normalize().scale(d2 * d2 * 0.1D)));
            }
        }

        this.move(MoverType.SELF, this.getMotion());
        float f = 0.98F;

        if (this.onGround) {
            BlockPos underPos = new BlockPos(MathHelper.floor(this.getPosX()), MathHelper.floor(this.getBoundingBox().minY) - 1, MathHelper.floor(this.getPosZ()));
            BlockState underState = this.world.getBlockState(underPos);
            f = underState.getBlock().getSlipperiness(underState, this.world, underPos, this) * 0.98F;
        }

        this.setMotion(this.getMotion().mul(f, 0.9800000190734863D, f));

        if (this.onGround) {
            this.setMotion(this.getMotion().mul(1D, -0.8999999761581421D, 1D));
        }


        this.age++;

        if (this.age >= 6000) {
            this.remove();
        }
    }

    @Override
    protected boolean canTriggerWalking() {
        return false;
    }

    @Override
    protected void readAdditional(CompoundNBT compound) {
        this.setVariant(VARIANT.valueOf(compound.getString("type")));
        this.age = compound.getInt("age");
        soulItemStack = null;//Reset item just in case a item of a different type has been created beforehand
    }

    @Override
    protected void registerData() {
        this.getDataManager().register(TYPE_PARAMETER, VARIANT.NONE.name());
    }

    @Override
    protected void writeAdditional(CompoundNBT compound) {
        compound.putString("type", this.getVariant().name());
        compound.putInt("age", age);
    }

    private ItemStack createSoulItemStack() {
        switch (getVariant()) {
            case VAMPIRE:
                return new ItemStack(ModItems.soul_orb_vampire);
            default:
                return new ItemStack(ModItems.soul_orb_vampire);
        }
    }


    public enum VARIANT {
        NONE, VAMPIRE
    }
}
