package net.mehvahdjukaar.moonlight.api.misc;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;

import java.util.Map;

/** Test-only substitute: no Fabric lifecycle or resource reload is started. */
public abstract class RegistryAccessJsonReloadListener {
    protected RegistryAccessJsonReloadListener(Gson gson, String directory) {
    }

    public abstract void parse(Map<ResourceLocation, JsonElement> json, RegistryAccess access);
}
