package com.hollingsworth.arsnouveau.common.block.tile;

import com.hollingsworth.arsnouveau.client.container.CraftingTerminalMenu;
import com.hollingsworth.arsnouveau.client.container.StoredItemStack;
import com.hollingsworth.arsnouveau.setup.BlockRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.Container;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.items.ItemHandlerHelper;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoBlockEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class CraftingLecternTile extends StorageLecternTile implements GeoBlockEntity {
	private AbstractContainerMenu craftingContainer = new AbstractContainerMenu(MenuType.CRAFTING, 0) {
		@Override
		public boolean stillValid(Player player) {
			return false;
		}

		@Override
		public void slotsChanged(Container inventory) {
			if (level != null && !level.isClientSide) {
				onCraftingMatrixChanged();
			}
		}

		@Override
		public ItemStack quickMoveStack(Player p_38941_, int p_38942_) {
			return ItemStack.EMPTY;
		}
	};
	private CraftingRecipe currentRecipe;
	private final CraftingContainer craftMatrix = new TransientCraftingContainer(craftingContainer, 3, 3);
	private ResultContainer craftResult = new ResultContainer();
	private HashSet<CraftingTerminalMenu> craftingListeners = new HashSet<>();


	public CraftingLecternTile(BlockPos pos, BlockState state) {
		super(BlockRegistry.CRAFTING_LECTERN_TILE.get(), pos, state);
	}

	@Override
	public AbstractContainerMenu createMenu(int id, Inventory plInv, Player arg2) {
		return new CraftingTerminalMenu(id, plInv, this);
	}

	@Override
	public void saveAdditional(CompoundTag compound) {
		super.saveAdditional(compound);

		ListTag listnbt = new ListTag();

		for(int i = 0; i < craftMatrix.getContainerSize(); ++i) {
			ItemStack itemstack = craftMatrix.getItem(i);
			if (!itemstack.isEmpty()) {
				CompoundTag compoundnbt = new CompoundTag();
				compoundnbt.putInt("Slot", i);
				itemstack.save(compoundnbt);
				listnbt.add(compoundnbt);
			}
		}

		compound.put("CraftingTable", listnbt);
	}

	private boolean reading;
	@Override
	public void load(CompoundTag compound) {
		super.load(compound);
		reading = true;
		ListTag listnbt = compound.getList("CraftingTable", 10);

		for(int i = 0; i < listnbt.size(); ++i) {
			CompoundTag compoundnbt = listnbt.getCompound(i);
			int j = compoundnbt.getInt("Slot");
			if (j >= 0 && j < craftMatrix.getContainerSize()) {
				craftMatrix.setItem(j, ItemStack.of(compoundnbt));
			}
		}
		reading = false;
	}

	public CraftingContainer getCraftingInv() {
		return craftMatrix;
	}

	public ResultContainer getCraftResult() {
		return craftResult;
	}

	public void craftShift(Player player, @Nullable String tab) {
		List<ItemStack> craftedItemsList = new ArrayList<>();
		int amountCrafted = 0;
		ItemStack crafted = craftResult.getItem(0);
		do {
			craft(player, tab);
			craftedItemsList.add(crafted.copy());
			amountCrafted += crafted.getCount();
		} while(ItemStack.isSame(crafted, craftResult.getItem(0)) && (amountCrafted+crafted.getCount()) <= crafted.getMaxStackSize());

		for (ItemStack craftedItem : craftedItemsList) {
			if (!player.getInventory().add(craftedItem.copy())) {
				ItemStack is = pushStack(craftedItem, tab);
				if(!is.isEmpty()) {
					Containers.dropItemStack(level, player.getX(), player.getY(), player.getZ(), is);
				}
			}
		}

		crafted.onCraftedBy(player.level, player, amountCrafted);
		ForgeEventFactory.firePlayerCraftingEvent(player, ItemHandlerHelper.copyStackWithSize(crafted, amountCrafted), craftMatrix);
	}

	public void craft(Player thePlayer, @Nullable String tab) {
		if(currentRecipe != null) {
			NonNullList<ItemStack> remainder = currentRecipe.getRemainingItems(craftMatrix);
			boolean playerInvUpdate = false;
			for (int i = 0; i < remainder.size(); ++i) {
				ItemStack slot = craftMatrix.getItem(i);
				ItemStack oldItem = slot.copy();
				ItemStack rem = remainder.get(i);
				if (!slot.isEmpty()) {
					craftMatrix.removeItem(i, 1);
					slot = craftMatrix.getItem(i);
				}
				if(slot.isEmpty() && !oldItem.isEmpty()) {
					StoredItemStack is = pullStack(new StoredItemStack(oldItem), 1, tab);
					if(is == null) {
						for(int j = 0;j<thePlayer.getInventory().getContainerSize();j++) {
							ItemStack st = thePlayer.getInventory().getItem(j);
							if(ItemStack.isSame(oldItem, st) && ItemStack.tagMatches(oldItem, st)) {
								st = thePlayer.getInventory().removeItem(j, 1);
								if(!st.isEmpty()) {
									is = new StoredItemStack(st, 1);
									playerInvUpdate = true;
									break;
								}
							}
						}
					}
					if(is != null) {
						craftMatrix.setItem(i, is.getActualStack());
						slot = craftMatrix.getItem(i);
					}
				}
				if (rem.isEmpty()) {
					continue;
				}
				if (slot.isEmpty()) {
					craftMatrix.setItem(i, rem);
					continue;
				}
				if (ItemStack.isSame(slot, rem) && ItemStack.tagMatches(slot, rem)) {
					rem.grow(slot.getCount());
					craftMatrix.setItem(i, rem);
					continue;
				}
				rem = pushStack(rem, tab);
				if(rem.isEmpty())continue;
				if (thePlayer.getInventory().add(rem)) continue;
				thePlayer.drop(rem, false);
			}
			if(playerInvUpdate)thePlayer.containerMenu.broadcastChanges();
			onCraftingMatrixChanged();
		}
	}

	public void unregisterCrafting(CraftingTerminalMenu containerCraftingTerminal) {
		craftingListeners.remove(containerCraftingTerminal);
	}

	public void registerCrafting(CraftingTerminalMenu containerCraftingTerminal) {
		craftingListeners.add(containerCraftingTerminal);
	}

	protected void onCraftingMatrixChanged() {
		if (currentRecipe == null || !currentRecipe.matches(craftMatrix, level)) {
			currentRecipe = level.getRecipeManager().getRecipeFor(RecipeType.CRAFTING, craftMatrix, level).orElse(null);
		}

		if (currentRecipe == null) {
			craftResult.setItem(0, ItemStack.EMPTY);
		} else {
			craftResult.setItem(0, currentRecipe.assemble(craftMatrix, level.registryAccess()));
		}

		craftingListeners.forEach(CraftingTerminalMenu::onCraftMatrixChanged);

		if (!reading) {
			setChanged();
		}
	}

	public void clear(@Nullable String tab) {
		for (int i = 0; i < craftMatrix.getContainerSize(); i++) {
			ItemStack st = craftMatrix.removeItemNoUpdate(i);
			if(!st.isEmpty()) {
				pushOrDrop(st, tab);
			}
		}
		onCraftingMatrixChanged();
	}

	public void handlerItemTransfer(Player player, ItemStack[][] items, @Nullable String tab) {
		clear(tab);
		for (int i = 0;i < 9;i++) {
			if (items[i] != null) {
				ItemStack stack = ItemStack.EMPTY;
				for (int j = 0;j < items[i].length;j++) {
					ItemStack pulled = pullStack(items[i][j], tab);
					if (!pulled.isEmpty()) {
						stack = pulled;
						break;
					}
				}
				if (stack.isEmpty()) {
					for (int j = 0;j < items[i].length;j++) {
						boolean br = false;
						for (int k = 0;k < player.getInventory().getContainerSize();k++) {
							if(ItemStack.isSame(player.getInventory().getItem(k), items[i][j])) {
								stack = player.getInventory().removeItem(k, 1);
								br = true;
								break;
							}
						}
						if (br)
							break;
					}
				}
				if (!stack.isEmpty()) {
					craftMatrix.setItem(i, stack);
				}
			}
		}
		onCraftingMatrixChanged();
	}

	private ItemStack pullStack(ItemStack itemStack, @Nullable String tab) {
		StoredItemStack is = pullStack(new StoredItemStack(itemStack), 1, tab);
		if(is == null)return ItemStack.EMPTY;
		else return is.getActualStack();
	}

	@Override
	public void registerControllers(AnimatableManager.ControllerRegistrar data) {
		data.add(new AnimationController<>(this, "controller", 1, (event -> {
			event.getController().setAnimation(RawAnimation.begin().thenPlay("ledger_float"));
			return PlayState.CONTINUE;
		})));
	}
	software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache AnimatableInstanceCache = GeckoLibUtil.createInstanceCache(this);
	@Override
	public AnimatableInstanceCache getAnimatableInstanceCache() {
		return AnimatableInstanceCache;
	}
}
