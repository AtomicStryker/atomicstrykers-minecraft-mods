package atomicstryker.minions.client;

import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;

/**
 * Minion Model class
 * 
 * 
 * @author AtomicStryker
 */

public class ModelMinion extends ModelBiped
{
	public ModelRenderer backPack;
	public boolean carryAnimation = false;
	
    public ModelMinion()
    {
        this(0.0F);
    }

    public ModelMinion(float var1)
    {
        this(var1, 0.0F);
    }

    public ModelMinion(float var1, float var2)
    {
		//super(0.0F);
        this.heldItemLeft = 0;
        this.heldItemRight = 0;
        this.isSneak = false;
        this.aimedBow = false;
        this.bipedHeadwear = new ModelRenderer(this, 24, 0);
        this.bipedHeadwear.showModel = false;
        this.bipedHeadwear = new ModelRenderer(this, 32, 0);
		this.bipedHeadwear.showModel = false;

		this.bipedHead = new ModelRenderer(this, 0, 0);
		this.bipedHead.addBox(-3F, -6F, -3F, 6, 5, 4);
		this.bipedHead.setRotationPoint(0F, 12F, 0F);
		setRotation(this.bipedHead, 0F, 0F, 0F);
		
		this.bipedBody = new ModelRenderer(this, 22, 0);
		this.bipedBody.addBox(-4F, -3F, -2F, 8, 8, 4);
		this.bipedBody.setRotationPoint(0F, 14F, 2F);
		setRotation(this.bipedBody, 0F, 0F, 0F);
		
		this.bipedRightLeg = new ModelRenderer(this, 0, 10);
		this.bipedRightLeg.addBox(-2F, 7F, 0F, 2, 5, 3);
		this.bipedRightLeg.setRotationPoint(-1F, 22F, 2F);
		setRotation(this.bipedRightLeg, 0F, 0F, 0F);
		
		this.bipedLeftLeg = new ModelRenderer(this, 0, 10);
		this.bipedLeftLeg.addBox(0F, 7F, -0F, 2, 5, 3);
		this.bipedLeftLeg.setRotationPoint(1F, 22F, 2F);
		this.bipedLeftLeg.mirror = true;
		setRotation(this.bipedLeftLeg, 0F, 0F, 0F);
		
		this.bipedRightArm = new ModelRenderer(this, 0, 19);
		this.bipedRightArm.addBox(-1F, 0F, -1F, 2, 7, 3);
		this.bipedRightArm.setRotationPoint(-4F, 11F, 1F);
		setRotation(this.bipedRightArm, 0F, 0F, 0F);
		
		this.bipedLeftArm = new ModelRenderer(this, 0, 19);
		this.bipedLeftArm.addBox(-1F, 0F, -1F, 2, 7, 3);
		this.bipedLeftArm.setRotationPoint(4F, 11F, 1F);
		this.bipedLeftArm.mirror = true;
		setRotation(this.bipedLeftArm, 0F, 0F, 0F);
		
		this.backPack = new ModelRenderer(this, 11, 13);
		this.backPack.addBox(-3F, -2F, -2F, 6, 7, 5);
		this.backPack.setRotationPoint(0F, 13F, 4F);
		setRotation(this.backPack, 0.7853982F, 0F, 0F);
	}
	
    @Override
	public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5)
	{		
		super.render(entity, f, f1, f2, f3, f4, f5);
		backPack.render(f5);
	}
	
	@Override
    public void setRotationAngles(float var1, float var2, float var3, float var4, float var5, float var6, Entity ent)
    {
    	super.setRotationAngles(var1, var2, var3, var4, var5, var6, ent);
		this.bipedHead.rotationPointY = 12F;
		
		if (carryAnimation)
		{
			bipedRightArm.rotateAngleX = bipedLeftArm.rotateAngleX = 3.141593F;
		}
    }

	private void setRotation(ModelRenderer model, float x, float y, float z)
	{
		model.rotateAngleX = x;
		model.rotateAngleY = y;
		model.rotateAngleZ = z;
	}
}
