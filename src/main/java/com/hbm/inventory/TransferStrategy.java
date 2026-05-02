package com.hbm.inventory;

import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.function.IntSupplier;
import java.util.function.Predicate;

/**
 * Declarative transfer routing for containers that append a player inventory after their machine slots.
 */
public final class TransferStrategy {

    private final IntSupplier machineSlotsSupplier;
    private final TransferRule[] machineRules;
    private final int genericMachineRangeStart;
    private final int genericMachineRangeEnd;
    private final PlayerInventoryLayout playerLayout;
    private final PlayerFallbackMode playerFallbackMode;
    private final RuleDispatchMode ruleDispatchMode;
    private final boolean requiresRuntimeValidation;

    private TransferStrategy(IntSupplier machineSlotsSupplier, TransferRule[] machineRules,
                              int genericMachineRangeStart,
                              int genericMachineRangeEnd, PlayerInventoryLayout playerLayout,
                              PlayerFallbackMode playerFallbackMode,
                              RuleDispatchMode ruleDispatchMode,
                              boolean requiresRuntimeValidation) {
        this.machineSlotsSupplier = machineSlotsSupplier;
        this.machineRules = machineRules;
        this.genericMachineRangeStart = genericMachineRangeStart;
        this.genericMachineRangeEnd = genericMachineRangeEnd;
        this.playerLayout = playerLayout;
        this.playerFallbackMode = playerFallbackMode;
        this.ruleDispatchMode = ruleDispatchMode;
        this.requiresRuntimeValidation = requiresRuntimeValidation;
    }

    public static Builder builder(int machineSlots) {
        return new Builder(machineSlots);
    }

    public static Builder builder(IntSupplier machineSlotsSupplier) {
        return new Builder(machineSlotsSupplier);
    }

    public int machineSlots() {
        return machineSlotsSupplier.getAsInt();
    }

    public TransferRule[] machineRules() {
        return machineRules;
    }

    public int genericMachineRangeStart(int machineSlots) {
        return genericMachineRangeStart == -1 ? machineSlots : genericMachineRangeStart;
    }

    public int genericMachineRangeEnd(int machineSlots) {
        return genericMachineRangeEnd == -1 ? machineSlots : genericMachineRangeEnd;
    }

    public PlayerInventoryLayout playerLayout() {
        return playerLayout;
    }

    public PlayerFallbackMode playerFallbackMode() {
        return playerFallbackMode;
    }

    public RuleDispatchMode ruleDispatchMode() {
        return ruleDispatchMode;
    }

    public void validateRuntimeRanges(int machineSlots) {
        if (!requiresRuntimeValidation) {
            return;
        }

        if (machineSlots < 0) {
            throw new IllegalStateException("machineSlots must be non-negative");
        }

        for (TransferRule rule : machineRules) {
            if (rule.endExclusive() > machineSlots) {
                throw new IllegalStateException("range must stay within [0, " + machineSlots + "]");
            }
        }

        if (genericMachineRangeStart(machineSlots) > machineSlots || genericMachineRangeEnd(
                machineSlots) > machineSlots) {
            throw new IllegalStateException("range must stay within [0, " + machineSlots + "]");
        }
    }

    public enum PlayerFallbackMode {
        NONE,
        REBALANCE_SECTIONS
    }

    public enum RuleDispatchMode {
        FIRST_MATCH_WINS,
        FALLTHROUGH_ON_FAILURE
    }

    public static final class Builder {
        private final IntSupplier machineSlotsSupplier;
        private final int staticMachineSlots;
        private final List<TransferRule> machineRules = new ArrayList<>();
        private int genericMachineRangeStart = -1;
        private int genericMachineRangeEnd = -1;
        private PlayerInventoryLayout playerLayout = PlayerInventoryLayout.STANDARD;
        private PlayerFallbackMode playerFallbackMode = PlayerFallbackMode.NONE;
        private RuleDispatchMode ruleDispatchMode = RuleDispatchMode.FIRST_MATCH_WINS;
        private final boolean requiresRuntimeValidation;

