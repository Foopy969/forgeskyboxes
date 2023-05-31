package com.foopy.forgeskyboxes.skyboxes;

import com.google.common.base.Preconditions;
import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import com.mojang.serialization.Codec;
import com.foopy.forgeskyboxes.FabricSkyBoxesClient;
import com.foopy.forgeskyboxes.skyboxes.textured.AnimatedSquareTexturedSkybox;
import com.foopy.forgeskyboxes.skyboxes.textured.SingleSpriteAnimatedSquareTexturedSkybox;
import com.foopy.forgeskyboxes.skyboxes.textured.SingleSpriteSquareTexturedSkybox;
import com.foopy.forgeskyboxes.skyboxes.textured.SquareTexturedSkybox;

import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryBuilder;
import net.minecraftforge.registries.RegistryObject;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

public class SkyboxType<T extends AbstractSkybox> {
    public static final DeferredRegister<SkyboxType<? extends AbstractSkybox>> SKYBOX_TYPE;
    public static final Supplier<IForgeRegistry<SkyboxType<? extends AbstractSkybox>>> REGISTRY;
    public static final RegistryObject<SkyboxType<MonoColorSkybox>> MONO_COLOR_SKYBOX;
    public static final RegistryObject<SkyboxType<OverworldSkybox>> OVERWORLD_SKYBOX;
    public static final RegistryObject<SkyboxType<EndSkybox>> END_SKYBOX;
    public static final RegistryObject<SkyboxType<SquareTexturedSkybox>> SQUARE_TEXTURED_SKYBOX;
    public static final RegistryObject<SkyboxType<SingleSpriteSquareTexturedSkybox>> SINGLE_SPRITE_SQUARE_TEXTURED_SKYBOX;
    public static final RegistryObject<SkyboxType<AnimatedSquareTexturedSkybox>> ANIMATED_SQUARE_TEXTURED_SKYBOX;
    public static final RegistryObject<SkyboxType<SingleSpriteAnimatedSquareTexturedSkybox>> SINGLE_SPRITE_ANIMATED_SQUARE_TEXTURED_SKYBOX;
    public static final Codec<ResourceLocation> SKYBOX_ID_CODEC;

    static {
        SKYBOX_TYPE = DeferredRegister.create(ResourceKey.createRegistryKey(new ResourceLocation(FabricSkyBoxesClient.MODID, "skybox_type")), FabricSkyBoxesClient.MODID);
        REGISTRY = SKYBOX_TYPE.makeRegistry(RegistryBuilder::new);
        MONO_COLOR_SKYBOX = SKYBOX_TYPE.register("monocolor", () -> SkyboxType.Builder.create(MonoColorSkybox.class, "monocolor").legacySupported().deserializer(LegacyDeserializer.MONO_COLOR_SKYBOX_DESERIALIZER.get()).factory(MonoColorSkybox::new).add(2, MonoColorSkybox.CODEC).build());
        OVERWORLD_SKYBOX = SKYBOX_TYPE.register("overworld", () -> SkyboxType.Builder.create(OverworldSkybox.class, "overworld").add(2, OverworldSkybox.CODEC).build());
        END_SKYBOX = SKYBOX_TYPE.register("end", () -> SkyboxType.Builder.create(EndSkybox.class, "end").add(2, EndSkybox.CODEC).build());
        SQUARE_TEXTURED_SKYBOX = SKYBOX_TYPE.register("square_textured", () -> SkyboxType.Builder.create(SquareTexturedSkybox.class, "square-textured").deserializer(LegacyDeserializer.SQUARE_TEXTURED_SKYBOX_DESERIALIZER.get()).legacySupported().factory(SquareTexturedSkybox::new).add(2, SquareTexturedSkybox.CODEC).build());
        SINGLE_SPRITE_SQUARE_TEXTURED_SKYBOX = SKYBOX_TYPE.register("single_sprite_square_textured", () -> SkyboxType.Builder.create(SingleSpriteSquareTexturedSkybox.class, "single-sprite-square-textured").add(2, SingleSpriteSquareTexturedSkybox.CODEC).build());
        ANIMATED_SQUARE_TEXTURED_SKYBOX = SKYBOX_TYPE.register("animated_square_textured", () -> SkyboxType.Builder.create(AnimatedSquareTexturedSkybox.class, "animated-square-textured").add(2, AnimatedSquareTexturedSkybox.CODEC).build());
        SINGLE_SPRITE_ANIMATED_SQUARE_TEXTURED_SKYBOX = SKYBOX_TYPE.register("single_sprite_animated_square_textured", () -> SkyboxType.Builder.create(SingleSpriteAnimatedSquareTexturedSkybox.class, "single-sprite-animated-square-textured").add(2, SingleSpriteAnimatedSquareTexturedSkybox.CODEC).build());
        SKYBOX_ID_CODEC = Codec.STRING.xmap((s) -> {
            if (!s.contains(":")) {
                return new ResourceLocation(FabricSkyBoxesClient.MODID, s.replace('-', '_'));
            }
            return new ResourceLocation(s.replace('-', '_'));
        }, (id) -> {
            if (id.getNamespace().equals(FabricSkyBoxesClient.MODID)) {
                return id.getPath().replace('_', '-');
            }
            return id.toString().replace('_', '-');
        });
    }

