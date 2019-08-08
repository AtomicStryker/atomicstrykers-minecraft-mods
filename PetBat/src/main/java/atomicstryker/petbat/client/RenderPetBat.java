package atomicstryker.petbat.client;

import atomicstryker.petbat.common.EntityPetBat;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.LivingRenderer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nullable;

public class RenderPetBat extends LivingRenderer<EntityPetBat, ModelPetBat> {

    private ResourceLocation tex = new ResourceLocation("petbat", "textures/model/petbat.png");
    private ResourceLocation texGlis = new ResourceLocation("petbat", "textures/model/petbat_glister.png");

    public RenderPetBat(EntityRendererManager manager) {
        super(manager, new ModelPetBat(), 0.25F);

        // TODO figure out render layer
        // layerRenderers.add(renderModel);
    }

    @Override
    protected void preRenderCallback(EntityPetBat par1LivingEntity, float par2) {
        GL11.glScalef(0.35F, 0.35F, 0.35F);
    }

    @Override
    protected void applyRotations(EntityPetBat par1LivingEntity, float par2, float par3, float par4) {
        this.rotateRenderedModel(par1LivingEntity, par2, par3, par4);
    }

    private void rotateRenderedModel(EntityPetBat par1EntityPetBat, float par2, float par3, float par4) {
        if (!par1EntityPetBat.getIsBatHanging()) {
            GL11.glTranslatef(0.0F, MathHelper.cos(par2 * 0.3F) * 0.1F, 0.0F);
        } else {
            GL11.glTranslatef(0.0F, -0.1F, 0.0F);
        }

        super.applyRotations(par1EntityPetBat, par2, par3, par4);
    }

    @Override
    public void renderName(EntityPetBat par1LivingEntity, double par2, double par4, double par6) {
        String name = (par1LivingEntity).getName().getUnformattedComponentText();
        if (!name.equals("")) {
            renderLivingLabel(par1LivingEntity, name, par2, par4 - 1D, par6, 64);
        }
        super.renderName(par1LivingEntity, par2, par4, par6);
    }

    @Nullable
    @Override
    protected ResourceLocation getEntityTexture(EntityPetBat entity) {
        return entity.getBatLevel() > 5 ? texGlis : tex;
    }

    /*
     * should maybe already work by layerRenderers.add in constructor
     * @Override protected int shouldRenderPass(LivingEntity
     * par1LivingEntity, int par2, float par3) { if (par2 == 2 &&
     * ((EntityPetBat)par1LivingEntity).getBatLevel() > 5) {
     * setRenderPassModel(renderModel); return 15; } return -1; }
     */
}