        private Builder(int machineSlots) {
            if (machineSlots < 0) {
                throw new IllegalArgumentException("machineSlots must be non-negative");
            }

            this.machineSlotsSupplier = () -> machineSlots;
            this.staticMachineSlots = machineSlots;
            this.requiresRuntimeValidation = false;
        }

        private Builder(IntSupplier machineSlotsSupplier) {
            this.machineSlotsSupplier = machineSlotsSupplier;
            this.staticMachineSlots = -1;
            this.requiresRuntimeValidation = true;
        }

        /**
         * Adds an ordered machine routing rule over the half-open range [startInclusive, endExclusive).
         * Dispatch behaviour is controlled by ruleDispatchMode.
         */
        public Builder rule(int startInclusive, int endExclusive, Predicate<ItemStack> accepts) {
            validateRange(startInclusive, endExclusive);
            machineRules.add(new TransferRule(startInclusive, endExclusive, accepts));
            return this;
        }

        /**
         * Sets the generic machine catch-all range used after rule dispatch leaves the stack unmoved.
         */
        public Builder genericMachineRange(int startInclusive, int endExclusive) {
            validateRange(startInclusive, endExclusive);
            this.genericMachineRangeStart = startInclusive;
            this.genericMachineRangeEnd = endExclusive;
            return this;
        }

        public Builder genericMachineRange(int startInclusive) {
            if (!requiresRuntimeValidation) {
                return genericMachineRange(startInclusive, staticMachineSlots);
            }

            validateRange(startInclusive, startInclusive);
            this.genericMachineRangeStart = startInclusive;
            this.genericMachineRangeEnd = -1;
            return this;
        }

        public Builder playerLayout(PlayerInventoryLayout playerLayout) {
            this.playerLayout = playerLayout;
            return this;
        }

        public Builder playerFallbackMode(PlayerFallbackMode playerFallbackMode) {
            this.playerFallbackMode = playerFallbackMode;
            return this;
        }

        public Builder ruleDispatchMode(RuleDispatchMode ruleDispatchMode) {
            this.ruleDispatchMode = ruleDispatchMode;
            return this;
        }

        public TransferStrategy build() {
            return new TransferStrategy(machineSlotsSupplier, machineRules.toArray(new TransferRule[machineRules.size()]),
                    genericMachineRangeStart,
                    genericMachineRangeEnd, playerLayout, playerFallbackMode, ruleDispatchMode,
                    requiresRuntimeValidation);
        }

        private void validateRange(int startInclusive, int endExclusive) {
            if (startInclusive < 0 || endExclusive < startInclusive) {
                throw new IllegalArgumentException("range start must be non-negative and end must not precede start");
            }

            if (!requiresRuntimeValidation && endExclusive > staticMachineSlots) {
                throw new IllegalArgumentException("range must stay within [0, " + staticMachineSlots + "]");
            }
        }
    }

    /**
     * Describes the player inventory section sizes expected after the machine slots.
     */
    public static final class PlayerInventoryLayout {
        public static final PlayerInventoryLayout STANDARD = new PlayerInventoryLayout(27, 9);
        private final int mainInventorySlots;
        private final int hotbarSlots;

        private PlayerInventoryLayout(int mainInventorySlots, int hotbarSlots) {
            this.mainInventorySlots = mainInventorySlots;
            this.hotbarSlots = hotbarSlots;
        }

        public static PlayerInventoryLayout of(int mainInventorySlots, int hotbarSlots) {
            return new PlayerInventoryLayout(mainInventorySlots, hotbarSlots);
        }

        public int mainInventorySlots() {
            return mainInventorySlots;
        }

        public int hotbarSlots() {
            return hotbarSlots;
        }
    }
}
