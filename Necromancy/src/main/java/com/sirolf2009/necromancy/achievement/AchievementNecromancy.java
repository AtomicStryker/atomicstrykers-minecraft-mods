package com.sirolf2009.necromancy.achievement;

import net.minecraft.init.Items;
import net.minecraft.stats.Achievement;
import net.minecraftforge.common.AchievementPage;

public class AchievementNecromancy
{

    public static Achievement NecronomiconAchieve;
    public static Achievement SpawnAchieve;
    public static Achievement TeddyAchieve;
    public static Achievement SewingAchieve;
    public static Achievement AltarAchieve;
    public static AchievementPage achievePage;

    public static void initAchievements()
    {
        NecronomiconAchieve = new Achievement("666", "NecronomiconAchieve", 0, 0, Items.book, null).registerStat();
        SewingAchieve = new Achievement("667", "SewingAchieve", -2, 0, Items.book, NecronomiconAchieve).registerStat();
        AltarAchieve = new Achievement("668", "AltarAchieve", 0, 2, Items.book, NecronomiconAchieve).registerStat();
        SpawnAchieve = new Achievement("669", "SpawnAchieve", 2, 4, Items.book, AltarAchieve).registerStat().setSpecial();

        achievePage = new AchievementPage("Necromancy", new Achievement[] { NecronomiconAchieve, SpawnAchieve, AltarAchieve, SewingAchieve });
        AchievementPage.registerAchievementPage(achievePage);
    }

}
