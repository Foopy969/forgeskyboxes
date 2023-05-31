package com.foopy.forgeskyboxes.skyboxes.textured;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.Util;

import com.foopy.forgeskyboxes.skyboxes.AbstractSkybox;
import com.foopy.forgeskyboxes.skyboxes.SkyboxType;
import com.foopy.forgeskyboxes.util.object.*;

public class SingleSpriteSquareTexturedSkybox extends SquareTexturedSkybox {
    public static Codec<SingleSpriteSquareTexturedSkybox> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Properties.CODEC.fieldOf("properties").forGetter(AbstractSkybox::getProperties),
            Conditions.CODEC.optionalFieldOf("conditions", Conditions.DEFAULT).forGetter(AbstractSkybox::getConditions),
            Decorations.CODEC.optionalFieldOf("decorations", Decorations.DEFAULT).forGetter(AbstractSkybox::getDecorations),
            Blend.CODEC.optionalFieldOf("blend", Blend.DEFAULT).forGetter(TexturedSkybox::getBlend),
            Texture.CODEC.fieldOf("texture").forGetter(SingleSpriteSquareTexturedSkybox::getTexture)
    ).apply(instance, SingleSpriteSquareTexturedSkybox::new));
    protected Texture texture;

    public SingleSpriteSquareTexturedSkybox(Properties properties, Conditions conditions, Decorations decorations, Blend blend, Texture texture) {
        super(properties, conditions, decorations, blend, Util.make(() -> new Textures(
                texture.withUV(1.0F / 3.0F, 1.0F / 2.0F, 2.0F / 3.0F, 1),
                texture.withUV(2.0F / 3.0F, 0, 1, 1.0F / 2.0F),
                texture.withUV(2.0F / 3.0F, 1.0F / 2.0F, 1, 1),
                texture.withUV(0, 1.0F / 2.0F, 1.0F / 3.0F, 1),
                texture.withUV(1.0F / 3.0F, 0, 2.0F / 3.0F, 1.0F / 2.0F),
                texture.withUV(0, 0, 1.0F / 3.0F, 1.0F / 2.0F)
        )));
        this.texture = texture;
    }

    @Override
    public SkyboxType<? extends AbstractSkybox> getType() {
        return SkyboxType.SINGLE_SPRITE_SQUARE_TEXTURED_SKYBOX.get();
    }

    public Texture getTexture() {
        return this.texture;
    }
}
