package io.github.aquerr.eaglefactionseconomy;

import com.google.inject.Inject;
import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactionseconomy.operation.cost.OperationMoneyCost;
import org.apache.logging.log4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.Command;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.lifecycle.ConstructPluginEvent;
import org.spongepowered.api.event.lifecycle.LoadedGameEvent;
import org.spongepowered.api.event.lifecycle.RegisterCommandEvent;
import org.spongepowered.api.service.economy.EconomyService;
import org.spongepowered.plugin.PluginContainer;
import org.spongepowered.plugin.builtin.jvm.Plugin;

import java.util.List;


@Plugin("eaglefactions-economy")
public class EagleFactionsEconomy
{
    private final PluginContainer container;
    private final Logger logger;

    private EagleFactions eagleFactions;

    private boolean disabled = false;

    @Inject
    EagleFactionsEconomy(final PluginContainer container, final Logger logger)
    {
        this.container = container;
        this.logger = logger;
    }

    @Listener
    public void onConstructPlugin(final ConstructPluginEvent event)
    {
        this.logger.info("Constructing EagleFactions-Economy...");

        PluginContainer efPluginContainer = event.game().pluginManager().plugin("eaglefactions").orElse(null);
        if (efPluginContainer == null)
        {
            this.logger.error("EagleFactions not detected");
            disablePlugin();
        }

        this.logger.info("Hey EagleFactions! Time for some faction economy! :)");
        this.eagleFactions = (EagleFactions) efPluginContainer.instance();
    }

    @Listener(order = Order.POST)
    public void onGameLoad(final LoadedGameEvent event)
    {
        if (disabled)
            return;

        EconomyService economyService = Sponge.server().serviceProvider().economyService().orElse(null);
        if (economyService == null)
        {
            disablePlugin();
            return;
        }

        // Let's add our own operation cost
        this.eagleFactions.getClaimManager().setClaimCosts(List.of(
                new OperationMoneyCost(economyService, 50)
        ));
    }

    @Listener
    public void onRegisterCommands(final RegisterCommandEvent<Command.Parameterized> event)
    {
        if (disabled)
            return;
    }

    private void disablePlugin()
    {
        this.disabled = true;
        this.logger.error("Plugin will be disabled...");
    }
}
