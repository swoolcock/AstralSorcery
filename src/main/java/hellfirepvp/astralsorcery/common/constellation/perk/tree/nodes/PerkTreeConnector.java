/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2019
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.constellation.perk.tree.nodes;

import com.google.common.collect.Lists;
import hellfirepvp.astralsorcery.common.constellation.perk.AbstractPerk;
import hellfirepvp.astralsorcery.common.constellation.perk.tree.PerkTree;
import hellfirepvp.astralsorcery.common.data.research.PlayerProgress;
import hellfirepvp.astralsorcery.common.data.research.ResearchManager;
import hellfirepvp.astralsorcery.common.util.log.LogCategory;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nullable;
import java.util.List;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: PerkTreeConnector
 * Created by HellFirePvP
 * Date: 15.07.2018 / 17:29
 */
public class PerkTreeConnector extends MajorPerk {

    private static List<PerkTreeConnector> connectorCache = Lists.newArrayList();

    public PerkTreeConnector(String name, int x, int y) {
        super(name, x, y);
        setCategory(CATEGORY_EPIPHANY);
        connectorCache.add(this);
    }

    @Override
    public boolean mayUnlockPerk(PlayerProgress progress, EntityPlayer player) {
        if (!progress.hasFreeAllocationPoint(player) ||
                !canSee(player, progress)) return false;

        boolean hasAllAdjacent = true;
        for (AbstractPerk otherPerks : PerkTree.PERK_TREE.getConnectedPerks(this)) {
            if (!progress.hasPerkEffect(otherPerks)) {
                hasAllAdjacent = false;
                break;
            }
        }
        if (!hasAllAdjacent) {
            for (PerkTreeConnector conn : connectorCache) {
                if (progress.hasPerkEffect(conn)) {
                    return true;
                }
            }
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void onUnlockPerkServer(@Nullable EntityPlayer player, PlayerProgress progress, NBTTagCompound dataStorage) {
        super.onUnlockPerkServer(player, progress, dataStorage);

        NBTTagList listTokens = new NBTTagList();
        for (AbstractPerk otherPerk : PerkTree.PERK_TREE.getConnectedPerks(this)) {
            if(ResearchManager.forceApplyPerk(player, otherPerk)) {
                String token = "connector-tk-" + otherPerk.getRegistryName().toString();
                if(ResearchManager.grantFreePerkPoint(player, token)) {
                    listTokens.appendTag(new NBTTagString(token));

                    LogCategory.PERKS.info(() -> "Granted perk point " + token + " to " + player.getName());
                }
            }
        }
        dataStorage.setTag("pointtokens", listTokens);
    }

    @Override
    public void onRemovePerkServer(EntityPlayer player, PlayerProgress progress, NBTTagCompound dataStorage) {
        super.onRemovePerkServer(player, progress, dataStorage);

        NBTTagList list = dataStorage.getTagList("pointtokens", Constants.NBT.TAG_STRING);
        for (int i = 0; i < list.tagCount(); i++) {
            String token = list.getStringTagAt(i);
            if (ResearchManager.revokeFreePoint(player, token)){
                LogCategory.PERKS.info(() -> "Revoked perk point " + token + " of " + player.getName());
            }
        }
    }

}
