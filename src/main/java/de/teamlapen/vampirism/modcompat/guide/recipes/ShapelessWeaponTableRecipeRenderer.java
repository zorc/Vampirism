package de.teamlapen.vampirism.modcompat.guide.recipes;

import com.mojang.blaze3d.matrix.MatrixStack;
import de.maxanier.guideapi.api.impl.Book;
import de.maxanier.guideapi.api.impl.abstraction.CategoryAbstract;
import de.maxanier.guideapi.api.impl.abstraction.EntryAbstract;
import de.maxanier.guideapi.api.util.GuiHelper;
import de.maxanier.guideapi.api.util.IngredientCycler;
import de.maxanier.guideapi.gui.BaseScreen;
import de.teamlapen.vampirism.inventory.recipes.ShapelessWeaponTableRecipe;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;


public class ShapelessWeaponTableRecipeRenderer extends BasicWeaponTableRecipeRenderer<ShapelessWeaponTableRecipe> {
    public ShapelessWeaponTableRecipeRenderer(ShapelessWeaponTableRecipe recipe) {
        super(recipe);
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void draw(MatrixStack stack, Book book, CategoryAbstract categoryAbstract, EntryAbstract entryAbstract, int guiLeft, int guiTop, int mouseX, int mouseY, BaseScreen baseScreen, FontRenderer fontRenderer, IngredientCycler ingredientCycler) {
        super.draw(stack, book, categoryAbstract, entryAbstract, guiLeft, guiTop, mouseX, mouseY, baseScreen, fontRenderer, ingredientCycler);
        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 3; x++) {
                int i = 3 * y + x;
                if (i < recipe.getIngredients().size()) {
                    int stackX = (x + 1) * 17 + (guiLeft + 49);
                    int stackY = (y + 1) * 17 + (guiTop + 30);
                    Ingredient ingredient = recipe.getIngredients().get(i);
                    ingredientCycler.getCycledIngredientStack(ingredient, i).ifPresent(itemStack -> {
                        GuiHelper.drawItemStack(stack, itemStack, stackX, stackY);
                        if (GuiHelper.isMouseBetween(mouseX, mouseY, stackX, stackY, 15, 15)) {
                            tooltips = GuiHelper.getTooltip(itemStack);
                        }
                    });
                }
            }
        }
    }

    @Override
    protected IFormattableTextComponent getRecipeName() {
        return new TranslationTextComponent("guideapi.text.crafting.shapeless");
    }
}
