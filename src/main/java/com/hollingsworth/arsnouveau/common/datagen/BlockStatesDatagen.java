package com.hollingsworth.arsnouveau.common.datagen;

//
//public class BlockStatesDatagen extends BlockStateProvider {
//
//    public BlockStatesDatagen(DataGenerator gen, String modid, ExistingFileHelper exFileHelper) {
//        super(gen, modid, exFileHelper);
//    }
//
//    @Override
//    protected void registerStatesAndModels() {
//        registerOnlyState(BlockRegistry.SOURCE_GEM_BLOCK, LibBlockNames.SOURCE_GEM_BLOCK);
//        registerOnlyState(BlockRegistry.RED_SBED, LibBlockNames.RED_SBED);
//        registerOnlyState(BlockRegistry.BLUE_SBED, LibBlockNames.BLUE_SBED);
//        registerOnlyState(BlockRegistry.GREEN_SBED, LibBlockNames.GREEN_SBED);
//        registerOnlyState(BlockRegistry.YELLOW_SBED, LibBlockNames.YELLOW_SBED);
//        registerOnlyState(BlockRegistry.ORANGE_SBED, LibBlockNames.ORANGE_SBED);
//        registerOnlyState(BlockRegistry.PURPLE_SBED, LibBlockNames.PURPLE_SBED);
//        registerOnlyState(BlockRegistry.POTION_DIFFUSER, LibBlockNames.POTION_DIFFUSER);
//        for (var pot : BlockRegistry.flowerPots.entrySet()){
//            registerOnlyState(pot.getValue(), "pots/" + LibBlockNames.Pot(pot.getKey().get().getPath()));
//        }
//        registerDoor(BlockRegistry.ARCHWOOD_DOOR, LibBlockNames.ARCHWOOD_DOOR);
//
//        registerNormalCube(BlockRegistry.VOID_PRISM, LibBlockNames.VOID_PRISM);
//        for (String s : LibBlockNames.DECORATIVE_SOURCESTONE) {
//            registerNormalCube(ForgeRegistries.BLOCKS.getValue(new ResourceLocation(ArsNouveau.MODID, s)), s);
//        }
//        registerNormalCube(BlockRegistry.MAGEBLOOM_BLOCK, LibBlockNames.MAGEBLOOM_BLOCK);
//        registerNormalCube(BlockRegistry.FALSE_WEAVE, LibBlockNames.FALSE_WEAVE);
//        registerNormalCube(BlockRegistry.GHOST_WEAVE, LibBlockNames.GHOST_WEAVE);
//        registerNormalCube(BlockRegistry.MIRROR_WEAVE, LibBlockNames.MIRROR_WEAVE);
//
//        for(String s : LibBlockNames.DECORATIVE_SOURCESTONE){
//            ResourceLocation tex = new ResourceLocation(ArsNouveau.MODID, "blocks/" + s);
//            Block block = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(ArsNouveau.MODID, s + "_stairs"));
//            stairsBlock((StairBlock) block, tex);
//
//            Block slab = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(ArsNouveau.MODID, s + "_slab"));
//            slabBlock((SlabBlock) slab, new ResourceLocation(ArsNouveau.MODID, s), tex);
//        }
//    }
//
//    private void registerOnlyState(Block block, String registry) {
//        simpleBlock(block, getUncheckedModel(registry));
//    }
//
//    //waiting for forge PR to fix the datagen
//    private void registerDoor(DoorBlock door, String reg) {
//        doorBlock(door, reg, getBlockLoc(reg + "_bottom"), getBlockLoc(reg + "_top"));
//    }
//
//
//
//    //will it work? idk
//    public void signBlock(Block sign, String reg) {
//        ModelFile signModel = models().withExistingParent(reg, new ResourceLocation("block/air")).texture("particle", new ResourceLocation(ArsNouveau.MODID, "block/" + reg));
//        getVariantBuilder(sign).forAllStates(s -> ConfiguredModel.builder().modelFile(signModel).build());
//    }
//
//    public void registerNormalCube(Block block, String registry) {
//        buildNormalCube(registry);
//        if (LibBlockNames.DIRECTIONAL_SOURCESTONE.contains(registry)) {
//            horizontalBlock(block, getUncheckedModel(registry));
//        } else {
//            simpleBlock(block, getUncheckedModel(registry));
//        }
//    }
//
//    public static ModelFile getUncheckedModel(String registry) {
//        return new ModelFile.UncheckedModelFile("ars_nouveau:block/" + registry);
//    }
//
//    public void buildNormalCube(String registryName) {
//        this.models().getBuilder(registryName).parent(new ModelFile.UncheckedModelFile("block/cube_all")).texture("all", getBlockLoc(registryName));
//    }
//
//    public ResourceLocation getBlockLoc(String registryName) {
//        return new ResourceLocation(ArsNouveau.MODID, "blocks" + "/" + registryName);
//    }
//
//}
