package io.github.aquerr.eaglefactionseconomy.operation.cost;

import io.github.aquerr.eaglefactions.api.config.FactionsConfig;
import io.github.aquerr.eaglefactions.api.logic.cost.OperationCost;
import org.spongepowered.api.service.economy.EconomyService;

import java.math.BigDecimal;

/**
 * This class name is soo beautiful that I just had to comment it ;D (irony)
 */
public class OperationCostConfigDefinitionToOperationCostMapper
{
    public static OperationCost map(EconomyService economyService,
                                    FactionsConfig.CostConfigDefinition definition)
    {
        if ("money".equalsIgnoreCase(definition.getType()))
        {
            return new OperationMoneyCost(economyService, new BigDecimal(definition.getValue().getString()));
        }

        throw new IllegalArgumentException("Unrecognized cost type = " + definition.getType());
    }

    private OperationCostConfigDefinitionToOperationCostMapper()
    {

    }
}
