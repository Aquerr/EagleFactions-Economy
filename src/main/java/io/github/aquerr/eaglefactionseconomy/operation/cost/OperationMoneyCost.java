package io.github.aquerr.eaglefactionseconomy.operation.cost;

import io.github.aquerr.eaglefactions.api.exception.CostNotSatisfiedException;
import io.github.aquerr.eaglefactions.api.logic.cost.OperationCost;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;
import org.spongepowered.api.service.economy.EconomyService;
import org.spongepowered.api.service.economy.account.UniqueAccount;
import org.spongepowered.api.service.economy.transaction.ResultType;
import org.spongepowered.api.service.economy.transaction.TransactionResult;

import java.math.BigDecimal;

import static java.lang.String.format;

public class OperationMoneyCost implements OperationCost
{
    private final EconomyService economyService;
    private final BigDecimal money;

    public OperationMoneyCost(EconomyService economyService,
                              BigDecimal money)
    {
        this.economyService = economyService;
        this.money = money;
    }

    @Override
    public void pay(ServerPlayer serverPlayer) throws CostNotSatisfiedException
    {
        UniqueAccount account = economyService.findOrCreateAccount(serverPlayer.uniqueId()).orElse(null);
        if (account == null)
            throw new CostNotSatisfiedException(format("Player could not pay for operation. Required money: %s", this.money));

        TransactionResult transactionResult = account.withdraw(economyService.defaultCurrency(), this.money);
        if (transactionResult.result() != ResultType.SUCCESS)
        {
            throw new CostNotSatisfiedException(format("Player could not pay for operation. Required money: %s", this.money));
        }
    }

    @Override
    public void rollBack(ServerPlayer serverPlayer)
    {
        UniqueAccount account = economyService.findOrCreateAccount(serverPlayer.uniqueId()).orElse(null);
        if (account != null)
        {
            account.deposit(economyService.defaultCurrency(), this.money);
        }
    }
}