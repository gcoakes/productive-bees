package cy.jdkdigital.productivebees.integrations.jei;

import cy.jdkdigital.productivebees.ProductiveBees;
import cy.jdkdigital.productivebees.init.ModTags;
import cy.jdkdigital.productivebees.integrations.jei.ingredients.BeeIngredient;
import cy.jdkdigital.productivebees.setup.BeeReloadListener;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IGuiIngredientGroup;
import mezz.jei.api.gui.ingredient.IGuiItemStackGroup;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.block.Block;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ITag.INamedTag;
import net.minecraft.util.ResourceLocation;

import java.util.*;

public class BeeFloweringRecipeCategory implements IRecipeCategory<BeeFloweringRecipeCategory.Recipe>
{
    public static final ResourceLocation ID = new ResourceLocation(ProductiveBees.MODID, "bee_flowering");
    private final IDrawable icon;
    private final IDrawable background;

    public BeeFloweringRecipeCategory(IGuiHelper guiHelper) {
        ResourceLocation location = new ResourceLocation(ProductiveBees.MODID, "textures/gui/jei/bee_flowering_recipe.png");
        this.background = guiHelper.createDrawable(location, 0, 0, 70, 82);
        this.icon = guiHelper.createDrawableIngredient(new ItemStack(Items.POPPY));
    }
    
    @Override
    public ResourceLocation getUid() {
        return ID;
    }

    @Override
    public Class<? extends Recipe> getRecipeClass() {
        return Recipe.class;
    }

    @Override
    public String getTitle() {
        return I18n.format("jei.productivebees.bee_flowering");
    }

    @Override
    public IDrawable getBackground() {
        return background;
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setIngredients(Recipe recipe, IIngredients ingredients) {
        ingredients.setInputs(ProductiveBeesJeiPlugin.BEE_INGREDIENT, Collections.singletonList(recipe.getBee()));

        List<ItemStack> stacks = new ArrayList<>();
        try {
            for (Block block: recipe.blockTag.getAllElements() ) {
                ItemStack item = new ItemStack(block.asItem());
                stacks.add(item);
            }
        } catch (Exception e) {
            ProductiveBees.LOGGER.error("Failed to find flowering requirements for " + recipe.getBee());
        }
        List<List<ItemStack>> items = new ArrayList<>();
        items.add(stacks);
        ingredients.setInputLists(VanillaTypes.ITEM, items);
    }

    @Override
    public void setRecipe(IRecipeLayout iRecipeLayout, Recipe recipe, IIngredients ingredients) {
        IGuiIngredientGroup<BeeIngredient> ingredientStacks = iRecipeLayout.getIngredientsGroup(ProductiveBeesJeiPlugin.BEE_INGREDIENT);
        ingredientStacks.init(0, true, 29, 12);
        ingredientStacks.set(0, ingredients.getInputs(ProductiveBeesJeiPlugin.BEE_INGREDIENT).get(0));

        IGuiItemStackGroup itemStacks = iRecipeLayout.getItemStacks();
        itemStacks.init(1, true, 26, 51);
        itemStacks.set(1, ingredients.getInputs(VanillaTypes.ITEM).get(0));
    }

    public static List<Recipe> getFlowersRecipes(Map<String, BeeIngredient> beeList) {
        List<Recipe> recipes = new ArrayList<>();

        // Hardcoded for now until bees are moved to config
        Map<String, INamedTag<Block>> flowering = new HashMap<>();
        flowering.put("productivebees:iron_bee", ModTags.FERRIC_FLOWERS);
        flowering.put("productivebees:lumber_bee", BlockTags.LOGS);
        flowering.put("productivebees:wither_bee", ModTags.WITHER_FLOWERS);
        flowering.put("productivebees:draconic_bee", ModTags.DRACONIC_FLOWERS);
        flowering.put("productivebees:ender_bee", ModTags.END_FLOWERS);
        flowering.put("productivebees:glowing_bee", ModTags.GLOWING_FLOWERS);
        flowering.put("productivebees:gold_bee", ModTags.GILDED_FLOWERS);
        flowering.put("productivebees:magmatic_bee", ModTags.MAGMATIC_FLOWERS);
        flowering.put("productivebees:quartz_bee", ModTags.CRYSTALLINE_FLOWERS);
        flowering.put("productivebees:slimy_bee", ModTags.SWAMP_FLOWERS);
        flowering.put("productivebees:blue_banded_bee", ModTags.RIVER_FLOWERS);
        flowering.put("productivebees:green_carpenter_bee", ModTags.FOREST_FLOWERS);
        flowering.put("productivebees:nomad_bee", ModTags.ARID_FLOWERS);
        flowering.put("productivebees:reed_bee", ModTags.SWAMP_FLOWERS);
        flowering.put("productivebees:resin_bee", ModTags.FOREST_FLOWERS);
        flowering.put("productivebees:sweaty_bee", ModTags.SNOW_FLOWERS);
        flowering.put("productivebees:yellow_black_carpenter_bee", ModTags.FOREST_FLOWERS);

        for (Map.Entry<String, BeeIngredient> entry : beeList.entrySet()){
            INamedTag<Block> blockTag = BlockTags.FLOWERS;
            if (entry.getValue().isConfigurable()) {
                CompoundNBT nbt = BeeReloadListener.INSTANCE.getData(entry.getValue().getBeeType());
                if (nbt.contains("flowerTag")) {
                    blockTag = ModTags.getTag(new ResourceLocation(nbt.getString("flowerTag")));
                }
            }
            else if (flowering.containsKey(entry.getValue().getBeeType().toString())) {
                blockTag = flowering.get(entry.getValue().getBeeType().toString());
            }

            recipes.add(new Recipe(blockTag, entry.getValue()));
        }
        return recipes;
    }

    public static class Recipe {

        private final BeeIngredient bee;

        private final INamedTag<Block> blockTag;

        public Recipe(INamedTag<Block> blockTag, BeeIngredient bee) {
            this.blockTag = blockTag;
            this.bee = bee;
        }

        public INamedTag<?> getTag() { return blockTag; }
        public BeeIngredient getBee() { return this.bee; }
    }
}
