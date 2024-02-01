package net.minestom.server.instance.block;

import net.minestom.server.instance.block.rule.BlockPlacementRule;
import net.minestom.server.utils.NamespaceID;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public interface BlockManager {
    void registerHandler(@NotNull String namespace, @NotNull Supplier<@NotNull BlockHandler> handlerSupplier);

    default void registerHandler(@NotNull NamespaceID namespace, @NotNull Supplier<@NotNull BlockHandler> handlerSupplier) {
        registerHandler(namespace.toString(), handlerSupplier);
    }

    @Nullable BlockHandler getHandler(@NotNull String namespace);

    @ApiStatus.Internal
    @NotNull BlockHandler getHandlerOrDummy(@NotNull String namespace);


    /**
     * Registers a {@link BlockPlacementRule}.
     *
     * @param blockPlacementRule the block placement rule to register
     * @throws IllegalArgumentException if <code>blockPlacementRule</code> block id is negative
     */
    void registerBlockPlacementRule(@NotNull BlockPlacementRule blockPlacementRule);

    /**
     * Gets the {@link BlockPlacementRule} of the specific block.
     *
     * @param block the block to check
     * @return the block placement rule associated with the block, null if not any
     */
    @Nullable BlockPlacementRule getBlockPlacementRule(@NotNull Block block);
}
