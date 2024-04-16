package io.github.aquerr.eaglefactionseconomy.config;

import io.github.aquerr.eaglefactions.api.config.FactionsConfig;
import io.github.aquerr.eaglefactionseconomy.operation.cost.CostApplyStrategy;
import io.github.aquerr.eaglefactionseconomy.util.FileUtils;
import io.github.aquerr.eaglefactionseconomy.util.resource.Resource;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurationOptions;
import org.spongepowered.configurate.hocon.HoconConfigurationLoader;
import org.spongepowered.configurate.loader.ConfigurationLoader;
import org.spongepowered.configurate.serialize.SerializationException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class Configuration
{
    private final ConfigurationLoader<CommentedConfigurationNode> configLoader;
    private CommentedConfigurationNode configNode;

    private Locale locale;
    private CostApplyStrategy creationCostsApplyStrategy;
    private CostApplyStrategy claimCostsApplyStrategy;
    private List<FactionsConfig.CostConfigDefinition> creationCostDefinitions;
    private List<FactionsConfig.CostConfigDefinition> claimCostDefinitions;

    public Configuration(final Path configDir, final Resource configAsset) throws IOException
    {
        FileUtils.createDirectoryIfNotExists(configDir);

        Path configPath = configDir.resolve("config.conf");

        try
        {
            if (Files.notExists(configPath))
                Files.copy(configAsset.getInputStream(), configPath);
        }
        catch (final IOException e)
        {
            throw new IllegalStateException(e);
        }

        this.configLoader = (HoconConfigurationLoader.builder()).path(configPath).build();
        reloadConfiguration();
    }

    public void reloadConfiguration() throws IOException
    {
        loadConfiguration();
        loadConfigValues();
    }

    private void loadConfigValues()
    {
        try
        {
            this.locale = Locale.forLanguageTag(this.configNode.node("language").getString("en"));

            this.creationCostsApplyStrategy = this.configNode.node("creation-cost", "apply-strategy").get(CostApplyStrategy.class, CostApplyStrategy.ADD);
            this.creationCostDefinitions = this.configNode.node("creation-cost", "costs").getList(FactionsConfig.CostConfigDefinition.class, Collections.emptyList());
            this.claimCostsApplyStrategy = this.configNode.node("claiming-cost", "apply-strategy").get(CostApplyStrategy.class, CostApplyStrategy.ADD);
            this.claimCostDefinitions = this.configNode.node("claiming-cost", "costs").getList(FactionsConfig.CostConfigDefinition.class, Collections.emptyList());
        }
        catch (SerializationException e)
        {
            throw new RuntimeException(e);
        }
    }

    private void loadConfiguration() throws IOException
    {
        configNode = configLoader.load(ConfigurationOptions.defaults().shouldCopyDefaults(true));
    }

    public void save()
    {
        try
        {
            configLoader.save(configNode);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public Locale getLocale()
    {
        return locale;
    }

    public CostApplyStrategy getClaimCostsApplyStrategy()
    {
        return claimCostsApplyStrategy;
    }

    public CostApplyStrategy getCreationCostsApplyStrategy()
    {
        return creationCostsApplyStrategy;
    }

    public List<FactionsConfig.CostConfigDefinition> getClaimCostDefinitions()
    {
        return claimCostDefinitions;
    }

    public List<FactionsConfig.CostConfigDefinition> getCreationCostDefinitions()
    {
        return creationCostDefinitions;
    }
}