    private final BiMap<Integer, Codec<T>> codecBiMap;
    private final boolean legacySupported;
    private final String name;
    @Nullable
    private final Supplier<T> factory;
    @Nullable
    private final LegacyDeserializer<T> deserializer;

    private SkyboxType(BiMap<Integer, Codec<T>> codecBiMap, boolean legacySupported, String name, @Nullable Supplier<T> factory, @Nullable LegacyDeserializer<T> deserializer) {
        this.codecBiMap = codecBiMap;
        this.legacySupported = legacySupported;
        this.name = name;
        this.factory = factory;
        this.deserializer = deserializer;
    }

    public static void initRegistry() {
        if (REGISTRY == null) {
            System.err.println("[FabricSkyboxes] Registry not loaded?");
        }
    }

    /*
    private static <T extends AbstractSkybox> SkyboxType<T> register(SkyboxType<T> type) {
        return Registry.register(SkyboxType.REGISTRY, type.createId(FabricSkyBoxesClient.MODID), type);
    } 
    */

    public String getName() {
        return this.name;
    }

    public boolean isLegacySupported() {
        return this.legacySupported;
    }

    @NotNull
    public T instantiate() {
        return Objects.requireNonNull(Objects.requireNonNull(this.factory, "Can't instantiate from a null factory").get());
    }

    @Nullable
    public LegacyDeserializer<T> getDeserializer() {
        return this.deserializer;
    }

    public ResourceLocation createId(String namespace) {
        return this.createIdFactory().apply(namespace);
    }

    public Function<String, ResourceLocation> createIdFactory() {
        return (ns) -> new ResourceLocation(ns, this.getName().replace('-', '_'));
    }

    public Codec<T> getCodec(int schemaVersion) {
        return Objects.requireNonNull(this.codecBiMap.get(schemaVersion), String.format("Unsupported schema version '%d' for skybox type %s", schemaVersion, this.name));
    }

    public static class Builder<T extends AbstractSkybox> {
        private final ImmutableBiMap.Builder<Integer, Codec<T>> builder = ImmutableBiMap.builder();
        private String name;
        private boolean legacySupported = false;
        private Supplier<T> factory;
        private LegacyDeserializer<T> deserializer;

        private Builder() {
        }

        public static <S extends AbstractSkybox> Builder<S> create(@SuppressWarnings("unused") Class<S> clazz, String name) {
            Builder<S> builder = new Builder<>();
            builder.name = name;
            return builder;
        }

        public static <S extends AbstractSkybox> Builder<S> create(String name) {
            Builder<S> builder = new Builder<>();
            builder.name = name;
            return builder;
        }

        protected Builder<T> legacySupported() {
            this.legacySupported = true;
            return this;
        }

        protected Builder<T> factory(Supplier<T> factory) {
            this.factory = factory;
            return this;
        }

        protected Builder<T> deserializer(LegacyDeserializer<T> deserializer) {
            this.deserializer = deserializer;
            return this;
        }

        public Builder<T> add(int schemaVersion, Codec<T> codec) {
            Preconditions.checkArgument(schemaVersion >= 2, "schema version was lesser than 2");
            Preconditions.checkNotNull(codec, "codec was null");
            this.builder.put(schemaVersion, codec);
            return this;
        }

        public SkyboxType<T> build() {
            if (this.legacySupported) {
                Preconditions.checkNotNull(this.factory, "factory was null");
                Preconditions.checkNotNull(this.deserializer, "deserializer was null");
            }
            return new SkyboxType<>(this.builder.build(), this.legacySupported, this.name, this.factory, this.deserializer);
        }

        /*
        public SkyboxType<T> buildAndRegister(String namespace) {
            return Registry.register(SkyboxType.REGISTRY, new ResourceLocation(namespace, this.name.replace('-', '_')), this.build());
        }
         */
    }
}
