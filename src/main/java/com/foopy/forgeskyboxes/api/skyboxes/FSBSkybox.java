package com.foopy.forgeskyboxes.api.skyboxes;

import com.foopy.forgeskyboxes.util.object.Conditions;
import com.foopy.forgeskyboxes.util.object.Decorations;
import com.foopy.forgeskyboxes.util.object.Properties;

public interface FSBSkybox extends Skybox {
    float getAlpha();

    float updateAlpha();

    Properties getProperties();

    Conditions getConditions();

    Decorations getDecorations();
}
