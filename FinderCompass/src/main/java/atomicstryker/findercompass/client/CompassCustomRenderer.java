package atomicstryker.findercompass.client;


public class CompassCustomRenderer // implements IItemRenderer
{
    /*
    private final float[] strongholdNeedlecolor = { 0.4f, 0f, 0.6f };
    
    private final RenderItem renderItem;
    private final Minecraft mc;
    
    public CompassCustomRenderer()
    {
        renderItem = (RenderItem) RenderManager.instance.entityRenderMap.get(EntityItem.class);
        mc = Minecraft.getMinecraft();
    }

    @Override
    public boolean handleRenderType(ItemStack item, ItemRenderType type)
    {
        switch (type)
        {
        case EQUIPPED_FIRST_PERSON:
            return true;
        case INVENTORY:
            return true;
        default:
            return false;
        }
    }

    @Override
    public boolean shouldUseRenderHelper(ItemRenderType type, ItemStack item, ItemRendererHelper helper)
    {
        // uses renderhelper because our needle needs to match the item bobbing around and stuff
        return type == ItemRenderType.EQUIPPED_FIRST_PERSON;
    }

    @Override
    public void renderItem(ItemRenderType type, ItemStack item, Object... data)
    {        
        switch (type)
        {
            case EQUIPPED_FIRST_PERSON:
            {
                // these translation/rotation values are taken from vanilla code somewhere
                renderCompassFirstPerson((RenderBlocks) data[0], item, -0.4f, 0.8f, 0.9f, 75f);
                break;
            }
            case INVENTORY:
            {
                renderCompassInventory((RenderBlocks) data[0], item);
                break;
            }
            default:
            {
            }
        }
    }
    
    private void renderCompassInventory(RenderBlocks renderBlocks, ItemStack item)
    {
        // vanilla render first
        IIcon icon = item.getIconIndex();
        renderItem.renderIcon(0, 0, icon, 16, 16);
        
        // save current ogl state for later
        GL11.glPushAttrib(GL11.GL_ENABLE_BIT);
        
        // switch off ogl stuff that breaks our rendering needs
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_CULL_FACE);
        GL11.glEnable(GL11.GL_BLEND);
        
        GL11.glTranslatef(8f, 8f, 0); // translate to the middle of the icon
        GL11.glRotatef(180, 0, 0, 1f); // flip 180 degrees because it is facing that wrong way without renderhelper
        
        CompassSetting css = FinderCompassClientTicker.instance.getCurrentSetting();
        
        for (Entry<CompassTargetData, BlockPos> entryTarget : css.getCustomNeedleTargets().entrySet())
        {
            final int[] configInts = css.getCustomNeedles().get(entryTarget.getKey());
            drawInventoryNeedle((float)configInts[0]/255f, (float)configInts[1]/255f, (float)configInts[2]/255f, computeNeedleHeading(entryTarget.getValue()));
        }
        
        if (css.isStrongholdNeedleEnabled() && FinderCompassLogic.hasStronghold)
        {
            drawInventoryNeedle(strongholdNeedlecolor[0], strongholdNeedlecolor[1], strongholdNeedlecolor[2], computeNeedleHeading(FinderCompassLogic.strongholdCoords));
        }
        
        // restore ogl state
        GL11.glPopAttrib();
    }
    
    private void drawInventoryNeedle(float r, float g, float b, float angle)
    {
        GL11.glRotatef(angle, 0, 0, 1f); // rotate around z axis, which is in the icon middle after our translation
        
        GL11.glBegin(GL11.GL_QUADS); // set ogl mode, need quads
        GL11.glColor4f(r, g, b, 0.75F); // set color
        
        // now draw each glorious needle as single quad
        GL11.glVertex3d(-1, -1, 0); // lower left
        GL11.glVertex3d(1, -1, 0); // lower right
        GL11.glVertex3d(1, 4, 0); // upper right
        GL11.glVertex3d(-1, 4, 0); // upper left
        
        GL11.glEnd(); // let ogl draw it all
        
        GL11.glRotatef(-angle, 0, 0, 1f); // revert rotation for next needle
        GL11.glTranslatef(0, 0, 0.01f); // translate slightly up
    }
    */

}
