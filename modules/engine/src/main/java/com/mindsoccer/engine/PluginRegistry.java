package com.mindsoccer.engine;

import com.mindsoccer.protocol.enums.RoundType;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Registre des plugins de règles.
 * Associe chaque type de rubrique à son plugin correspondant.
 */
@Component

public class PluginRegistry {

    private final Map<RoundType, RulePlugin> plugins;

    public PluginRegistry(List<RulePlugin> pluginList) {
        this.plugins = new EnumMap<>(RoundType.class);
        for (RulePlugin plugin : pluginList) {
            plugins.put(plugin.type(), plugin);
        }
    }

    /**
     * Récupère le plugin pour un type de rubrique.
     */
    public Optional<RulePlugin> getPlugin(RoundType type) {
        return Optional.ofNullable(plugins.get(type));
    }

    /**
     * Récupère le plugin pour un type de rubrique (exception si non trouvé).
     */
    public RulePlugin getPluginOrThrow(RoundType type) {
        return getPlugin(type).orElseThrow(() ->
                new IllegalArgumentException("No plugin registered for round type: " + type));
    }

    /**
     * Vérifie si un plugin est enregistré pour un type.
     */
    public boolean hasPlugin(RoundType type) {
        return plugins.containsKey(type);
    }

    /**
     * Retourne tous les types de rubriques supportés.
     */
    public List<RoundType> getSupportedTypes() {
        return List.copyOf(plugins.keySet());
    }

    /**
     * Retourne le nombre de plugins enregistrés.
     */
    public int getPluginCount() {
        return plugins.size();
    }
}
