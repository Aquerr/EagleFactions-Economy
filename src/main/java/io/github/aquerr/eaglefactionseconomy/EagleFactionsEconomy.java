package io.github.aquerr.eaglefactionseconomy;

import com.google.inject.Inject;
import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.api.config.FactionsConfig;
import io.github.aquerr.eaglefactions.api.logic.cost.OperationCost;
import io.github.aquerr.eaglefactionseconomy.config.Configuration;
import io.github.aquerr.eaglefactionseconomy.operation.cost.CostApplyStrategy;
import io.github.aquerr.eaglefactionseconomy.operation.cost.OperationCostConfigDefinitionToOperationCostMapper;
import io.github.aquerr.eaglefactionseconomy.util.resource.Resource;
import io.github.aquerr.eaglefactionseconomy.util.resource.ResourceUtils;
import net.kyori.adventure.text.Component;
import org.apache.logging.log4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.Command;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.lifecycle.ConstructPluginEvent;
import org.spongepowered.api.event.lifecycle.LoadedGameEvent;
import org.spongepowered.api.event.lifecycle.RefreshGameEvent;
import org.spongepowered.api.event.lifecycle.RegisterCommandEvent;
import org.spongepowered.api.service.economy.EconomyService;
import org.spongepowered.plugin.PluginContainer;
import org.spongepowered.plugin.builtin.jvm.Plugin;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static java.lang.String.format;


@Plugin("eaglefactions-economy")
public class EagleFactionsEconomy
{
    private final PluginContainer container;
    private final Logger logger;


    private EagleFactions eagleFactions;
    private Configuration configuration;
    private Path configDir;

    private EconomyService economyService;

    private boolean disabled = false;

    @Inject
    EagleFactionsEconomy(final PluginContainer container,
                         final Logger logger,
                         @ConfigDir(sharedRoot = false) final Path configDir)
    {
        this.container = container;
        this.logger = logger;
        this.configDir = configDir;
    }

    @Listener
    public void onConstructPlugin(final ConstructPluginEvent event)
    {
        try
        {
            this.logger.info("Constructing EagleFactions-Economy...");

            PluginContainer efPluginContainer = event.game().pluginManager().plugin("eaglefactions").orElse(null);
            if (efPluginContainer == null)
            {
                throw new IllegalStateException("EagleFactions not detected");
            }

            this.logger.info("Hey EagleFactions! Time for some faction economy! :)");
            this.eagleFactions = (EagleFactions) efPluginContainer.instance();

            setupConfigs();
        }
        catch (Exception exception)
        {
            exception.printStackTrace();
            disablePlugin();
        }
    }

    @Listener(order = Order.POST)
    public void onGameLoad(final LoadedGameEvent event)
    {
        if (disabled)
            return;

        this.economyService = Sponge.server().serviceProvider().economyService().orElse(null);
        if (economyService == null)
        {
            disablePlugin();
            return;
        }

        prepareCreationCosts();
        prepareClaimCosts();
    }

    private void prepareCreationCosts()
    {
        CostApplyStrategy costApplyStrategy = this.configuration.getCreationCostsApplyStrategy();
        List<FactionsConfig.CostConfigDefinition> operationCostDefinitions = this.configuration.getCreationCostDefinitions();
        List<OperationCost> creationCosts = new ArrayList<>(operationCostDefinitions.size());
        for (FactionsConfig.CostConfigDefinition definition : operationCostDefinitions)
        {
            creationCosts.add(OperationCostConfigDefinitionToOperationCostMapper.map(this.economyService, definition));
        }

        if (costApplyStrategy == CostApplyStrategy.ADD)
        {
            creationCosts.forEach(eagleFactions.getFactionCreationManager()::addCreationCost);
        }
        else if (costApplyStrategy == CostApplyStrategy.OVERRIDE)
        {
            eagleFactions.getFactionCreationManager().setCreationCosts(creationCosts);
        }
    }

    private void prepareClaimCosts()
    {
        CostApplyStrategy costApplyStrategy = this.configuration.getClaimCostsApplyStrategy();
        List<FactionsConfig.CostConfigDefinition> operationCostDefinitions = this.configuration.getClaimCostDefinitions();
        List<OperationCost> creationCosts = new ArrayList<>(operationCostDefinitions.size());
        for (FactionsConfig.CostConfigDefinition definition : operationCostDefinitions)
        {
            creationCosts.add(OperationCostConfigDefinitionToOperationCostMapper.map(this.economyService, definition));
        }

        if (costApplyStrategy == CostApplyStrategy.ADD)
        {
            creationCosts.forEach(eagleFactions.getClaimManager()::addClaimCost);
        }
        else if (costApplyStrategy == CostApplyStrategy.OVERRIDE)
        {
            eagleFactions.getClaimManager().setClaimCosts(creationCosts);
        }
    }

    @Listener
    public void onRegisterCommands(final RegisterCommandEvent<Command.Parameterized> event)
    {
        if (disabled)
            return;
    }

    @Listener
    public void onReload(final RefreshGameEvent event)
    {
        if (disabled)
            return;

        try
        {
            this.configuration.reloadConfiguration();

            if(event.source() instanceof Player)
            {
                Player player = (Player)event.source();
                player.sendMessage(Component.text("Config reloaded!"));
            }
        }
        catch (IOException e)
        {
            Player player = (Player)event.source();
            player.sendMessage(Component.text("An error occurred during plugin reload."));
            e.printStackTrace();
        }
    }

    private void disablePlugin()
    {
        this.disabled = true;
        this.logger.error("Plugin will be disabled...");
    }

    private void setupConfigs() throws IOException
    {
        String configResourcePath = "assets/eaglefactions-economy/config.conf";
        Resource resource = ResourceUtils.getResource(configResourcePath);
        if (resource == null)
        {
            throw new IllegalStateException(format("Config file could not be found at path %s", configResourcePath));
        }

        configuration = new Configuration(configDir, resource);
    }
}
