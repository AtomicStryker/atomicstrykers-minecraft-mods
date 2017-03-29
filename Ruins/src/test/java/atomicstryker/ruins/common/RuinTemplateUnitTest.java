package atomicstryker.ruins.common;

import java.io.PrintWriter;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import atomicstryker.ruins.common.RuinTemplateRule.SpecialFlags;
import net.minecraft.block.Block;
import net.minecraft.block.IGrowable;

/**
 * This is literally the first time i thought a unit test was a good idea
 * while coding for Minecraft.
 */
public class RuinTemplateUnitTest
{

    private PrintWriter printWriter;
    private RuinTemplate ruinTemplate;

    private Block mockPlanks;
    private Block mockPPPlanks;

    @Before
    public void setup()
    {
        printWriter = new PrintWriter(System.out);
        ruinTemplate = Mockito.mock(RuinTemplate.class);
        mockPlanks = Mockito.mock(Block.class);
        mockPPPlanks = Mockito.mock(Block.class, Mockito.withSettings().extraInterfaces(IGrowable.class));
    }

    @Test
    public void testConstructorCases()
    {
        Mockito.when(ruinTemplate.getName()).thenReturn("Dummy");
        Mockito.when(ruinTemplate.getAirBlock()).thenReturn(null);
        Mockito.when(ruinTemplate.tryFindingBlockOfName("planks")).thenReturn(mockPlanks);
        Mockito.when(ruinTemplate.tryFindingBlockOfName("p-p-planks")).thenReturn(mockPPPlanks);

        try
        {
            RuinTemplateRule testee = new RuinTemplateRule(printWriter, ruinTemplate, "0,100,planks-3");
            Assert.assertSame(testee.blockIDs[0], mockPlanks);
            Assert.assertSame(testee.blockMDs[0], 3);

            testee = new RuinTemplateRule(printWriter, ruinTemplate, "0,100,planks");
            Assert.assertSame(testee.blockIDs[0], mockPlanks);
            Assert.assertSame(testee.blockMDs[0], 0);

            testee = new RuinTemplateRule(printWriter, ruinTemplate, "0,100,p-p-planks-3");
            Assert.assertSame(testee.blockIDs[0], mockPPPlanks);
            Assert.assertSame(testee.blockMDs[0], 3);

            testee = new RuinTemplateRule(printWriter, ruinTemplate, "0,100,p-p-planks");
            Assert.assertSame(testee.blockIDs[0], mockPPPlanks);
            Assert.assertSame(testee.blockMDs[0], 0);

            testee = new RuinTemplateRule(printWriter, ruinTemplate, "0,100,p-p-planks-4-addbonemeal");
            Assert.assertSame(testee.blockIDs[0], mockPPPlanks);
            Assert.assertSame(testee.blockMDs[0], 4);
            Assert.assertSame(testee.specialFlags[0], SpecialFlags.ADDBONEMEAL);
        }
        catch (Exception e)
        {
            System.out.println(e);
            Assert.fail("exception!");
        }
    }

}
